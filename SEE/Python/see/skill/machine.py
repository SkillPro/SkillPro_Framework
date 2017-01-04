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
# @file machine
# @author Denis Štogl
##

import skill


class Recognize(skill.Skill):

    def __init__(self, sbrc):
        super(Recognize, self).__init__(sbrc)
        self.PARAMETERS |= set(['Object'])

    def check_parameters(self, data):
        ok, kwargs = super(Recognize, self).check_parameters(data)

        return ok, kwargs


class Move(skill.Skill):

    SYSTEM_IMPORTS = {'json': 'json'}

    def __init__(self, sbrc):
        super(Move, self).__init__(sbrc)
        self.PARAMETERS |= set(['Goal'])

    def check_parameters(self, data):
        ok, kwargs = super(Move, self).check_parameters(data)
        if ok:
            kwargs['Goal'] = self.sbrc.see._es_comm.read_object(kwargs['Goal'])

        return ok, kwargs


class MaterialTransfer(skill.Skill):

    SYSTEM_IMPORTS = {'json': 'json'}

    def __init__(self, sbrc):
        super(MaterialTransfer, self).__init__(sbrc)
        self.PARAMETERS |= set(['Products'])

    def check_parameters(self, data):
        ok, kwargs = super(MaterialTransfer, self).check_parameters(data)

        if ok:
            try:
                kwargs['Products'] = self.sys_mods['json'].loads(kwargs['Products'])
                if not isinstance(kwargs['Products'], dict):
                    raise ValueError
            except ValueError:
                self.sbrc.see.logger.logerror(
                    "The 'Products' parameter in Skill " + self.NAME +
                    " should be JSON dictionary!")
                ok = False

        return ok, kwargs


class Transport(Move, MaterialTransfer):

    def __init__(self, sbrc):
        super(Transport, self).__init__(sbrc)


class MoveSpeed(Move):

    def __init__(self, sbrc):
        super(MoveSpeed, self).__init__(sbrc)
        self.PARAMETERS |= set(['Speed'])

    def check_parameters(self, data):
        ok, kwargs = super(MoveSpeed, self).check_parameters(data)
        if ok:
            try:
                kwargs['Speed'] = float(kwargs['Speed'])
            except ValueError:
                self.sbrc.see.logger.logerror(
                    "The 'Speed' parameter in Skill " + self.NAME +
                    " should be one numeric (float)!")
                ok = False

        return ok, kwargs


class Move1DDistance(Move):

    def __init__(self, sbrc):
        super(Move1DDistance, self).__init__(sbrc)

    def check_parameters(self, data):
        ok, kwargs = super(Move1DDistance, self).check_parameters(data)
        if ok:
            try:
                kwargs['Goal'] = float(kwargs['Goal'])
            except ValueError:
                self.sbrc.see.logger.logerror(
                    "The 'Goal' parameter in Skill " + self.NAME +
                    " should be one numeric (float)!")
                ok = False

        return ok, kwargs


class Move1DDistanceSpeed(Move1DDistance, MoveSpeed):

    def __init__(self, sbrc):
        super(Move1DDistanceSpeed, self).__init__(sbrc)


class Transport1DDistanceSpeed(Transport, Move1DDistanceSpeed):

    def __init__(self, sbrc):
        super(Transport1DDistanceSpeed, self).__init__(sbrc)


class Move6D(Move):

    def __init__(self, sbrc):
        super(Move6D, self).__init__(sbrc)

    def check_parameters(self, data):
        ok, kwargs = super(Move6D, self).check_parameters(data)
        if ok:
            if not isinstance(kwargs['Goal'], list) or len(kwargs['Goal']) != 6:
                #self.sbrc.see.logger.logerror("The 'Goal' parameter in Skill "
                                              #+ self.NAME + " should be list of six numerics (floats)!")
                #ok = False
                #FIXME: remove comment
                pass

        return ok, kwargs


class Move6DSpeed(MoveSpeed, Move6D):

    def __init__(self, sbrc):
        super(Move6DSpeed, self).__init__(sbrc)

    def check_parameters(self, data):
        ok, kwargs = super(Move6DSpeed, self).check_parameters(data)
        if ok:
            pass

        return ok, kwargs


class Move3DXYYaw(Move):

    def __init__(self, sbrc):
        super(Move3DXYYaw, self).__init__(sbrc)

    def check_parameters(self, data):
        ok, kwargs = super(Move3DXYYaw, self).check_parameters(data)
        if ok:
            if not isinstance(kwargs['Goal'], list) or len(kwargs['Goal']) < 3:
                self.sbrc.see.logger.logerror("The 'Goal' parameter in Skill "
                                + self.NAME +
                                " should be list of six numerics (floats)!")
            if len(kwargs['Goal']) == 3:
                pass
            elif len(kwargs['Goal']) == 6:
                kwargs['Goal'] = [kwargs['Goal'][0], kwargs['Goal'][1], kwargs['Goal'][5]]

        return ok, kwargs


class Transport3DXYYaw(Transport, Move3DXYYaw):

    def __init__(self, sbrc):
        super(Transport3DXYYaw, self).__init__(sbrc)


class Transport6D(Transport, Move6D):

    def __init__(self, sbrc):
        super(Transport6D, self).__init__(sbrc)


class Transport6DSpeed(Transport, Move6DSpeed):

    def __init__(self, sbrc):
        super(Transport6DSpeed, self).__init__(sbrc)


class Load(MaterialTransfer):

    def __init__(self, sbrc):
        super(Load, self).__init__(sbrc)


class Unload(MaterialTransfer):

    def __init__(self, sbrc):
        super(Unload, self).__init__(sbrc)


class RecognizeMove6D(Recognize, Move6D):

    FROM_ES = False

    def __init__(self, sbrc):
        super(RecognizeMove6D, self).__init__(sbrc)


class RecognizeTransport6D(Recognize, Transport6D):

    def __init__(self, sbrc):
        super(RecognizeTransport6D, self).__init__(sbrc)


class RecognizeMove6D(Recognize, Move6D):

    FROM_ES = False

    def __init__(self, sbrc):
        super(RecognizeMove6D, self).__init__(sbrc)


class Grip(MaterialTransfer):

    def __init__(self, sbrc):
        super(Grip, self).__init__(sbrc)


class RecognizeGrip(Recognize, Grip):

    def __init__(self, sbrc):
        super(RecognizeGrip, self).__init__(sbrc)


class Release(MaterialTransfer):

    def __init__(self, sbrc):
        super(Release, self).__init__(sbrc)
