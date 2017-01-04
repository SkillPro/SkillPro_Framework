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
# @file opcua_communication
# @author Denis Štogl
##

import sys
import time
import opcua
from opcua.common.uaerrors import UaStatusCodeError
import threading
import logging

logger = logging.getLogger("opcua.address_space")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.internal_server")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.binary_server_asyncio")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.uaprocessor")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.subscription_service")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.client.ua_client")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.uaprotocol")
logger.setLevel(logging.WARN)
logger = logging.getLogger("opcua.common.subscription")
logger.setLevel(logging.WARN)

mutex = threading.Lock()
sub_mutex = threading.Lock()


class SubHandler(object):

    def __init__(self, callback_func):
        self.callback = callback_func

    def datachange_notification(self, node, val, data):
        self.callback(val, node)


class SkillPro_OpcUa(object):

    def __init__(self, see, comm):
        self.see = see
        self.client = None
        if isinstance(comm, str):
            self.client = opcua.Client(comm)
        else:
            self.client = opcua.Client(comm.uri)

        # Variables init
        self.slave_node_id_value = None
        self.listen_for_message = None
        self.listen_master_on = None
        self.recived_callback_from_slave = None

        self.sub = {}
        self.handle = {}
        self.node_to_delete = None

    def opcua_connect(self):
        try:
            self.client.connect()
            return True
        except Exception, e:
            print e
            return False

    def opcua_disconnect(self):
        self.client.disconnect()

    def opcua_create_subscription(self, node, callback):
        if isinstance(node, str):
            print(node)
            node = opcua.ua.NodeId.from_string(node)
        if isinstance(node, opcua.ua.NodeId):
            node = self.client.get_node(node)
        self.see.logger.loginfo("Subscribing to node: " + str(node) + " with callback" + str(callback))
        key = node.nodeid.to_string()
        self.sub[key] = self.client.create_subscription(500, SubHandler(callback))
        self.handle[key] = self.sub[key].subscribe_data_change(node)

    def opcua_delete_subscription(self, node):
        self.node_to_delete = node
        self.see.thread_manager.startThread(
                    self.__delete_subscription)

    def __delete_subscription(self):
        key = self.node_to_delete.nodeid.to_string()
        self.sub[key].unsubscribe(self.handle[key])
        self.sub[key].delete()
        del self.sub[key]
        del self.handle[key]

        self.node_to_delete = None


    def get_nodeId_members_prefix_from_nodeId(self, nodeId):
        # OpcUa variables nodeId is in standard format: "ns=2;i=3001"
        splitted = nodeId.split(";")
        ns = splitted[0].split("=")
        node = splitted[1].split("=")

        return "ns=" + ns[1] + ";" + "s=" + node[1]

    def set_node_value(self, nodeId, value, typ):
        var = self.client.get_node(nodeId)
        var.set_value(value, typ)

    def find_SEE_on_server(self, base_path):
        found = False
        objects = self.client.get_objects_node()
        for obj in objects.get_children():
            if base_path in obj.get_browse_name().Name:
                for child in obj.get_children():
                    if child.get_browse_name().Name in self.see.name:
                        found = True
                        var = self.client.get_node(self.get_nodeId_members_prefix_from_nodeId(child.nodeid.to_string()) + '.SEE_Id')
                        self.see.logger.logdebug(var)
                        res2 = var.get_value()
                        #res2 = self.read_srv(
                            #opcua_srvs.ReadRequest(opcua_msgs.Address(
                                #self.get_nodeId_members_prefix_from_nodeId(
                                    #child.nodeId) + '.SEE_Id', '')))
                        if res2 != self.see.Id:
                            self.see.logger.logerror("OPC-UA Communication: Found SEE with corresponding Name ('" + self.see.name + "') but wrong Id! AML-File id: '" + self.see.Id + "', MES server id: '" + res2 + "'. Id: " + str(child.nodeid))
                        else:
                            self.see.logger.loginfo("OPC-UA Communication: Found SEE '" + self.see.name + "' (Id: '" + self.see.Id + "') on MES server. Id: " + str(child.nodeid))
                        break

                        #TODO maybe search ruther - for now do nothing

        return found, child.nodeid.to_string()

    def find_slave_SEEs(self, address):
        one_not_found = False
        self.see.logger.loginfo("OPC-UA Communication: Search for slave triggering address: " + address)
        self.slave_node_id_value = {}
        self.recived_callback_from_slave = {}
        browse_tree_paths = eval(address)
        for key in browse_tree_paths.keys():
            nodeID_to_list = self.find_nodeId_through_three(key)
            if nodeID_to_list:
                self.slave_node_id_value[nodeID_to_list] = browse_tree_paths[key]
                self.recived_callback_from_slave[nodeID_to_list] = False

                with mutex:
                    self.see.logger.loginfo("OPC-UA Communication: '" + self.slave_node_id_value[nodeID_to_list] + "' will be written to the node: " + str(nodeID_to_list))
                    #self.set_node_value(nodeID_to_list, '', opcua.ua.VariantType.String)
                    self.listen_for_message = "Ready"
                    self.opcua_create_subscription(nodeID_to_list, self.triggered_master_start)
            else:
                one_not_found = True

        if one_not_found and len(self.slave_node_id_value) == 1:
            return False
        else:
            while len(self.recived_callback_from_slave):
                self.see.logger.loginfo("Waiting for Slave SEE to get ready...")
                time.sleep(1)
            return True

    def find_nodeId_through_three(self, key):
        nodeid = None
        path = key.split("/")
        three = ["0:Objects", "2:SEE"]
        for i in range(0, len(path)):
            three.append("2:"+path[i])
        self.see.logger.logdebug("OPC-UA Communication: Search three is: " + str(three))
        try:
            nodeid = self.client.get_root_node().get_child(three).nodeid
        except UaStatusCodeError, e:
            self.see.logger.logerror("OPC-UA Communication: Something went wrong by searching the node three: " + three + ". The erorr message is: " + e)

        return nodeid

    def trigger_end_on_slave_SEEs(self):
        for key in self.slave_node_id_value.keys():
            self.see.logger.loginfo("OPC-UA Communication: Writing '" + self.slave_node_id_value[key] + "' to the node: " + str(key))
            mutex.acquire()
            self.set_node_value(key, self.slave_node_id_value[key], opcua.ua.VariantType.String)
            mutex.release()

    def listen_master_on_address(self, address, slave_end_cb):
        self.see.logger.loginfo("OPC-UA Communication: Search for listening address: " + address)
        self.slave_end_cb = slave_end_cb
        browse_tree_paths = eval(address)
        self.listen_master_on = self.find_nodeId_through_three(
            browse_tree_paths.keys()[0])
        if self.listen_master_on:
            with mutex:
                self.listen_for_message = browse_tree_paths[browse_tree_paths.keys()[0]]
                self.see.logger.loginfo("OPC-UA Communication: '" + self.listen_for_message + "' will be listen on the node: " + str(self.listen_master_on))
                self.set_node_value(self.listen_master_on, 'Ready', opcua.ua.VariantType.String)
            # Subscribe to Call variable
            self.opcua_create_subscription(self.listen_master_on, self.triggered_slave_end)

            return True
        else:
            return False

    def triggered_master_start(self, val, node):
        self.see.logger.logdebug("OPC-UA Communication: Master start trigger received!")
        if self.listen_for_message and val == self.listen_for_message:
            with mutex:
                if node.nodeid in self.recived_callback_from_slave.keys():
                    del self.recived_callback_from_slave[node.nodeid]
                    self.opcua_delete_subscription(node)
                else:
                    self.see.logger.logwarn("OPC-UA Communication: recived trigger from node not existing in the list of slaves: " + str(node))
        else:
            self.see.logger.logwarn("OPC-UA Communication: Maser recived keyword: " + val + ", but expected: " + self.listen_for_message)

    def triggered_slave_end(self, val, node):
        self.see.logger.logdebug("OPC-UA Communication: Slave trigger received!")
        if self.listen_for_message and val == self.listen_for_message:
            with mutex:
                self.see.thread_manager.startThread(
                    self.slave_end_cb)
                self.listen_for_message = None
                self.opcua_delete_subscription(node)
            self.see.logger.loginfo("OPC-UA Communication: Started thread slave trigger recived")
        else:
            self.see.logger.logwarn("OPC-UA Communication: Slave recived keyword: " + val + ", but expected: " + self.listen_for_message)

    def shutdown(self):
        for node in self.sub.keys():
            self.opcua_delete_subscription(node)
        self.client.disconnect()
