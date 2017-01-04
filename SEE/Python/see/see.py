#!/usr/bin/env python
# coding=utf-8

##############################################################################
#
# Copyright 2012-2016 SkillPro Consortium
#
# Author: Denis Štogl, email: denis.stogl@kit.edu
#
# Date of creation: 2014-2016
#
# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#
#
# This file is part of the SkillPro Framework. The SkillPro Framework
# is developed in the SkillPro project, funded by the European FP7
# programme (Grant Agreement 287733).
#
# The SkillPro Framework is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# The SkillPro Framework is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with the SkillPro Framework.  If not, see <http://www.gnu.org/licenses/>.
##############################################################################

## @package skillpro_execution_engine.see
# @file see
# @author Denis Štogl
##

import time
import threading
import importlib

from data_model.condition import Condition
from data_model.communication import CommunicationDescription
from data_model.communication import CommunicationPoint
from data_model.executable_skill import ExecutableSkill
from data_model.call import Call
from data_model.environment import SEE_EnvironmentData

mutex = threading.Lock()


## SkillExecutionEngine class.
# This class implements Skill-Execution-Engine.
##
class SkillExecutionEngine(object):

    ## SEE states according to MES data model
    STATES = {
        # UNAVAILABLE = 1xx,
        'PreOperational': 110,              # Shut off, unknown state
        # RUNNING = 2xx,
        'ExecutingSkill': 210,
        'ExecutingSkillPausing': 211,
        'ExecutingSkillResumable': 212,
        'Idle': 220,
        'IdleQueuedSkill': 221,
        # ERROR = 3xx
        'Error': 300,
    }
    ## SEE Error states according to MES data model
    ERRORS = {
        # ERROR = 3xx
        'UnknownConditionError': 310,        # 23x => we don't know the state of the product
        'UnknownProductError': 311,          # or the state of the configuration .. human assistance needed
        'UnknownConfigurationError': 312,
        'CommunicationError': 320,
    }

    def __init__(self, logger, thread_manager, aml_file_path='', see_id='', auto_init=False,
                 sbrc_module=None, sbrc_type=None, simulated=False,
                 ssm_module=None, ssm_type=None):

        # Internal variables of SEE
        self.ams_connected = False
        self.mes_connected = False
        self.es_connected = False
        self.aml_parser_initalized = False
        self.logger = logger
        self.thread_manager = thread_manager
        self.sbrc_module = sbrc_module
        self.sbrc_type = sbrc_type
        self.simulated = simulated
        self.ssm_module = ssm_module
        self.ssm_type = ssm_type

        if auto_init:
            self.initalise(aml_file_path, see_id)

    def initalise(self, aml_file_path, see_id=''):
        #Check if there is defined module first
        #Where to put this???
        obj_module = importlib.import_module('see.aml_parser')
        obj_class = getattr(obj_module, 'SEE_AMLParser')
        self._aml_parser = obj_class(self.logger)

        self.Id, self._description = self._aml_parser.parse_SEE_from_file(aml_file_path)

        # Init other variable for SEE
        self.name = ''
        self.condition = Condition()
        self.call = Call()
        self.remaining_duration = 0
        self.skill_id = ''
        self.next_available = time.time()*1000000
        self.skill = ExecutableSkill()
        self.skills_todo = []
        self.known_executable_skills = {}
        self.mode = SkillExecutionEngine.STATES['PreOperational']
        self.env_data = SEE_EnvironmentData(self.name, self.Id, '')
        self.productive = False

        if self.initalise_AMSComm(self._description._ams_comm):

            self.logger.loginfo("AMS communication initialized!")
            self.ams_connected = True

            if self._ams_comm.registerSEE(self.Id,
                                          self.name,
                                          self._description._aml_descr):

                while True:
                    success, mes_comm_descr, aml_descr = self._ams_comm.getRegisteredSEE(self.Id)

                    if success:
                        self.logger.loginfo("Registered SEE received from AMSService. Parsing received AML file...")
                        self.Id, self._description = self._aml_parser.parse_SEE_from_aml_file(aml_descr, self.Id)
                        break

                    else:
                        self.logger.logwarn("SEE is not yet registered on AMSService. Retrying in 5 seconds...")
                        time.sleep(5)

            else:
                    self.logger.logerror("Registering of SEE failed!")
        else:
            self.logger.logerror('AMS initalisation failed!')

        self.name = self._description._name
        self.condition = self._description._condition
        self.known_executable_skills = self.retrieve_executable_skills_for_SEE(self.Id)
        self.logger.logdebug("Known executable skills: " + str(self.known_executable_skills))

        if self._description._mes_comm.point is None:
            self._description._mes_comm = mes_comm_descr

        if self.initalise_MESComm(self._description._mes_comm):
            self.mes_connected = True
            self.logger.loginfo("MES communication initialized!")

        if self.initalise_ESComm(self._description._es_comm):
            self.es_connected = True
            self.logger.loginfo("ES communication initialized!")

        #TODO remove this fast hack
        obj_module = importlib.import_module('see.'+self.ssm_module)
        obj_class = getattr(obj_module, self.ssm_type)
        self.ssm = obj_class(self)

        # Init state machine
        self.ssm.init(self.ssm_callback)
        if (self.mes_connected):
            self._mes_comm.configureSEE()

        # Initi SBRC
        success, start_as_idle = self.initalise_SBRC(self.sbrc_module,
                                                     self.sbrc_type,
                                                     self.simulated)

        if success:
            if start_as_idle:
                self.ssm.update_state_event("EXT_CONFIGURED")
                self.logger.loginfo("See successfully configured!")
            else:
                self.logger.logwarn("SEE will not start in idle state!")
        else:
            self.logger.logerror("Could not initalise SBRC")

    def initalise_AMSComm(self, AMSComm=None):
        ret = False
        if AMSComm is None:
            AMSComm = self._description._ams_comm

        #Check if there is defined module first
        obj_module = importlib.import_module('see.ams_communication')
        obj_class = getattr(obj_module, 'AMSCommunication_WebServices')
        self._ams_comm = obj_class(self, AMSComm)
        self.logger.logdebug("AMS communication initialized!")
        if self._ams_comm.connect():
            ret = True
        #TODO initialisation error detect with exception
        #else:
            #self.logger.logcritical("Initialisation of AMS communication" + str(self._ams_comm) + " witretrieve_executable_skills_for_SEEh type: " + AMSComm.type + " on URI: " + AMSComm.uri + " failed!!")

        return ret

    def initalise_MESComm(self, MESComm=None):
        ret = False    
        if MESComm is None:
            MESComm = self.self._description._mes_comm

        #Check if there is defined moduretrieve_executable_skills_for_SEEle first
        obj_module = importlib.import_module('see.mes_communication')
        obj_class = getattr(obj_module, 'MESCommunication_OpcUa')
        self._mes_comm = obj_class(self, MESComm)
        # Init MES communication services
        if self._mes_comm.connect():
            ret = True
        else:
            self.logger.logcritical("Connection to MES on URI: " + MESComm.uri + " failed!!")
            raise SEE_Exception("SEE")
            ret = False
        return ret

    def initalise_ESComm(self, ESComm=None):
        ret = False
        if ESComm is None:
            ESComm = self.self._description._es_comm

        #Check if there is defined module first
        obj_module = importlib.import_module('see.es_communication')
        obj_class = getattr(obj_module, 'ESCommunication_SimulatedOpcUa')
        self._es_comm = obj_class(self, ESComm)
        # ES comm initalisation
        if self._es_comm.connect():
            ret = True
        else:
            #TODO
            self.logger.logcritical("Connection to ES on URI: " + ESComm.uri + " failed!!")
            raise SEE_Exception(self.name + ": Connection to MES on URI: " + MESComm.uri + " failed!")
        #else:
            #self.logger.logcritical("Initialisation of ES communication" + str(self_es_com) + " with type: " + ESComm.type + " on URI: " + ESComm.uri + " failed!!")

        return ret

    def initalise_SBRC(self, sbrc_module, sbrc_type, simulated=False):
        if sbrc_module is None or sbrc_type is None:
            raise SEE_Exception("SBRC module or type not defined. SEE can not be initalised without SBRC!")

        obj_module = importlib.import_module('see.'+sbrc_module)
        obj_class = getattr(obj_module, sbrc_type)
        self.sbrc = obj_class(self, simulated)
        success, start_as_idle = self.sbrc.initalise()

        if not success:
            self.logger.logcritical("SBRC not initalised successfully!")

        return success, start_as_idle

    def retrieve_executable_skills_for_SEE(self, seeId=None):
        ret = {}
        if seeId is None:
            seeId = self.Id
        ## Get all Executable Skill for the SEE
        success_skills, retrieved_skills = self._ams_comm.getExecutableSkills(seeId)
        if success_skills:
            ret = self._aml_parser.parseExecuableSkillsFromAMS(retrieved_skills)
            self.logger.loginfo("Got skills from AMS.")

        return ret

    def ssm_callback(self, new_state):
        self.logger.loginfo("SEE-SM Callback: " + str(new_state) + " received.")
        self.mode = new_state
        self.logger.logdebug(self.name + ": SEE State changed to mode: " + str(self.mode))
        self._mes_comm.updateSEE()

    def mes_callback(self):

        self.logger.loginfo("MES Call: '" + self.call.call + "' received.")
        self.logger.logdebug("SEE state: " + str(self.mode))
        skill = None

        if not self.call.call_exception:
            if self.call.command == 'ExecSkill':
                if self.mode < 300:
                    exists = False
                    # First try to get skill from AMS
                    exists, aml_exec_skill = self._ams_comm.getExecutableSkill(self.call.skill_id)
                    if exists:
                        skill = self._aml_parser.parseExecutableSkill(self.call.skill_id, aml_exec_skill)
                        self.known_executable_skills[skill.id] = skill
                        from_ams = True
                        self.logger.logdebug("Skill (ID: " + self.call.skill_id + ") recived from AMS")
                    elif self.call.skill_id in self.known_executable_skills.keys():
                        skill = self.known_executable_skills[self.call.skill_id]
                        exists = True
                        self.logger.logwarn("Skill (ID: " + self.call.skill_id + ") not found in AMS but in the list of known skills!")
                    else:
                        self.logger.logwarn("Skill (ID: " + self.call.skill_id + ") not found in AMS nor in the list of known skills!! Trying the local AML file.")
                        exists, skill = self._description.get_executable_skill(self.call.skill_id)

                    if exists:
                        with mutex:
                            self.skills_todo.append(skill)
                        self.thread_manager.startThread(self.execute_skill)

                    else:
                        self.call.set_error_NonexistingSkill()
                        self.logger.logerror("Skill with Id: " + self.call.skill_id + " does not exist!")
                        self._mes_comm.updateSEE()

                else:
                    self.call.set_error_SEENotInIdle()
                    self._mes_comm.updateSEE()

            elif self.call.command == 'Clear':
                self.logger.loginfo("Clearing Skill with ID: " + skill.id)
                self.sbrc.clear(skill.id)
            elif self.call.command == 'Pause':
                self.logger.loginfo("Pausing Skill with ID: " + skill.id)
                self.sbrc.pause(skill.id)
            elif self.call.command == 'Resume':
                self.logger.loginfo("Reasuming Skill with ID: " + skill.id)
                self.sbrc.resume(skill.id)

        else:
            self._mes_comm.updateSEE()

    def execute_skill(self):
        while not self.mode == SkillExecutionEngine.STATES['Idle'] and (self.mode in range(200, 300) or self.mode == SkillExecutionEngine.STATES['PreOperational']):
            self.logger.logdebug("Waiting for 'Idle' state to start executing skill '" + self.skills_todo[0].id + "'")
            time.sleep(1)

        self.ssm.update_state_event("MES_ExecSkillTimestamp")
        if len(self.skills_todo):
            with mutex:
                self.skill = self.skills_todo.pop(0)
                self.skill_id = self.skill.id
                self.remaining_duration = 1000
            success, error_message = self.sbrc.check_preCondition(self.skill.preCondition)
            if success:
                self._feedback_thread = self.thread_manager.startThread(self.execution_feedback_thread)
                self.logger.loginfo("Executing Skill with ID: " + self.skill_id + " at timestamp: " + str(self.call.timestamp))
                if (self.call.timestamp != 0):
                    self.ssm.update_state_event("MES_ExecSkillTimestamp")
                self.call.set_EverythingOK()
                self.sbrc.execute(self.skill, self.call.timestamp)
            else:
                self.call.set_error_PreconditionsNotMet()
                self.logger.logwarn("For Skill with Id: " + self.call.skill_id + " preconditions are not met!" + " Error: " + error_message)
                self._mes_comm.updateSEE()

    def execution_done_callback(self, succeeded, alternative):
        with mutex:
            self.remaining_duration = 0
        self.next_available = time.time()*1000000
        self.productive = False

        if succeeded:
            with mutex:
                self.logger.loginfo("ExecutableSkill: " + self.skill.id + " finished successfully.")
                if not alternative:
                    self.condition.apply_postcondition(self.skill.postCondition)
                else:
                    self.condition.apply_postcondition(self.skill.altPostCondition)

                self.logger.logdebug("SEE condition is: " + str(self.condition))

            self.ssm.update_state_event("INT_SKILL_EXECUTED")

        else:
            self.logger.logerror("ExecutableSkill: " + self.skill.id + " Aborted!")

    def execution_active_callback(self):
        self.logger.loginfo("ExecutableSkill: " + self.skill.id + " is active!")

        with mutex:
            if len(self.skill.postCondition.products):
                self.productive = True
        if (self.mode == SkillExecutionEngine.STATES['Idle']):
            self.ssm.update_state_event("MES_ExecSkill")
        if (self.mode == SkillExecutionEngine.STATES['IdleQueuedSkill']):
            self.ssm.update_state_event("INT_EXEC_SKILL")

    def execution_feedback_thread(self):
        while (self.remaining_duration):
            time.sleep(0.5)
            with mutex:
                self.remaining_duration = self.skill.remaining_duration
            self.logger.logdebug("Remaining duration for ExecutableSkill: " + self.skill.id + " is " + str(self.remaining_duration) + " s")
            self.next_available = (time.time()+self.remaining_duration)*1000000

            self._mes_comm.updateSEE()

    def set_productive(self, productive):
        self.productive = productive
        self._mes_comm.updateSEE()

    def __str__(self):
        return "name: " + self.name

    ######## Implemented for HMI, but can be used for GUI after
    def set_condition(self, condition):
        self.condition = condition
        self._mes_comm.updateSEE()

    def shutdown(self):
        #TODO: add for the rest of the stuff
        self._mes_comm.disconnect()
        self._es_comm.disconnect()
        self.sbrc.shutdown()


## Class SEE_ManagerException implements
class SEE_Exception(Exception):

    def __init__(self, message):
        super(SEE_Exception, self).__init__(message)
