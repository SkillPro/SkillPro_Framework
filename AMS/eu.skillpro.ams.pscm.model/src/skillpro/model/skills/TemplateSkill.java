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

import skillpro.model.properties.Property;

public class TemplateSkill extends Skill {
	private TemplateSkill parent;
	private List<TemplateSkill> children = new ArrayList<>();
	
	public TemplateSkill() {
		super("");
	}
	
	public TemplateSkill(String name) {
		super(name);
	}
	
	public TemplateSkill(String name, List<Property> properties) {
		super(name, properties);
	}
	
	public TemplateSkill(String name, List<Property> properties, TemplateSkill parent) {
		super(name, properties);
		setParent(parent);
	}
	
	public void setParent(TemplateSkill parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
			for (Property property : parent.getProperties()) {
				if (!getProperties().contains(property)) {
					getProperties().add(property);
				}
			}
		}
	}
	
	public TemplateSkill getParent() {
		return parent;
	}
	
	public List<TemplateSkill> getChildren() {
		return children;
	}
	
	public boolean addChild(TemplateSkill child) {
		if (child != null && !children.contains(child)) {
			if (children.add(child)) {
				child.setParent(this);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateSkill other = (TemplateSkill) obj;
		return Objects.equals(parent, other.parent);
	}
}
