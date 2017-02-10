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

package aml.transformation.repo.transformation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import transformation.interfaces.ITransformable;

public class TransformationRepo {
	private Map<Object, Class<? extends ITransformable>> interfaceTransformablesMapping = new HashMap<>();
	private Map<Object, Class<? extends ITransformable>> pivotElementToAdapterTransformableMapping = new HashMap<>();
	private Map<Object, Class<? extends ITransformable>> adapterTransformablesMapping = new HashMap<>();
	private Map<Object, ITransformable> transformedObjectsMap = new HashMap<>();
	private Map<ITransformable, Object> reverseTransformedObjectsMap = new HashMap<>();
	
	public Map<Object, Class<? extends ITransformable>> getAdapterTransformablesMapping() {
		return adapterTransformablesMapping;
	}
	
	public Map<Object, Class<? extends ITransformable>> getInterfaceTransformablesMapping() {
		return interfaceTransformablesMapping;
	}
	
	public Map<Object, ITransformable> getTransformedObjectsMap() {
		return transformedObjectsMap;
	}
	
	public Map<ITransformable, Object> getReverseTransformedObjectsMap() {
		return reverseTransformedObjectsMap;
	}
	
	public Map<Object, Class<? extends ITransformable>> getPivotElementToAdapterTransformableMapping() {
		return pivotElementToAdapterTransformableMapping;
	}
	
	public Map<Class<? extends ITransformable>, Object> getInvertedPivotAdapterTransformableMapping() {
		Map<Class<? extends ITransformable>, Object> invertedMapping = new HashMap<>();
		
		for (Entry<Object, Class<? extends ITransformable>> entry : pivotElementToAdapterTransformableMapping.entrySet()) {
			invertedMapping.put(entry.getValue(), entry.getKey());
		}
		
		return invertedMapping;
	}
	
	public void wipeAllData() {
		interfaceTransformablesMapping.clear();
		adapterTransformablesMapping.clear();
		transformedObjectsMap.clear();
		reverseTransformedObjectsMap.clear();
		pivotElementToAdapterTransformableMapping.clear();
	}
}
