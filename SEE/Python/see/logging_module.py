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
# @file logging_module
# @author Denis Štogl
##

import logging


class LoggingModule(object):

    def __init__(self, name, level):
        FORMAT = '%(asctime)-15s %(clientip)s %(user)-8s %(message)s'
        self.logger = logging.getLogger(name)

        if level == 'DEBUG':
            self.logger.setLevel(logging.DEBUG)
        if level == 'INFO':
            self.logger.setLevel(logging.INFO)
        if level == 'WARN':
            self.logger.setLevel(logging.WARN)
        if level == 'ERROR':
            self.logger.setLevel(logging.ERROR)

    def logdebug(self, message):
        self.logger.debug(message)

    def loginfo(self, message):
        self.logger.info(message)

    def logwarn(self, message):
        self.logger.warn(message)

    def logerror(self, message):
        self.logger.error(message)

    def logcritical(self, message):
        self.logger.critical(message)
