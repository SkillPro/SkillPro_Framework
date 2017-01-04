#!/usr/bin/env python
# coding=utf-8

##############################################################################
#
# Copyright 2012-2016 SkillPro Consortium
#
# Author: Andreas Pfeil, email: pfeil@fzi.de
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
# @file remote
# @author Andreas Pfeil
##

import socket


class Remote:
    def __init__(self, data, protocol=socket.SOCK_STREAM):
        self.data = data
        self.protocol = protocol

    def connect(self):
        pass

    def disconnect(self):
        for socket in self.sockets:
            socket.shutdown(SHUT_RD)

    def sendMsg(self, msg):
        answers = []
        for conn in self.data:
            sock = socket.socket(socket.AF_INET, self.protocol)
            sock.connect((conn['address'], conn['port']))
            sock.sendall(msg)
            answer = sock.recvfrom(10)
            answers.append((conn['name'], answer))
            sock.shutdown(socket.SHUT_RDWR)
            sock.close()
        return answers
