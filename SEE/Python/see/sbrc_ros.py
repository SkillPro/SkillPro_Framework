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
# @file sbrc_ros
# @author Denis Štogl
##

import importlib
import threading
import time

from sbrc import SkillBasedResourceController

import rospy


## SkillBasedResourceController_ROS class.
# This class implements base structure of Skill based resource controller.
##
class SkillBasedResourceController_ROS(SkillBasedResourceController):

    def __init__(self, see, simulated):
        super(SkillBasedResourceController_ROS, self).__init__(see, simulated)


class Human_SBRC_ROS(SkillBasedResourceController_ROS):

    SYSTEM_IMPORTS = {'requests': 'requests',
                      'json': 'json',
                      'see_srvs': 'see_srvs.srv'}

    SKILLS = [
        #['electronic_scenario', 'ConfigureSMTLineOperator'],
        #['electronic_scenario', 'ConfigureTHTWaveSolderingOperator'],
        #['electronic_scenario', 'DoTHTAssembly'],
        #['electronic_scenario', 'FunctionalTesting'],
        #['electronic_scenario', 'HandSoldering'],
        #['electronic_scenario', 'ManualInspection'],
        #['electronic_scenario', 'OpticalInspection'],
        #['electronic_scenario', 'QualityControl'],
        ['human', 'TransportHuman'],
        ['human', 'PackagingHuman'],
        ['human', 'OperateMilling'],
        ['machine', 'Unload'],
        #['electronic_scenario', 'Washing'],

        #['hmi2015_scenario', 'BuildLegoFlag'],
        #['human', 'Instruction'],
        #['human', 'Logout'],
        #['hmi2015_scenario', 'PickUpLegos'],
        ]

    base_ip = '141.21.14.245:8080'

    PUBLISH_TO_ES = False
    ES_DATA_TYPE = 'TCP_position'
    STATIC_POSITION = True

    def __init__(self, see, simulated):
        super(Human_SBRC_ROS, self).__init__(see, simulated)
        self.frame = self.see.name

    def initalise(self):
        start_as_idle = super(Human_SBRC_ROS, self).initalise(False)

        if not self.simulated:
            rospy.init_node('human_SEE_' + self.see.name)

            self._url = "http://" + Human_SBRC_ROS.base_ip + "/api/" + self.see.name + "/ExecutableSkills"
            self._headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}

            self.see.logger.loginfo("Waiting for service: " + rospy.resolve_name('/registerHuman'))
            rospy.wait_for_service('/registerHuman')
            register = rospy.ServiceProxy('/registerHuman', self.sys_mods['see_srvs'].AMS_registerSEE)

            rospy.Service('answer', self.sys_mods['see_srvs'].AMS_registerSEE, self.human_callback)

            register(self.sys_mods['see_srvs'].AMS_registerSEERequest(self.see.name, rospy.resolve_name('answer')))

        else:
            start_as_idle = True

        self.see.logger.loginfo("Human SBRC initalised!")
        return True, start_as_idle

    def _generate_parameter_list(self, data):
        program = "<ul>"
        for parameter in data.keys():
            program += "<li><p class=\"ng-binding\">" + str(parameter) + ":  <i>" + data[parameter] + "</p></i></li>"
        program += "</ul>"

        return program

    def _instruction_with_figure(self, instruction, figure):
        program = "<p class=\"ng-binding\"><b>" + instruction + "</b>:</p>"
        program += "<br>"
        program += "<img src=\"" + figure + "\" width=\"90\%\">"

        return program

    def exec_TransportHuman(self, products, start, goal):
        program = "<p class=\"ng-binding\"><b>Please transport products</b>:</p>"
        program += "<ul>"
        for product in products.keys():
            program += "<li><p class=\"ng-binding\">" + str(products[product]) + "  <i>" + str(product) + "</p></i></li>"
        program += "</ul>"
        program += "<p class=\"ng-binding\">"
        program += "<b>From</b>: " + "<i>" + start + "</i><br>"
        program += "<b>To</b>: " + "<i>" + goal + "</i><br>"
        program += "</p>"

        self.start_execution_on_hardware(program)

    def exec_PackagingHuman(self, products):
        program = "<p class=\"ng-binding\"><b>Please package products</b>:</p>"
        program += "<ul>"
        for product in products.keys():
            program += "<li><p class=\"ng-binding\">" + str(products[product]) + "  <i>" + str(product) + "</p></i></li>"
        program += "</ul>"

        self.start_execution_on_hardware(program)

    def exec_OperateMilling(self, instruction, figure, speed, depth):

        program = self._instruction_with_figure(instruction, figure)
        program += "<br>"
        program += self._generate_parameter_list({'Speed': speed,
                                                  'Depth': depth})

        self.start_execution_on_hardware(program)

    def start_execution_on_hardware(self, program):
        # Old version of stuff
        if isinstance(program, dict):
            data = program

        # New version of stuff
        else:
            data = {}

            data['html'] = program

        data['name'] = self.skills[self.skill.execution.type].NAME
        data['status'] = 'new'
        data['skillId'] = self.skill.id + str(time.time())

        if not self.simulated:
            json_string = self.sys_mods['json'].dumps(data, indent=4, separators=(',', ': '))

            try:
                r = self.sys_mods['requests'].post(self._url, data=json_string, headers=self._headers)
            except:
                rospy.logerr("Requests error!!!")

            if r.status_code == 200:
                self._current_skill_id = data['skillId']
                self.skill_status = self._get_human_status()
                # Why this doesn't work with skill_type thread
#                 self._current_skill_type = self.skill.execution.type
#                 self._current_skill_id = data['skillId']
#                 self._human_get_status = threading.Thread(target=self.self._get_human_status)
#                 self._human_get_status.start()
                pass

        else:
            self.execute_simulated()
            self.finished(True, False)

    def _get_human_status_thread(self):
        skill_status = 'new'
        self.see.logger.logwarn("Started Thread")

        rate = rospy.Rate(20)
        while not rospy.is_shutdown():

            r = self.sys_mods['requests'].get(self._url, headers=self._headers)
            if (r.status_code == 200):
                data = self.sys_mods['json'].loads(r.text)

                for exec_skill in data:
                    if exec_skill['skillId'] == self._current_skill_id:

                        if exec_skill['status'] == 'succeeded':
                            self.skills[self._current_skill_type].finished = True
                            self.skills[self._current_skill_type].finished_successfull = True

                        elif exec_skill['status'] == 'pause':
                            if skill_status == 'accepted':
                                self.see.ssm.update_state_event("MES_Pause")
                                time.sleep(0.2)
                                self.see.ssm.update_state_event("INT_CAN_RESUME")

                        elif exec_skill['status'] == 'accepted':
                            if skill_status == 'pause':
                                self.see.ssm.update_state_event("MES_Resume")

                        skill_status = exec_skill['status']
            rate.sleep()

    def human_callback(self, req):

        res = self.sys_mods['see_srvs'].AMS_registerSEEResponse()

        self.see.logger.loginfo("Data received from Mobile-SEE: " + req.seeId)

        if req.seeId == 'CONFIGURE':
            if req.see_aml != '':
                self.see.ssm.update_state_event("EXT_CONFIGURED")

            res.success = True
            res.error_message = 'Everything OK!'

        elif req.seeId == 'SHUTDOWN':
            if self.skill:
                if self._get_human_status() == 'pause':
                    self.see.ssm.update_state_event("MES_Resume")
                #TODO check if this line needed
                self.see.ssm.update_state_event("INT_SKILL_EXECUTED")

                self.finished(True, False)

            time.sleep(0.2)
            self.see.ssm.update_state_event("EXT_SHUTDOWN")
            res.success = True
            res.error_message = 'Everything OK!'

        elif self.skill.execution.type:
            if self.skill:

                current_skill_status = self._get_human_status()

                if req.seeId == 'PAUSE':
                    if self.skill_status == 'accepted' and current_skill_status == 'pause':
                        self.see.ssm.update_state_event("MES_Pause")
                        time.sleep(0.2)
                        self.see.ssm.update_state_event("INT_CAN_RESUME")

                if req.seeId == 'UPDATE':
                    if self.skill_status == 'new' and current_skill_status == 'accepted':
                        self.see.set_productive(True)
                    if self.skill_status == 'pause' and current_skill_status == 'accepted':
                        self.see.ssm.update_state_event("MES_Resume")

                elif req.seeId == 'STOP':
                    if self.skill_status == 'accepted' and  current_skill_status == 'succeeded':

                        self.finished(True, False)

                elif req.seeId == 'THROWTE':
                    if self.skill_status == 'accepted' and  current_skill_status == 'failed':

                        self.finished(False, False)

                elif req.seeId == 'RECOVER':
                    pass

                self.skill_status = current_skill_status
                res.success = True
                res.error_message = 'Everything OK!'

            else:
                res.success = True
                res.error_message = 'Received command: ' + req.seeId

        else:
            res.success = False
            res.error_message = 'Received command: ' + req.seeId

        return res

    def _get_human_status(self):

        r = self.sys_mods['requests'].get(self._url, headers=self._headers)
        if (r.status_code == 200):
            data = self.sys_mods['json'].loads(r.text)

            for exec_skill in data:
                if exec_skill['skillId'] == self._current_skill_id:
                    return exec_skill['status']


## @package see_sbrc.sbrc
# @file mobile_platform_sbrc
# @author Denis Štogl
##
class MobilePlatfom_SBRC_ROS(SkillBasedResourceController_ROS):

    SYSTEM_IMPORTS = {'math': 'math',
                      'actionlib': 'actionlib',
                      'tf2_ros': 'tf2_ros',
                      'transform': 'tf.transformations',
                      'actionlib_msgs': 'actionlib_msgs.msg',
                      'cob_srvs': 'cob_srvs.srv',
                      'std_srvs': 'std_srvs.srv',
                      'move_base_msg': 'move_base_msgs.msg'}

    SKILLS = [
        ['machine', 'Transport3DXYYaw'],
        ['machine', 'Load'],
        ['machine', 'Unload'],
        ['skill', 'Waiting']
        ]

    PUBLISH_TO_ES = True
    ES_DATA_TYPE = 'TCP_position'
    STATIC_POSITION = False

    IMPORTANT_COORDINATES = {
        'base_pose': ('map', [1.9723, -1.076, 0, 0, -0.5263, -0.5263]),
        'kr5': ('map', [0.5662, -0.0145, 0, 0, 0.2885, 0.2885]),
        'ur5': ('map', [0.5206, -1.791, 0, 0, 0.2629, 0.9648]),
        'quality_control': ('map', [0.9133, -0.609, 0, 0, 0.3046, 0.9524]),
        'packageing_station': ('map', [0.9133, -0.609, 0, 0, -0.493, 0.8696]),
    }

    def __init__(self, see, simulated):
        super(MobilePlatfom_SBRC_ROS, self).__init__(see, simulated)

        self.multiple_goals_thread = None
        self.frame = 'map'

    def initalise(self):
        start_as_idle = super(MobilePlatfom_SBRC_ROS, self).initalise(True)

        if not self.simulated:
            rospy.wait_for_service("/base_controller/init")
            self.init_platform_srv = rospy.ServiceProxy("/base_controller/init", self.sys_mods['cob_srvs'].Trigger)

            response = self.init_platform_srv.call(self.sys_mods['cob_srvs'].TriggerRequest())
            while (not response.success.data):
                rospy.logerr("Failed to initialize the platform! Trying again...")
                time.sleep(2)
                response = self.init_platform_srv.call(self.sys_mods['cob_srvs'].TriggerRequest())

            self.move_action_client = self.sys_mods['actionlib'].SimpleActionClient("/move_base", self.sys_mods['move_base_msg'].MoveBaseAction)
            self.see.logger.loginfo("Waiting for /move_base action server...")
            self.move_action_client.wait_for_server()

            if self.PUBLISH_TO_ES:
                self.tfBuffer = self.sys_mods['tf2_ros'].Buffer()
                self.tfListener = self.sys_mods['tf2_ros'].TransformListener(self.tfBuffer)

                self.publish_position = True
                self.get_position_thread = self.see.thread_manager.startThread(
                    self.get_position)
                #self.get_position_thread = threading.Thread(target=self.get_position)
                #self.get_position_thread.start()

                time.sleep(0.5)
                self.publish_position = False

        else:
            i = 0
            for key in self.tcp_position.keys():
                self.tcp_position[key] = i
                i += 1

        self.see.logger.loginfo("MobilePlatfom SBRC initalised!")

        return True, start_as_idle

    def exec_Transport3DXYYaw(self, products, goal):
        (rx, ry, rz, rw) = self.sys_mods['transform'].quaternion_from_euler(0, 0, goal[2])
        self.start_execution_on_hardware([goal[0], goal[1], 0, rx, ry, rz, rw])

    def start_execution_on_hardware(self, program):

        if not self.simulated:

            ### HMI only - doesn't has to be afer implementig special configuration things
            #if self._condition.configuration == 'pos-kr6':
                #kr6_path = rospy.get_param('/script_server/base/KR6')
                #kr6_path.reverse()
                #program = kr6_path[1:len(kr6_path)] + [program]

            self.publish_position = True

            if isinstance(program[0], list):

                self.goal = {}
                for i in range(len(program)):
                    self.goal[i] = self.sys_mods['move_base_msg'].MoveBaseGoal()
                    self.goal[i].target_pose.header.frame_id = "map"
                    self.goal[i].target_pose.pose.position.x = program[i][0]
                    self.goal[i].target_pose.pose.position.y = program[i][1]
                    self.goal[i].target_pose.pose.position.z = program[i][2]
                    self.goal[i].target_pose.pose.orientation.x = program[i][3]
                    self.goal[i].target_pose.pose.orientation.y = program[i][4]
                    self.goal[i].target_pose.pose.orientation.z = program[i][5]
                    self.goal[i].target_pose.pose.orientation.w = program[i][6]

                self._active_goal = None
                self._active_goal_num = None
                self._num_of_goals = len(self.goal)

                self.multiple_goals_thread = self.see.thread_manager.startThread(
                    self.execute_multiple_goals)
                #self.multiple_goals_thread = threading.Thread(target=self.execute_multiple_goals)
                #self.multiple_goals_thread.start()

            else:
                self.goal = self.sys_mods['move_base_msg'].MoveBaseGoal()
                self.goal.target_pose.header.frame_id = "map"
                self.goal.target_pose.pose.position.x = program[0]
                self.goal.target_pose.pose.position.y = program[1]
                self.goal.target_pose.pose.position.z = program[2]
                self.goal.target_pose.pose.orientation.x = program[3]
                self.goal.target_pose.pose.orientation.y = program[4]
                self.goal.target_pose.pose.orientation.z = program[5]
                self.goal.target_pose.pose.orientation.w = program[6]
                self.move_action_client.send_goal(self.goal, done_cb=self.move_base_done_callback, feedback_cb=self.move_base_feedback_callback)

        else:
            self.execute_simulated()
            if self.PUBLISH_TO_ES:
                self.see.logger.loginfo("Sending test Environement data: ")

                for i in range(0, 4):
                    self.mutex.acquire()
                    for key in self.tcp_position.keys():
                        self.tcp_position[key] = self.tcp_position[key] + 1
                    self.es_data_change = True
                    print(self.tcp_position)
                    self.mutex.release()
                    time.sleep(1)
                print("-----------------------------------------------")

            self.finished(True, False)

    def move_base_done_callback(self, status, result):
        #TODO Do review
        self.see.logger.loginfo("Action ended with status: " + str(status))
        if status == self.sys_mods['actionlib_msgs'].GoalStatus.SUCCEEDED:

            self.finished(True, False)
            self.publish_position = False
            if self.multiple_goals_thread:
                self.see.thread_manager.joinThread(self.multiple_goals_thread)
                #self.multiple_goals_thread.join(0)
                self.multiple_goals_thread = None
        else:
            self.move_action_client.send_goal(self.goal, done_cb=self.move_base_done_callback, feedback_cb=self.move_base_feedback_callback)

    def move_base_feedback_callback(self, feedback):
        self.see.logger.logdebug(rospy.get_name() + ": position is: " + str(feedback.base_position.pose.position) + "and orientation is: " + str(feedback.base_position.pose.orientation))
        #TODO: Calculate some cool value for feedback
        self.feedback(5)

    def get_position(self):
        rate = rospy.Rate(2)
        while not rospy.is_shutdown():
            if self.publish_position:
                try:
                    trans = self.tfBuffer.lookup_transform('map', 'base_link', rospy.Time(0))
                    self.mutex.acquire()
                    self.frame = trans.header.frame_id
                    self.tcp_position['x'] = trans.transform.translation.x
                    self.tcp_position['y'] = trans.transform.translation.y
                    self.tcp_position['z'] = trans.transform.translation.z
                    (rx, ry, rz) = self.sys_mods['transform'].euler_from_quaternion((trans.transform.rotation.x, trans.transform.rotation.y, trans.transform.rotation.z, trans.transform.rotation.w))
                    self.tcp_position['rx'] = rx
                    self.tcp_position['ry'] = ry
                    self.tcp_position['rz'] = rz
                    self.es_data_change = True
                    self.mutex.release()

                except (self.sys_mods['tf2_ros'].LookupException, self.sys_mods['tf2_ros'].ConnectivityException, self.sys_mods['tf2_ros'].ExtrapolationException):
                    continue
            rate.sleep()

    def execute_multiple_goals(self):
        rate = rospy.Rate(10)
        i = 0
        self.multiple_finished = True
        distance_limit = 0.3

        while not rospy.is_shutdown():
            if self._active_goal != None and self._active_goal_num != (self._num_of_goals-1):
                distance = math.sqrt(pow(self.tcp_position['x'] - self._active_goal.target_pose.pose.position.x, 2) + pow(self.tcp_position['y'] - self._active_goal.target_pose.pose.position.y, 2))

                if distance <= distance_limit:
                    self.see.logger.loginfo('Distance smaller than ' + str(distance_limit))
                    self.multiple_finished = True

            if self.multiple_finished:
                self.multiple_finished = False
                self._active_goal = self.goal[i]
                self._active_goal_num = i
                self.see.logger.loginfo('Sending goal ' + str(self._active_goal_num) + ' of ' + str(self._num_of_goals) + ': ' + str(self._active_goal))
                if i < self._num_of_goals-1:
                    callback = self.intermediate_move_base_done_callback
                else:
                    callback = self.move_base_done_callback
                self.move_action_client.send_goal(self._active_goal,
                                                  done_cb=callback)
                i += 1

            rate.sleep()

    def intermediate_move_base_done_callback(self, status, result):
        if (status == self.sys_mods['actionlib_msgs'].GoalStatus.SUCCEEDED):
            self.see.logger.loginfo('Successfully finished the goal ' +
                                    str(self._active_goal_num+1) + ' of ' +
                                    str(self._num_of_goals))
        else:
            self.move_action_client.send_goal(
                self._active_goal,
                done_cb=self.intermediate_move_base_done_callback)


class IPR_Conveyor_SBRC_ROS(SkillBasedResourceController_ROS):

    SYSTEM_IMPORTS = {'actionlib': 'actionlib',
                      'conveyor_msgs': 'ipr_conveyor_belt.msg',
                      'actionlib_msgs': 'actionlib_msgs.msg'}

    SKILLS = [
        ['machine', 'Load'],
        ['machine', 'Unload'],
        ['machine', 'Transport1DDistanceSpeed']
        ]

    PUBLISH_TO_ES = True
    ES_DATA_TYPE = 'TCP_position'
    STATIC_POSITION = True

    def __init__(self, see, simulated):
        super(IPR_Conveyor_SBRC_ROS, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(IPR_Conveyor_SBRC_ROS, self).initalise(True)

        if not self.simulated:
            self.move_distance_ac = self.sys_mods['actionlib'].SimpleActionClient(
                "/ipr_conveyor/move_distance",
                self.sys_mods['conveyor_msgs'].MoveDistanceAction)
            self.see.logger.loginfo("Wait for /ipr_conveyor/move_distance action server...")
            self.move_distance_ac.wait_for_server()

            self.see.logger.loginfo("IPR_Conveyor_SBRC_ROS initalised!")
        else:
            i = 0
            for key in self.tcp_position.keys():
                self.tcp_position[key] = i
                i += 1

        return True, start_as_idle

    def exec_Transport1DDistanceSpeed(self, goal, speed):
        if not self.simulated:
            self.goal = self.sys_mods['conveyor_msgs'].MoveDistanceGoal()
            self.goal.distance = goal
            self.goal.speed = speed
            print("Sending goal: " + str(goal) + " with speed: " + str(speed))
            self.move_distance_ac.send_goal(
                self.goal,
                done_cb=self.move_distance_done_callback,
                feedback_cb=self.move_distance_feedback_callback)
        else:
            self.execute_simulated()
            self.finished(True, False)

    def move_distance_done_callback(self, status, result):
        if status == self.sys_mods['actionlib_msgs'].GoalStatus.SUCCEEDED:
            self.see.logger.loginfo("Goal achieved with error: " +
                                    str(result.distance_to_goal))
            self.finished(True, False)

    def move_distance_feedback_callback(self, feedback):
        self.see.logger.loginfo("Distance to goal: " +
                                str(feedback.distance_to_goal) +
                                " and time to goal: " +
                                str(feedback.seconds_to_goal))
        self.feedback(feedback.seconds_to_goal)


class KR5_SBRC_ROS(SkillBasedResourceController_ROS):

    SYSTEM_IMPORTS = {'tf2_ros': 'tf2_ros',
                      'transform': 'tf.transformations',
                      'std_msgs': 'std_msgs.msg',
                      'std_srvs': 'std_srvs.srv',
                      'sensor_msgs': 'sensor_msgs.msg',
                      'viz_serv_srvs': 'viz_serv.srv',
                      }

    SKILLS = [
        ['machine', 'RecognizeGrip'],
        ['machine', 'Transport6D'],
        ['machine', 'Transport6DSpeed'],
        ['machine', 'Unload'],
        ['machine', 'Release'],
        ['machine', 'Move6D'],
        ]

    PUBLISH_TO_ES = True
    ES_DATA_TYPE = 'joint_values'       # 'TCP_position']
    STATIC_POSITION = False

    IMPORTANT_COORDINATES = {
        'neutral': ('base_link', [1043, 640, 510, 0, 0, -90]),
        'soldering_input': ('base_link', [1043,345, 510, 0, 0, -90]),
        'soldering_output': ('base_link', [1043, -46, 510, 0, 0, -90]),
        'mobile_platform': ('base_link', [58, 1260, 833.1, 0, 0, -90]),
        'mobile_platform_between': ('base_link', [500, 500, 833.1, 0, 0, -90])
    }

    def __init__(self, see, simulated):
        super(KR5_SBRC_ROS, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(KR5_SBRC_ROS, self).initalise(True)

        if not self.simulated:
            self.see.logger.loginfo("Waiting for service /vacuum_pim60/release ")
            rospy.wait_for_service("/vacuum_pim60/release")
            self.release_srv = rospy.ServiceProxy("/vacuum_pim60/release", self.sys_mods['std_srvs'].Trigger)

            rospy.wait_for_service("/kr5/track_and_grab")
            self.recognize_grip_srv = rospy.ServiceProxy("/kr5/track_and_grab", self.sys_mods['viz_serv_srvs'].Track_and_Grab)
            self.move_srv = rospy.ServiceProxy("/kr5/ptp", self.sys_mods['viz_serv_srvs'].PTP_move)

            rospy.Subscriber("/joint_states", self.sys_mods['sensor_msgs'].JointState, self.joint_states_update)
            #rospy.Subscriber("/arm_controller/cartesian_state", self.sys_mods['sensor_msgs'].JointState, self.cartesian_state_update)

            #self.tfBuffer = self.sys_mods['tf2_ros'].Buffer()
            #self.tfListener = self.sys_mods['tf2_ros'].TransformListener(self.tfBuffer)

        self.see.logger.loginfo("KR5_SBRC_ROS initalised!")

        return True, start_as_idle

    def joint_states_update(self, msg):
        self.joint_values = msg.position

    def cartesian_state_update(self, msg):
        self.tcp_position = msg.position

    def _send_to_move_srv(self, goal, speed):

        if self.see.condition.configuration == 'soldering_output' and self.skill.postCondition.configuration == 'mobile_platform':
            req = self.sys_mods['viz_serv_srvs'].PTP_moveRequest()
            req.pose.data = self.IMPORTANT_COORDINATES['mobile_platform_between'][1]
            req.velocity_hor = speed
            req.velocity_vert = speed
            self.move_srv.call(req)

        req = self.sys_mods['viz_serv_srvs'].PTP_moveRequest()
        req.pose.data = goal
        req.velocity_hor = speed
        req.velocity_vert = speed

        if self.move_srv.call(req):
            self.finished(True, False)
        else:
            self.finished(False, False)

    def exec_RecognizeGrip(self, object, products):
        self.recognize_grip_srv.call(self.sys_mods['viz_serv_srvs'].Track_and_GrabRequest())
        self.finished(True, False)

    def exec_Move6D(self, goal):
        self._send_to_move_srv(goal, 0)

    def exec_Transport6D(self, goal):
        self._send_to_move_srv(goal, 0)

    def exec_Transport6DSpeed(self, goal, speed):
        self._send_to_move_srv(goal, speed)

    def exec_Unload(self, products):
        self.exec_Release(products)
        self.finished(True, False)

    def exec_Release(self, products):
        self.release_srv.call(self.sys_mods['std_srvs'].TriggerRequest())
        self.finished(True, False)


class IPR_SimulatedSoldering_SBRC_ROS(SkillBasedResourceController_ROS):

    SYSTEM_IMPORTS = {'actionlib': 'actionlib',
                      'actionlib_msgs': 'actionlib_msgs.msg',
                      'led_msgs': 'iirob_led.msg'
                      }

    SKILLS = [
        ['lab_scenario', 'Soldering']
        ]

    PUBLISH_TO_ES = True
    ES_DATA_TYPE = 'TCP_position'
    STATIC_POSITION = True

    def __init__(self, see, simulated):
        super(IPR_SimulatedSoldering_SBRC_ROS, self).__init__(see, simulated)

    def initalise(self):
        start_as_idle = super(IPR_SimulatedSoldering_SBRC_ROS, self).initalise(True)

        if not self.simulated:
            self.play_led_ac = self.sys_mods['actionlib'].SimpleActionClient(
                "/leds/running_bunny", self.sys_mods['led_msgs'].RunningBunnyAction)
            self.play_led_ac.wait_for_server()

            self.see.logger.loginfo("IPR_SimulatedSoldering_SBRC_ROS initalised!")

        return True, start_as_idle

    def exec_Soldering(self, zone_temperatures, conveyor_speed):
        goal = self.sys_mods['led_msgs'].RunningBunnyGoal()
        goal.color.r = 1
        goal.color.g = 0
        goal.color.b = 1
        goal.head = 1
        goal.body = 5
        goal.num_circles = 5
        goal.skip_leds_per_step = 1
        self.play_led_action_client.send_goal(goal, done_cb=self.action_done,
                                              feedback_cb=self.action_feedback)

    def action_done(self, status, result):
        if status == self.sys_mods['actionlib_msgs'].GoalStatus.SUCCEEDED:
            self.finished(True, False)
        else:
            self.finished(False, False)

    def action_feedback(self, feedback):
        self.feedback(3)
