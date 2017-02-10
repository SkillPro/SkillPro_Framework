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

package aml.skillpro.transformation.adapters;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyConstraintNominal;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.SkillSynchronizationType;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IResourceExecutableSkillTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;

public class ResourceExecutableSkillAdapter extends TransformableAdapterTemplate implements IResourceExecutableSkillTransformable {
	private ResourceExecutableSkill resourceExecutableSkill;
	
	public ResourceExecutableSkillAdapter() {
		this(new ResourceExecutableSkill());
	}
	
	public ResourceExecutableSkillAdapter(ResourceExecutableSkill resourceExecutableSkill) {
		this.resourceExecutableSkill = resourceExecutableSkill;
	}
	
	@Override
	public String getTransformableName() {
		return resourceExecutableSkill.getName();
	}

	@Override
	public String getTransformableID() {
		return resourceExecutableSkill.getId();
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
		return resourceExecutableSkill;
	}

	@SuppressWarnings("unchecked")
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
		internalElement.addSupportedRole(((Hierarchy<Role>) getTransformableTemplateSkill().reverseTransform()).getElement());
		
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
		addAttribute(ie, resourceId, resourceExecutableSkill.getResource().getResponsibleSEE().getSeeID());
		
		//ResponsibleSEE
		Attribute responsibleSEE = new Attribute("ResponsibleSEE", AttributeType.STRING, "");
		addAttribute(ie, responsibleSEE, resourceExecutableSkill.getResource().getName());
		
		//SkillSyncType
		Attribute skillSync = new Attribute("SkillSynchronization", AttributeType.STRING, "");
		addAttribute(ie, skillSync, "");
		
		Attribute syncType = new Attribute("Type", AttributeType.STRING, "");
		skillSync.addAttribute(syncType);
		//is this correct? sure hope so
		SkillSynchronizationType skillSyncType = resourceExecutableSkill.getSyncType();
		if (skillSyncType == null) {
			skillSyncType = SkillSynchronizationType.NONE;
		}
		addAttribute(ie, syncType, skillSyncType.getName());
		
		Attribute syncAddress = new Attribute("Address", AttributeType.STRING, "");
		skillSync.addAttribute(syncAddress);
		JsonObjectBuilder addressJsonOB = Json.createObjectBuilder();
		//Address for master will be done in ExSkillAdapter
		//FIXME please change how this is done if it bothers you
		//maybe you can add a reference to the ExSkill from the RexSkill?
		if (skillSyncType == SkillSynchronizationType.SLAVE) {
			addressJsonOB.add(resourceExecutableSkill.getResource().getName() + "/Inter_SEE_Comm" +
					"/Synchronisation/message", "Done");
		}
		addAttribute(ie, syncAddress, addressJsonOB.build().toString());
		
		//Duration
		Attribute duration = new Attribute("Duration", AttributeType.INTEGER, "sec");
		addAttribute(ie, duration, resourceExecutableSkill.getDuration() + "");
		
		//Slack
		Attribute slack = new Attribute("Slack", AttributeType.INTEGER, "sec");
		addAttribute(ie, slack, resourceExecutableSkill.getSlack() + "");
		
		//Execution
		Attribute execution = new Attribute("Execution", AttributeType.STRING, "");
		addAttribute(ie, execution, "");
		
		Attribute skillType = new Attribute("Type", AttributeType.STRING, "");
		execution.addAttribute(skillType);
		//is this correct? sure hope so
		addAttribute(ie, skillType, resourceExecutableSkill.getTemplateSkill().getName());
		
		Attribute skillData = new Attribute("Data", AttributeType.STRING, "");
		execution.addAttribute(skillData);
		addAttribute(ie, skillData, "");
		for (PropertyDesignator des : resourceExecutableSkill.getPropertyDesignators()) {
			Attribute attribute = new PropertyAdapter(des.getProperty()).reverseTransform();
			if (attribute == null) {
				throw new IllegalArgumentException("Cannot reverse transform attribute: " + des.getProperty().getName());
			}
			skillData.addAttribute(attribute);
			String value = des.getValue();
			if (value == null || value.isEmpty() || value.equals("-")) {
				if (des.getConstraints().size() == 1) {
					PropertyConstraint propertyConstraint = des.getConstraints().get(0);
					if (propertyConstraint instanceof PropertyConstraintNominal) {
						PropertyConstraintNominal propertyConstraintNominal = (PropertyConstraintNominal) propertyConstraint;
						if (propertyConstraintNominal.getValues().size() == 1) {
							value = propertyConstraintNominal.getValues().get(0);
						}
					}
				}
			}
			addAttribute(ie, attribute, value);
		}
		
		List<ProductQuantity> inputs = new ArrayList<>();
		ProductConfiguration preProductConfiguration = resourceExecutableSkill.getPreProductConfiguration();
		if (preProductConfiguration != null) {
			inputs.addAll(preProductConfiguration.getProductQuantities());
		}
		List<ProductQuantity> outputs = new ArrayList<>();
		ProductConfiguration postProductConfiguration = resourceExecutableSkill
				.getPostProductConfiguration();
		if (postProductConfiguration != null) {
			outputs.addAll(postProductConfiguration.getProductQuantities());
		}
		
		JsonObjectBuilder preReqJsonOB = Json.createObjectBuilder();
		JsonObjectBuilder postReqJsonOB = Json.createObjectBuilder();
		
		if (!inputs.equals(outputs)) {
			for (int i = 0; i < inputs.size(); i++) {
				ProductQuantity input = inputs.get(i);
				preReqJsonOB.add(input.getProduct().getName(), Json.createArrayBuilder()
						.add(Json.createObjectBuilder().add(">=", input.getQuantity()).build())
						.add(Json.createObjectBuilder().add("<=", 65536).build())
						.build()
						);
				postReqJsonOB.add(input.getProduct().getName(), -1 * input.getQuantity());
			}
			
			for (int i = 0; i < outputs.size(); i++) {
				ProductQuantity output = outputs.get(i);
				preReqJsonOB.add(output.getProduct().getName(), Json.createArrayBuilder()
						.add(Json.createObjectBuilder().add(">=", 0).build())
						.add(Json.createObjectBuilder().add("<=", 65536 - output.getQuantity()).build())
						.build()
						);
				postReqJsonOB.add(output.getProduct().getName(), output.getQuantity());
			}
		} else {
			for (int i = 0; i < inputs.size(); i++) {
				ProductQuantity input = inputs.get(i);
				preReqJsonOB.add(input.getProduct().getName(), Json.createArrayBuilder()
						.add(Json.createObjectBuilder().add(">=", input.getQuantity()).build())
						.add(Json.createObjectBuilder().add("<=", 65536).build())
						.build()
						);
				postReqJsonOB.add(input.getProduct().getName(), 0);
			}
		}
		
		String preProductConfigurationValue = preReqJsonOB.build().toString();
		String postProductConfigurationValue = postReqJsonOB.build().toString();
		
		//PRE CONDITION
		Attribute preCondition = new Attribute("PreCondition", AttributeType.STRING, "");
		addAttribute(ie, preCondition, "");
		
		Attribute preConfiguration = new Attribute("Configuration", AttributeType.STRING, "");
		preCondition.addAttribute(preConfiguration);
		if (resourceExecutableSkill.getPreResourceConfiguration() == null) {
			throw new IllegalArgumentException("This ResourceExecutableSkill: " + resourceExecutableSkill
					+ ", does not have a pre-ResourceConfiguration");
		}
		addAttribute(ie, preConfiguration, Json.createArrayBuilder().add(resourceExecutableSkill.getPreResourceConfiguration().getName()).build().toString());
		
		Attribute preProduct = new Attribute("Product", AttributeType.STRING, "");
		preCondition.addAttribute(preProduct);
		addAttribute(ie, preProduct, preProductConfigurationValue);
		
		//POST CONDITION
		Attribute postCondition = new Attribute("PostCondition", AttributeType.STRING, "");
		addAttribute(ie, postCondition, "");
		
		Attribute postConfiguration = new Attribute("Configuration", AttributeType.STRING, "");
		postCondition.addAttribute(postConfiguration);
		addAttribute(ie, postConfiguration, resourceExecutableSkill.getPostResourceConfiguration().getName());
		
		Attribute postProduct = new Attribute("Product", AttributeType.STRING, "");
		postCondition.addAttribute(postProduct);
		addAttribute(ie, postProduct, postProductConfigurationValue);
		
		if (resourceExecutableSkill.getName().toLowerCase().contains("qualitychecking")) {
			//ALT POST CONDITION
			Attribute altPostCondition = new Attribute("AltPostCondition", AttributeType.STRING, "");
			addAttribute(ie, altPostCondition, "");
			
			Attribute altPostConfiguration = new Attribute("Configuration", AttributeType.STRING, "");
			altPostCondition.addAttribute(altPostConfiguration);
			addAttribute(ie, altPostConfiguration, resourceExecutableSkill.getPostResourceConfiguration().getName());
			
			Attribute altPostProduct = new Attribute("Product", AttributeType.STRING, "");
			altPostCondition.addAttribute(altPostProduct);
			addAttribute(ie, altPostProduct, postProductConfigurationValue.replace("PCBChecked", "PCBChecked-NOK"));
		}
		
		//Correcting some attributes
		AttributeDesignator productsAttributeDesignator = ie.getDesignatorByName("Products");
		if (productsAttributeDesignator != null) {
			productsAttributeDesignator.setValue(postProductConfigurationValue);
		}
		AttributeDesignator goalAttributeDesignator = ie.getDesignatorByName("Goal");
		//FIXME, maybe separate goal and postResourceConfiguration in the future?
		if (goalAttributeDesignator != null && !resourceExecutableSkill.getResource().getName().toLowerCase().contains("human")) {
			goalAttributeDesignator.setValue(resourceExecutableSkill.getPostResourceConfiguration().getName());
		}
	}
	
	@Override
	public ITemplateSkillTransformable getTransformableTemplateSkill() {
		if (resourceExecutableSkill == null) {
			throw new IllegalArgumentException("ResourceSkill or TemplateSkill is null for this ExecutableSkill: " + getTransformableName());
		}
		return new TemplateSkillAdapter(resourceExecutableSkill.getTemplateSkill());
	}
}
