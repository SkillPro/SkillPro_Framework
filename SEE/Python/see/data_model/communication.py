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
# @file communication
# @author Denis Štogl
##


## Class CommunicaitonDescription stores data about communication interface to other components.
class CommunicationDescription(object):

    def __init__(self, type='', uri='', point=None):
        self.type = type
        self.uri = uri
        self.point = point

    def set_point(self, point):
        self.point = point
        self.uri = point.uri


## Class CommunicatonPoint stores data about communication point in communication interface to other components.
class CommunicationPoint(object):

    def __init__(self, uri='', nodeId=''):
        self.uri = uri
        self.nodeId = nodeId
