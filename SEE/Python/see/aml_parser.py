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
# @file aml_parser
# @author Denis Štogl
##

from lxml import etree

from data_model.see_aml import SEE_AMLDescription
from data_model.condition import Condition, PrePostCondition
from data_model.communication import *
from data_model.executable_skill import ExecutableSkill, Execution


## AMLParser class.
# This class implements generic AML parser which converts AML file tree into dictionary.
##
class AMLParser(object):

    ## Static dictionary with complex data types as keys and theirs children as values
    complex_types = {
        "InstanceHierarchy": "InternalElement",
        "InterfaceClassLib": "InterfaceClass",
        "RoleClassLib": "RoleClass",
    }

    ## Static dictionray with mapping of AML tree elemnts and theirs identifiers
    type_uid_pairs = {
        # TODO: add _id Id string
        "InstanceHierarchy": "Name",
        "InternalElement": "ID",
        "Attribute": "Name",
        "RoleRequirements": "RefBaseRoleClassPath",
        "SupportedRoleClass": "RefRoleClassPath",

        "InterfaceClassLib": "Name",
        "RoleClassLib": "Name",
    }

    ## Constructor
    def __init__(self, logger):
        self._logger = logger

    ## Public function to parse the AML file
    def parse(self, aml_file_path):

        result = False
        self._dict = None

        try:
            tree = etree.parse(aml_file_path)
            self._dict = self._parse_aml_file(tree.getroot())
            result = True
        except Exception, e:
            self._logger.logerror("Exception during parsing: " + str(e))
            result = False

        return result

   ## Private function to map the AML file to dictionary
    def _parse_aml_file(self, root):

        self._dict = {}

        self._dict[root.tag] = {}
        self._dict[root.tag]['attribs'] = root.attrib

        for element in list(root):

            # TODO: Write parsing of header
            if not element.tag in AMLParser.complex_types.keys():
                pass
            else:
                if not element.tag in self._dict.keys():
                    self._dict[element.tag] = {}
                self._dict[element.tag][element.attrib[AMLParser.type_uid_pairs[element.tag]]] = self._create_dict_from_aml_struct(element)

    ## Private function to map the AML snippet to dictionary
    def _parse_aml_snippet(self, root):

        dict = {}

        dict[root.tag] = {}
        dict[root.tag][root.attrib[AMLParser.type_uid_pairs[root.tag]]] = self._create_dict_from_aml_struct(root)

        return dict

    # Private function which recusivly creates dictionary form AML struct
    def _create_dict_from_aml_struct(self, element):

        dict = {}

        for attrib_name in element.attrib.keys():
            dict[attrib_name] = element.attrib[attrib_name]

        for child in list(element):
            if child.tag in AMLParser.type_uid_pairs.keys():
                if not child.tag in dict.keys():
                    dict[child.tag] = {}
                dict[child.tag][child.attrib[AMLParser.type_uid_pairs[child.tag]]] = self._create_dict_from_aml_struct(child)
            else:
                dict[child.tag] = child.text

        return dict


## SEE_AMLParserException class.
# This class implements exceptions for SEE_AMLParser
##
class AMLParserException(Exception):

    def __init__(self):
        super(AMLParserException, self).__init__()


## SEE_AMLParser class.
# This class implements mapping from AML data model to SkillPro Skill Execution Engine model.
##
class SEE_AMLParser(AMLParser):

    ## Static dictionry with Roles used in SkillPro.
    SkillPro_ROLES = {
        'SEE': "SkillProRoleClassLib/SkillExecutionEngine",
        'Asset': "SkillProRoleClassLib/SkillProResource",
        'Action': "SkillProRoleClassLib/ResourceExecutableSkill"
    }

    ## Constructor
    # @returns Pointer to AMLParser object if everyting OK
    ##
    def __init__(self, logger):
        super(SEE_AMLParser, self).__init__(logger)

    def parse_SEE_from_aml_file(self, aml_file, see_id=''):
        return self._find_see_with_id(self.parse_SEEs_from_aml_file(aml_file), see_id)

    def parse_SEE_from_file(self, aml_file_path, see_id=''):
        return self._find_see_with_id(self.parse_SEEs_from_file(aml_file_path), see_id)

    def _find_see_with_id(self, parsed_sees, see_id=''):
        if see_id == '':
            if len(parsed_sees.keys()) == 1:
                see_id = parsed_sees.keys()[0]
            else:
                raise SEE_AMLParserException("SEE ID is not provided and there is more than one SEE defined in the provided AML file. Therefore no SEE data can be loaded. Please provide SEE ID or AML file with only one SEE.")
        else:
            if not see_id in parsed_sees.keys():
                raise SEE_AMLParserException("Provided SEE ID is not defined in the provided AML file. Therefore no SEE data can be loaded. Please provide SEE ID or AML file with only one SEE.")

        return see_id, parsed_sees[see_id]

    ## Parse file method parses all SEEs from AML file
    #  @param aml_file_path Path to the AML file to parse
    #  @returns Dictionary with SEEs found in the files
    def parse_SEEs_from_file(self, aml_file_path):
        if aml_file_path is None or aml_file_path == '':
            return None

        self._logger.logdebug('Reading AML file from path' + aml_file_path)
        return self.parse_SEEs_from_aml_file(open(aml_file_path, 'r').read(), aml_file_path)

    def parse_SEEs_from_aml_file(self, aml_file, aml_file_path=''):
        if aml_file is None or aml_file == '':
            return None

        see_dict = {}
        self._parse_aml_file(etree.fromstring(aml_file))

        # Find and add all SEEs
        for element in self._dict['InstanceHierarchy']['Configuration']['InternalElement'].keys():
            base_dict = self._dict['InstanceHierarchy']['Configuration']['InternalElement'][element]
            if SEE_AMLParser.SkillPro_ROLES['SEE'] in base_dict['RoleRequirements'].keys():
                see_aml = SEE_AMLDescription(element, aml_file_path)
                #TODO: this should be nicer
                see_aml._aml_descr = aml_file
                #TODO: Change to getters and setters
                see_aml._name = base_dict['Name']
                #print base_dict['Name']
                print base_dict['Attribute']['Default_Condition']['Attribute']['Product']['Value']
                print base_dict['Attribute']['Default_Condition']['Attribute']['Product']['Value']
                see_aml._condition = Condition(
                    base_dict['Attribute']['Default_Condition']['Attribute']
                    ['Configuration']['Value'],
                    base_dict['Attribute']['Default_Condition']['Attribute']
                    ['Product']['Value'])
                # TODO for this should be one function
                see_aml._ams_comm = CommunicationDescription(
                    base_dict['Attribute']['AMSCommType']['Value'],
                    base_dict['Attribute']['AMSCommType']['Attribute']
                    ['uri']['Value'])
                see_aml._mes_comm = self.get_mes_comm(base_dict)
                see_aml._es_comm = self.get_es_comm(base_dict)
                see_aml.see_type = (base_dict['Attribute']['SEEType']['Value'],
                                    base_dict['Attribute']['SEEType']
                                    ['Attribute']['simulated']['Value'])
                #TODO add find resource
                see_dict[element] = see_aml

                # Find and add all Skills if SEE exists
                for child_element in base_dict['InternalElement'].keys():
                    child_base_dict = base_dict['InternalElement'][child_element]
                    if 'RoleRequirements' in child_base_dict.keys():
                        if SEE_AMLParser.SkillPro_ROLES['Action'] in child_base_dict['RoleRequirements'].keys():
                            see_id, skill = self._create_skill_from_dict(child_element, child_base_dict)
                            if see_id in see_dict.keys():
                                see_dict[see_id]._executable_skills[child_element] = skill

        # Find and add all Skills if SEE exists
        for element in self._dict['InstanceHierarchy']['Configuration']['InternalElement'].keys():
            base_dict = self._dict['InstanceHierarchy']['Configuration']['InternalElement'][element]
            if SEE_AMLParser.SkillPro_ROLES['Action'] in base_dict['RoleRequirements'].keys():
                see_id, skill = self._create_skill_from_dict(element, base_dict)
                if see_id in see_dict.keys():
                    see_dict[see_id]._executable_skills[element] = skill

        return see_dict

    def get_mes_comm(self, see_base_dict):
#TODO
        if 'Value' in see_base_dict['Attribute']['MESCommType']['Attribute']['uri'].keys():
            comm_dscr = CommunicationDescription(see_base_dict['Attribute']['MESCommType']['Value'],
                                             see_base_dict['Attribute']['MESCommType']['Attribute']['uri']['Value'])

            if 'Value' in see_base_dict['Attribute']['MESCommType']['Attribute']['nodeId'].keys():
                if see_base_dict['Attribute']['MESCommType']['Attribute']['nodeId']['Value'] != '':
                    comm_dscr.set_point(CommunicationPoint(see_base_dict['Attribute']['MESCommType']['Attribute']['uri']['Value'],
                                                        see_base_dict['Attribute']['MESCommType']['Attribute']['nodeId']['Value'])
                                        )
        else:
            comm_dscr = CommunicationDescription(
                see_base_dict['Attribute']['MESCommType']['Value'],
                '')

        return comm_dscr

    def get_es_comm(self, see_base_dict):
        if 'Value' in see_base_dict['Attribute']['ESCommType']['Attribute']['uri'].keys():
            comm_dscr = CommunicationDescription(
                see_base_dict['Attribute']['ESCommType']['Value'],
                see_base_dict['Attribute']['ESCommType']['Attribute']['uri']['Value'])

            if 'Value' in see_base_dict['Attribute']['ESCommType']['Attribute']['nodeId'].keys():
                if see_base_dict['Attribute']['ESCommType']['Attribute']['nodeId']['Value'] != '':
                    comm_dscr.set_point(CommunicationPoint(see_base_dict['Attribute']['ESCommType']['Attribute']['uri']['Value'],
                                                        see_base_dict['Attribute']['ESCommType']['Attribute']['nodeId']['Value'])
                                        )
        else:
            comm_dscr = CommunicationDescription(
                see_base_dict['Attribute']['ESCommType']['Value'],
                '')

        return comm_dscr

    def _create_skill_from_dict(self, skill_id, dict):
        skill = ExecutableSkill(skill_id)

        skill.name = dict['Name']
        skill.preCondition = self._getConditions(dict['Attribute']['PreCondition'])
        skill.postCondition = self._getConditions(dict['Attribute']['PostCondition'])
        if 'AltPostCondition' in dict['Attribute']:
            skill.altPostCondition = self._getConditions(dict['Attribute']['AltPostCondition'])

        if 'SkillSynchronization' in dict['Attribute']:
            if 'Attribute' in dict['Attribute']['SkillSynchronization']:
                skill.sync.type = dict['Attribute']['SkillSynchronization']['Attribute']['Type']['Value']
                skill.sync.address = dict['Attribute']['SkillSynchronization']['Attribute']['Address']['Value']

        skill.execution = self._getExecution(dict['Attribute']['Execution'])

        skill.duration = int(dict['Attribute']['Duration']['Value'])
        skill.remaining_duration = skill.duration

        return dict['Attribute']['ResponsibleSEE']['Value'], skill

    def _getConditions(self, base_dict):
        return PrePostCondition(base_dict['Attribute']['Configuration']['Value'], base_dict['Attribute']['Product']['Value'])

    def _getExecution(self, base_dict):
        data = {}

        if 'Attribute' in base_dict['Attribute']['Data'].keys():
            for item in base_dict['Attribute']['Data']['Attribute'].keys():
                data[item] = base_dict['Attribute']['Data']['Attribute'][item]['Value']

        return Execution(base_dict['Attribute']['Type']['Value'], data)

    def parseExecuableSkillsFromAMS(self, retrieved_executable_skills):

        executable_skills = {}

        for key in retrieved_executable_skills.keys():
            executable_skills[key] = self.parseExecutableSkill(
                key, retrieved_executable_skills[key].aml_description)

        return executable_skills

    def parseExecutableSkill(self, skill_id, aml):

        skill = None

        dict = self._parse_aml_snippet(etree.fromstring(aml))
        see_id, skill = self._create_skill_from_dict(
            skill_id, dict['InternalElement'][skill_id])

        return skill


## SEE_AMLParserException class.
# This class implements exceptions for SEE_AMLParser
##
class SEE_AMLParserException(AMLParserException):

    def __init__(self):
        super(SEE_AMLParserException, self).__init__()
