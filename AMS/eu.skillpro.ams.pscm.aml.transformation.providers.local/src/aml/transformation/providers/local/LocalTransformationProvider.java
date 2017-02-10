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

package aml.transformation.providers.local;

import transformation.interfaces.ITransformable;
import aml.transformation.providers.ITransformationProvider;
import aml.transformation.repo.transformation.TransformationRepo;

public class LocalTransformationProvider implements ITransformationProvider {
	private TransformationRepo transformationRepo;
	
	@Override
	public TransformationRepo getTransformationRepo() {
		if (transformationRepo == null) {
			transformationRepo = new TransformationRepo();
			return transformationRepo;
		}
		return transformationRepo;
	}

	@Override
	public void putTransformable(Object element, Class<? extends ITransformable> transformable) {
		getTransformationRepo().getInterfaceTransformablesMapping().put(element, transformable);
	}
	
	@Override
	public void putElementTransformable(Object element, Class<? extends ITransformable> transformable) {
		if (element == null) {
			throw new IllegalArgumentException("Element should not be null");
		}
		getTransformationRepo().getAdapterTransformablesMapping().put(element, transformable);
	}
	
	@Override
	public void putTransformedElement(Object element, ITransformable transformedElement) {
		getTransformationRepo().getTransformedObjectsMap().put(element, transformedElement);
	}
	
	@Override
	public void wipeAllData() {
		getTransformationRepo().wipeAllData();
	}
}
