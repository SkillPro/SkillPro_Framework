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
# @file factory_scenario
# @author Denis Štogl
##


import human
import skill
import machine


class Printing(skill.Skill):

    def __init__(self, sbrc):
        super(Printing, self).__init__(sbrc)

    def check_parameters(self, data):
        ok, kwargs = super(Printing, self).check_parameters(data)

        return ok, kwargs

class Pick(machine.MaterialTransfer):

    def __init__(self, sbrc):
        super(Pick, self).__init__(sbrc)


# Example of Skill inheritance
class PickRelease(machine.MaterialTransfer):

    def __init__(self, sbrc):
        super(PickRelease, self).__init__(sbrc)

class QualityChecking(human.HumanBase):

    NAME = "QualityChecking"
    KEY = NAME

    def __init__(self, sbrc):
        super(QualityChecking, self).__init__(sbrc)

    def check_parameters(self, data):
        print "QualityChecking data: ", data
        ok = True
        kwargs = {}
        return ok, kwargs

        #program = "<p class=\"ng-binding\"><b>" + data['controlplan'] + ".</b></p>"

        #getattr(self.sbrc, self.EXEC_FUNC)(program)
