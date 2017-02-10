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
import java.util.Set;

import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.skills.ResourceSkill;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IPostConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;

public class PostConfigurationAdapter extends TransformableAdapterTemplate implements IPostConfigurationTransformable {
	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context)
			throws InstantiationException, IllegalAccessException {
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
		
		//resourceSkill
		ResourceSkill resourceSkill = null;
		Hierarchy<InternalElement> parent = object.getParent();
		if (parent != null && context.contains(parent)) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(parent.getElement());
			ITransformable transformedOwner = ((IResourceSkillTransformable) transClass.newInstance())
					.transform(parent, context);
			resourceSkill = (ResourceSkill) transformedOwner.getElement();
		}
		
		//resourceConfiguration
		if (object.getChildren() != null && object.getChildren().size() > 1) {
			throw new IllegalArgumentException("Post-Configuration size should be 1 for this ResourceSkill: " + resourceSkill);
		}
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
			ITransformable transformedConf = ((IResourceConfigurationTransformable) transClass.newInstance())
					.transform(child, context);
			resourceSkill.setPostConfiguration((ResourceConfiguration) transformedConf.getElement());
		}
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}
	
	@Override
	public Object reverseTransform() {
		return null;
	}

	@Override
	public String getTransformableName() {
		return null;
	}

	@Override
	public String getTransformableID() {
		return null;
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
		return null;
	}
}
