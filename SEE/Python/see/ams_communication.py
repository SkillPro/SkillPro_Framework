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
# @file ams_communication
# @author Denis Štogl
##

import types
import requests
import json

from data_model.communication import CommunicationDescription, CommunicationPoint
from data_model.executable_skill import RetrievedExecutableSkill

# AMSCommunication class.
# This class is base class for AMS communication.
##
class AMSCommunication(object):

    def __init__(self, see, AMSComm):
        # Communication description
        self.see = see
        self._comm = AMSComm
        # Connection status
        self._connected = False

    def connect(self):
        raise NotImplementedError("Should have implemented this")

    def disconnect(self):
        raise NotImplementedError("Should have implemented this")

    def registerSEE(self, id, aml):
        raise NotImplementedError("Should have implemented this")

    def getRegisteredSEE(self, id):
        raise NotImplementedError("Should have implemented this")

    def getExecutableSkill(self, skill_id):
        raise NotImplementedError("Should have implemented this")

    def getExecutableSkills(self, see_id):
        raise NotImplementedError("Should have implemented this")


class AMSCommunication_WebServices(AMSCommunication):

    def __init__(self, see, AMSComm):
        super(AMSCommunication_WebServices, self).__init__(see, AMSComm)

        # WebServices headers
        self._headers = {'Content-type': 'application/x-www-form-urlencoded', 'Accept': 'text/plain'}

    def delete(self):
        pass

    def connect(self):
        url = self._comm.uri + 'checkConnection'
        self.see.logger.logdebug("Calling 'checkConnection' service of AMS at: " + url)
        self._connected, data = self._call_ams_service(url)

        if self._connected:
            if (data == '"Hello from AMSService"'):
                self._connected = True
            else:
                self._connected = False
                self.see.logger.logerror("Unsuccessful connection to AMS! Error: " + "Wrong answer: " + r.text + " form AMS service at: " + url)
        else:
            self.see.logger.logerror("Unsuccessful connection to AMS! Error: " + data)

        return self._connected

    def disconnect(self):
        self._connected = False
        return True

    def registerSEE(self, id, name, aml):
        url = self._comm.uri + 'registerSEE'
        data = {'seeId': id, 'assetTypeNames': name, 'amlFile': aml}
        self.see.logger.logdebug("Calling 'registerSEE' service of AMS at: " + url)

        success, message = self._call_ams_service(url, data)
        if not success:
            self.see.logger.logerror("Unsuccessful registration of see with id: " + id + " on AMS!!")
            self.see.logger.logerror("Error message: " + message)

        return success

    def getRegisteredSEE(self, id):
        url = self._comm.uri + 'getRegisteredSEE' + '?seeId=' + id
        self.see.logger.logdebug("Calling 'getRegisteredSEE' service of AMS at: " + url)

        comm_descr = CommunicationDescription()
        aml_descr = None

        success, data = self._call_ams_service(url)
        data = json.loads(data)

        if 'status' in data.keys():
            if 'ERROR' in data['status']:
                success = False
                self.see.logger.logerror(data['message'])

        if success:
            comm_descr.set_point(CommunicationPoint(str(data['opcuaAddress']),
                                                    "ns=" + str(data['nameSpace']) +
                                                    ";" + "i=" +
                                                    str(data['identifier'])))
            aml_descr = str(data['amlDescription'])

        return success, comm_descr, aml_descr

    def getExecutableSkill(self, skill_id):
        url = self._comm.uri + 'retrieveResourceExecutableSkill' + '?id=' + skill_id
        self.see.logger.logdebug("Calling 'retrieveResourceExecutableSkill' service of AMS at: " + url)
        aml_description = ''

        success, data = self._call_ams_service(url)
        data = json.loads(data)

        if success:
            if 'status' in data.keys():
                if 'ERROR' in data['status']:
                    success = False
            if success:
                #TODO: Check if the correct SEE
                #skill_id = data['resourceExecutableSkillID']
                #see_id = data['seeID']
                aml_description = data['amlDescription']
            else:
                self.see.logger.logwarn("Unsuccessful access to ExecutableSkill with id: " + skill_id + " on AMS!! Error: " + data['message'])

        else:
            self.see.logger.logerror("Unsuccessful access to ExecutableSkill with id: " + skill_id + " on AMS!! Error: " + data)

        return success, aml_description

    def getExecutableSkills(self, see_id):
        url = self._comm.uri + 'retrieveResourceExecutableSkills' + '?seeId=' + see_id
        self.see.logger.logdebug("Calling 'retrieveResourceExecutableSkills' service of AMS at: " + url)

        executable_skills = {}

        success, data = self._call_ams_service(url)
        data = json.loads(data)

        if success:

            if type(data) != types.ListType:
                if 'ERROR' in data['status']:
                    success = False
            if success:
                for member in data:
                    executable_skill = RetrievedExecutableSkill(member['amlDescription'])
                    executable_skills[member['resourceExecutableSkillID']] = executable_skill
                    #TODO: Check seeID
                    #executable_skill.see_id = member['seeID']
            else:
                self.see.logger.logerror("Unsuccessful access to ExecutableSkills of SEE with id: " + see_id + " on AMS!! Error: " + data['message'])
        else:
            self.see.logger.logerror("Unsuccessful access to ExecutableSkills of SEE with id: " + see_id + " on AMS!! Error: " + data)

        return success, executable_skills

    def _call_ams_service(self, url, data=None):

        if data is None:
            r = requests.get(url, headers=self._headers)
        else:
            r = requests.post(url,
                              data=json.dumps(data),
                              headers=self._headers)

        if (r.status_code == 200):
            return True, r.text
        else:
            return False, "Communication error: " + str(r.status_code) + " with AMS service at: " + url


class AMSCommunication_SimulatedWebServices(AMSCommunication_WebServices):

    def __init__(self, see, AMSComm):
        super(AMSCommunication_SimulatedWebServices, self).__init__(see, AMSComm)

    def connect(self):
        return True

    def registerSEE(self, id, aml):
        return True

    def getRegisteredSEE(self, id):
        return True, CommunicationPoint('opc.tcp://localhost:51200', '')

    def getExecutableSkill(self, skill_id):
        return False, ''

    def getExecutableSkills(self, see_id):
        return False, {}
