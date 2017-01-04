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

## @package skillpro_execution_engine
# @file create_sees
# @author Denis Štogl
##

import sys
from opcua import Client
from opcua import ua

SEES = ["HumanWorker",
        "KR5",
        "SchunkArm",
        "BoxOutput",
        "MobilePlatform",
        "SolderingMachine",
        "QualityCheck",
        "KR5Output",
        "LineOutput",
        "PCBPrinter"
        ]


def main(args):

    endpoint = "localhost:51200"
    aml_snippet_path = './aml/see_additional.aml'

    if len(args) > 1:
        endpoint = args[1]
    if len(args) > 2:
        aml_snippet_path = args[2]

    aml_snippet = open(aml_snippet_path).read()

    if "opc.tcp" not in endpoint.lower():
		endpoint = "opc.tcp://" + endpoint
	
    print("Used endpoint: " + endpoint)
    client = Client(endpoint)
    client.connect()
    objects = client.get_objects_node()
    see_obj = objects.get_child(["2:SEE"])

    for see in SEES:
        ret = see_obj.call_method("2:CreateSEE", see, aml_snippet, '')
        print("SEE: " + see + " NodeId: " + str(ret))


    client.disconnect()
    exit()

if __name__ == '__main__':
    main(sys.argv)
