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
# @file see_aml
# @author Denis Štogl
##

from executable_skill import ExecutableSkill


## SEE_AMLDescription class is used as placeholder for the data from AML file.
# This data are used for initalisation of SEE in the other componenents.
##
class SEE_AMLDescription(object):

    def __init__(self, id, aml_file_path):
        self._id = id
        self._aml_file_path = aml_file_path

        self._aml_descr = ''
        self._name = ''
        self._condition = None
        self._ams_comm = None
        self._mes_comm = None
        self._es_comm = None
        self.see_type = None
        self._executable_skills = {}

    def get_executable_skill(self, skill_id):
        print self._executable_skills.keys()
        if skill_id in self._executable_skills.keys():
            return True, self._executable_skills[skill_id]
        else:
            return False, ExecutableSkill()

