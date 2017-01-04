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
# @file es_communication
# @author Denis Štogl
##

import sys
from enum import Enum

from data_model.environment import EnvironemntData, Object_EnvironmentData
from data_model.environment import SEE_EnvironmentData, CartesianPosition

from mes_communication import SkillPro_OpcUa


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


class ESCommunication_OpcUa(ESCommunication, SkillPro_OpcUa):

    def __init__(self, see, ESComm):
        super(ESCommunication_OpcUa, self).__init__(see, ESComm)

    def disconnect(self):
        self.client.disconnect()


class ESCommunication_SimulatedOpcUa(ESCommunication_OpcUa):

    def __init__(self, see, ESComm):
        super(ESCommunication_SimulatedOpcUa, self).__init__(see, ESComm)

        self.data = {}

        self.see.logger.logdebug("Initialisation of ESCommunication_SimulatedOpcUa successuful!")

    def connect(self):
        return True

    def disconnect(self):
        return True

    def write(self, data_type, coordinates, frame=''):
        self.see.logger.logdebug("Writing to simulated ES: \nType: " + data_type + "\n Coordinates: " + str(coordinates) + "\n Frame: " + frame)

        return True

    def add_object(self, name, frame, coordinates):
        #self.data[name] = Object_EnvironmentData(
            #name, name, '',
            #CartesianPosition(frame, coordinates)).to_json()
        self.data[name] = [frame, coordinates]

    def update_object(self, name, frame, coordinates):
        self.add_object(name, frame, coordinates)

    def read_object(self, name):
        return self.data[name][1]
