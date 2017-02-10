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
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;

public class UR5OnlyOrderScenario extends HMIScenario{
	
	private static final UR5OnlyOrderScenario INSTANCE = new UR5OnlyOrderScenario();
	
	public static UR5OnlyOrderScenario getInstance() {
		return INSTANCE;
	}
	
	public String getName() {
		return "UR5 scenario";
	}
	
	@Override
	public String getSuffix() {
		return "SUrfive";
	}
	
	@Override
	public List<String> getGoalSkills() {
		return addIDSuffixToGoalskills(Arrays.asList("ID_GripperReleaseChocolate"));
	}
	
	@Override
	protected List<ExecutableSkillDummy> getScenarioSkills() {
		if (executableSkillList == null) {
			List<ExecutableSkillDummy> list = new ArrayList<>();
			list.add(createGoToChocolatePositionExecutableSkill());
			list.add(createGripperPicksChocolateExecutableSkill());
			list.add(createPlatformGoesToUR5ExecutableSkill());
			list.add(createGoesToPlatformPositionExecutableSkill());
			list.add(createUR5GoesBackToNeutralExecutableSkill());
			list.add(createUR5GoesFromChocolateToNeutralExecutableSkill());
			list.add(createUR5GripperReleaseChocolateExecutableSkill());
			list.addAll(createEmptyBoxToggle());
			list.addAll(createPlatformFromToNeutral(ConditionProduct.BOX_EMPTY));
			list.addAll(createEmptyUR5ToNeutral());
			
			list = addIDSuffixToSkills(list);
			checkIDs(list);
			checkConditions(list);
			executableSkillList = list;
		}
		return new ArrayList<>(executableSkillList);
	}
	
	private ExecutableSkillDummy createGoToChocolatePositionExecutableSkill() {
		String id = "GoToChocolatePosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "UR5-GoToChocolatePosition";
		String templateSkill = "RecognizeGoToPosition";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, UR5, templateSkill);
		res1.setDuration(30);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.addProperty("Color", "string", "", CHOCOLATE_COLOR);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGripperPicksChocolateExecutableSkill() {
		String id = "GripperPicksChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "UR5-GripperPicksChocolate";
		String templateSkill = "Waiting";
		
		String resourceExecutableSkill2 = "UR5-Gripper-GripperPicksChocolate";
		String templateSkill2 = "Picking";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, UR5, templateSkill);
		res1.setDuration(1);
		res1.setPreCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res2 = dummy.add(resourceExecutableSkill2, UR5_GRIPPER, templateSkill2);
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformGoesToUR5ExecutableSkill() {
		String id = "PlatformGoesToUR5";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "MobilePlatform-PlatformGoesToUR5";
		String templateSkill = "Transport3D";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, MOBILE_PLATFORM, templateSkill);
		res1.setDuration(30);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res1.addProperty("Position", "string", "", "UR5");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoesToPlatformPositionExecutableSkill() {
		String id = "GoesToPlatformPosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "UR5-GoesToPlatformPosition";
		String templateSkill = "Transport6D";
		
		String resourceExecutableSkill2 = "MobilePlatform-GoesToPlatformPosition";
		String templateSkill2 = "Waiting";
		
		String resourceExecutableSkill3 = "Gripper-GoesToPlatformPosition";
		
		String templateSkill3 = "Waiting";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, UR5, templateSkill);
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_BOX, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "place");
		
		ResourceExecutableSkillDummy res2 = dummy.add(resourceExecutableSkill2, MOBILE_PLATFORM, templateSkill2);
		res2.setDuration(1);
		res2.setPreCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res2.setPostCondition(ConditionConfiguration.POS_UR5, ConditionProduct.BOX_EMPTY);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res3 = dummy.add(resourceExecutableSkill3, UR5_GRIPPER, templateSkill3);
		res3.setDuration(1);
		res3.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.CHOCOLATE);
		res3.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.TRANSFORMEDCHOCOLATE);
		res3.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GripperReleaseChocolateExecutableSkill() {
		String id = "GripperReleaseChocolate";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "UR5-GripperReleaseChocolate";
		String templateSkill = "Waiting";
		
		String resourceExecutableSkill2 = "UR5-Gripper-GripperReleaseChocolate";
		String templateSkill2 = "Release";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, UR5, templateSkill);
		res1.setDuration(1);
		res1.setPreCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res2 = dummy.add(resourceExecutableSkill2, UR5_GRIPPER, templateSkill2);
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.TRANSFORMEDCHOCOLATE);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GoesBackToNeutralExecutableSkill() {
		String id = "UR5GoesBackToNeutral";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "UR5-UR5GoesBackToNeutral";
		String templateSkill = "Transport6D";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, UR5, templateSkill);
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_BOX, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "basepos");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createUR5GoesFromChocolateToNeutralExecutableSkill() {
		String id = "UR5GoesFromChocolateToNeutral";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		String resourceExecutableSkill = "UR5-UR5GoesFromChocolateToNeutral";
		String templateSkill = "Transport6D";
		
		ResourceExecutableSkillDummy res1 = dummy.add(resourceExecutableSkill, UR5, templateSkill);
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_CHOCOLATE, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.addProperty("Position", "string", "", "basepos");
		
		return dummy;
	}
}