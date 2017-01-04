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
# @file lab_scenario
# @author Denis Štogl
##

import skill


class Soldering(skill.Skill):

    SYSTEM_IMPORTS = {'json': 'json'}

    def __init__(self, sbrc):
        super(Soldering, self).__init__(sbrc)
        self.PARAMETERS |= set(['ZoneTemperatures', 'ConveyorSpeed'])

    def check_parameters(self, data):
        ok, kwargs = super(Soldering, self).check_parameters(data)

        if ok:
            try:
                kwargs['ZoneTemperatures'] = self.sys_mods['json'].loads(kwargs['ZoneTemperatures'])
                if not isinstance(kwargs['ZoneTemperatures'], dict):
                    raise ValueError
                kwargs['ConveyorSpeed'] = float(kwargs['ConveyorSpeed'])
            except ValueError:
                self.sbrc.see.logger.logerror(
                    "In the Skill " + self.NAME +
                    "parameter 'ZoneTemperatures' should be JSON dictionary and ConveyorSpeed numeric (float).")
                ok = False

        return ok, kwargs
