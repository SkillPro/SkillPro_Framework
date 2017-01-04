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
# @file sbrc
# @author Denis Štogl
##

import importlib
import threading
import time


## SkillBasedResourceController class.
# This class implements base structure of Skill based resource controller.
##
class SkillBasedResourceController(object):

    def __init__(self, see, simulated):
        if getattr(self, "SYSTEM_IMPORTS", None):
            self.sys_mods = {}
            for module in self.SYSTEM_IMPORTS:
                self.sys_mods[module] = importlib.import_module(self.SYSTEM_IMPORTS[module])

        self.see = see
        self.simulated = simulated
        self.skills = {}
        self.load_skills()

        if getattr(self, "IMPORTANT_COORDINATES", None):
            for key in self.IMPORTANT_COORDINATES.keys():
                self.see._es_comm.add_object(
                    key,
                    self.IMPORTANT_COORDINATES[key][0],
                    self.IMPORTANT_COORDINATES[key][1])
                self.see._es_comm.update_object(
                    key,
                    self.IMPORTANT_COORDINATES[key][0],
                    self.IMPORTANT_COORDINATES[key][1])

        if self.simulated:
            self.see.logger.logwarn("Simulated flag set!")

        self.skill = None
        self.finished_successfull = False

        if self.PUBLISH_TO_ES:
            if not self.STATIC_POSITION:
                self.mutex = threading.Lock()
                self.es_data_change = False

            if self.ES_DATA_TYPE == 'joint_values':
                self.joint_values = [0] * 6

            if self.ES_DATA_TYPE == 'TCP_position':
                self.frame = ''
                self.tcp_position = {}
                self.tcp_position['x'] = 0
                self.tcp_position['y'] = 0
                self.tcp_position['z'] = 0
                self.tcp_position['rx'] = 0
                self.tcp_position['ry'] = 0
                self.tcp_position['rz'] = 0

    def load_skills(self):
        for skill in self.SKILLS:
            obj_module = importlib.import_module('see.skill.'+skill[0])
            obj_class = getattr(obj_module, skill[1])
            instance = obj_class(self)
            self.skills[instance.KEY] = instance

    def initalise(self, auto_init):
        self._initalise = auto_init

        if self.PUBLISH_TO_ES and self.STATIC_POSITION:
            self.publish_to_ES()
        return auto_init

    def check_preCondition(self, precondition):
        self.see.logger.logwarn("Using default precondition checking!")
        return self.see.condition.check_precondition(precondition)

    def execute(self, skill, timestamp=0):
        self.skill = skill
        self.skill_is_master = False
        self.skill_is_slave = False
        self.slave_finished = False

        self.see.logger.loginfo("Executing Skill: " + skill.name + " - " + skill.id)

        if self.skill.sync.type == "MASTER":
            self.skill_is_master = True
            if not self.see._mes_comm.find_slave_SEEs(self.skill.sync.address):
                self.finished(False, False)
                return
        if self.skill.sync.type == "SLAVE":
            self.skill_is_slave = True
            if not self.see._mes_comm.listen_master_on_address(
                self.skill.sync.address, self.slave_callback):
                self.finished(False, False)
                return

        if self.skill.execution.type in self.skills.keys():
            if self.PUBLISH_TO_ES and not self.STATIC_POSITION:
                self._es_comm_thread = self.see.thread_manager.startThread(
                    self.publish_to_ES_thread)

            if timestamp != 0:
                wait = timestamp - time.time()
                if wait > 0:
                    time.sleep(wait)

            ok, kwargs = self.skills[self.skill.execution.type].check_parameters(
                self.skill.execution.data)
            if ok:
                self.see.execution_active_callback()
                self.skills[self.skill.execution.type].execute(**kwargs)

                if self.skill:
                    self.see.thread_manager.startThread(
                        self.wait_skill_execution_to_finish)

        else:
            self.see.logger.logwarn("Skill type: " + self.skill.execution.type + " not implemented for this RC!")
            self.finished(False, False)

    def wait_skill_execution_to_finish(self):
        while self.skill:
            self.see.logger.loginfo("Waiting on a " + self.__class__.__name__ + " to finish skill: " + self.skill.execution.type + " (" + self.skill.id + ")")
            #raise Exception()
            time.sleep(3)

    def feedback(self, remaining_duration):
        if self.skill:
            self.skill.remaining_duration = remaining_duration

    def execute_simulated(self):
        self.see.logger.logwarn("Executing simulated skill!")
        print("-----------------------------------------------")
        print("Recived Skill paramters:")
        print("Id: " + self.skill.id)
        print("Name: " + self.skill.name)
        print("Preconditions: ")
        print("    configuration: " + str(self.skill.preCondition.configurations))
        print("    product: " + str(self.skill.preCondition.products))
        print("Postcondition: ")
        print("    configuration: " + str(self.skill.postCondition.configurations))
        print("    product: " + str(self.skill.postCondition.products))
        print("AlternativePostcondition: ")
        print("    configuration: " + str(self.skill.altPostCondition.configurations))
        print("    product: " + str(self.skill.altPostCondition.products))
        print("SkillSync: ")
        print("    type: " + self.skill.sync.type)
        print("    address: " + str(self.skill.sync.address))
        print("Duration: " + str(self.skill.duration))
        print("Execution: ")
        print("    type: " + self.skill.execution.type)
        print("    data: " + str(self.skill.execution.data))
        print("Duration: " + str(self.skill.duration))
        print("-----------------------------------------------")

    def start_execution_on_hardware(self, program):
        raise Exception("You should implement this for SkillBasedResourceController: " + self.__class__.__name__)

    def exec_default(self, **kwargs):
        self.see.logger.logwarn(
            "Executing: 'exec_default_sim' function'. Recived arguments: " +
            str(kwargs))
        self.execute_simulated()
        if not self.skill_is_slave:
            self.finished(True, False)

    def exec_default_sim(self, **kwargs):
        self.see.logger.logwarn(
            "Executing: 'exec_default_sim' function'. Recived arguments: " +
            str(kwargs))
        self.execute_simulated()
        if not self.skill_is_slave:
            self.finished(True, False)

    def slave_callback(self):
        self.slave_finished = True
        self.finished(True, False)

    def finished(self, success, alternative):
        if not self.skill:
            return
        if self.skill_is_slave and not self.slave_finished:
            return

        if self.PUBLISH_TO_ES and not self.STATIC_POSITION:
            self.see.thread_manager.joinThread(self._es_comm_thread)
            self.see.logger.logdebug("Finished ES thread")

        self.skill = None
        if self.skill_is_master:
            self.see._mes_comm.trigger_end_on_slave_SEEs()
        self.see.execution_done_callback(success, alternative)
        self.see.logger.loginfo("postCond:" + str(self.see.condition.__dict__))

    def publish_to_ES(self):
        if self.ES_DATA_TYPE == 'joint_states':
            self.see.logger.logdebug("Writing joint_states")
            self.see.env_data.internal_state.states = self.joint_values
            #BEGIN - Just for End-Demo - Christer
            joint_values_mes = {}
            i = 0
            for key in ['x', 'y', 'z', 'rx', 'ry', 'rz']:
                joint_values_mes[key] = self.joint_values[i]
                i += 1
                self.see._mes_comm.write(self.ES_DATA_TYPE, joint_values_mes)
            #END - Just for End-Demo - Christer

        if self.ES_DATA_TYPE == 'TCP_position':
            self.see.logger.logdebug("Writing TCP_position")
            self.see.env_data.tcp_pose.frame = self.frame
            self.see.env_data.tcp_pose.coordinates = self.tcp_position

        #TODO this should be written on the begin
        if self.ES_DATA_TYPE == 'Base_position':
            self.see.logger.logdebug("Writing Base_position")
            self.env_data.base_pose.frame = self.frame
            #self.env_data.base_pose.coordinates = self.base_pose

        self.see._es_comm.write(self.see.env_data)

    def publish_to_ES_thread(self):
        self.see.logger.loginfo("Running publish to ES")

        while(True):
            if self.es_data_change:
                self.mutex.acquire()
                self.es_data_change = False
                self.publish_to_ES()
                self.mutex.release()

    # Default waiting function
    def exec_Waiting(self, duration):
        if self.simulated:
                self.execute_simulated()
        else:
            for i in range(0, duration):
                self.feedback(duration-i)
                self.see.logger.loginfo("Until now waiting for " + str(i)
                                        + " seconds. Still to wait for "
                                        + str(duration-i) + " seconds!")
                time.sleep(1)

        self.finished(True, False)

    def shutdown(self):
        self.see.logger.logwarn("Executing default SBRC shutdown function")
        pass


class Simple_SBRC(SkillBasedResourceController):

    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['skill', 'Simple'],
        ['skill', 'Waiting'],
        ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Simple_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Simple_SBRC, self).initalise(True)

        self.see.logger.loginfo("Simple SBRC initalised!")
        return True, True

    def exec_Simple(self, program):
        print
        print("------------------------------------------------------")
        print(program)
        print("------------------------------------------------------")
        print
        self.finished(True, False)


class Buffer_SBRC(SkillBasedResourceController):

    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['machine', 'Load'],
        ['machine', 'Unload'],
        ['skill', 'Waiting']
        ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Buffer_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Buffer_SBRC, self).initalise(True)

        self.see.logger.loginfo("Buffer_SBRC initalised!")
        return True, True


class UR5_SBRC(SkillBasedResourceController):

    SYSTEM_IMPORTS = {'socket': 'socket'}

    SKILLS = [
        ['skill', 'Waiting'],
        ['machine', 'Load'],
        ['machine', 'Unload'],
        ['machine', 'Move6D'],
        ['machine', 'RecognizeMove6D'],
        ['machine', 'RecognizeTransport6D'],
        #['machine', 'RecognizeGoToPosition'],
        #['machine', 'Transport6D'],
        ]

    ur5_address = '141.3.82.78'

    server_address = ('141.3.81.130', 30000)
    client_address = ('141.3.82.78', 29999)

    Timeout = 20

    PUBLISH_TO_ES = True
    ES_DATA_TYPE = 'joint_states'
    STATIC_POSITION = False
    IMPORTANT_COORDINATES = {
        'table': ('UR5_frame', [10, 20, 0.5, 12, 13, 15])
    }

    def __init__(self, see, simulated):
        super(UR5_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(UR5_SBRC, self).initalise(True)

        if not self.simulated:
            self._server_socket = self.sys_mods['socket'].socket(
                self.sys_mods['socket'].AF_INET,
                self.sys_mods['socket'].SOCK_STREAM)
            self._server_socket.bind(UR5_SBRC.server_address)
            self._client_sock = self.sys_mods['socket'].socket(
                self.sys_mods['socket'].AF_INET,
                self.sys_mods['socket'].SOCK_STREAM)

            self.see.logger.loginfo("Connecting to UR5 on address: " + str(UR5_SBRC.client_address))
            self._client_sock.connect(UR5_SBRC.client_address)
            data = self._client_sock.recv(4096)
            self.see.logger.loginfo(data)
            self.see.logger.loginfo("Stopping the UR5")
            resp = self.send_to_UR5('stop\n')
            self.see.logger.loginfo(resp)

        else:
            self.joint_values = [0, 1, 2, 3, 4, 5]

        self.see.logger.loginfo("UR5 SBRC initalised!")
        return True, start_as_idle

    def exec_Move6D(self, goal):
        self.start_execution_on_hardware(goal)

    def exec_RecognizeMove6D(self, object, goal):
        self.start_execution_on_hardware(object)

    def exec_RecognizeTransport6D(self, object, goal):
        self.start_execution_on_hardware(object)

    def start_execution_on_hardware(self, program):

        if not self.simulated:
            #START - For the end Demo
            if isinstance(program, list):
                program = 'SP_FD_BASEPOS_2'
            #END - For the end Demo
            message = 'load /programs/' + program.upper() + '.urp\n'
            self.see.logger.loginfo("Loading program to UR5 with command: " + message)
            resp = self.send_to_UR5(message)
            self.see.logger.loginfo("Response: " + resp)
            #Need to wait for 2 seconds for UR5 "Klar zu kommen"
            time.sleep(3)
            t = self.see.thread_manager.startThread(self.wait_for_answer)
            #t = threading.Thread(target=self.wait_for_answer)
            #t.start()

            message = 'play\n'
            self.see.logger.loginfo("Starting program on UR5 with message: "
                                    + message)
            resp = self.send_to_UR5(message)
            self.see.logger.loginfo("Response: " + resp)

        else:
            self.execute_simulated()
            if self.PUBLISH_TO_ES:
                print("Sending test Environement data: ")
                for i in range(0, 4):
                    self.mutex.acquire()
                    self.joint_values = [elem + 1 for elem in self.joint_values]
                    self.es_data_change = True
                    print(self.joint_values)
                    self.mutex.release()
                    time.sleep(1)
                print("-----------------------------------------------")
                self.finished(True, False)

    def send_to_UR5(self, message):
        self._client_sock.sendall(message)
        return self._client_sock.recv(4096)

    def wait_for_answer(self):
        self._server_socket.listen(0)
        connection, client_address = self._server_socket.accept()

        self.see.logger.loginfo("Recieved connection from: " + str(client_address))

        start = time.time()
        self.joint_values = [0] * 6
        run = True

        while run:
            if client_address[0] == UR5_SBRC.ur5_address:
                recived = connection.recv(32768)
                self.see.logger.logdebug("Recived data: " + recived)

                for data in recived.split("\n"):

                    if 'finished' in data:
                        self.finished(True, False)
                        run = False

                    if 'failed' in data:
                        self.finished(False, False)
                        run = False

                    if '[' and ']' in data:
                        if self.PUBLISH_TO_ES:
                            self.mutex.acquire()
                            self.joint_values = eval(data)
                            self.es_data_change = True
                            self.mutex.release()

                print(time.time() - start)
                if time.time() - start > UR5_SBRC.Timeout:

                    self.finished(True, False)
                    self.see.logger.logwarn('Execution Timeout!!')
                    run = False

        connection.close()


class Human_SBRC(SkillBasedResourceController):

    SYSTEM_IMPORTS = {'requests': 'requests',
                      'json': 'json',
                      'communication': 'see.opcua_communication'}

    SKILLS = [
        ['human', 'TransportHuman'],
        ['human', 'PackagingHuman'],
        ['human', 'OperateMilling'],
        ['machine', 'Unload'],
        ]

    gui_ip = 'localhost:8282'

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = 'TCP_position'
    STATIC_POSITION = True

    IMPORTANT_COORDINATES = {
        "PositionBoxOutput"    : "PositionBoxOutput" ,
        "PositionQualityCheck" : "PositionQualityCheck",
        "PositionMobileRobot"  : "PositionMobileRobot",
        "PositionLineOutput"   : "PositionLineOutput",
        "PositionHumanLineOutput"   :"PositionHumanLineOutput",
        #--- above: Schunk, Cell2 ---#
        "PositionHumanKR5Output"    : "PositionHumanKR5Output",
        "PositionKR5Output"    : "PositionKR5Output",
        "PositionPCBPrinter"   : "PositionPCBPrinter",
        "PositionMobilePlatform"   : "PositionMobilePlatform",
        "PositionSolderingMachineInput"   : "PositionSolderingMachineInput",
        "PositionSolderingMachineOutput"   : "PositionSolderingMachineOutput",
        #"PositionLineOutput"   : [[],[],up],
        #--- above: Kuka KR5, Cell1 ---#
        "PositionInput"   : "PositionInput",
        "PositionOutput"   : "PositionOutput",
        #--- above: Soldering Machine ---#
        "PositionKR5"   : "PositionKR5",
        "PositionSchunkArm"   : "PositionSchunkArm",
        #--- above: Mobile Platform ---#
        # "Neutral" : handled by robots #
    }

    def __init__(self, see, simulated):
        super(Human_SBRC, self).__init__(see, simulated)
        self.frame = self.see.name
        self.callback_data = None

    def initalise(self):
        start_as_idle = super(Human_SBRC, self).initalise(False)

        if not self.simulated:
            self.comm = self.sys_mods['communication'].SkillPro_OpcUa(self.see, "opc.tcp://localhost:13576/skillpro/humanSEE/")

            while True:
                try:
                    self.comm.opcua_connect()
                    break
                except:
                    self.see.logger.logwarn("OPC UA server for HumanSEE not found. Please start the server")
                    time.sleep(3)

            self._url = "http://" + Human_SBRC.gui_ip + "/api/" + self.see.Id + "/ExecutableSkills"
            self._headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}

            self.objects = self.comm.client.get_objects_node()
            self.answer = self.objects.call_method("2:RegisterHuman", self.see.Id)

            self.comm.opcua_create_subscription(self.answer, self.callback)

        else:
            start_as_idle = True

        self.see.logger.loginfo("Human SBRC initalised!")
        return True, start_as_idle

    def callback(self, data, node):
        self.see.logger.logdebug("HumanSEE: Callback from server received!")
        self.callback_data = data
        self.see.thread_manager.startThread(
            self.human_callback)
        self.see.logger.logdebug("HumanSEE: Started thread human_callback!")

    def human_callback(self):

        self.see.logger.loginfo("Data received from Mobile-SEE: '" + self.callback_data + "'")

        if self.callback_data == '':
            return

        if self.callback_data == 'CONFIGURE':
            self.see.ssm.update_state_event("EXT_CONFIGURED")

        elif self.callback_data == 'SHUTDOWN':
            if self.skill:
                if self._get_human_status() == 'pause':
                    self.see.ssm.update_state_event("MES_Resume")
                self.see.ssm.update_state_event("INT_SKILL_EXECUTED")
                self.finished(True, False)
            time.sleep(0.2)
            self.see.ssm.update_state_event("EXT_SHUTDOWN")

        if self.skill:
                current_skill_status = self._get_human_status()
                if self.callback_data == 'PAUSE':
                    if self.skill_status == 'accepted' and current_skill_status == 'pause':
                        self.see.ssm.update_state_event("MES_Pause")
                        time.sleep(0.2)
                        self.see.ssm.update_state_event("INT_CAN_RESUME")

                if self.callback_data == 'UPDATE':
                    if self.skill_status == 'new' and current_skill_status == 'accepted':
                        self.see.set_productive(True)
                    if self.skill_status == 'pause' and current_skill_status == 'accepted':
                        self.see.ssm.update_state_event("MES_Resume")

                elif self.callback_data == 'STOP':
                    if self.skill_status == 'accepted' and  current_skill_status == 'succeeded':
                        self.finished(True, False)

                elif self.callback_data == 'THROWTE':
                    if self.skill_status == 'accepted' and  current_skill_status == 'failed':
                        self.finished(False, False)

                elif self.callback_data == 'RECOVER':
                    pass
                self.skill_status = current_skill_status

    def _generate_parameter_list(self, data):
        program = "<ul>"
        for parameter in data.keys():
            program += "<li><p class=\"ng-binding\">" + str(parameter) + ":  <i>" + data[parameter] + "</p></i></li>"
        program += "</ul>"

        return program

    def _instruction_with_figure(self, instruction, figure):
        program = "<p class=\"ng-binding\"><b>" + instruction + "</b>:</p>"
        program += "<br>"
        program += "<img src=\"" + figure + "\" width=\"90\%\">"

        return program

    def exec_TransportHuman(self, products, start, goal):
        program = "<p class=\"ng-binding\"><b>Please transport products</b>:</p>"
        program += "<ul>"
        for product in products.keys():
            program += "<li><p class=\"ng-binding\">" + str(products[product]) + "  <i>" + str(product) + "</p></i></li>"
        program += "</ul>"
        program += "<p class=\"ng-binding\">"
        program += "<b>From</b>: " + "<i>" + start + "</i><br>"
        program += "<b>To</b>: " + "<i>" + goal + "</i><br>"
        program += "</p>"

        self.start_execution_on_hardware(program)

    def exec_PackagingHuman(self, products):
        program = "<p class=\"ng-binding\"><b>Please package products</b>:</p>"
        program += "<ul>"
        for product in products.keys():
            program += "<li><p class=\"ng-binding\">" + str(products[product]) + "  <i>" + str(product) + "</p></i></li>"
        program += "</ul>"

        self.start_execution_on_hardware(program)

    def exec_OperateMilling(self, instruction, figure, speed, depth):

        program = self._instruction_with_figure(instruction, figure)
        program += "<br>"
        program += self._generate_parameter_list({'Speed': speed,
                                                  'Depth': depth})

        self.start_execution_on_hardware(program)

    def start_execution_on_hardware(self, program):
        data = {}

        data['html'] = program

        data['name'] = self.skills[self.skill.execution.type].NAME
        data['status'] = 'new'
        data['skillId'] = self.skill.id + str(time.time())

        if not self.simulated:
            json_string = self.sys_mods['json'].dumps(data, indent=4, separators=(',', ': '))

            try:
                r = self.sys_mods['requests'].post(self._url, data=json_string, headers=self._headers)
            except:
                self.see.logger.logerror("Requests error!!!")

            if r.status_code == 200:
                self._current_skill_id = data['skillId']
                self.skill_status = self._get_human_status()
                # Why this doesn't work with skill_type thread
#                 self._current_skill_type = self.skill.execution.type
#                 self._current_skill_id = data['skillId']
#                 self._human_get_status = threading.Thread(target=self.self._get_human_status)
#                 self._human_get_status.start()
                pass

        else:
            self.execute_simulated()
            self.finished(True, False)


    #def _get_human_status_thread(self):
        #skill_status = 'new'
        #self.see.logger.logwarn("Started Thread")

        #rate = rospy.Rate(20)
        #while not rospy.is_shutdown():

            #r = self.sys_mods['requests'].get(self._url, headers=self._headers)
            #if (r.status_code == 200):
                #data = self.sys_mods['json'].loads(r.text)

                #for exec_skill in data:
                    #if exec_skill['skillId'] == self._current_skill_id:

                        #if exec_skill['status'] == 'succeeded':
                            #self.skills[self._current_skill_type].finished = True
                            #self.skills[self._current_skill_type].finished_successfull = True

                        #elif exec_skill['status'] == 'pause':
                            #if skill_status == 'accepted':
                                #self.see.ssm.update_state_event("MES_Pause")
                                #time.sleep(0.2)
                                #self.see.ssm.update_state_event("INT_CAN_RESUME")

                        #elif exec_skill['status'] == 'accepted':
                            #if skill_status == 'pause':
                                #self.see.ssm.update_state_event("MES_Resume")

                        #skill_status = exec_skill['status']
            #rate.sleep()

    def _get_human_status(self):

        r = self.sys_mods['requests'].get(self._url, headers=self._headers)
        if (r.status_code == 200):
            data = self.sys_mods['json'].loads(r.text)

            for exec_skill in data:
                if exec_skill['skillId'] == self._current_skill_id:
                    return exec_skill['status']

    def shutdown(self):
        self.comm.shutdown()
