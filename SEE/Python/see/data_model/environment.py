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

## @package skillpro_execution_engine.see.data_model
# @file environment
# @author Denis Štogl
##


## SkillExecutionEngine class.
# This class implements Skill-Execution-Engine.
##

import json


class EnvironemntData(object):

    def __init__(self, type, browse_name=None, nodeId=None):
        self.type = type
        self.browse_name = browse_name
        self.nodeId = nodeId
        self.fields = None

    def set_field_nodeIds(self, field_list):
        self.fields = {}
        for field in field_list:
            self.fields[field.qualifiedName] = field.nodeId


class Position(object):

    def __init__(self, type, frame):
        self.type = type
        self.frame = frame


class CartesianPosition(Position):

    def __init__(self, frame='', coordinates=[0 for i in range(0, 6)]):
        super(CartesianPosition, self).__init__('6DPose', frame)
        self.coordinates = coordinates

    @property
    def coordinates(self):
        return {'x': self.x, 'y': self.y, 'z': self.z,
                'rx': self.rx, 'ry': self.ry, 'rz': self.rz}

    @coordinates.setter
    def coordinates(self, coordinates):
        if isinstance(coordinates, list):
            self.x = coordinates[0]
            self.y = coordinates[1]
            self.z = coordinates[2]
            self.rx = coordinates[3]
            self.ry = coordinates[4]
            self.rz = coordinates[5]
        elif isinstance(coordinates, dict):
            self.x = coordinates['x']
            self.y = coordinates['y']
            self.z = coordinates['z']
            self.rx = coordinates['rx']
            self.ry = coordinates['ry']
            self.rz = coordinates['rz']

    def to_list(self):
        return [self.x, self.y, self.z, self.rx, self.ry, self.rz]


class JointStates(Position):

    def __init__(self, states=[0 for i in range(0, 6)]):
        super(JointStates, self).__init__('joint_states', '')
        self.states = states

    @property
    def states(self):
        return {'a1': self.a1, 'a2': self.a2, 'a3': self.a3,
                'a4': self.a4, 'a5': self.a5, 'a6': self.a6}

    @states.setter
    def states(self, states):
        if isinstance(states, list):
            self.a1 = states[0]
            self.a2 = states[1]
            self.a3 = states[2]
            self.a4 = states[3]
            self.a5 = states[4]
            self.a6 = states[5]
        elif isinstance(states, dict):
            self.a1 = states['a1']
            self.a2 = states['a2']
            self.a3 = states['a3']
            self.a4 = states['a4']
            self.a5 = states['a5']
            self.a6 = states['a6']

    def to_list(self):
        return [self.a1, self.a2, self.a3, self.a4, self.a5, self.a6]


class Object_EnvironmentData(object):

    def __init__(self, name='', id='', type='', base_pose=CartesianPosition()):
        self.name = name
        self.id = id
        self.type = type

        self.base_pose = base_pose

    def to_json(self):
        out = {}
        for key in self.__dict__.keys():
            if isinstance(self.__dict__[key], Position):
                out[key] = self.__dict__[key].__dict__
            else:
                out[key] = self.__dict__[key]
        return json.dumps(out)

    def from_json(self, json_string):
        dict = json.loads(json_string)
        for key in dict.keys():
            if isinstance(self.__dict__[key], Position):
                for key2 in dict[key].keys():
                    self.__dict__[key].__dict__[key2] = dict[key][key2]
            else:
                self.__dict__[key] = dict[key]

        return self


class SEE_EnvironmentData(Object_EnvironmentData):

    def __init__(self, name='', id='', type='', base_pose=CartesianPosition(),
                 internal_state=JointStates(), tcp_pose=CartesianPosition()):
        super(SEE_EnvironmentData, self).__init__(name, id, type, base_pose)

        self.internal_state = internal_state
        self.tcp_pose = tcp_pose
