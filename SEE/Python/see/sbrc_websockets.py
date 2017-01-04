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
# @file sbrc_websockets
# @author Andreas Pfeil
##

import importlib
import numpy
import time
import types
from remoteModule import Remote

from sbrc import SkillBasedResourceController

# common vectors
up = [0, 1, 0]
down = [0, -1, 0]
MAP = {
    "PositionBoxOutput": ('map', [[4, 0.4, -1.7], [0.5, 0, -0.2], up]),
    "PositionQualityCheck": ('map', [[3.45, 0.6, -1.7], [0.3, 0, -1], [-1, 0, 0]]),
    "PositionMobileRobot": ('map', [[3.6, 0.4, -0.84], [0, 0, 1], up]),
    "PositionLineOutput": ('map', [[2.84, 0.68, -1.4], [-1, 0, 0], up]),
    "PositionHumanLineOutput": ('map', [[2.2, 0.0, -1.5], [1, 0, 0], up]),
    # --- above: Schunk, Cell2 ---#
    "PositionHumanKR5Output": ('map', [[0.5, 0, 2.55], [1, 0, 0], up]),
    "PositionKR5Output": ('map', [[1.5, 0.65, 2.55], [-1, 0, 0], up]),
    "PositionPCBPrinter": ('map', [[2, 0.7, 3.2], [0, 0, 1], up]),
    "PositionMobilePlatform": ('map', [[2.1, 0.5, 1.97], [0, 0, -1], up]),
    "PositionSolderingMachineInput": ('map', [[2.5, 0.8, 2.7], [1, 0, 0], up]),
    "PositionSolderingMachineOutput": ('map', [[2.5, 0.8, 2.7], [1, 0, 0], down]),
    # "PositionLineOutput"   : [[],[],up],
    # --- above: Kuka KR5, Cell1 ---#
    # "PositionInput"   : ('map', [[2.5,0.8,2.7],[1,0,0],up]),
    # "PositionOutput"   : ('map', [[2.5,0.8,2.7],[1,0,0],down]),
    # --- above: Soldering Machine ---#
    "PositionKR5": ('map', [[2.1, 0, 1.5], [0, 0, 1], up]),
    "PositionSchunkArm": ('map', [[3.6, 0, -0.7], [0, 0, -1], up]),
    # --- above: Mobile Platform ---#
    # "Neutral" : handled by robots #
    ### BOTTLE SCENE ###
    "PositionFoerderbandGravur": ('map', [[-4.8, 0.86, 0.74], [-1, 0, 0], up]),
    "PositionFoerderband": ('map', [[-4.65, 0.618, 0.74], [-1, 0, 0], up]),
    "PositionDeckelTisch": ('map', [[-4.18, 0.475, 0.35], [0, -1, 0], [0, 0, -1]]),
    "PositionWarenausgang": ('map', [[-4.3, 0.65, 0.85], [0, 0, 1], up]),
}


class Basic_SBRC(SkillBasedResourceController):
    """
	Base class for SBRCs. Implements common functions.
	"""

    IMPORTANT_COORDINATES = MAP

    def __init__(self, see, simulated):
        super(Basic_SBRC, self).__init__(see, simulated)
        self.remote = Remote()

    def initalise(self, auto_init):  # TODO add auto_init parameter and change parameter in human and platform
        start_as_idle = super(Basic_SBRC, self).initalise(auto_init)
        self.see.logger.loginfo("Basic_SBRC initalised!")
        return True, start_as_idle

    def checkPose(self, name):
        """Translates a position into a usable format."""
        if type(name) is types.StringType:
            val = self.robot.translatePosition(name)
            return val
        else:
            self.see.logger.logdebug("Checkpose Type was:" + str(type(name)) + str(name))
            return name

    def exec_Load(self, products, finish=True):
        self.remote.sendMsg("JSON mit RoboName, Auftragsnummer, ")
        self.see.logger.loginfo("exec_Load: " + str(products))
        actions = [
            {'function': self.robot.load, 'params': [products]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Unload(self, products, finish=True):
        self.see.logger.loginfo("exec_Unload: " + str(products))
        actions = [
            {'function': self.robot.unload, 'params': [products]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)


class QualityCheck_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['factory_scenario', 'QualityChecking'],
        ['skill', 'Waiting'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(QualityCheck_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(QualityCheck_SBRC, self).initalise(True)

        self.see.logger.loginfo("QualityCheck_SBRC initalised!")
        return True, start_as_idle

    def exec_QualityChecking(self):  # TODO animate this skill
        actions = [
            {'function': self.robot.qualityCheckON, 'params': []},  # TODO implement: Make 3D model red or so.
            {'function': self.robot.wait, 'params': [2]},  # seconds
            {'function': self.robot.qualityCheckOFF, 'params': []},  # TODO implement: change back to normal state
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)


class Table_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['machine', 'Load'],
        ['machine', 'Unload'],
        # ['skill', 'Waiting'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Table_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Table_SBRC, self).initalise(True)
        self.see.logger.loginfo("Table_SBRC initalised!")
        return True, start_as_idle


class Human_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        # ['human', 'HumanBase'],
        # ['human', 'Instruction'],
        # ['human', 'InstructionWithFigure'],
        # ['human', 'Logout'],
        ['human', 'TransportHuman'],
        # ['human', 'PackagingHuman'],
        # ['human', 'OperateMilling'],
        ['skill', 'Waiting'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Human_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Human_SBRC, self).initalise(True)

        self.see.logger.loginfo("Human_SBRC initalised!")
        return True, start_as_idle

    def exec_TransportHuman(self, start, products, goal):
        print "[start, products, goal]", start, products, goal
        start = self.checkPose(start)
        goal = self.checkPose(goal)
        # split into load and unload products
        loadProd = {}
        unloadProd = {}
        for name in products.keys():
            if products[name] > 0:
                loadProd[name] = products[name]
            elif products[name] < 0:
                unloadProd[name] = products[name]
        # Human does not get a product in many configurations.
        # If so, you can define a products human should take.
        product = 'PCBSoldered'
        if loadProd == {}: loadProd = {product: 1}
        if unloadProd == {}: unloadProd = {product: -1}

        actions = [
            {'function': self.robot.moveTo, 'params': [start]},
            {'function': self.robot.load, 'params': [loadProd]},
            {'function': self.robot.moveTo, 'params': [goal]},
            {'function': self.robot.unload, 'params': [unloadProd]},
            {'function': self.finished, 'params': [True, False]},
        ]

        self.robot.appendActions(actions)


class Solder_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        # ['machine', 'Load'],
        # ['machine', 'Unload'],
        ['lab_scenario', 'Soldering'],
        # ['skill', 'Waiting'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Solder_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Solder_SBRC, self).initalise(True)

        self.see.logger.loginfo("Solder_SBRC initalised!")
        return True, start_as_idle

    def exec_Soldering(self, zonetemperatures, conveyorspeed):
        self.see.logger.loginfo("called exec_Soldering")
        actions = [
            {'function': self.robot.solderingON, 'params': []},  # TODO implement: Make 3D model red or so.
            {'function': self.robot.wait, 'params': [2]},  # seconds
            {'function': self.robot.solderingOFF, 'params': []},  # TODO implement: change back to normal state
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)
        self.see.logger.loginfo("finished exec_Soldering")


class Platform_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['machine', 'Transport3DXYYaw'],
        ['machine', 'Move3DXYYaw'],
        ['machine', 'Load'],
        ['machine', 'Unload'],
        ['skill', 'Waiting'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Platform_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Platform_SBRC, self).initalise(True)
        self.see.logger.loginfo("Platform_SBRC initalised!")
        return True, start_as_idle

    def exec_Transport3DXYYaw(self, goal, products):
        self.exec_Move3DXYYaw(goal)

    def exec_Move3DXYYaw(self, goal):
        actions = [
            {'function': self.robot.moveTo, 'params': [goal]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)


class Schunk_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['machine', 'Move6D'],
        ['machine', 'Transport6D'],
        # ['machine', 'Move6DGrip'],
        # ['machine', 'Release'],
        ['skill', 'Waiting'],
        ['factory_scenario', 'Pick'],
        ['factory_scenario', 'PickRelease'],
        # Bottle Stuff
        ['demo', 'Screwing'],
        ['demo', 'Engraving'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(Schunk_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(Schunk_SBRC, self).initalise(True)

        self.see.logger.loginfo("Schunk_SBRC initalised!")
        return True, start_as_idle

    def exec_Screwing(self):
        self.see.logger.logdebug('exec_Screwing called')
        actions = [
            # { 'function' : self.robot.load, 'params' : [products] },
            # { 'function' : self.robot.moveTo, 'params' : [goal] },
            # { 'function' : self.robot.unload, 'params' : [products] },
            {'function': self.robot.wait, 'params': [1]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Engraving(self):
        self.see.logger.logdebug('exec_Engraving called')
        actions = [
            # { 'function' : self.robot.load, 'params' : [products] },
            # { 'function' : self.robot.moveTo, 'params' : [goal] },
            # { 'function' : self.robot.unload, 'params' : [products] },
            {'function': self.robot.wait, 'params': [1]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Pick(self, products):
        self.exec_Load(products)

    def exec_PickRelease(self, products):
        self.exec_Unload(products)

    def exec_Move6D(self, goal):
        self.see.logger.logdebug('Move6D called')
        goal = self.checkPose(goal)
        """
		pos = goal[0]
		ax = goal[1][0]
		ay = goal[1][1]
		az = goal[1][2]
		mx = numpy.matrix([[1,0,0],[0,numpy.cos(ax),-1 * numpy.sin(ax)],[0,numpy.sin(ax),numpy.cos(ax)]])
		my = numpy.matrix([[numpy.cos(ay),0,numpy.sin(ay)],[0,1,0],[-1 * numpy.sin(ay),0,numpy.cos(ay)]])
		mz = numpy.matrix([[numpy.cos(az),-1 * numpy.sin(az),0],[numpy.sin(az),numpy.cos(az),0],[0,0,1]])
		# TODO links oder rechts multiplizieren?
		direction = ( (mx * my * mz).dot(numpy.array([1,0,0])) ).tolist()
		up = ( (mx * my * mz).dot( numpy.array([0,1,0]) ) ).tolist()
		"""
        actions = [
            {'function': self.robot.moveTo, 'params': [goal]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Transport6D(self, goal, products):
        self.see.logger.logdebug('Transport6D called')
        goal = self.checkPose(goal)

        actions = [
            {'function': self.robot.load, 'params': [products]},
            {'function': self.robot.moveTo, 'params': [goal]},
            {'function': self.robot.unload, 'params': [products]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)


class KR5_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['machine', 'Move6D'],
        ['machine', 'Transport6D'],
        ['machine', 'Transport6DSpeed'],
        ['machine', 'RecognizeGrip'],
        ['machine', 'Unload'],
        # ['skill', 'Waiting'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        super(KR5_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(KR5_SBRC, self).initalise(True)

        self.see.logger.loginfo("KR5_SBRC initalised!")
        return True, start_as_idle

    def exec_Move6D(self, goal):
        self.see.logger.logdebug('Move6D called')
        goal = self.checkPose(goal)

        actions = [
            {'function': self.robot.moveTo, 'params': [goal]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Transport6D(self, goal, products):
        self.see.logger.logdebug('Transport6D called')
        goal = self.checkPose(goal)

        actions = [
            {'function': self.robot.load, 'params': [products]},
            {'function': self.robot.moveTo, 'params': [goal]},
            {'function': self.robot.unload, 'params': [products]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Transport6DSpeed(self, goal, products, speed):
        self.see.logger.logdebug('Transport6DSpeed called')
        self.exec_Transport6D(goal, products)

    def exec_RecognizeGrip(self, products, object):
        self.see.logger.logdebug('RecognizeGrip called')
        self.exec_Load(products)


class PCBPrinter_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['factory_scenario', 'Printing'],
        ['machine', 'Unload'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        simulated = False  # TODO hack
        super(PCBPrinter_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(PCBPrinter_SBRC, self).initalise(True)

        self.see.logger.loginfo("PCBPrinter_SBRC initalised!")
        return True, True

    def exec_Printing(self):
        actions = [
            {'function': self.robot.printingON, 'params': []},
            {'function': self.robot.wait, 'params': [1]},
            {'function': self.robot.load, 'params': [{'PCBPrinted': 1}]},
            {'function': self.robot.wait, 'params': [0.5]},
            {'function': self.robot.printingOFF, 'params': []},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)


class FillingSystem_SBRC(Basic_SBRC):
    SYSTEM_IMPORTS = {}

    SKILLS = [
        ['skill', 'Waiting'],
        ['machine', 'Unload'],
        ['demo', 'Filling'],
    ]

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = ''
    STATIC_POSITION = False

    def __init__(self, see, simulated):
        simulated = False  # TODO hack
        super(FillingSystem_SBRC, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(FillingSystem_SBRC, self).initalise(True)

        self.see.logger.loginfo("FillingSystem_SBRC initalised!")
        return True, True

    def exec_Printing(self):
        actions = [
            {'function': self.robot.printingON, 'params': []},
            {'function': self.robot.wait, 'params': [1]},
            {'function': self.robot.load, 'params': [{'PCBPrinted': 1}]},
            {'function': self.robot.wait, 'params': [0.5]},
            {'function': self.robot.printingOFF, 'params': []},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)

    def exec_Filling(self):
        self.exec_Load({'GefuellteFlasche': 1})

    def exec_Waiting(self, duration):
        if self.simulated:
            self.execute_simulated()
        else:
            for i in range(0, duration):
                self.feedback(duration - i)
                self.see.logger.loginfo("Until now waiting for " + str(i)
                                        + " seconds. Still to wait for "
                                        + str(duration - i) + " seconds!")
                time.sleep(1)

        products = self.see.skill.postCondition.products
        actions = [
            {'function': self.robot.load, 'params': [products]},
            {'function': self.robot.unload, 'params': [products]},
            {'function': self.finished, 'params': [True, False]},
        ]
        self.robot.appendActions(actions)
