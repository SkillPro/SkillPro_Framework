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

package skillpro.model.skills.dummy;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.skills.Skill;

public class ResourceExecutableSkillDummy extends Skill {
	private ResourceDummy resource;
	private int duration;
	private int slack;
	
	private String templateSkill;
	private List<PropertyDummy> propertyDummies;
	
	private Condition preCondition;
	private Condition postCondition;
	
	public ResourceExecutableSkillDummy(String name, ResourceDummy resource, int duration, int slack,
			String templateSkill, List<PropertyDummy> propertyDesignator,
			Condition preCondition,
			Condition postCondition) {
		super(name);
		
		this.resource = resource;
		this.duration = duration;
		this.slack = slack;
		this.templateSkill = templateSkill;
		this.propertyDummies = propertyDesignator;
		this.preCondition = preCondition;
		this.postCondition = postCondition;
	}

	public ResourceExecutableSkillDummy(String name, ResourceDummy resource, String templateSkill) {
		super(name);
		this.resource = resource;
		this.templateSkill = templateSkill;
		this.propertyDummies = new ArrayList<>();
	}

	public ResourceDummy getResource() {
		return resource;
	}

	public void setResource(ResourceDummy resource) {
		this.resource = resource;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getSlack() {
		return slack;
	}

	public void setSlack(int slack) {
		this.slack = slack;
	}

	public String getTemplateSkill() {
		return templateSkill;
	}

	public void setTemplateSkill(String templateSkill) {
		this.templateSkill = templateSkill;
	}

	public List<PropertyDummy> getPropertyDummies() {
		return propertyDummies;
	}

	public void setPropertyDummies(List<PropertyDummy> propertyDummies) {
		this.propertyDummies = propertyDummies;
	}

	public Condition getPreCondition() {
		return preCondition;
	}

	public void setPreCondition(Condition preCondition) {
		this.preCondition = preCondition;
	}

	public void setPreCondition(ConfigurationSet ccPre, String preProduct) {
		setPreCondition(new Condition(ccPre, preProduct));
	}

	public Condition getPostCondition() {
		return postCondition;
	}

	public void setPostCondition(Condition postCondition) {
		this.postCondition = postCondition;
	}

	public void setPostCondition(ConfigurationSet ccPost, String postProduct) {
		setPostCondition(new Condition(ccPost, postProduct));
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public void setPreCondition(ConditionConfiguration configuration, ConditionProduct products) {
		setPreCondition(new Condition(configuration, products));
	}

	public void setPreCondition(ConditionConfiguration configuration, String products) {
		setPreCondition(new Condition(configuration, products));
		
	}

	public void setPostCondition(ConditionConfiguration configuration, ConditionProduct products) {
		setPostCondition(new Condition(configuration, products));
	}

	public void setPostCondition(ConditionConfiguration configuration, String products) {
		setPostCondition(new Condition(configuration, products));
		
	}

	public void addProperty(String name, String dataType, String unit, String value) {
		propertyDummies.add(new PropertyDummy(name, dataType, unit, value));
	}
}
