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

package aml.skillpro.transformation.adapters.dummy;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.skills.dummy.PropertyDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IResourceExecutableSkillTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;

public class ResourceExecutableSkillDummyAdapter extends TransformableAdapterTemplate implements IResourceExecutableSkillTransformable {
	private ResourceExecutableSkillDummy dummy;
	
	public ResourceExecutableSkillDummyAdapter(ResourceExecutableSkillDummy dummy) {
		this.dummy = dummy;
	}
	
	@Override
	public String getTransformableName() {
		return dummy.getName();
	}

	@Override
	public String getTransformableID() {
		return "ID_" + dummy.getName();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		return null;
	}

	@Override
	public ITransformable getTransformableParent() {
		return null;
	}

	@Override
	public Object getElement() {
		return dummy;
	}


	@Override
	public Object reverseTransform() {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		//add supportedRole
		Role role = new Role(dummy.getTemplateSkill(), templateSkillHierarchy.getElement());
		
		internalElement.addSupportedRole(role);
		
		//add default attributes
		addDefaultDesignators(internalElement);
		
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
	}
	
	private void addDefaultDesignators(InternalElement ie) {
		//ResourceId
		Attribute resourceId = new Attribute("ResourceId", AttributeType.STRING, "");
		addAttribute(ie, resourceId, dummy.getResource().getResourceId());
		
		//ResponsibleSEE
		Attribute responsibleSEE = new Attribute("ResponsibleSEE", AttributeType.STRING, "");
		addAttribute(ie, responsibleSEE, dummy.getResource().getResponsibleSEE());
		
		
		//Duration
		//TODO double or int?
		Attribute duration = new Attribute("Duration", AttributeType.INTEGER, "sec");
		addAttribute(ie, duration, dummy.getDuration() + "");
		
		//Slack
		//TODO double or int?
		Attribute slack = new Attribute("Slack", AttributeType.INTEGER, "sec");
		addAttribute(ie, slack, dummy.getSlack() + "");
		
		//Execution
		Attribute execution = new Attribute("Execution", AttributeType.STRING, "");
		addAttribute(ie, execution, "");
		
		Attribute skillType = new Attribute("Type", AttributeType.STRING, "");
		execution.addAttribute(skillType);
		//is this correct? sure hope so
		addAttribute(ie, skillType, dummy.getTemplateSkill());
		
		Attribute skillData = new Attribute("Data", AttributeType.STRING, "");
		execution.addAttribute(skillData);
		addAttribute(ie, skillData, "");
		for (PropertyDummy des : dummy.getPropertyDummies()) {
			Attribute attribute = new Attribute(des.getName(), des.getDataType(), des.getUnit());
			skillData.addAttribute(attribute);
			AttributeDesignator attDes = new AttributeDesignator(attribute, 
					ie, new ArrayList<Constraint>(), des.getValue());
			ie.addDesignator(attDes);
			attDes.getAttribute().addDesignator(attDes);
		}
		
		
		//PRE CONDITION
		Attribute preCondition = new Attribute("PreCondition", AttributeType.STRING, "");
		addAttribute(ie, preCondition, "");
		
		Attribute preConfiguration = new Attribute("Configuration", AttributeType.STRING, "");
		preCondition.addAttribute(preConfiguration);
		addAttribute(ie, preConfiguration, dummy.getPreCondition().getFirstElement().toString());
		
		Attribute preProduct = new Attribute("Product", AttributeType.STRING, "");
		preCondition.addAttribute(preProduct);
		addAttribute(ie, preProduct, dummy.getPreCondition().getSecondElement().toString());
		
		//POST CONDITION
		Attribute postCondition = new Attribute("PostCondition", AttributeType.STRING, "");
		addAttribute(ie, postCondition, "");
		
		Attribute postConfiguration = new Attribute("Configuration", AttributeType.STRING, "");
		postCondition.addAttribute(postConfiguration);
		addAttribute(ie, postConfiguration, dummy.getPostCondition().getFirstElement().toString());
		
		Attribute postProduct = new Attribute("Product", AttributeType.STRING, "");
		postCondition.addAttribute(postProduct);
		addAttribute(ie, postProduct, dummy.getPostCondition().getSecondElement().toString());
	}
	
	@Override
	public ITemplateSkillTransformable getTransformableTemplateSkill() {
		return null;
	}
}
