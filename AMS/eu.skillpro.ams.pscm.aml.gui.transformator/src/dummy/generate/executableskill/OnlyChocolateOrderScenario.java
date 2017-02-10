/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: Production System Configuration Manager (PSCM)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the AMS (Asset Management System), which has been developed
 * at the PDE department of the FZI, Karlsruhe. It is part of the SkillPro Framework,
 * which is is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733).
 *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework. If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

package dummy.generate.executableskill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import skillpro.model.skills.dummy.ConditionConfiguration;
import skillpro.model.skills.dummy.ConditionProduct;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;

public class OnlyChocolateOrderScenario extends HMIScenario{
	
	private static final OnlyChocolateOrderScenario INSTANCE = new OnlyChocolateOrderScenario();
	
	public static OnlyChocolateOrderScenario getInstance() {
		return INSTANCE;
	}
	
	public String getName() {
		return "Chocolate scenario";
	}
	
	@Override
	public String getSuffix() {
		return "SChoc";
	}
	
	@Override
	public List<String> getGoalSkills() {
		return addIDSuffixToGoalskills(Arrays.asList("ID_HumanWP1Logout", "ID_HumanWP2Logout", "ID_HumanWP3Logout"));
	}
	
	@Override
	protected List<ExecutableSkillDummy> getScenarioSkills() {
		if (executableSkillList == null) {
			List<ExecutableSkillDummy> list = new ArrayList<>();
			for (WorkplaceInfo wpi : workplaces) {
				list.add(createEmptyPlatformToWPExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createHumanFoldsNewBoxExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createPutEmptyBoxOnPlatformExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			list.add(createProduceChocolateExecutableSkill());
			list.add(createGiveChocolateToUR5ExecutableSkill());
			
			list.add(createGoToChocolatePositionExecutableSkill());
			list.add(createGripperPicksChocolateExecutableSkill());
			list.add(createPlatformGoesToUR5ExecutableSkill());
			list.add(createGoesToBoxPositionExecutableSkill());
			list.add(createUR5GripperReleaseChocolateExecutableSkill());
			for (WorkplaceInfo wpi : workplaces) {
				list.add(createPlatformToWP1ExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			list.add(createUR5GoesBackToNeutralExecutableSkill());
			for (WorkplaceInfo wpi : workplaces) {
				list.add(createHumanTakesChocolateExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createHumanLogoutExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createMoveEmptyPlatformFromWP1ToNeutralExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			
			list.addAll(createEmptyBoxToggle());
			list.addAll(createPlatformFromToNeutral(ConditionProduct.EMPTY));
			list.addAll(createPlatformFromToNeutral(ConditionProduct.BOX_EMPTY));
			list.addAll(createEmptyUR5ToNeutral());
			
			list = addIDSuffixToSkills(list);
			executableSkillList = list;
		}
		return new ArrayList<>(executableSkillList);
	}
	
	private ExecutableSkillDummy createEmptyPlatformToWPExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "EmptyPlatformTo" + workplaceName;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res.setDuration(5);
		res.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res.setPostCondition(workplacePosition, ConditionProduct.EMPTY);
		res.addProperty("Position", "string", "", workplaceName);
		
		return dummy;
	}
	private ExecutableSkillDummy createHumanFoldsNewBoxExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "FoldsBox";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_UNFOLDED);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.addProperty("Instruction", "string", "", "Please fold a new box!");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPutEmptyBoxOnPlatformExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "HumanPutsEmptyBoxOnPlatform";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.addProperty("Instruction", "string", "", "Please put the box on the mobile platform!");
		
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(workplacePosition, ConditionProduct.EMPTY);
		res2.setPostCondition(workplacePosition, ConditionProduct.BOX_EMPTY);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createMoveEmptyPlatformFromWP1ToNeutralExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "MoveEmptyPlatformFrom" + workplaceName + "ToNeutral";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res1.setDuration(30);
		res1.setPreCondition(workplacePosition, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "NEUT");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoToChocolatePositionExecutableSkill() {
		String id = "GoTo$chocolate-color$ChocolatePosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "RecognizeGoToPosition");
		res1.setDuration(30);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.addProperty("Color", "string", "", CHOCOLATE_COLOR);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGripperPicksChocolateExecutableSkill() {
		String id = "GripperPicksChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Waiting");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.addProperty("Duration", "integer", "sec", "3");
		
		ResourceExecutableSkillDummy res2 = dummy.add("UR5-Gripper-"+id, UR5_GRIPPER, "Picking");
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformGoesToUR5ExecutableSkill() {
		String id = "PlatformGoesToUR5";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res1.setDuration(30);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res1.addProperty("Position", "string", "", "UR5");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoesToBoxPositionExecutableSkill() {
		String id = "GoesToBoxPosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Transport6D");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.POS_BOX, ConditionProduct.CHOCOLATE);
		res1.addProperty("Position", "string", "", "place");
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res2.setPostCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GripperReleaseChocolateExecutableSkill() {
		String id = "GripperReleaseChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Waiting");
		res1.setDuration(1);
		res1.setPreCondition(ConditionConfiguration.POS_BOX, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.POS_BOX, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res2 = dummy.add("UR5-Gripper-"+id, UR5_GRIPPER, "Release");
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		
		
		ResourceExecutableSkillDummy res3 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res3.setDuration(5);
		res3.setPreCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res3.setPostCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_WITH_CHOCOLATE);
		res3.addProperty("Duration", "integer", "sec", "5");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformToWP1ExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "PlatformTo" + workplaceName;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res.setDuration(5);
		res.setPreCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_WITH_CHOCOLATE);
		res.setPostCondition(workplacePosition, ConditionProduct.BOX_WITH_CHOCOLATE);
		res.addProperty("Position", "string", "", workplaceName);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GoesBackToNeutralExecutableSkill() {
		String id = "UR5GoesBackToNeutral";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Transport6D");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_BOX, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "basepos");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createHumanTakesChocolateExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "TakesChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_WITH_CHOCOLATE);
		res1.addProperty("Instruction", "string", "", "Please take your box with the chocolate, and put a SkillPro sticker on it.");
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(workplacePosition, ConditionProduct.BOX_WITH_CHOCOLATE);
		res2.setPostCondition(workplacePosition, ConditionProduct.EMPTY);
		res2.addProperty("Duration", "string", "sec", "1");
		
		return dummy;
	}
	
	
	
	private ExecutableSkillDummy createHumanLogoutExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "Logout";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_WITH_CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_UNFOLDED);
		res1.addProperty("Instruction", "string", "", "Thank you and enjoy! Please log out from the system now.");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createProduceChocolateExecutableSkill() {
		String id = "ProduceChocolate:chocolate-" + CHOCOLATE_COLOR;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Simulation-"+id, SIMULATION, "Produce");
		res1.setDuration(60);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGiveChocolateToUR5ExecutableSkill() {
		String id = "Give$chocolate-color$ChocolateToUR5";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Simulation-"+id, SIMULATION, "Simulate");
		res1.setDuration(1);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		
		ResourceExecutableSkillDummy res2 = dummy.add("UR5-"+id, UR5, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
}