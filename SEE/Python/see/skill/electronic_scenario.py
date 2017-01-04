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
# @file electronic_scenario
# @author Denis Štogl
##


import human


class ConfigureSMTLineOperator(human.HumanBase):

    NAME = "ConfigureSMTLineOperator"
    KEY = NAME

    PARAMETERS = {
        'OvenHeatingZoneTemperatures': "Oven Heating Zone Temperatures",
        'OvenConveyorSpeed': "Oven Conveyor Speed",
        'OvenNitorgenUsage': "Oven Nitrogen Usage",
        'PastType': "Paste Type",
        'StancilThickness': "Stencil Thickness",
        'ProgramName': "Program Name",
        'PartsFeedersProperty': "Parts Feeders Property",
    }

    def __init__(self, sbrc):
        super(ConfigureSMTLineOperator, self).__init__(sbrc)

    def check_parameters(self, data):
        program = "<p class=\"ng-binding\"><b>" + data['instruction'] + "</b>:</p>"
        program += self._generate_parameter_list(data)

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class ConfigureTHTWaveSolderingOperator(human.HumanBase):

    NAME = "ConfigureTHTWaveSolderingOperator"
    KEY = NAME

    PARAMETERS = {
        'HeatingZoneTemperatures': "Heating Zone Temperatures",
        'ConveyorSpeed': "Conveyor Speed",
    }

    def __init__(self, sbrc):
        super(ConfigureTHTWaveSolderingOperator, self).__init__(sbrc)

    def check_parameters(self, data):
        program = "<p class=\"ng-binding\"><b>" + data['instruction'] + "</b>:</p>"
        program += self._generate_parameter_list(data)

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class DoTHTAssembly(human.HumanBase):

    NAME = "DoTHTAssembly"
    KEY = NAME

    def __init__(self, sbrc):
        super(DoTHTAssembly, self).__init__(sbrc)

    def check_parameters(self, data):
        program = self._instruction_with_figure(data['instruction'], data['plan'])
        getattr(self.sbrc, self.EXEC_FUNC)(program)


class FunctionalTesting(human.HumanBase):

    NAME = "FunctionalTesting"
    KEY = NAME

    def __init__(self, sbrc):
        super(FunctionalTesting, self).__init__(sbrc)

    def check_parameters(self, data):


        program = "<p class=\"ng-binding\"><b>Please do testing</b>:</p>"
        program += "<p class=\"ng-binding\">For testing you will need:</p>"
        equipment = eval(data['testequipement'])
        program += "<ul>"

        for tool in equipment.keys():
            program += "<li><p class=\"ng-binding\">" + str(equipment[tool]) + "  <i>" + str(tool) + "</p></i></li>"

        program += "</ul>"

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class HandSoldering(human.HumanBase):

    NAME = "HandSoldering"
    KEY = NAME

    def __init__(self, sbrc):
        super(HandSoldering, self).__init__(sbrc)

    def check_parameters(self, data):



        program = self._instruction_with_figure(data['instruction'], data['plan'])

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class ManualInspection(human.HumanBase):

    NAME = "ManualInspection"
    KEY = NAME

    def __init__(self, sbrc):
        super(ManualInspection, self).__init__(sbrc)

    def check_parameters(self, data):


        program = self._instruction_with_figure(data['instruction'], data['productinformation'])

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class OpticalInspection(human.HumanBase):

    NAME = "OpticalInspection"
    KEY = NAME

    def __init__(self, sbrc):
        super(OpticalInspection, self).__init__(sbrc)

    def check_parameters(self, data):


        program = self._instruction_with_figure(data['instruction'], data['inspectionplan'])

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class QualityControl(human.HumanBase):

    NAME = "QualityControl"
    KEY = NAME

    def __init__(self, sbrc):
        super(QualityControl, self).__init__(sbrc)

    def check_parameters(self, data):


        program = "<p class=\"ng-binding\"><b>" + data['controlplan'] + ".</b></p>"

        getattr(self.sbrc, self.EXEC_FUNC)(program)


class Washing(human.Instruction):

    NAME = "Washing"
    KEY = NAME

    def __init__(self, sbrc):
        super(Washing, self).__init__(sbrc)
