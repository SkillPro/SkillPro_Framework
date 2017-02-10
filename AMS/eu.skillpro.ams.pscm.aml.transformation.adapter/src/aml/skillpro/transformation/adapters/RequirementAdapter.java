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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.RequirementProductConfigType;
import skillpro.model.skills.RequirementResourceConfigType;
import skillpro.model.skills.RequirementSkillType;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.SkillSynchronizationType;
import skillpro.model.skills.TemplateSkill;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.AttributeDesignator;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IRequirementTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;

public class RequirementAdapter extends TransformableAdapterTemplate implements IRequirementTransformable {
	private static final String SKILL_SYNCHRONIZATION_TYPE = "SkillSynchronizationType";
	private Requirement requirement;
	
	public RequirementAdapter() {
		requirement = new Requirement();
	}
	
	public RequirementAdapter(Requirement requirement) {
		this.requirement = requirement;
	}
	
	@Override
	public String getTransformableName() {
		//FIXME fix Requirement name
		return "Requirement";
	}

	@Override
	public String getTransformableID() {
		return requirement.getId();
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
		return requirement;
	}

	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context) throws InstantiationException, IllegalAccessException {
		//ritual start
		if (!context.contains(object)) {
			return null;
		}
		//returns a transformed element if it has already been transformed before.
		InternalElement ie = object.getElement();
		if (transformedObjectsMap.containsKey(ie)) {
			return (ITransformable) transformedObjectsMap.get(ie);
		}
		//ritual end
		
		TemplateSkill templateSkill = null;
		for (Role supportedRole : ie.getSupportedRoles()) {
			if (templateSkill != null) {
				throw new IllegalArgumentException("Expected 1 supported role, found: " + ie.getSupportedRoles().size());
			}
			ITransformable transformedTempSkill = new TemplateSkillAdapter().transform
					(supportedRole, context);
			templateSkill = (TemplateSkill) transformedTempSkill.getElement();
		}
		
		requirement.setRequiredTemplateSkill(templateSkill);
		
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
			if (IResourceConfigurationTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedConf = ((IResourceConfigurationTransformable) transClass.newInstance())
						.transform(child, context);
				requirement.setRequiredResourceConfiguration((ResourceConfiguration) transformedConf.getElement());
			} else if (IResourceSkillTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedConf = ((IResourceSkillTransformable) transClass.newInstance())
						.transform(child, context);
				requirement.setRequiredResourceSkill((ResourceSkill) transformedConf.getElement());
			} else if (IProductConfigurationTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedConf = ((IProductConfigurationTransformable) transClass.newInstance())
						.transform(child, context);
				requirement.setRequiredProductConfiguration((ProductConfiguration) transformedConf.getElement());
			} else {
				throw new IllegalArgumentException("Unexpected TransClass: " + transClass);
			}
			
		}
		
		RequirementSkillType skillType = null;
		RequirementResourceConfigType resourceConfigType = null;
		RequirementProductConfigType productConfigType = null;
		SkillSynchronizationType skillSyncType = null;
		for (AttributeDesignator des : ie.getDesignators()) {
			//get the required types
			if (des.getAttribute().getName().equalsIgnoreCase("RequiredSkillType")) {
				skillType = extractRequirementSkillType(des.getValue());
			} else if (des.getAttribute().getName().equalsIgnoreCase("RequiredResourceConfigurationType")) {
				resourceConfigType = extractRequirementResourceConfigType(des.getValue());
			} else if (des.getAttribute().getName().equalsIgnoreCase("RequiredProductConfigurationType")) {
				productConfigType = extractRequirementProductConfigType(des.getValue());
			} else if (des.getAttribute().getName().equalsIgnoreCase(SKILL_SYNCHRONIZATION_TYPE)) {
				skillSyncType = extractSkillSyncType(des.getValue());
			} else {
				throw new IllegalArgumentException("Unknown Attribute: " + des.getAttribute().getName());
			}
		}
		
		if (skillType == null || resourceConfigType == null || productConfigType == null) {
			throw new IllegalArgumentException("This Requirement has incomplete Attributes!");
		}
		
		requirement.setSkillType(skillType);
		requirement.setProductConfigType(productConfigType);
		requirement.setResourceConfigType(resourceConfigType);
		requirement.setId(ie.getId());
		requirement.setSyncType(skillSyncType);
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}
	
	private RequirementSkillType extractRequirementSkillType(String string) {
		for (RequirementSkillType type : RequirementSkillType.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
	
	private RequirementResourceConfigType extractRequirementResourceConfigType(String string) {
		for (RequirementResourceConfigType type : RequirementResourceConfigType.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
	
	private RequirementProductConfigType extractRequirementProductConfigType(String string) {
		for (RequirementProductConfigType type : RequirementProductConfigType.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
	
	private SkillSynchronizationType extractSkillSyncType(String string) {
		for (SkillSynchronizationType type : SkillSynchronizationType.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
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
		
		InternalElement requirementIE = new InternalElement(getTransformableID(), getTransformableName());
		requirementIE.setRequiredRole(requiredRole);

		Hierarchy<InternalElement> requirementHie = new Hierarchy<InternalElement>(requirementIE.getName(), requirementIE);

		//add SkillType
		addAttribute(requirementIE, "RequiredSkillType", "String", "", requirement.getSkillType().toString());
		//add TemplateSkill
		if (getTransformableTemplateSkill() != null) {
			requirementIE.addSupportedRole(((Hierarchy<Role>) getTransformableTemplateSkill().reverseTransform()).getElement());
		}
		
		//add ResourceSkill
		if (getTransformableResourceSkill() != null) {
			Hierarchy<InternalElement> resourceSkillHie = (Hierarchy<InternalElement>) getTransformableResourceSkill().reverseTransform();
			InternalElement linkResourceSkill = new InternalElement(UUID.randomUUID().toString(), resourceSkillHie.getName());
			linkResourceSkill.setReferencedInternalElement(resourceSkillHie.getElement());
			Hierarchy<InternalElement> linkResourceSkillHierarchy = new Hierarchy<InternalElement>(linkResourceSkill.getName(), linkResourceSkill);
			linkResourceSkillHierarchy.setParent(requirementHie);
		}
		
		//add ResourceConfigurationType
		addAttribute(requirementIE, "RequiredResourceConfigurationType", "String", "", requirement.getResourceConfigType().toString());
		//add ResourceConfiguration
		if (getTransformableResourceConfiguration() != null) {
			Hierarchy<InternalElement> resourceConfigurationHie = (Hierarchy<InternalElement>) getTransformableResourceConfiguration().reverseTransform();
			InternalElement linkResourceConfiguration = new InternalElement(UUID.randomUUID().toString(), resourceConfigurationHie.getName());
			linkResourceConfiguration.setReferencedInternalElement(resourceConfigurationHie.getElement());
			Hierarchy<InternalElement> linkResourceConfigurationHierarchy = new Hierarchy<InternalElement>(linkResourceConfiguration.getName(), linkResourceConfiguration);
			linkResourceConfigurationHierarchy.setParent(requirementHie);
		}
		
		//add ProductConfigurationType
		addAttribute(requirementIE, "RequiredProductConfigurationType", "String", "", requirement.getProductConfigType().toString());
		//add ProductConfiguration
		if (getTransformableProductConfiguration() != null) {
			Hierarchy<InternalElement> productConfigurationHie = (Hierarchy<InternalElement>) getTransformableProductConfiguration().reverseTransform();
			productConfigurationHie.setParent(requirementHie);
		}	
		
		//add SkillSyncType
		addAttribute(requirementIE, SKILL_SYNCHRONIZATION_TYPE, "String", "", requirement.getSyncType().toString());
		//start ritual
		reverseTransformedObjectsMap.put(this, requirementHie);
		return requirementHie;
		//end ritual
	}
	
	@Override
	public IProductConfigurationTransformable getTransformableProductConfiguration() {
		if (requirement.getRequiredProductConfiguration() == null) {
			return null;
		}
		return new ProductConfigurationAdapter(requirement.getRequiredProductConfiguration());
	}
	
	@Override
	public IResourceConfigurationTransformable getTransformableResourceConfiguration() {
		if (requirement.getRequiredResourceConfiguration() == null) {
			return null;
		}
		return new ResourceConfigurationAdapter(requirement.getRequiredResourceConfiguration());
	}
	
	@Override
	public IResourceSkillTransformable getTransformableResourceSkill() {
		if (requirement.getRequiredResourceSkill() == null) {
			return null;
		}
		return new ResourceSkillAdapter(requirement.getRequiredResourceSkill());
	}
	
	@Override
	public ITemplateSkillTransformable getTransformableTemplateSkill() {
		if (requirement.getRequiredTemplateSkill() == null) {
			return null;
		}
		return new TemplateSkillAdapter(requirement.getRequiredTemplateSkill());
	}

	@Override
	public int hashCode() {
		return Objects.hash(requirement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequirementAdapter other = (RequirementAdapter) obj;
		return Objects.equals(requirement, other.requirement);
	}
}

