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
# @file skill
# @author Denis Štogl
##

import time
import importlib


class Skill(object):

    def __init__(self, sbrc):
        self.PARAMETERS = set([])
        self.sbrc = sbrc

        self.NAME = self.__class__.__name__
        self.KEY = self.NAME

        if getattr(self, "EXEC_FUNC", None):
            self.sbrc.see.logger.logwarn(
                sbrc.__class__.__name__ + " is using hard coded method '"
                + self.EXEC_FUNC +
                "' for execution of Skill.")
        else:
            self.EXEC_FUNC = 'exec_' + self.NAME

        if not callable(getattr(sbrc, self.EXEC_FUNC, None)):
            self.sbrc.see.logger.logwarn(
                sbrc.__class__.__name__ + " does not implement method '" +
                self.EXEC_FUNC +
                "' needed for execution of skill. Using 'exec_default' function")
            self.EXEC_FUNC = 'exec_default'

        if getattr(self, "EXEC_FUNC_SIM", None):
            self.sbrc.see.logger.logwarn(
                sbrc.__class__.__name__ + " is using hard coded method '"
                + self.EXEC_FUNC_SIM +
                "' for execution of Skill.")
        else:
            self.EXEC_FUNC_SIM = 'exec_' + self.NAME + '_sim'

        if not callable(getattr(sbrc, self.EXEC_FUNC_SIM, None)):
            self.sbrc.see.logger.logwarn(
                sbrc.__class__.__name__ + " does not implement method '" +
                self.EXEC_FUNC_SIM +
                "' needed for execution of skill. Using 'exec_default_sim' function")
            self.EXEC_FUNC_SIM = 'exec_default_sim'

        if getattr(self, "SYSTEM_IMPORTS", None):
            self.sys_mods = {}
            for module in self.SYSTEM_IMPORTS:
                self.sys_mods[module] = importlib.import_module(
                    self.SYSTEM_IMPORTS[module])

    #return True, False
    def check_parameters(self, data):
        kwargs = {}
        ok = True

        result = filter(lambda x: x not in data.keys(), self.PARAMETERS)
        if result:
            self.sbrc.see.logger.logerror(
                "Action can not be executed! Missing parameters: " +
                str(result) + " from Skill: '" + self.NAME + "' definition")
            ok = False
        else:
            result = filter(lambda x: x not in self.PARAMETERS, data.keys())
            if result:
                self.sbrc.see.logger.logwarn(
                    "Action will be executed but some of provided parameters: "
                    + str(result) + " not in Skill: '" + self.NAME
                    + "' definition. They will be ignored")
                for param in self.PARAMETERS:
                    kwargs[param] = data[param]
            else:
                self.sbrc.see.logger.logdebug(
                    "Provided all paramters from Skill: '" + self.NAME
                    + "' definition")
                # this is probably not neded after since is cool to store data into original object (Skill will be checked only once, not every time as here)
                kwargs = dict(data)

        return ok, kwargs

    def execute(self, **kwargs):
        kwargs_out = {}
        for key in kwargs.keys():
            kwargs_out[key.lower()] = kwargs[key]

        if self.sbrc.simulated:
            getattr(self.sbrc, self.EXEC_FUNC_SIM)(**kwargs_out)
        else:
            getattr(self.sbrc, self.EXEC_FUNC)(**kwargs_out)


class Simple(Skill):

    def __init__(self, sbrc):
        super(Simple, self).__init__(sbrc)
        self.PARAMETERS |= set(['Text'])

    def check_parameters(self, data):
        ok, kwargs = super(Simple, self).check_parameters(data)

        if ok:
            kwargs_out = {}
            for key in kwargs.keys():
                kwargs_out[key.lower()] = kwargs[key]

        return ok, kwargs_out

    def execute(self, text):
        getattr(self.sbrc, self.EXEC_FUNC)(text)


class Waiting(Skill):

    SYSTEM_IMPORTS = {'time': 'time'}

    NO_DEFAULT_EXEC_FUNC = True

    def __init__(self, sbrc):
        super(Waiting, self).__init__(sbrc)
        self.PARAMETERS |= set(['Duration'])

    def check_parameters(self, data):
        ok, kwargs = super(Waiting, self).check_parameters(data)

        if ok:
            try:
                kwargs['Duration'] = int(kwargs['Duration'])
            except ValueError:
                self.sbrc.see.logger.logerror(
                    "The 'duration' parameter in Skill " + self.NAME +
                    " should be numeric (int)!")
                ok = False

        return ok, kwargs
