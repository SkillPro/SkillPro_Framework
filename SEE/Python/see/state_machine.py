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
# @file state_machine
# @author Denis Štogl
##

from fysom import Fysom

## SEE_SSM_interface class.
# This class implements base interface to state machine.
##
class StateMachine(object):

    # Resource modes according to MES data model
    CODES = {
        # UNAVAILABLE = 1xx,
        'PreOperational': 110, # Shut off, unknown state
        'LongTermBreakdown': 120, # Don't consider for planning.
        'InMaintenance': 130, # We have a timestamp and condition for the resource to be operational again
        # RUNNING = 2xx,
        'ExecutingSkill': 210,
        'ExecutingSkillPaused': 211,
        'ExecutingSkillResumable': 212,
        'Idle': 220,
        'IdleQueuedSkill': 221,
        # ERROR = 3xx
        'Error': 300,
        'UnknownConditionError': 310,        # 23x => we don't know the state of the product
        'UnknownProductError': 311,          # or the state of the configuration .. human assistance needed
        'UnknownConfigurationError': 312,
        'CommunicationError': 320,
    }

    def __init__(self, see):
        self.see = see

    def init(self, see_state_change):
        self.see_state_change = see_state_change

    def state_change(self, new_state):
        self.see.ssm_callback(new_state)

    def update_state_event(self, event):
        raise NotImplementedError("Should have implemented this")


#TODO: integrate it so that callbacks are called for executing stuff
class StateMachine_Fysom(StateMachine):

    INITIAL = 'PreOperational'
    EVENTS = [
        #PreOperational
        {'name': 'EXT_CONFIGURED',          'src': 'PreOperational',    'dst': 'Idle'},
        {'name': 'INT_ERROR',               'src': 'PreOperational',    'dst': 'Error'},
        #Idle
        {'name': 'EXT_SHUTDOWN',            'src': 'Idle',              'dst': 'PreOperational'},
        {'name': 'INT_ERROR',               'src': 'Idle',              'dst': 'Error'},
        {'name': 'MES_ExecSkillTimestamp',  'src': 'Idle',              'dst': 'IdleQueuedSkill'},
        {'name': 'MES_ExecSkill',           'src': 'Idle',              'dst': 'ExecutingSkill'},
        #IdleQueuedSkill
        {'name': 'MES_Clear',               'src': 'IdleQueuedSkill',   'dst': 'Idle'},
        {'name': 'INT_EXEC_SKILL',          'src': 'IdleQueuedSkill',   'dst': 'ExecutingSkill'},
        {'name': 'INT_ERROR',               'src': 'IdleQueuedSkill',   'dst': 'Error'},
        #ExecutingSkill
        {'name': 'INT_SKILL_EXECUTED',      'src': 'ExecutingSkill',    'dst': 'Idle'},
        {'name': 'MES_Pause',               'src': 'ExecutingSkill',    'dst': 'ExecutingSkillPausing'},
        {'name': 'INT_ERROR',               'src': 'ExecutingSkill',    'dst': 'Error'},
        #ExecutingSkillPausing
        {'name': 'INT_NO_LONGER_PAUSABLE',  'src': 'ExecutingSkillPausing',     'dst': 'ExecutingSkill'},
        {'name': 'INT_CAN_RESUME',          'src': 'ExecutingSkillPausing',     'dst': 'ExecutingSkillResumable'},
        {'name': 'INT_ERROR',               'src': 'ExecutingSkillPausing',     'dst': 'Error'},
        #ExecutingSkillResumable
        {'name': 'MES_Resume',              'src': 'ExecutingSkillResumable',   'dst': 'ExecutingSkill'},
        {'name': 'INT_ERROR',               'src': 'ExecutingSkillResumable',   'dst': 'Error'},
        {'name': 'MES_Clear',               'src': 'IdleQueuedSkill',           'dst': 'Idle'},
        #Error
        {'name': 'EXT_RECOVER',             'src': 'Error',             'dst': 'PreOperational'},
        ]

    def __init__(self, see):
        super(StateMachine_Fysom, self).__init__(see)
        self.fsm = Fysom(initial={'state': StateMachine_Fysom.INITIAL,
                                  'event': 'init', 'defer': True},
                         events=StateMachine_Fysom.EVENTS,
                         callbacks={
                             'onchangestate': self.onchangestate
                             }
                         )
        self.fsm.init()

    def update_state_event(self, event):
        self.fsm.trigger(event)

    def onchangestate(self, event):
        self.state_change(StateMachine.CODES[self.fsm.current])