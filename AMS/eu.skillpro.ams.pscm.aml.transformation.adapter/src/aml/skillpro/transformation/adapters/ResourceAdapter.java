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

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.assets.Setup;
import skillpro.model.products.ProductConfiguration;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTypeTransformable;
import aml.skillpro.transformation.interfaces.IResourceTransformable;
import aml.skillpro.transformation.interfaces.ISetupTransformable;

public class ResourceAdapter extends FactoryNodeAdapter implements IResourceTransformable {
	public ResourceAdapter() {
		super(new Resource());
	}
	
	public ResourceAdapter(Resource resource) {
		super(resource);
	}
	
	@Override
	public List<IResourceConfigurationTransformable> getTransformableResourceConfigurations() {
		List<IResourceConfigurationTransformable> configurations = new ArrayList<>();
		for (ResourceConfiguration config : ((Resource) getElement()).getResourceConfigurations()) {
			configurations.add(new ResourceConfigurationAdapter(config));
		}
		return configurations;
	}
	
	@Override
	public List<IResourceConfigurationTypeTransformable> getTransformableResourceConfigurationTypes() {
		List<IResourceConfigurationTypeTransformable> configurationTypes = new ArrayList<>();
		for (ResourceConfigurationType type : ((Resource) getElement()).getResourceConfigurationTypes()) {
			configurationTypes.add(new ResourceConfigurationTypeAdapter(type));
		}
		return configurationTypes;
	}
	
	@Override
	public List<ISetupTransformable> getTransformableSetups() {
		List<ISetupTransformable> setups = new ArrayList<>();
		for (Setup type : ((Resource) getElement()).getSetups()) {
			setups.add(new SetupAdapter(type));
		}
		return setups;
	}

	@Override
	public IProductConfigurationTransformable getTransformableProductConfiguration() {
		ProductConfiguration currentProductConfiguration = ((Resource) getElement()).getCurrentProductConfiguration();
		if (currentProductConfiguration != null) {
			return new ProductConfigurationAdapter(currentProductConfiguration);
		}
		return null;
	}
}
