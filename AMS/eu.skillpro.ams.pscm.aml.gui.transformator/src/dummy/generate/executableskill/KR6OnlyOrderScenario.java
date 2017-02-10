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

public class KR6OnlyOrderScenario extends HMIScenario{
	
	private static final KR6OnlyOrderScenario INSTANCE = new KR6OnlyOrderScenario();
	
	public static KR6OnlyOrderScenario getInstance() {
		return INSTANCE;
	}
	
	public String getName() {
		return "KR6 scenario";
	}
	
	@Override
	public String getSuffix() {
		return "SKrsix";
	}
	
	@Override
	public List<String> getGoalSkills() {
		return addIDSuffixToGoalskills(Arrays.asList("ID_GripperReleaseLego"));
	}
	
	@Override
	protected List<ExecutableSkillDummy> getScenarioSkills() {
		if (executableSkillList == null) {
			List<ExecutableSkillDummy> list = new ArrayList<>();
			
			list.add(createGoToLegoPositionExecutableSkill());
			list.add(createGripperPicksLegoExecutableSkill());
			list.add(createPlatformGoesToKR6ExecutableSkill());
			list.add(createGoesToPlatformPositionExecutableSkill());
			list.add(createKR6GoesFromPlatformToLegoExecutableSkill());
			list.add(createKR6GripperReleaseLegoExecutableSkill());
			
			list.addAll(createEmptyBoxToggle());
			list.addAll(createPlatformFromToNeutral(ConditionProduct.BOX_EMPTY));
			list.addAll(createEmptyKR6ToNeutral());
			
			list = addIDSuffixToSkills(list);
			executableSkillList = list;
		}
		return new ArrayList<>(executableSkillList);
	}
	
	private ExecutableSkillDummy createGoToLegoPositionExecutableSkill() {
		String id = "GoTo"+LEGO_COLOR_TOP_NAME+"LegoPosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "RecognizeGoToPosition");
		res1.setDuration(30);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.addProperty("LegoColor", "string", "HexaColor", LEGO_COLOR_TOP);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGripperPicksLegoExecutableSkill() {
		String id = "GripperPicksLego";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "Waiting");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "3");
		
		ResourceExecutableSkillDummy res2 = dummy.add("KR6-"+id, KR6_GRIPPER, "Picking");
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.LEGO_TOP);
		
		return dummy;
	}
	
	private ExecutableSkillDummy createPlatformGoesToKR6ExecutableSkill() {
		String id = "PlatformGoesToKR6";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
		res1.setDuration(30);
		res1.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_KR6, ConditionProduct.BOX_EMPTY);
		res1.addProperty("Position", "string", "", "KR6");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createGoesToPlatformPositionExecutableSkill() {
		String id = "GoesToPlatformPosition";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "TransportSemantic");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_PLATFORM, ConditionProduct.EMPTY);
		res1.addProperty("SemanticPosition", "string", "", "AboveThePlatform");
		
		ResourceExecutableSkillDummy res2 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(ConditionConfiguration.POS_KR6, ConditionProduct.BOX_EMPTY);
		res2.setPostCondition(ConditionConfiguration.POS_KR6, ConditionProduct.BOX_EMPTY);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res3 = dummy.add("Gripper-"+id, KR6_GRIPPER, "Waiting");
		res3.setDuration(1);
		res3.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.LEGO_TOP);
		res3.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.TRANSFORMED_LEGO);
		res3.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createKR6GoesFromPlatformToLegoExecutableSkill() {
		String id = "KR6GoesBackToLego";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "TransportSemantic");
		res1.setDuration(3);
		res1.setPreCondition(ConditionConfiguration.POS_PLATFORM, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.addProperty("SemanticPosition", "string", "", ConditionConfiguration.POS_LEGO_TOP.toString());
		
		ResourceExecutableSkillDummy res2 = dummy.add("KR6-Gripper-"+id, KR6_GRIPPER, "Waiting");
		res2.setDuration(1);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.TRANSFORMED_LEGO);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.TRANSFORMED_LEGO);
		res2.addProperty("Duration", "integer", "sec", "1");
		
		return dummy;
	}
	
	private ExecutableSkillDummy createKR6GripperReleaseLegoExecutableSkill() {
		String id = "GripperReleaseLego";
		ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
		
		ResourceExecutableSkillDummy res1 = dummy.add("KR6-"+id, KR6, "Waiting");
		res1.setDuration(1);
		res1.setPreCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.setPostCondition(ConditionConfiguration.POS_LEGO_TOP, ConditionProduct.EMPTY);
		res1.addProperty("Duration", "integer", "sec", "1");
		
		ResourceExecutableSkillDummy res2 = dummy.add("KR6-Gripper-"+id, KR6_GRIPPER, "Release");
		res2.setDuration(0);
		res2.setPreCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.TRANSFORMED_LEGO);
		res2.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
		
		return dummy;
	}
}