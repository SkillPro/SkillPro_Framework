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

import java.util.Objects;
import java.util.UUID;

import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.ProductConfiguration;

public class Requirement {
	private String id = UUID.randomUUID().toString();

	private ResourceSkill mainResourceSkill;
	private RequirementSkillType skillType;
	private ResourceSkill requiredResourceSkill;
	private TemplateSkill requiredTemplateSkill;
	
	private RequirementResourceConfigType resourceConfigType;
	private ResourceConfiguration requiredResourceConfiguration;
	
	private RequirementProductConfigType productConfigType;
	private ProductConfiguration requiredProductConfiguration;
	
	private SkillSynchronizationType syncType = SkillSynchronizationType.NONE;
	
	public Requirement() {
		
	}
	
	public Requirement(RequirementSkillType skillType, RequirementResourceConfigType resourceConfigType,
			RequirementProductConfigType productConfigType, ResourceConfiguration requiredResourceConfiguration,
			ProductConfiguration requiredProductConfiguration, Skill skill, ResourceSkill mainResourceSkill,
			SkillSynchronizationType syncType) {
		if (skill instanceof TemplateSkill) {
			requiredTemplateSkill = (TemplateSkill) skill;
		} else if (skill instanceof ResourceSkill) {
			requiredResourceSkill = (ResourceSkill) skill;
		}
		this.skillType = skillType;
		this.resourceConfigType = resourceConfigType;
		this.productConfigType = productConfigType;
		this.requiredResourceConfiguration = requiredResourceConfiguration;
		this.requiredProductConfiguration = requiredProductConfiguration;
		this.mainResourceSkill = mainResourceSkill;
		this.syncType = syncType;
	}
	
	public SkillSynchronizationType getSyncType() {
		return syncType;
	}
	
	public void setSyncType(SkillSynchronizationType syncType) {
		this.syncType = syncType;
	}
	
	public RequirementSkillType getSkillType() {
		return skillType;
	}

	public void setSkillType(RequirementSkillType skillType) {
		this.skillType = skillType;
	}

	public ResourceSkill getRequiredResourceSkill() {
		return requiredResourceSkill;
	}

	public void setRequiredResourceSkill(ResourceSkill requiredResourceSkill) {
		this.requiredResourceSkill = requiredResourceSkill;
	}

	public TemplateSkill getRequiredTemplateSkill() {
		return requiredTemplateSkill;
	}

	public void setRequiredTemplateSkill(TemplateSkill requiredTemplateSkill) {
		this.requiredTemplateSkill = requiredTemplateSkill;
	}

	public RequirementResourceConfigType getResourceConfigType() {
		return resourceConfigType;
	}

	public void setResourceConfigType(
			RequirementResourceConfigType resourceConfigType) {
		this.resourceConfigType = resourceConfigType;
	}

	public ResourceConfiguration getRequiredResourceConfiguration() {
		return requiredResourceConfiguration;
	}

	public void setRequiredResourceConfiguration(
			ResourceConfiguration requiredResourceConfiguration) {
		this.requiredResourceConfiguration = requiredResourceConfiguration;
	}

	public RequirementProductConfigType getProductConfigType() {
		return productConfigType;
	}

	public void setProductConfigType(RequirementProductConfigType productConfigType) {
		this.productConfigType = productConfigType;
	}

	public ProductConfiguration getRequiredProductConfiguration() {
		return requiredProductConfiguration;
	}

	public void setRequiredProductConfiguration(
			ProductConfiguration requiredProductConfiguration) {
		this.requiredProductConfiguration = requiredProductConfiguration;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public ResourceSkill getMainResourceSkill() {
		return mainResourceSkill;
	}
	
	public void setMainResourceSkill(ResourceSkill mainResourceSkill) {
		this.mainResourceSkill = mainResourceSkill;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mainResourceSkill, productConfigType, requiredProductConfiguration, requiredResourceConfiguration, requiredResourceSkill, requiredTemplateSkill, resourceConfigType, skillType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Requirement other = (Requirement) obj;
		return Objects.equals(mainResourceSkill, other.mainResourceSkill)
				&& Objects.equals(productConfigType, other.productConfigType)
				&& Objects.equals(requiredProductConfiguration, other.requiredProductConfiguration)
				&& Objects.equals(requiredResourceConfiguration, other.requiredResourceConfiguration)
				&& Objects.equals(requiredResourceSkill, other.requiredResourceSkill)
				&& Objects.equals(requiredTemplateSkill, other.requiredTemplateSkill)
				&& Objects.equals(resourceConfigType, other.resourceConfigType)
				&& Objects.equals(skillType, other.skillType);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		if (requiredResourceSkill != null) {
			sb.append("Resource: " + requiredResourceSkill.getResource() + " -> ");  
		}
		sb.append("RConf: " + requiredResourceConfiguration);
		sb.append(", PConf: " + requiredProductConfiguration);
		sb.append(">");
		return sb.toString();
	}
}
