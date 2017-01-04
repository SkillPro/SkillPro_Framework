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
# @file threading_module
# @author Denis Štogl
##

import threading
try:
    import VR
except:
    pass


class ThreadingModule(object):

    def startThread(self, callback, args=()):
        thread = threading.Thread(target=callback, args=args)
        thread.start()
        return thread

    def joinThread(self, thread):
        thread.joint(0)


class ThreadingModulePVR(ThreadingModule):

    def startThread(self, callback, parameters=()):
        return VR.startThread(callback, parameters)

    def joinThread(self, identity_number):
        VR.joinThread(identity_number)
