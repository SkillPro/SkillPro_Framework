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
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_WP1;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_WP2;
import static skillpro.model.skills.dummy.ConditionConfiguration.POS_WP3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import skillpro.model.skills.dummy.ConditionConfiguration;
import skillpro.model.skills.dummy.ConditionProduct;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import skillpro.model.utils.Triple;

public abstract class HMIScenario extends DummyScenario{
	protected static final String CHOCOLATE_COLOR = "$chocolate-color$";
	protected static final String LEGO_COLOR_BOTTOM = "$bottom-color$";
	protected static final String LEGO_COLOR_MIDDLE = "$middle-color$";
	protected static final String LEGO_COLOR_TOP = "$top-color$";
	protected static final String LEGO_COLOR_BOTTOM_NAME = "$bottom-color-name$";
	protected static final String LEGO_COLOR_MIDDLE_NAME = "$middle-color-name$";
	protected static final String LEGO_COLOR_TOP_NAME = "$top-color-name$";
	
	protected final static ResourceDummy MOBILE_PLATFORM = new ResourceDummy("MobilePlatform", "MobilePlatform");
	protected final static ResourceDummy KR6_GRIPPER = new ResourceDummy("KR6-Gripper", "KR6-Gripper");
	protected final static ResourceDummy KR6 = new ResourceDummy("KR6", "KR6");
	protected final static ResourceDummy UR5 = new ResourceDummy("UR5", "UR5");
	protected final static ResourceDummy UR5_GRIPPER = new ResourceDummy("UR5-Gripper", "UR5-Gripper");
	protected final static ResourceDummy HUMAN_WP1 = new ResourceDummy("HumanWP1", "HumanWP1");
	protected final static ResourceDummy HUMAN_WP2 = new ResourceDummy("HumanWP2", "HumanWP2");
	protected final static ResourceDummy HUMAN_WP3 = new ResourceDummy("HumanWP3", "HumanWP3");
	protected final static ResourceDummy SIMULATION = new ResourceDummy("Simulation", "Simulation");
	
	protected static class WorkplaceInfo extends Triple<String, ResourceDummy, ConditionConfiguration>{
		public WorkplaceInfo(String e1, ResourceDummy e2, ConditionConfiguration e3) {
			super(e1, e2, e3);
		}
	}
	
	static final List<WorkplaceInfo> workplaces = Collections.unmodifiableList(Arrays.asList(
			new WorkplaceInfo("WP1", HUMAN_WP1, POS_WP1),
			new WorkplaceInfo("WP2", HUMAN_WP2, POS_WP2),
			new WorkplaceInfo("WP3", HUMAN_WP3, POS_WP3)
			));

	public static List<DummyScenario> getAllScenarios() {
		return Arrays.<DummyScenario>asList(
				FullOrderScenario.getInstance(),
				OnlyChocolateOrderScenario.getInstance(),
				KR6OnlyOrderScenario.getInstance(),
				UR5OnlyOrderScenario.getInstance()
		);
	}
	
	protected Collection<? extends ExecutableSkillDummy> createEmptyBoxToggle() {
		List<ConditionConfiguration> positions = Arrays.asList(POS_UR5, POS_KR6, POS_WP1, POS_WP2, POS_WP3);

		List<ExecutableSkillDummy> result = new ArrayList<>();
		String templateSkill = "Waiting";
		for (ConditionConfiguration workplacePosition : positions) {
			{
				String resourceExecutableSkill = "PlatformCreateBoxAt" + workplacePosition;
				ExecutableSkillDummy dummy = new ExecutableSkillDummy(resourceExecutableSkill);
				
				ResourceExecutableSkillDummy res = dummy.add(resourceExecutableSkill, MOBILE_PLATFORM, templateSkill);
				res.setDuration(0);
				res.setPreCondition(workplacePosition, ConditionProduct.EMPTY);
				res.setPostCondition(workplacePosition, ConditionProduct.BOX_EMPTY);
				res.addProperty("Duration", "string", "sec", "0");
				
				result.add(dummy);
			}
			{
				String resourceExecutableSkill = "PlatformDeleteBoxAt" + workplacePosition;
				ExecutableSkillDummy dummy = new ExecutableSkillDummy(resourceExecutableSkill);
				
				ResourceExecutableSkillDummy res = dummy.add(resourceExecutableSkill, MOBILE_PLATFORM, templateSkill);
				res.setDuration(0);
				res.setPreCondition(workplacePosition, ConditionProduct.BOX_EMPTY);
				res.setPostCondition(workplacePosition, ConditionProduct.EMPTY);
				res.addProperty("Duration", "string", "sec", "0");
				
				result.add(dummy);
			}
		}
		return result;
	}
	
	protected Collection<? extends ExecutableSkillDummy> createPlatformFromToNeutral(ConditionProduct product) {
		return createPlatformFromToNeutral(product, product.getName());
	}
	
	protected List<ExecutableSkillDummy> createPlatformFromToNeutral(ConditionProduct product, String productName) {
		List<ConditionConfiguration> positions = Arrays.asList(POS_UR5, POS_KR6, POS_WP1, POS_WP2, POS_WP3);
		List<ExecutableSkillDummy> result = new ArrayList<>();
		
		for (ConditionConfiguration position : positions) {
			String positionName = position.toString().replace("pos-", "").toUpperCase();
			
			{ // to neutral
				String id = "MoveEmptyPlatformFrom" + positionName + "ToNeutral" + productName;
				ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
				
				ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
				res1.setDuration(30);
				res1.setPreCondition(position, product);
				res1.setPostCondition(NEUTRAL, product);
				res1.addProperty("Position", "string", "", "NEUT");
	
				result.add(dummy);
			}
			{ // from neutral
				String id = "PlatformGoesTo" + positionName + productName;
				ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
				
				ResourceExecutableSkillDummy res1 = dummy.add("MobilePlatform-"+id, MOBILE_PLATFORM, "Transport3D");
				res1.setDuration(30);
				res1.setPreCondition(NEUTRAL, product);
				res1.setPostCondition(position, product);
				res1.addProperty("Position", "string", "", positionName);
				
				result.add(dummy);
			}
		}
		return result;
	}
	
	protected List<ExecutableSkillDummy> createEmptyUR5ToNeutral() {
		List<ConditionConfiguration> positions = Arrays.asList(POS_BOX, POS_CHOCOLATE);
		List<ExecutableSkillDummy> result = new ArrayList<>();
		
		for (ConditionConfiguration position : positions) {
			String positionName = position.toString().replace("pos-", "").toUpperCase();
			String upperCaseFirst = upperCaseFirst(positionName);
			String id = "GoFrom" + upperCaseFirst + "ToNeutralPosition";
			
			ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
			
			String resourceExecutableSkill = "UR5-"+id;
			ResourceExecutableSkillDummy res = dummy.add(resourceExecutableSkill, UR5, "Transport6D");
			res.setDuration(30);
			res.setPreCondition(position, ConditionProduct.EMPTY);
			res.setPostCondition(NEUTRAL, ConditionProduct.EMPTY);
			res.addProperty("Position", "string", "", "basepos");
			
			result.add(dummy);
		}
		return result;
	}
	
	protected List<ExecutableSkillDummy> createEmptyKR6ToNeutral() {
		List<ConditionConfiguration> positions = Arrays.asList(POS_PLATFORM, POS_LEGO_TOP, POS_LEGO_MIDDLE, POS_LEGO_BOTTOM);
		List<ExecutableSkillDummy> result = new ArrayList<>();
		
		for (ConditionConfiguration position : positions) {
			String positionName = position.toString().replace("pos-", "").toUpperCase();
			String upperCaseFirst = upperCaseFirst(positionName);
			String id = upperCaseFirst + "ToNeutralPosition";
			ExecutableSkillDummy dummy = new ExecutableSkillDummy(id);
			
			ResourceExecutableSkillDummy res1 = dummy.add("KR6-" + id, KR6, "TransportSemantic");
			res1.setDuration(15);
			res1.setPreCondition(position, ConditionProduct.EMPTY);
			res1.setPostCondition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY);
			res1.addProperty("SemanticPosition", "string", "", "Neutral");
			
			result.add(dummy);
		}
		return result;
	}
	
	private String upperCaseFirst(String s) {
		if (s.length() <= 1) {
			return s.toUpperCase();
		} else {
			return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
		}
	}
}
