/**
 * 
 */
/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: AMS Server
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

package eu.skillpro.ams.service.to.assets;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.properties.Property;
import skillpro.model.skills.ResourceSkill;

/**
 * @author caliqi
 * @date 25.09.2014
 *
 */
public class SkillTO {
	private String id;
	private String name;
	private String templateSkill;
	private List<AttributeTO> attributes = new ArrayList<AttributeTO>();
	private boolean active = true;

	public SkillTO(ResourceSkill resourceSkill) {
		this.id = resourceSkill.getId();
		this.name = resourceSkill.getName();
		for(Property p : resourceSkill.getProperties()) {
			addProperty(resourceSkill, p);
		}
		//FIXME what's "; " used for?
		this.templateSkill = resourceSkill.getTemplateSkill().getName() + "; ";
	}

	/**
	 * @param resourceSkill
	 * @param property
	 */
	private void addProperty(ResourceSkill resourceSkill, Property property) {
		if(resourceSkill != null && property != null) {
			attributes.add(new AttributeTO(property, resourceSkill.getPropertyDesignator(property)));
			for(Property pTemp : property.getSubProperties()) {
				addProperty(resourceSkill, pTemp);
			}
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the templateSkill
	 */
	public String getTemplateSkill() {
		return templateSkill;
	}

	/**
	 * @param templateSkill the templateSkill to set
	 */
	public void setTemplateSkill(String templateSkill) {
		this.templateSkill = templateSkill;
	}

	/**
	 * @return the attributes
	 */
	public List<AttributeTO> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<AttributeTO> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SkillTO [id=" + id + ", name=" + name + ", templateSkill="
				+ templateSkill + ", attributes=" + attributes + 
				", active=" + active +"]";
	}
}
