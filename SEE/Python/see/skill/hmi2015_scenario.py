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
# @file hmi2015_scenario
# @author Denis Štogl
##

import skill
import human


class BuildLegoFlag(human.HumanBase):

    NAME = "BuildLegoFlag"
    KEY = NAME

    def __init__(self, sbrc):
        super(BuildLegoFlag, self).__init__(sbrc)

        self.finished = True
        self.finished_successfull = False

    def check_parameters(self, data):
        program = "<p class=\"ng-binding\"><b>" + data['instruction'] + "</b>:</p>"
        colors = data['colors'].split('-')
        #TODO: Check this
        #program += '{\"orientation\" : \"horizontal\", \"colors\" : [\"' + colors[0] + '\",\"' + colors[1] + '\",\"' + colors[2] + '\"]}'

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class PickUpLegos(human.HumanBase):

    NAME = "PickUpLegos"
    KEY = NAME

    def __init__(self, sbrc):
        super(PickUpLegos, self).__init__(sbrc)

    def check_parameters(self, data):
        program = "<p class=\"ng-binding\"><b>" + data['instruction'] + "</b>:</p>"
        colors = data['colors'].split(':')
        #program += '{\"orientation\" : \"vertical\", \"colors\" : [\"' + colors[0] + '\",\"' + colors[1] + '\",\"' + colors[2] + '\"]}'

        getattr(self.sbrc, self.EXEC_FUNC)(program)



class RecognizeColorGoToPosition(skill.Skill):

    #TODO: Remove name/key by all skills
    NAME = "RecognizeColorGoToPosition"
    KEY = NAME

    def __init__(self, sbrc):
        super(RecognizeColorGoToPosition, self).__init__(sbrc)
        self.color_list = ['blue', 'darkblue', 'brown', 'yellow', 'green', 'red', 'white']

    def check_parameters(self, data):


        if (data['color'] in self.color_list):
            program = data['color']
        else:
            self.sbrc.see.logger.logerror("Color of object to recognize does not exist!!")
            self.sbrc.finished(False, False)
            return

        getattr(self.sbrc, self.EXEC_FUNC)(program)

