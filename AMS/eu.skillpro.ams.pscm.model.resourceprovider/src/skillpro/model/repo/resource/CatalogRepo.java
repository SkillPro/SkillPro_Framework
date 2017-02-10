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

package skillpro.model.repo.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.repo.Repo;
import skillpro.model.skills.ResourceSkill;

public class CatalogRepo extends Repo<Resource> {
	
	private List<Setup> setups = new ArrayList<>();
	private Map<ResourceSkill, ResourceSkill> referenceResourceSkillsMapping = new HashMap<>();
	
	@Override
	public void wipeAllData() {
		super.wipeAllData();
		setups.clear();
	}
	
	public Map<ResourceSkill, ResourceSkill> getReferenceResourceSkillsMapping() {
		return referenceResourceSkillsMapping;
	}
	
	public List<Setup> getSetups() {
		return setups;
	}
	
	public List<Setup> getCorrespondingSetups(ResourceSkill resourceSkill) {
		List<Setup> configs = new ArrayList<>();
		for (Setup setup : setups) {
			List<ResourceSkill> resourceSkills = setup.getResourceSkills();
			for (ResourceSkill res : resourceSkills) {
				if (res.equals(resourceSkill)) {
					configs.add(setup);
				}
			}
		}
		return configs;
	}
	
	public List<Resource> getCorrespondingResources(Setup setup) {
		List<Resource> corrResources = new ArrayList<>();
		List<Resource> allResources = getEntities();
		for (Resource resource : allResources) {
			if (resource.getSetups().contains(setup)) {
				if (!corrResources.contains(resource)) {
					corrResources.add(resource);
				}
			}
		}
		return corrResources;
	}
	
	public List<Resource> getCorrespondingResources(ResourceSkill resourceSkill) {
		List<Setup> confs = getCorrespondingSetups(resourceSkill);
		List<Resource> corrResources = new ArrayList<>();
		for (Setup setup : confs) {
			for (Resource resource : list) {
				if (resource.getSetups().contains(setup)) {
					if (!corrResources.contains(resource)) {
						corrResources.add(resource);
					}
				}
			}
		}
		return corrResources;
	}
}
