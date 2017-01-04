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
# @file mes_communication
# @author Denis Štogl
##

import sys
import opcua

from opcua_communication import SkillPro_OpcUa

from data_model.call import Call
from data_model.environment import EnvironemntData
from see import SEE_Exception

import threading
mutex = threading.Lock()
sub_mutex = threading.Lock()


## MESCommunication class.
# This class is base class for AMS communication.
##
class MESCommunication(object):

    def __init__(self, see, MESComm):
        # Communication description
        self.see = see
        self.comm = MESComm

        # Connection status
        self._connected = False

    def connect(self):
        raise NotImplementedError("Should have implemented this")

    def disconnect(self):
        raise NotImplementedError("Should have implemented this")

    def configureSEE(self):
        raise NotImplementedError("Should have implemented this")

    def update(self):
        raise NotImplementedError("Should have implemented this")

    def callback(self, data, node):
        self.see.logger.logdebug("MESCommuniction: Callback received!")
        self.see.call = Call(data)
        self.see.thread_manager.startThread(
            self.see.mes_callback)
        self.see.logger.logdebug("MESCommunication: Started thread mes_callback!")

    def shutdown(self):
        raise NotImplementedError("Should have implemented this")


class MESCommunication_OpcUa(MESCommunication, SkillPro_OpcUa):

    def __init__(self, see, MESComm):
        # TODO: why super is not working here?
        #super(MESCommunication_OpcUa, self).__init__(see, MESComm)
        MESCommunication.__init__(self, see, MESComm)
        SkillPro_OpcUa.__init__(self, see, MESComm)

    def connect(self):
        self.see.logger.logdebug("Connecting to OPC UA server url: " +
                                 self.comm.uri)
        self._connected = self.opcua_connect()

        if self._connected:
            if not self.comm.point.nodeId:
                found, self.comm.point.nodeId = self.find_SEE_on_server("SEE")

            self.nodeId_members_prefix = self.get_nodeId_members_prefix_from_nodeId(
                self.comm.point.nodeId)

            # Subscribe to Call variable
            self.opcua_create_subscription(self.nodeId_members_prefix + '.Call',
                                        self.callback)

        else:
            self.see.logger.logerror("Connection to OPC UA server on: " +
                                     self.comm.uri + " was not successfull")

        return self._connected

    def disconnect(self):
        self.opcua_disconnect()

    def configureSEE(self):
        self.see.logger.logdebug('Configure SEE called')

        if not self._connected:
            self.see.logger.logerror('MES interaface not connected!')
            return False

        ########### Skip this to not generate additional structures
        #self.see.logger.logdebug('Writing AML_Description!')
        #self.set_node_value(self.nodeId_members_prefix + '.AML_Description',
                            #self.see._description._aml_descr,
                            #opcua.ua.VariantType.String)

        self.see.logger.logdebug('Writing SEE_Id!')
        self.set_node_value(self.nodeId_members_prefix + '.SEE_Id',
                            self.see.Id,
                            opcua.ua.VariantType.String)

        mutex.acquire()
        res = self._update_see_data()
        mutex.release()

        return res

    def updateSEE(self):
        self.see.thread_manager.startThread(self._updateSEE)

    def _updateSEE(self):
        self.see.logger.logdebug('Update SEE thread: started...')

        if not self._connected:
            return False

        self.see.logger.logdebug('... Update SEE thread: waiting for mutex...')
        mutex.acquire()
        self.see.logger.logdebug('... Update SEE thread: updating data...')
        res = self._update_see_data()
        obj = self.client.get_node(self.comm.point.nodeId)
        #res1 = obj.call_method(self.nodeId_members_prefix + '.UpdateComplete')
        res1 = obj.call_method('2:UpdateComplete')
        mutex.release()
        self.see.logger.logdebug('... finishing Update SEE thread!')

    def _update_see_data(self):
        self.see.logger.logdebug('Writing Call_Exception: ' + str(self.see.call.call_exception))
        self.set_node_value(self.nodeId_members_prefix + '.Call_Exception',
                            self.see.call.call_exception,
                            opcua.ua.VariantType.Int32)

        self.see.logger.logdebug('Writing Call_Exception_Description: ' + self.see.call.call_exception_description)
        self.set_node_value(self.nodeId_members_prefix + '.Call_Exception_Description',
                            self.see.call.call_exception_description,
                            opcua.ua.VariantType.String)

        self.see.logger.logdebug('Writing Condition_Configuration: ' + self.see.condition.configuration)
        self.set_node_value(self.nodeId_members_prefix + '.Condition_Configuration',
                            self.see.condition.configuration,
                            opcua.ua.VariantType.String)

        self.see.logger.logdebug('Writing Condition_Product: ' + self.see.condition.products_to_json_string())
        self.set_node_value(self.nodeId_members_prefix + '.Condition_Product',
                            self.see.condition.products_to_json_string(),
                            opcua.ua.VariantType.String)

        self.see.logger.logdebug('Writing Mode: ' + str(self.see.mode))
        self.set_node_value(self.nodeId_members_prefix + '.Mode',
                            self.see.mode,
                            opcua.ua.VariantType.Int32)

        self.see.logger.logdebug('Writing Next_Available:' + str(self.see.next_available))
        self.set_node_value(self.nodeId_members_prefix + '.Next_Available',
                            self.see.next_available,
                            opcua.ua.VariantType.UInt64)

        #self.see.logger.logdebug('Writing Productive!')
        #self.set_node_value(self.nodeId_members_prefix + '.Productive',
                            #self.productive,
                            #opcua.ua.VariantType.Boolean)

        self.see.logger.logdebug('Writing Remaining_Duration: ' + str(self.see.remaining_duration*1000000))
        self.set_node_value(self.nodeId_members_prefix + '.Remaining_Duration',
                            self.see.remaining_duration*1000000,
                            opcua.ua.VariantType.UInt32)

        self.see.logger.logdebug('Writing Skill: ' + self.see.skill_id)
        self.set_node_value(self.nodeId_members_prefix + '.Skill',
                            self.see.skill_id,
                            opcua.ua.VariantType.String)

        self.see.logger.logdebug('Updating of SEE data finished!')

        return True
