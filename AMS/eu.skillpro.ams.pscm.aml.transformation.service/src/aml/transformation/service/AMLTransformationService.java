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

package aml.transformation.service;

import aml.transformation.providers.IAMLProvider;
import aml.transformation.providers.ITransformationProvider;

public class AMLTransformationService {
	private static IAMLProvider amlProvider;
	private static ITransformationProvider transformationProvider;
	
	public static ITransformationProvider getTransformationProvider() {
		return transformationProvider;
	}
	
	public static void setTransformationProvider(ITransformationProvider transformationProvider) {
		AMLTransformationService.transformationProvider = transformationProvider;
	}
	
	
	public static IAMLProvider getAMLProvider() {
		return amlProvider;
	}
	
	public static boolean isAMLProviderDirty() {
		if (amlProvider == null) {
			return false;
		} else {
			return amlProvider.isDirty();
		}
	}
	
	public static void setAMLProvider(IAMLProvider amlProvider) {
		AMLTransformationService.amlProvider = amlProvider;
	}
}
