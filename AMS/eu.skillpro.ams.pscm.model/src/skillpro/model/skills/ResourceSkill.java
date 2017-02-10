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

package skillpro.model.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;

public class ResourceSkill extends Skill {
	private TemplateSkill templateSkill;
	private Resource resource;
	private List<PropertyDesignator> propertyDesignators = new ArrayList<>();
	private List<PrePostRequirement> prePostRequirements = new ArrayList<>();
	
	//change default resource config?
	private RequirementResourceConfigType preResourceConfigType = RequirementResourceConfigType.SPECIFIC;
	private ResourceConfiguration preConfiguration;
	private RequirementResourceConfigType postResourceConfigType = RequirementResourceConfigType.SPECIFIC;
	private ResourceConfiguration postConfiguration;
	
	public ResourceSkill() {
		super("");
		setId(UUID.randomUUID().toString());
	}
	
	public ResourceSkill(String name, TemplateSkill templateSkill, Resource resource) {
		super(name);
		setId(UUID.randomUUID().toString());
		this.templateSkill = templateSkill;
		this.resource = resource;
	}
	
	public ResourceSkill(String name, TemplateSkill templateSkill, Resource asset,
			List<PrePostRequirement> prePostRequirements) {
		this(name, templateSkill, asset);
		this.prePostRequirements.addAll(prePostRequirements);
	}
	
	public RequirementResourceConfigType getPreResourceConfigType() {
		return preResourceConfigType;
	}
	
	public void setPrePostRequirements(
			List<PrePostRequirement> prePostRequirements) {
		this.prePostRequirements = prePostRequirements;
	}
	
	public RequirementResourceConfigType getPostResourceConfigType() {
		return postResourceConfigType;
	}
	
	public void setPostResourceConfigType(
			RequirementResourceConfigType postResourceConfigType) {
		this.postResourceConfigType = postResourceConfigType;
	}
	
	@Override
	public List<Property> getProperties() {
		return templateSkill.getProperties();
	}
	
	public TemplateSkill getTemplateSkill() {
		return templateSkill;
	}
	
	public List<PrePostRequirement> getPrePostRequirements() {
		return prePostRequirements;
	}
	
	public boolean addPrePostRequirement(PrePostRequirement prePostRequirement) {
		if (prePostRequirement != null && !prePostRequirements.contains(prePostRequirement)) {
			return prePostRequirements.add(prePostRequirement);
		}
		return false;
	}
	
	public List<PropertyDesignator> getPropertyDesignators() {
		return propertyDesignators;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public List<PropertyDesignator> getEmptyAndFilledDesignators() {
		for (Property property : getProperties()) {
			boolean exists = false;
			for (PropertyDesignator designator : propertyDesignators) {
				if (designator.getProperty().equals(property)) {
					exists = true;
				}
			}
			if (!exists) {
				propertyDesignators.add(new PropertyDesignator(property, this, "-"));
			}
		}
		
		return propertyDesignators;
	}
	
	public PropertyDesignator getPropertyDesignator(Property property) {
		PropertyDesignator propertyDesignator = null;
		if (getProperties().contains(property)) {
			propertyDesignator = new PropertyDesignator(property, this, "-");
		}
		
		for (PropertyDesignator designator : propertyDesignators) {
			if (designator.getProperty().equals(property)) {
				propertyDesignator = designator;
			}
		}
		return propertyDesignator;
	}
	
	public boolean addPropertyDesignator(PropertyDesignator designator) {
		if (designator != null && !propertyDesignators.contains(designator)) {
			return propertyDesignators.add(designator);
		}
		
		return false;
	}
	
	public boolean addPropertyDesignator(Property property, String value, List<PropertyConstraint> constraints) {
		PropertyDesignator designator = new PropertyDesignator(property, this, value, constraints);
		if (designator != null && !propertyDesignators.contains(designator)) {
			return propertyDesignators.add(designator);
		}
		
		return false;
	}

	public ResourceConfiguration getPreConfiguration() {
		if (preResourceConfigType == RequirementResourceConfigType.SPECIFIC) {
			return preConfiguration;
		}
		return null;
	}
	
	public void setPreConfiguration(ResourceConfiguration preConfiguration) {
		this.preConfiguration = preConfiguration;
	}
	
	public ResourceConfiguration getPostConfiguration() {
		if (postResourceConfigType == RequirementResourceConfigType.SPECIFIC) {
			return postConfiguration;
		} else if (postResourceConfigType == RequirementResourceConfigType.SAME
				&& preResourceConfigType == RequirementResourceConfigType.SPECIFIC) {
			return preConfiguration;
		}
		return null;
	}
	
	public void setPostConfiguration(ResourceConfiguration postConfiguration) {
		this.postConfiguration = postConfiguration;
	}
	
	public void setTemplateSkill(TemplateSkill templateSkill) {
		this.templateSkill = templateSkill;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), resource, templateSkill);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceSkill other = (ResourceSkill) obj;
		return Objects.equals(resource, other.resource)
				&& Objects.equals(templateSkill, other.templateSkill);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
