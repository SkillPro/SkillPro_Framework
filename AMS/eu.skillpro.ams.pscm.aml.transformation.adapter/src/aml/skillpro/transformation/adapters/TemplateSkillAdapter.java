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
import java.util.Objects;
import java.util.Set;

import skillpro.model.properties.Property;
import skillpro.model.skills.TemplateSkill;
import transformation.interfaces.ITransformable;
import aml.domain.Role;
import aml.model.AttributeDesignator;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IPropertyTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;

public class TemplateSkillAdapter extends TransformableAdapterTemplate implements ITemplateSkillTransformable {
	private TemplateSkill templateSkill;
	
	public TemplateSkillAdapter() {
		this(new TemplateSkill());
	}
	
	public TemplateSkillAdapter(TemplateSkill templateSkill) {
		this.templateSkill = templateSkill;
	}
	
	@Override
	public TemplateSkill getElement() {
		return templateSkill;
	}
	
	@Override
	public ITransformable transform(Role object, Set<Object> context) {
		if (!context.contains(object)) {
			return null;
		}
		//returns a transformed element if it has already been transformed before.
		if (transformedObjectsMap.containsKey(object)) {
			return transformedObjectsMap.get(object);
		}
		templateSkill.setName(object.getName());
		Role parentRole = object.getReferencedRole();
		if (parentRole != null && context.contains(parentRole)) {
			//transform will put this transformed parent into transformed objects map as well.
			ITransformable transformedParent = new TemplateSkillAdapter().transform(parentRole, context);
			templateSkill.setParent((TemplateSkill) transformedParent.getElement());
		}
		
		//properties
		for (AttributeDesignator des : object.getDesignators()) {
			//transform will put this transformed prop into transformed objects map as well.
			IPropertyTransformable transformedProp = new PropertyAdapter().transform(des.getAttribute());
			templateSkill.addProperty((Property) transformedProp.getElement());
		}
		
		transformedObjectsMap.put(object, this);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object reverseTransform() {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		Hierarchy<Role> parentHie;
		if (getTransformableParent() == null) {
			parentHie = templateSkillHierarchy;
		} else {
			parentHie = (Hierarchy<Role>) getTransformableParent().reverseTransform();
		}
		
		Role role = null;
		//find role
		for (Object obj : interfaceTransformablesMapping.keySet()) {
			if (obj instanceof Role) {
				if (((Role) obj).getName().equals(getTransformableName())) {
					role = (Role) obj;
				}
			}
		}
		if (role == null) {
			Role referencedRole = parentHie.getElement();
			role = new Role(getTransformableName(), referencedRole);
		}
		
		for (IPropertyTransformable propTrans : getTransformableProperties()) {
			role.addAttribute(propTrans.reverseTransform());
		}
		
		Hierarchy<Role> roleHie = new Hierarchy<Role>(role.getName(), role);
		if (parentHie != templateSkillHierarchy) {
			roleHie.setParent(parentHie);
		}
		//find template skill then set it as parent of roleHie
		
		//start ritual
		reverseTransformedObjectsMap.put(this, roleHie);
		return roleHie;
		//end ritual
	}

	@Override
	public String getTransformableName() {
		return this.templateSkill.getName();
	}

	@Override
	public String getTransformableID() {
		return this.templateSkill.getId();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		List<ITransformable> transformables = new ArrayList<>();
		for (TemplateSkill child: this.templateSkill.getChildren()) {
			transformables.add(new TemplateSkillAdapter(child));
		}
		return transformables;
	}

	@Override
	public ITemplateSkillTransformable getTransformableParent() {
		ITemplateSkillTransformable transformableParent = null;
		if (templateSkill.getParent() != null) {
			transformableParent = new TemplateSkillAdapter(templateSkill.getParent());
		}
		return transformableParent;
	}

	@Override
	public List<IPropertyTransformable> getTransformableProperties() {
		List<IPropertyTransformable> propTrans = new ArrayList<>();
		for (Property prop : this.templateSkill.getProperties()) {
			propTrans.add(new PropertyAdapter(prop));
		}
		return propTrans;
	}

	@Override
	public int hashCode() {
		return Objects.hash(templateSkill);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplateSkillAdapter other = (TemplateSkillAdapter) obj;
		return Objects.equals(templateSkill, other.templateSkill);
	}
}
