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

## @package skillpro_execution_engine.see.skill
# @file human
# @author Denis Štogl
##

import skill

from machine import MaterialTransfer, Transport


class HumanBase(skill.Skill):

    def __init__(self, sbrc):
        super(HumanBase, self).__init__(sbrc)


class Instruction(HumanBase):

    def __init__(self, sbrc):
        super(Instruction, self).__init__(sbrc)
        self.PARAMETERS |= set(['instruction'])


class InstructionWithFigure(Instruction):

    def __init__(self, sbrc):
        super(InstructionWithFigure, self).__init__(sbrc)
        self.PARAMETERS |= set(['figure'])


class Logout(Instruction):

    EXEC_FUNC = 'exec_Instruction'

    def __init__(self, sbrc):
        super(Logout, self).__init__(sbrc)


class TransportHuman(HumanBase, Transport):

    def __init__(self, sbrc):
        super(TransportHuman, self).__init__(sbrc)
        self.PARAMETERS |= set(['Start'])

    def check_parameters(self, data):
        if not 'Start' in data.keys():
            data['Start'] = {}

        ok, kwargs = super(TransportHuman, self).check_parameters(data)
        #if ok:
            #kwargs['goal'] = self.sbrc.see._es_comm.read_object(kwargs['goal'])

        return ok, kwargs


class PackagingHuman(MaterialTransfer):

    def __init__(self, sbrc):
        super(PackagingHuman, self).__init__(sbrc)
        self.PARAMETERS |= set(['packaging_description'])


class OperateMilling(InstructionWithFigure):

    def __init__(self, sbrc):
        super(OperateMilling, self).__init__(sbrc)
        self.PARAMETERS |= set(['speed',
                      'depth'])
