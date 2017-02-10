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

import skillpro.model.products.ProductConfiguration;


public class Resource extends FactoryNode {
	private List<Setup> setups = new ArrayList<>();
	private Setup currentSetup;
	private State state = State.IN_CONFIGURATION;
	private List<ResourceConfigurationType> resourceConfigurationTypes = new ArrayList<ResourceConfigurationType>();
	private List<ResourceConfiguration> resourceConfigurations = new ArrayList<ResourceConfiguration>();
	private ResourceConfiguration currentResourceConfiguration;
	private ProductConfiguration currentProductConfiguration;
	private SEE responsibleSEE;
	
	public Resource() {
		super("Resource", true);
	}
	
	public Resource(String name, List<Setup> setups, FactoryNode parent) {
		super(name, true);
		this.setups = setups;
		this.setParent(parent);
	}
	
	public void setResponsibleSEE(SEE responsibleSEE) {
		this.responsibleSEE = responsibleSEE;
	}
	
	public SEE getResponsibleSEE() {
		return responsibleSEE;
	}
	
	public boolean addSetup(Setup setup) {
		if (setup != null && !setups.contains(setup)) {
			if (setup.getResource() == null || !setup.getResource().equals(this)) {
				setup.setResource(this);
			}
			return setups.add(setup);
		}
		return false;
	}
	
	public List<Setup> getSetups() {
		return setups;
	}
	
	public void setCurrentSetup(Setup currentSetup) {
		this.currentSetup = currentSetup;
	}
	
	public Setup getCurrentSetup() {
		return currentSetup;
	}
	
	public ResourceConfiguration getCurrentResourceConfiguration() {
		return currentResourceConfiguration;
	}
	
	public void setCurrentResourceConfiguration(ResourceConfiguration currentResourceConfiguration) {
		this.currentResourceConfiguration = currentResourceConfiguration;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State condition) {
		this.state = condition;
	}

	public List<ResourceConfiguration> getResourceConfigurations() {
		return resourceConfigurations;
	}
	
	public boolean addResourceConfiguration(ResourceConfiguration configuration) {
		if (configuration != null && !resourceConfigurations.contains(configuration)) {
			if (configuration.getResource() == null) {
				configuration.setResource(this);
			}
			return resourceConfigurations.add(configuration);
		}
		
		return false;
	}
	
	public void setResourceConfigurations(
			List<ResourceConfiguration> resourceConfigurations) {
		this.resourceConfigurations = resourceConfigurations;
	}

	public List<ResourceConfigurationType> getResourceConfigurationTypes() {
		return resourceConfigurationTypes;
	}

	public boolean addResourceConfigurationType(ResourceConfigurationType configurationType) {
		if (configurationType != null && !resourceConfigurationTypes.contains(configurationType)) {
			return resourceConfigurationTypes.add(configurationType);
		}
		
		return false;
	}
	
	public ProductConfiguration getCurrentProductConfiguration() {
		return currentProductConfiguration;
	}
	
	public void setCurrentProductConfiguration(ProductConfiguration currentProductConfiguration) {
		this.currentProductConfiguration = currentProductConfiguration;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
