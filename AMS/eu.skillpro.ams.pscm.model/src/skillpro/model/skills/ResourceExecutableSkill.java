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

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.properties.PropertyDesignator;




public class ResourceExecutableSkill extends Skill {

	private ResourceSkill resourceSkill;
	
	private ResourceConfiguration preResourceConfiguration;
	private ResourceConfiguration postResourceConfiguration;
	private ProductConfiguration preProductConfiguration;
	private ProductConfiguration postProductConfiguration;
	private SkillSynchronizationType syncType;
	
	private List<PropertyDesignator> propertyDesignators = new ArrayList<>();

	private int slack;
	private int duration;
	//min max quantity
	private int minQuantity;
	private int maxQuantity;

	public ResourceExecutableSkill() {
		super("");
	}

	public ResourceExecutableSkill(String name, ResourceSkill resourceSkill,
			ResourceConfiguration preResourceConfiguration,
			ResourceConfiguration postResourceConfiguration,
			ProductConfiguration preProductConfiguration,
			ProductConfiguration postProductConfiguration,
			List<PropertyDesignator> propertyDesignators, int slack, int duration,
			SkillSynchronizationType syncType) {
		super(name);
		this.resourceSkill = resourceSkill;
		this.preResourceConfiguration = preResourceConfiguration;
		this.postResourceConfiguration = postResourceConfiguration;
		this.preProductConfiguration = preProductConfiguration;
		this.postProductConfiguration = postProductConfiguration;
		if (propertyDesignators != null) {
			this.propertyDesignators.addAll(propertyDesignators);
		}
		this.slack = slack;
		this.duration = duration;
		this.syncType = syncType;
	}

	public ResourceExecutableSkill(String name, ResourceSkill rSkill, Requirement preRequirement,
			ResourceConfiguration postResourceConfiguration, ProductConfiguration postProductConfiguration,
			List<PropertyDesignator> propertyDesignators, int slack, int duration, SkillSynchronizationType syncType) {
		this(name, rSkill, preRequirement.getRequiredResourceConfiguration(), 
				postResourceConfiguration, 
				preRequirement.getRequiredProductConfiguration(), postProductConfiguration,
				propertyDesignators, slack, duration, syncType);
	}

	public int getSlack() {
		return slack;
	}

	public int getDuration() {
		return duration;
	}

	public Resource getResource() {
		return resourceSkill.getResource();
	}

	public ResourceSkill getResourceSkill() {
		return resourceSkill;
	}
	
	public void setResourceSkill(ResourceSkill resourceSkill) {
		this.resourceSkill = resourceSkill;
	}

	public TemplateSkill getTemplateSkill() {
		return resourceSkill.getTemplateSkill();
	}

	public ResourceConfiguration getPreResourceConfiguration() {
		return preResourceConfiguration;
	}

	public void setPreResourceConfiguration(
			ResourceConfiguration preResourceConfiguration) {
		this.preResourceConfiguration = preResourceConfiguration;
	}

	public ResourceConfiguration getPostResourceConfiguration() {
		return postResourceConfiguration;
	}

	public void setPostResourceConfiguration(
			ResourceConfiguration postResourceConfiguration) {
		this.postResourceConfiguration = postResourceConfiguration;
	}

	public ProductConfiguration getPreProductConfiguration() {
		return preProductConfiguration;
	}

	public void setPreProductConfiguration(
			ProductConfiguration preProductConfiguration) {
		this.preProductConfiguration = preProductConfiguration;
	}

	public ProductConfiguration getPostProductConfiguration() {
		return postProductConfiguration;
	}

	public void setPostProductConfiguration(
			ProductConfiguration postProductConfiguration) {
		this.postProductConfiguration = postProductConfiguration;
	}

	public List<PropertyDesignator> getPropertyDesignators() {
		return propertyDesignators;
	}

	public void setSlack(int slack) {
		this.slack = slack;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void setMinQuantity(int minQuantity) {
		this.minQuantity = minQuantity;
	}
	
	public void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}
	
	public int getMinQuantity() {
		return minQuantity;
	}
	
	public int getMaxQuantity() {
		return maxQuantity;
	}
	
	public SkillSynchronizationType getSyncType() {
		return syncType;
	}
	
	public void setSyncType(SkillSynchronizationType syncType) {
		this.syncType = syncType;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), postProductConfiguration, postResourceConfiguration, preProductConfiguration, preResourceConfiguration, resourceSkill);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceExecutableSkill other = (ResourceExecutableSkill) obj;
		return Objects.equals(postProductConfiguration, other.postProductConfiguration)
				&& Objects.equals(postResourceConfiguration, other.postResourceConfiguration)
				&& Objects.equals(preProductConfiguration, other.preProductConfiguration)
				&& Objects.equals(preResourceConfiguration, other.preResourceConfiguration)
				&& Objects.equals(resourceSkill, other.resourceSkill);
	}
	
	@Override
	public String toString() {
		return "REX: " + resourceSkill.getName() + ", pre p: " + preProductConfiguration + ", post p: " + postProductConfiguration
				+ ", preC: " + preResourceConfiguration + ", postC: " + postResourceConfiguration;
	}
}
