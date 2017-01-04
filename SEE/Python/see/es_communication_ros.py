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
# @file es_communication_ros
# @author Denis Štogl
##

import sys
import rospy
from enum import Enum

import std_msgs.msg as std_msgs
import see_srvs.srv as see_srvs

import opcua_msgs.msg as opcua_msgs
import opcua_srvs.srv as opcua_srvs

from data_model.environment import EnvironemntData, Object_EnvironmentData
from data_model.environment import SEE_EnvironmentData, CartesianPosition

#class SEE_ON_ES(object):


## ESCommunication class.
# This class is base class for AMS communication.
##
class ESCommunication(object):

    def __init__(self, see, ESComm):

        # Communication description
        self.comm = ESComm
        # Connection status
        self._connected = False
        self.see = see

        self.see_es = [self.see.name, self.see.Id, '6DPose']
        self.frame = 'map'
        self.base_pos = [0 for i in range(0, 6)]
        self.tcp_pos = [0 for i in range(0, 6)]
        self.internal_state = [0 for i in range(0, 6)]

    @property
    def base_pos(self):
        return self.__base_pos

    @base_pos.setter
    def base_pos(self, pos):
        self.__base_pos = self.input_convert(pos)

    @property
    def tcp_pos(self):
        return self.__tcp_pos

    @tcp_pos.setter
    def tcp_pos(self, pos):
        self.__tcp_pos = self.input_convert(pos)

    @property
    def internal_state(self):
        return self.__internal_state

    @internal_state.setter
    def internal_state(self, pos):
        if isinstance(pos, dict):
            self.__internal_state = [pos['a1'], pos['a2'], pos['a3'], pos['a4'], pos['a5'], pos['a6']]
        elif isinstance(pos, list):
            self.__internal_state = pos

    def input_convert(self, pos, ):
        if isinstance(pos, dict):
            return [pos['x'], pos['y'], pos['z'], pos['rx'], pos['ry'], pos['rz']]
        elif isinstance(pos, list):
            return pos

    def serialise_to_json(self):
        lista = list(self.see_es)
        lista.append(self.frame)
        lista.append(self.base_pos)
        lista.append(self.tcp_pos)
        lista.append(self.internal_state)
        return json.dumps(lista)

    def connect(self):
        raise NotImplementedError("Should have implemented this")

    def disconnect(self):
        raise NotImplementedError("Should have implemented this")

    def write(self, data_type, coordinates, frame=''):
        raise NotImplementedError("Should have implemented this")

    def read(self):
        raise NotImplementedError("Should have implemented this")


class ESCommunication_OpcUa_ROS(ESCommunication):

    def __init__(self, see, ESComm):
        super(ESCommunication_OpcUa_ROS, self).__init__(see, ESComm)

        # OpcUa variables nodeId is in format: "ns=2;i=3001"
        self._nodeId = ESComm.point.nodeId
        self.environment_data = {}
        self.environment_data['Base_position'] = EnvironemntData('Base_position', 'base_pose')
        self.environment_data['TCP_position'] = EnvironemntData('TCP_position', 'tcp_pose')
        self.environment_data['joint_states'] = EnvironemntData('joint_states', 'internal_state')

        # Wait for OpcUa-client services
        rospy.wait_for_service('es_comm_opcua_client/connect')

        self.connect_srv = rospy.ServiceProxy('es_comm_opcua_client/connect', opcua_srvs.Connect)
        self.disconnect_srv = rospy.ServiceProxy('es_comm_opcua_client/disconnect', opcua_srvs.Disconnect)
        self.listNode_srv = rospy.ServiceProxy('es_comm_opcua_client/list_node', opcua_srvs.ListNode)
        self.read_srv = rospy.ServiceProxy('es_comm_opcua_client/read', opcua_srvs.Read)
        self.write_srv = rospy.ServiceProxy('es_comm_opcua_client/write', opcua_srvs.Write)
        self.callMethod_srv = rospy.ServiceProxy('es_comm_opcua_client/call_method', opcua_srvs.CallMethod)

        self.see.logger.logdebug("Initialisation of ESCommunication_OpcUa_ROS successuful!")

    def delete(self):
        self.connect_srv.close()
        self.disconnect_srv.close()
        self.listNode_srv.close()
        self.read_srv.close()
        self.write_srv.close()
        self.callMethod_srv.close()

    def connect(self):
        if not self._connected:
            res = self.connect_srv(opcua_srvs.ConnectRequest(self.comm.uri))

            if res.success:
                self._connected = True
                #TODO if thre is NodeId check if OK
                if not self.comm.point.nodeId:
                    found, self.comm.point.nodeId = self._find_SEE_on_server('SEEFolder')
                    if not found:
                        self.see.logger.logwarn(
                            "SEE '" + self.see.name +
                            "' not found on ES server, creating new object")
                        res2 = self.callMethod_srv(opcua_srvs.CallMethodRequest(
                            opcua_msgs.Address('ns=0;i=85', ''),
                            opcua_msgs.Address('ns=2;i=2033', ''),
                            [opcua_msgs.TypeValue(
                                type='string',
                                string_d=self.see.env_data.to_json())]))
                    else:
                        res2 = self.callMethod_srv(opcua_srvs.CallMethodRequest(
                            opcua_msgs.Address('ns=0;i=85', ''),
                            opcua_msgs.Address('ns=2;i=2036', ''),
                            [opcua_msgs.TypeValue(
                                type='string',
                                string_d=self.see.env_data.to_json())]))

            if not (res.success and res2.success):
                rospy.logerr('OpcUa Connect service returned error: ' +
                             res.error_message + ' ' + res2.error_message)
                self._connected = False

        return self._connected

    def _find_SEE_on_server(self, base_path):
        found = False
        res = self.listNode_srv(opcua_srvs.ListNodeRequest())
        for obj in res.children:
            if base_path in obj.qualifiedName:
                res = self.listNode_srv(opcua_srvs.ListNodeRequest(
                    opcua_msgs.Address(obj.nodeId, '')))
                for child in res.children:
                    if child.qualifiedName in self.see.name:
                        found = True
                        res = self.listNode_srv(opcua_srvs.ListNodeRequest(
                            opcua_msgs.Address(child.nodeId, '')))
                        for prop in res.children:
                            if prop.qualifiedName in 'id':
                                res2 = self.read_srv(
                                    opcua_srvs.ReadRequest(opcua_msgs.Address(
                                        prop.nodeId, '')))
                                if res2.data.string_d != self.see.Id:
                                    self.see.logger.logerror("Found SEE with corresponding Name ('" + self.see.name + "') but wrong Id! AML-File id: '" + self.see.Id + "', ES server id: '" + res2.data.string_d + "'.")
                                else:
                                    self.see.logger.loginfo("Found SEE '" + self.see.name + "' (Id: '" + self.see.Id + "') on ES server!")
                                    break

                        #TODO maybe search further - for now do nothing

        return found, child.nodeId

    def find_environment_data(self):
        res2 = self.listNode_srv(opcua_srvs.ListNodeRequest(
            opcua_msgs.Address(self.comm.point.nodeId, '')))
        if res2.success:
            for child in res2.children:
                if child.qualifiedName == 'environment_data':
                    res2 = self.listNode_srv(opcua_srvs.ListNodeRequest(opcua_msgs.Address(child.nodeId, '')))
                    if res2.success:
                        for child in res2.children:
                            for key in self.environment_data.keys():
                                if child.qualifiedName in self.environment_data[key].browse_name:
                                    self.environment_data[key].nodeId = child.nodeId
                                    res3 = self.listNode_srv(
                                        opcua_srvs.ListNodeRequest(
                                            opcua_msgs.Address(
                                                child.nodeId, '')))
                                    if res3.success:
                                        self.environment_data[key].set_field_nodeIds(
                                            res3.children)

                # TODO: Error handling

    def write(self, env_see):
        if not self._connected:
            return False

        self.callMethod_srv(opcua_srvs.CallMethodRequest(
            opcua_msgs.Address('ns=0;i=85', ''),
            opcua_msgs.Address('ns=2;i=2036', ''),
            [opcua_msgs.TypeValue(
                type='string',
                string_d=env_see.to_json())]))
        return True

    def disconnect(self):
        res = self.disconnect_srv(opcua_srvs.DisconnectRequest())

        if res.success:
            self._connected = False
        else:
            rospy.logerr('OpcUa Disconnect service return error: ' + res.error_message)

        return res.succes

    def add_object(self, name, frame, coordinates):
        if not self._connected:
            return False

        res = self.callMethod_srv(opcua_srvs.CallMethodRequest(
            opcua_msgs.Address('ns=0;i=85', ''),
            opcua_msgs.Address('ns=2;i=2042', ''),
            [opcua_msgs.TypeValue(
                type='string',
                string_d=Object_EnvironmentData(
                    name, name, '',
                    CartesianPosition(frame, coordinates)).to_json())]))
        return res.success

    def update_object(self, name, frame, coordinates):
        if not self._connected:
            return False

        res = self.callMethod_srv(opcua_srvs.CallMethodRequest(
            opcua_msgs.Address('ns=0;i=85', ''),
            opcua_msgs.Address('ns=2;i=2045', ''),
            [opcua_msgs.TypeValue(
                type='string',
                string_d=Object_EnvironmentData(
                    name, name, '',
                    CartesianPosition(frame, coordinates)).to_json())]))
        return res.success

    def read_object(self, object_name):
        if not self._connected:
            return False

        ret = self.callMethod_srv(opcua_srvs.CallMethodRequest(
            opcua_msgs.Address('ns=0;i=85', ''),
            opcua_msgs.Address('ns=2;i=2048', ''),
            [opcua_msgs.TypeValue(
                type='string', string_d=object_name)]))

        if ret.data[0].string_d == 'FAILED':
            self.see.logger.logerror("Object with name: '" +
                                     object_name + "' not found in ES server!")
            return None
        else:
            return SEE_EnvironmentData().from_json(ret.data[0].string_d).base_pose

    def disconnect(self):
        res = self.disconnect_srv(opcua_srvs.DisconnectRequest())

        if res.success:
            self._connected = False
        else:
            rospy.logerr('OpcUa Disconnect service return error: ' + res.error_message)

        return res.success


class ESCommunication_SimulatedOpcUa_ROS(ESCommunication):

    def __init__(self, see, ESComm):
        super(ESCommunication_SimulatedOpcUa_ROS, self).__init__(see, ESComm)

        # OpcUa variables nodeId is in format: "ns=2;i=3001"
        self._nodeId = ESComm.point.nodeId
        self.environment_data = {}
        self.environment_data['TCP_position'] = EnvironemntData('TCP_position', 'TCP_position')
        self.environment_data['joint_states'] = EnvironemntData('joint_states', 'joint_states')

        self.see.logger.logdebug("Initialisation of ESCommunicationOpcUa_ROS successuful!")

    def connect(self):
        return True

    def disconnect(self, req):
        return True

    def write(self, data_type, coordinates, frame=''):
        print("Writing to simulated ES: \nType: " + data_type + "\n Coordinates: " + str(coordinates) + "\n Frame: " + frame)
        return True
