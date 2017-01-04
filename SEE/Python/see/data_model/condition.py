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

## @package skillpro_execution_engine.see.data_model
# @file condition
# @author Denis Štogl
##

import json


## Condition class represents Conditon_Condiguraiton and Condition_Product variables in OPC UA server.
class Condition(object):

    def __init__(self, configuration='"neutral"', products='{}'):
        self.configuration = configuration
        self.products = products

    def __str__(self):
        #return "\n    Configuration: " + str(self.configuration) + "\n    Products: " + str(self.products)
        return "\n    Configuration: " + "\n    Products: " + str(self.products)

    @property
    def configuration(self):
        return self.__configuration

    @configuration.setter
    def configuration(self, configuration):
        if configuration.startswith('"'):
            self.__configuration = json.loads(configuration)
        else:
            self.__configuration = configuration

    @property
    def products(self):
        return self.__products

    @products.setter
    def products(self, products):
        if isinstance(products, dict):
            prods = products
        else:
            prods = json.loads(products)

        self.__products = {}
        for key in prods.keys():
            self.__products[key] = prods[key]

    def products_to_json_string(self):
        return json.dumps(self.products)

    def empty(self):
        if len(self.products) == 0:
            return True
        else:
            return False

    def check_precondition(self, precondition):
        ret = True
        message = ''

        if precondition.configurations is None or "ANY" in precondition.configurations:
            pass

        elif self.configuration not in precondition.configurations:
            ret = False
            message += "Current configuration: '" + str(self.configuration) + "' does not corresponds to needed Precondition configuration: '" + str(precondition.configurations) + "'!\n"

        # Precondition product is a dictionary
        if isinstance(precondition.products, dict):
            for product in precondition.products.keys():
                # If there does not exist product with <id> in the list of knwon products
                if product not in self.products.keys():
                    for constraint in precondition.products[product]:
                        if constraint.keys()[0] in ['>=', '='] and constraint[constraint.keys()[0]] == 0:
                            self.products[product] = 0

                # If there exist product with <id> in the list of known products
                if product in self.products.keys():
                    # Check Precondition for every condition for the given product
                    for constraint in precondition.products[product]:
                        if constraint.keys()[0] == "=":
                            constraint = ("==", constraint[constraint.keys()[0]])
                        con = str(constraint.keys()[0]) + ' '
                        con += str(constraint[constraint.keys()[0]])
                        if not eval(str(self.products[product]) + ' ' + con):
                            ret = False
                            message += "Product '" + str(product) + ": " + str(self.products[product]) + "' does not satisfy Precondition constraint'" + con + "'!\n"
                # There does not exist product with <id> in the list of knwon products
                else:
                    ret = False
                    message += "Product '" + str(product) + "' cannot be added into list of products because in the Precondition product constraints '" + str(precondition.products[product]) + "' there is no '(\">=\", 0)' or '(\"=\", 0)' constraints!\n"
        # Precondition product is a variable
        else:
            if precondition.products in self.products.keys():
                if self.products[precondition.products] != 1:
                    ret = False
                    message += "Number of pieces of product '" + str(precondition.products) + "' is " + str(self.products[precondition.products]) + " which is not equal 1 therefore not compatible with old format of Precondition product!\n"
                for product in self.products.keys():
                    if product != precondition.products and self.products[product] > 0:
                        ret = False
                        message += "Number of pieces of product '" + str(product) + "' is " + str(self.products[product]) + " which is not allowed for products other than '" + str(precondition.products) + "' therefore not compatible with old format of Precondition product!\n"
            else:
                ret = False
                message += "Product '" + str(precondition.products) + "' does is unknown!\n"
        return ret, message

    def apply_postcondition(self, postcondition):
        ret = True
        message = ''

        # Leave the same condition
        if postcondition.configurations is None:
            pass
        elif "SAME" in postcondition.configurations:
            pass
        else:
            self.configuration = postcondition.configurations

        # If the Postcondition product is in the new format
        if isinstance(postcondition.products, dict):
            for product in postcondition.products.keys():
                # If there exists in the list sum it with the current value
                if product in self.products.keys():
                    self.products[product] += postcondition.products[product]
                # If not do nothing - for now
                else:
                    pass
        # If the Postcondition product is in the old format
        else:
            for product in self.products.keys():
                if product == postcondition.products:
                    self.products[product] = 1
                else:
                    self.products[product] = 0


class PrePostCondition(Condition):

    def __init__(self, configuration='', products='{}'):
        self.configurations = configuration
        self.products = products

    @property
    def configurations(self):
        return self.__configurations

    @configurations.setter
    def configurations(self, configurations):
        if configurations.startswith('"') or configurations.startswith("["):
            confs = json.loads(configurations)
            if isinstance(confs, list):
                self.__configurations = [conf for conf in confs]
            else:
                self.__configurations = confs
        else:
            self.__configurations = configurations
