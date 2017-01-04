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
# @file call
# @author Denis Štogl
##

import time

class Call(object):

    ## Call Error code according to MES data model
    ERRORS = {
        'EverythingOK': 0,
        # Call Error 1xx
        'WrongFormat': 110,
        # Technical Error - 2xx
        'NonexistingSkill': 210,
        'PreconditionsNotMet': 220,
        'TimestampInThePast': 230,
        'SkillCannnotBePaused': 340,
        'SkillCannnotBePausedNoExec': 341,
        'SkillCannotBeResumed': 250,
        'SEENotInIdle': 260,
        # Internal error 3xx
        }

    METHODS = [
        'ExecSkill',
        'Clear',
        'Pause',
        'Resume'
        ]

    def __init__(self):
        self.call = ''
        self.call_exception = 0
        self.call_exception_description = ''

        self.command = None
        self.skill_id = None
        self.timestamp = None

    @property
    def skill_id(self):
        return self.__skill_id

    @skill_id.setter
    def skill_id(self, id):
        if isinstance(id, str):
            self.__skill_id = id
        else:
            self.__skill_id = id

    def __init__(self, call_string=''):
        self.call = call_string
        self.call_exception = 0
        self.call_exception_description = ''

        self.command = None
        self.skill_id = None
        self.timestamp = None

        if call_string != '':
            splitted = call_string.split(":")
            if splitted[0] in Call.METHODS:
                self.command = splitted[0]
                if splitted[0] == 'ExecSkill':
                    if len(splitted) >= 2:
                        self.skill_id = splitted[1]
                        if self.skill_id == '':
                            self.call_exception = Call.ERRORS['WrongFormat']
                        self.call_exception_description = 'ExecSkill formats are: "ExecSkill:<id>" and "ExecSkill:<id>:<timestamp>"'
                        if len(splitted) == 3:
                            self.timestamp = int(splitted[2])
                            if self.timestamp and self.timestamp < time.time()*1000000:
                                self.call_exception = Call.ERRORS['TimestampInThePast']
                        else:
                            self.timestamp = 0
                    else:
                        self.call_exception = Call.ERRORS['WrongFormat']
                        self.call_exception_description = 'ExecSkill formats are: "ExecSkill:<id>" and "ExecSkill:<id>:<timestamp>"'

            else:
                self.call_exception = Call.ERRORS['WrongFormat']
                self.call_exception_description = 'Command: "' + splitted[0] + '" not known.'
        else:
            self.call_exception = Call.ERRORS['WrongFormat']
            self.call_exception_description = 'Empty call'

    def set_EverythingOK(self):
        self.call_exception = Call.ERRORS['EverythingOK']
        self.call_exception_description = ''

    def set_error_NonexistingSkill(self):
        self.call_exception = Call.ERRORS['NonexistingSkill']
        self.call_exception_description = 'Skill with Id: ' + self.skill_id + ' does not exist.'

    def set_error_PreconditionsNotMet(self):
        self.call_exception = Call.ERRORS['PreconditionsNotMet']
        self.call_exception_description = 'For Skill with Id: ' + self.skill_id + ' preconditions are not met'

    def set_error_SkillCannnotBePaused(self):
        self.call_exception = Call.ERRORS['SkillCannnotBePaused']
        self.call_exception_description = 'Skill with Id: ' + self.skill_id + ' cannot be paused'

    def set_error_SkillCannnotBePaused(self):
        self.call_exception = Call.ERRORS['SkillCannnotBePausedNoExec']
        self.call_exception_description = 'Skill cannot be paused becasue no skill is executing'

    def set_error_SkillCannotBeResumed(self):
        self.call_exception = Call.ERRORS['SkillCannotBeResumed']
        self.call_exception_description = 'Skill with Id: ' + self.skill_id + ' cannot be resumed'

    def set_error_SEENotInIdle(self):
        self.call_exception = Call.ERRORS['SEENotInIdle']
        self.call_exception_description = 'Skill with Id: ' + self.skill_id + ' cannot be executed. SEE not in Idle mode.'
