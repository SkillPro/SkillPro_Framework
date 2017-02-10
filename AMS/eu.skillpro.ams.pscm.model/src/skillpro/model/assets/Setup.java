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

package skillpro.model.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import skillpro.model.skills.ResourceSkill;

public class Setup {
	private String name;
	private String id = UUID.randomUUID().toString();
	private Tool tool;
	private List<ResourceSkill> resourceSkills = new ArrayList<>();
	private Resource resource;
	
	public Setup() {
		this("", null);
	}
	
	public Setup(String name, Resource resource) {
		this.name = name;
		setResource(resource);
	}
	
	public Setup(String name, Resource resource, List<ResourceSkill> resourceSkills) {
		this(name, resource);
		this.resourceSkills = resourceSkills;
		
	}

	public List<ResourceSkill> getResourceSkills() {
		return resourceSkills;
	}
	
	public boolean addResourceSkill(ResourceSkill resourceSkill) {
		if (resourceSkill != null && !resourceSkills.contains(resourceSkill)) {
			return resourceSkills.add(resourceSkill);
		}
		return false;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		if (this.resource != null && this.resource.equals(resource)) {
			this.resource = resource;
			if (resource != null) {
				resource.addSetup(this);
			}
		} else {
			this.resource = resource;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Tool getTool() {
		return tool;
	}
	
	public void setTool(Tool tool) {
		this.tool = tool;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, resourceSkills);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Setup other = (Setup) obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(resourceSkills, other.resourceSkills);
	}
}
