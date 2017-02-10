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

import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.Requirement;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IPostRequirementTransformable;
import aml.skillpro.transformation.interfaces.IPrePostRequirementTransformable;
import aml.skillpro.transformation.interfaces.IPreRequirementTransformable;

public class PrePostRequirementAdapter extends TransformableAdapterTemplate implements IPrePostRequirementTransformable {
	private PrePostRequirement prePostRequirement;
	
	public PrePostRequirementAdapter() {
	}
	
	public PrePostRequirementAdapter(PrePostRequirement prePostRequirement) {
		this.prePostRequirement = prePostRequirement;
	}
	
	@Override
	public String getTransformableName() {
		//FIXME new name?
		return "PrePost-Requirement";
	}

	@Override
	public String getTransformableID() {
		return UUID.randomUUID().toString();
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
		return prePostRequirement;
	}

	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context) throws InstantiationException,
			IllegalAccessException {
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
		if (object.getChildren().size() != 2) {
			throw new IllegalArgumentException("Pre-Post Requirement IE has to have 2 children!");
		}
		Requirement preRequirement = null;
		Requirement postRequirement = null;
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
			if (IPreRequirementTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedConf = ((IPreRequirementTransformable) transClass.newInstance())
						.transform(child, context);
				if (preRequirement != null) {
					throw new IllegalArgumentException("Found more than 1 preRequirement");
				}
				preRequirement = (Requirement) transformedConf.getElement();
			} else if (IPostRequirementTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedConf = ((IPostRequirementTransformable) transClass.newInstance())
						.transform(child, context);
				if (postRequirement != null) {
					throw new IllegalArgumentException("Found more than 1 postRequirement");
				}
				postRequirement = (Requirement) transformedConf.getElement();
			} else {
				throw new IllegalArgumentException("Unexpected TransClass: " + transClass);
			}
		}
		prePostRequirement = new PrePostRequirement(preRequirement, postRequirement);
		
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
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
		
		
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		//add Pre and Post Requirement as children
		Hierarchy<InternalElement> preReqHie = (Hierarchy<InternalElement>) getTransformablePreRequirement()
				.reverseTransform();
		Hierarchy<InternalElement> postReqHie = (Hierarchy<InternalElement>) getTransformablePostRequirement()
				.reverseTransform();
		
		preReqHie.setParent(objectHie);
		postReqHie.setParent(objectHie);
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}

	@Override
	public IPreRequirementTransformable getTransformablePreRequirement() {
		if (prePostRequirement.getPreRequirement() == null) {
			throw new IllegalArgumentException("Pre-Requirement can't be null");
		}
		return new PreRequirementAdapter(prePostRequirement.getPreRequirement());
	}

	@Override
	public IPostRequirementTransformable getTransformablePostRequirement() {
		if (prePostRequirement.getPostRequirement() == null) {
			throw new IllegalArgumentException("Post-Requirement can't be null");
		}
		return new PostRequirementAdapter(prePostRequirement.getPostRequirement());
	}

	@Override
	public int hashCode() {
		return Objects.hash(prePostRequirement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrePostRequirementAdapter other = (PrePostRequirementAdapter) obj;
		return Objects.equals(prePostRequirement, other.prePostRequirement);
	}
}
