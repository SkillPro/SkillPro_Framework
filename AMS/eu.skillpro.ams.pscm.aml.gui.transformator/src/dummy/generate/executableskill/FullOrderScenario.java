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

import static skillpro.model.skills.dummy.ConditionConfiguration.NEUTRAL;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_BOX;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_CHOCOLATE;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_KR6;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_LEGO_BOTTOM;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_LEGO_MIDDLE;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_LEGO_TOP;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_PLATFORM;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_UR5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import skillpro.model.skills.dummy.ConditionConfiguration;
import skillpro.model.skills.dummy.ConditionProduct;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;

public class FullOrderScenario extends HMIScenario{
	
	private static final FullOrderScenario INSTANCE = new FullOrderScenario();
	
	public static FullOrderScenario getInstance() {
		return INSTANCE;
	}
	
	public String getName() {
		return "Full order scenario";
	}
	
	@Override
	public String getSuffix() {
		return "SFull";
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
				list.add(createHumanFoldsNewBoxExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			
			// Platform goes to KR6, and gets the three legos
			list.add(createPlatformGoesToKR6ExecutableSkill());
			//first:
			list.add(createGoToLegoPositionExecutableSkill(POS_LEGO_TOP, LEGO_COLOR_TOP, LEGO_COLOR_TOP_NAME));
			list.add(createGripperPicksLegoExecutableSkill(POS_LEGO_TOP, ConditionProduct.LEGO_TOP));
			list.add(createGoesToPlatformPositionExecutableSkill(POS_LEGO_TOP, ConditionProduct.LEGO_TOP, ConditionProduct.EMPTY));
			list.add(createKR6GripperReleaseLegoExecutableSkill(ConditionProduct.LEGO_TOP, ConditionProduct.EMPTY, ConditionProduct.ONE_LEGO));
			//second:
			list.add(createGoToLegoPositionExecutableSkill(POS_LEGO_MIDDLE, LEGO_COLOR_MIDDLE, LEGO_COLOR_MIDDLE_NAME));
			list.add(createGripperPicksLegoExecutableSkill(POS_LEGO_MIDDLE, ConditionProduct.LEGO_MIDDLE));
			list.add(createGoesToPlatformPositionExecutableSkill(POS_LEGO_MIDDLE, ConditionProduct.LEGO_MIDDLE, ConditionProduct.ONE_LEGO));
			list.add(createKR6GripperReleaseLegoExecutableSkill(ConditionProduct.LEGO_MIDDLE, ConditionProduct.ONE_LEGO, ConditionProduct.TWO_LEGOS));
			//third
			list.add(createGoToLegoPositionExecutableSkill(POS_LEGO_BOTTOM, LEGO_COLOR_BOTTOM, LEGO_COLOR_BOTTOM_NAME));
			list.add(createGripperPicksLegoExecutableSkill(POS_LEGO_BOTTOM, ConditionProduct.LEGO_BOTTOM));
			list.add(createGoesToPlatformPositionExecutableSkill(POS_LEGO_BOTTOM, ConditionProduct.LEGO_BOTTOM, ConditionProduct.TWO_LEGOS));
			list.add(createKR6GripperReleaseLegoExecutableSkill(ConditionProduct.LEGO_BOTTOM, ConditionProduct.TWO_LEGOS, ConditionProduct.THREE_LEGOS));
			
			// Human swaps box and legos
			for (WorkplaceInfo wpi : workplaces) {
				list.add(createThreeLegosPlatformToWPExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createPutEmptyBoxOnPlatformAndTakeLegosExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createAssembleIntoFlagExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			
			//chocolate
			list.add(createProduceChocolateExecutableSkill());
			list.add(createGiveChocolateToUR5ExecutableSkill());
			
			list.add(createGoToChocolatePositionExecutableSkill());
			list.add(createGripperPicksChocolateExecutableSkill());
			list.add(createPlatformGoesToUR5ExecutableSkill());
			list.add(createGoesToBoxPositionExecutableSkill());
			list.add(createUR5GripperReleaseChocolateExecutableSkill());
			for (WorkplaceInfo wpi : workplaces) {
				list.add(createPlatformToWPExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			list.add(createUR5GoesBackToNeutralExecutableSkill());
			for (WorkplaceInfo wpi : workplaces) {
				list.add(createHumanTakesChocolateExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createHumanFinishesBoxExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
				list.add(createHumanLogoutExecutableSkill(wpi.e1, wpi.e2, wpi.e3));
			}
			
			list.addAll(createEmptyBoxToggle());
			list.addAll(createPlatformFromToNeutral(ConditionProduct.EMPTY));
			list.addAll(createPlatformFromToNeutral(ConditionProduct.BOX_EMPTY));
			list.addAll(createEmptyUR5ToNeutral());
			list.addAll(createEmptyKR6ToNeutral());
			
			list = addIDSuffixToSkills(list);
			executableSkillList = list;
		}
		return new ArrayList<>(executableSkillList);
	}
	
	private ExecutableSkillDummy createThreeLegosPlatformToWPExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "ThreeLegosPlatformTo" + workplaceName;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res.setDuration(5);
		res.setPreCondition(POS_KR6, ConditionProduct.THREE_LEGOS);
		res.setPostCondition(workplacePosition, ConditionProduct.THREE_LEGOS);
		res.addProperty("Position", "string", "", workplaceName);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createHumanFoldsNewBoxExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "FoldsBox";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(NEUTRAL, ConditionProduct.BOX_UNFOLDED);
		res1.setPostCondition(NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.addProperty("Instruction", "string", "", "Please fold a new box!");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPutEmptyBoxOnPlatformAndTakeLegosExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "HumanPutsEmptyBoxOnPlatformAndTakesLegos";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-" + id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.setPostCondition(NEUTRAL, ConditionProduct.THREE_LEGOS);
		res1.addProperty("Instruction", "string", "", "Please take your lego pieces, and put the new box on the mobile platform!");
		
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(workplacePosition, ConditionProduct.THREE_LEGOS);
		res2.setPostCondition(workplacePosition, ConditionProduct.BOX_EMPTY);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoToChocolatePositionExecutableSkill() {
		String id = "GoTo$chocolate-color$ChocolatePosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "RecognizeGoToPosition");
		res1.setDuration(30);
		res1.setPreCondition(NEUTRAL, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.addProperty("Color", "string", "", CHOCOLATE_COLOR);
		return dummy;
	}
	
	private ExecutableSkillDummy createGripperPicksChocolateExecutableSkill() {
		String id = "GripperPicksChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Waiting");
		res1.setDuration(3);
		res1.setPreCondition(POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.addProperty("Duration", "integer", "sec", "3");
		
		ResourceExecutableSkillDummy res2 = dummy.add("UR5-Gripper-"+id, UR5_GRIPPER, "Picking");
		res2.setDuration(0);
		res2.setPreCondition(NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(NEUTRAL, ConditionProduct.CHOCOLATE);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformGoesToUR5ExecutableSkill() {
		String id = "PlatformGoesToUR5";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res1.setDuration(30);
		res1.setPreCondition(NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.setPostCondition(POS_UR5, ConditionProduct.BOX_EMPTY);
		res1.addProperty("Position", "string", "", "UR5");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoesToBoxPositionExecutableSkill() {
		String id = "GoesToBoxPosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-GoesToBoxPosition", UR5, "Transport6D");
		res1.setDuration(3);
		res1.setPreCondition(POS_CHOCOLATE, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(POS_BOX, ConditionProduct.CHOCOLATE);
		res1.addProperty("Position", "string", "", "place");
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-GoesToBoxPosition", MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(POS_UR5, ConditionProduct.BOX_EMPTY);
		res2.setPostCondition(POS_UR5, ConditionProduct.BOX_EMPTY);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GripperReleaseChocolateExecutableSkill() {
		String id = "GripperReleaseChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Waiting");
		res1.setDuration(1);
		res1.setPreCondition(POS_BOX, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(POS_BOX, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res2 = dummy.add("UR5-Gripper-"+id, UR5_GRIPPER, "Release");
		res2.setDuration(0);
		res2.setPreCondition(NEUTRAL, ConditionProduct.CHOCOLATE);
		res2.setPostCondition(NEUTRAL, ConditionProduct.EMPTY);
		
		ResourceExecutableSkillDummy res3 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res3.setDuration(5);
		res3.setPreCondition(POS_UR5, ConditionProduct.BOX_EMPTY);
		res3.setPostCondition(POS_UR5, ConditionProduct.BOX_WITH_CHOCOLATE);
		res3.addProperty("Duration", "integer", "sec", "5");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformToWPExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "PlatformTo" + workplaceName;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res.setDuration(5);
		res.setPreCondition(POS_UR5, ConditionProduct.BOX_WITH_CHOCOLATE);
		res.setPostCondition(workplacePosition, ConditionProduct.BOX_WITH_CHOCOLATE);
		res.addProperty("Position", "string", "", workplaceName);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GoesBackToNeutralExecutableSkill() {
		String id = "UR5GoesBackToNeutral";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("UR5-"+id, UR5, "Transport6D");
		res1.setDuration(3);
		res1.setPreCondition(POS_BOX, ConditionProduct.EMPTY);
		res1.setPostCondition(NEUTRAL, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "basepos");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createHumanTakesChocolateExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "TakesChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(3);
		res1.setPreCondition(NEUTRAL, ConditionProduct.FLAG);
		res1.setPostCondition(NEUTRAL, ConditionProduct.FLAG_AND_BOX_WITH_CHOCOLATE);
		res1.addProperty("Instruction", "string", "", "Please take your box with the chocolate.");
		
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
		res1.setPreCondition(NEUTRAL, ConditionProduct.BOX_FINISHED);
		res1.setPostCondition(NEUTRAL, ConditionProduct.BOX_UNFOLDED);
		res1.addProperty("Instruction", "string", "", "Thank you and enjoy! Please log out from the system now.");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createProduceChocolateExecutableSkill() {
		String id = "ProduceChocolate:chocolate-" + CHOCOLATE_COLOR;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Simulation-"+id, SIMULATION, "Produce");
		res1.setDuration(60);
		res1.setPreCondition(NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(NEUTRAL, ConditionProduct.CHOCOLATE);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGiveChocolateToUR5ExecutableSkill() {
		String id = "Give$chocolate-color$ChocolateToUR5";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Simulation-"+id, SIMULATION, "Simulate");
		res1.setDuration(1);
		res1.setPreCondition(NEUTRAL, ConditionProduct.CHOCOLATE);
		res1.setPostCondition(NEUTRAL, ConditionProduct.EMPTY);
		
		ResourceExecutableSkillDummy res2 = dummy.add("UR5-"+id, UR5, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(NEUTRAL, ConditionProduct.CHOCOLATE);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformGoesToKR6ExecutableSkill() {
		String id = "PlatformGoesToKR6";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res1.setDuration(30);
		res1.setPreCondition(NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(POS_KR6, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "KR6");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoToLegoPositionExecutableSkill(ConditionConfiguration position, String legoColor, String legoColorName) {
		String id = "GoTo" + position + "Lego" + legoColorName + "Position";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "RecognizeGoToPosition");
		res1.setDuration(30);
		res1.setPreCondition(NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(position, ConditionProduct.EMPTY);
		res1.addProperty("LegoColor", "string", "HexaColor", legoColor);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGripperPicksLegoExecutableSkill(ConditionConfiguration position, ConditionProduct product) {
		String id = "Gripper"+position+"PicksLego";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "Waiting");
		res1.setDuration(3);
		res1.setPreCondition(position, ConditionProduct.EMPTY);
		res1.setPostCondition(position, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "3");
		
		ResourceExecutableSkillDummy res2 = dummy.add("KR6-Gripper-"+id, KR6_GRIPPER, "Picking");
		res2.setDuration(0);
		res2.setPreCondition(NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(NEUTRAL, product);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoesToPlatformPositionExecutableSkill(ConditionConfiguration position, ConditionProduct product, ConditionProduct platformConfiguration) {
		String id = "GoesFrom"+position+"ToPlatformPosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "TransportSemantic");
		res1.setDuration(3);
		res1.setPreCondition(position, ConditionProduct.EMPTY);
		res1.setPostCondition(POS_PLATFORM, ConditionProduct.EMPTY);
		res1.addProperty("SemanticPosition", "string", "", "AboveThePlatform");
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(POS_KR6, platformConfiguration);
		res2.setPostCondition(POS_KR6, platformConfiguration);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res3 = dummy.add("KR6-Gripper-"+id, KR6_GRIPPER, "Waiting");
		res3.setDuration(1);
		res3.setPreCondition(NEUTRAL, product);
		res3.setPostCondition(NEUTRAL, product);
		res3.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createKR6GripperReleaseLegoExecutableSkill(ConditionProduct product, ConditionProduct platformBefore, ConditionProduct platformAfter) {
		String id = "GripperRelease"+product+"Lego";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "Waiting");
		res1.setDuration(1);
		res1.setPreCondition(ConditionConfiguration.POS_PLATFORM, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_PLATFORM, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res2 = dummy.add("KR6-Gripper-"+id, KR6_GRIPPER, "Release");
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, product);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		
		ResourceExecutableSkillDummy res3 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res3.setDuration(0);
		res3.setPreCondition(ConditionConfiguration.POS_KR6, platformBefore);
		res3.setPostCondition(ConditionConfiguration.POS_KR6, platformAfter);
		res3.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createAssembleIntoFlagExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "AssembleIntoFlag" + workplaceName + "-" + LEGO_COLOR_TOP_NAME + "-" + LEGO_COLOR_MIDDLE_NAME + "-" + LEGO_COLOR_BOTTOM_NAME;
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "BuildLegoFlag");
		res1.setDuration(10);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.THREE_LEGOS);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.FLAG);
		res1.addProperty("Instruction", "string", "", "Please build a flag from your legos.");
		res1.addProperty("Colors", "string", "", LEGO_COLOR_TOP_NAME + "-" + LEGO_COLOR_MIDDLE_NAME + "-" + LEGO_COLOR_BOTTOM_NAME);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createHumanFinishesBoxExecutableSkill(String workplaceName, ResourceDummy humanResource, ConditionConfiguration workplacePosition) {
		String id = "Human" + workplaceName + "FinishesBox";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("Human-"+id, humanResource, "Instruction");
		res1.setDuration(10);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.FLAG_AND_BOX_WITH_CHOCOLATE);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_FINISHED);
		res1.addProperty("Instruction", "string", "", "Please put your flag into the box, then close the box and put sticker on it.");
		
		return dummy;
	}
}