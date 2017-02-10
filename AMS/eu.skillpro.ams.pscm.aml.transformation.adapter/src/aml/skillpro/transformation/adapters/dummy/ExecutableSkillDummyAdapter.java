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

package aml.skillpro.transformation.adapters.dummy;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IExecutableSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceExecutableSkillTransformable;

public class ExecutableSkillDummyAdapter extends TransformableAdapterTemplate implements IExecutableSkillTransformable {
	private ExecutableSkillDummy dummy;
	
	public ExecutableSkillDummyAdapter() {
		this(new ExecutableSkillDummy());
	}
	
	public ExecutableSkillDummyAdapter(ExecutableSkillDummy dummy) {
		this.dummy = dummy;
	}
	
	@Override
	public String getTransformableName() {
		return dummy.getName();
	}

	@Override
	public String getTransformableID() {
		return "ID_" + dummy.getName();
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
		return dummy;
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
		
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		//add resourceExecutableSkills
		for (IResourceExecutableSkillTransformable trans : getTransformableResourceExecutableSkills()) {
			objectHie.addChild((Hierarchy<InternalElement>) trans.reverseTransform());
		}
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}

	@Override
	public List<IResourceExecutableSkillTransformable> getTransformableResourceExecutableSkills() {
		List<IResourceExecutableSkillTransformable> transformables = new ArrayList<>();
		for (ResourceExecutableSkillDummy res : dummy.getDummies()) {
			transformables.add(new ResourceExecutableSkillDummyAdapter(res));
		}
		return transformables;
	}
	
}