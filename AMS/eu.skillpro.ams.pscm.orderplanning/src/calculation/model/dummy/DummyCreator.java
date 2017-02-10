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

package calculation.model.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.Setup;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.SkillSynchronizationType;

public class DummyCreator {
	
	private static final DummyCreator INSTANCE = new DummyCreator();
	
	private DummyCreator() {
		
	}
	
	public static DummyCreator getInstance() {
		return INSTANCE;
	}
	
	public void updateDummyDurations(List<ExecutableSkill> executableSkills) {
		Map<String, Integer> durationResourceMapping = createDurationResourceMapping();
		Map<String, Integer> durationResourceSkillMapping = createDurationResourceSkillMapping();
		for (ExecutableSkill exSkill : executableSkills) {
			for (ResourceExecutableSkill rexSkill : exSkill.getResourceExecutableSkills()) {
				ResourceSkill resourceSkill = rexSkill.getResourceSkill();
				Resource resource = resourceSkill.getResource();
				
				Integer durationResource = durationResourceMapping.get(resource.getName());
				Integer durationResourceSkill = durationResourceSkillMapping.get(resourceSkill.getName());
				if (durationResource != null) {
					rexSkill.setDuration(durationResource);
				}
				
				if (durationResourceSkill != null) {
					rexSkill.setDuration(durationResourceSkill);
				}
			}
		}
	}

	public List<ExecutableSkill> createDummyToFromNeutralExecutableSkills() {
		List<ExecutableSkill> executableSkills = new ArrayList<>();
		int id = 0;
		for (Entry<String, String> toFromNeutralResourcePair : createToFromNeutralResourceMapping().entrySet()) {
			String resourceName = toFromNeutralResourcePair.getKey();
			String resourceSkillName = toFromNeutralResourcePair.getValue();
			ResourceConfiguration neutralConfiguration = null;
			Set<ResourceConfiguration> otherConfigurations = new HashSet<>();
			Resource resource = null;
			ResourceSkill resourceSkill = null;
			for (Resource existingResource : SkillproService.getSkillproProvider().getAssetRepo().getAllAssignedResources()) {
				if (existingResource.getName().equalsIgnoreCase(resourceName)) {
					resource = existingResource;
					for (Setup setup : resource.getSetups()) {
						for (ResourceSkill rSkill : setup.getResourceSkills()) {
							if (rSkill.getName().equalsIgnoreCase(resourceSkillName)) {
								resourceSkill = rSkill;
							}
						}
					}
					
					for (ResourceConfiguration conf : resource.getResourceConfigurations()) {
						if (conf.getName().equalsIgnoreCase("neutral")) {
							neutralConfiguration = conf;
						} else {
							otherConfigurations.add(conf);
						}
					}
					break;
				}
			}
			
			for (ResourceConfiguration conf : otherConfigurations) {
				ResourceExecutableSkill toNeutralREx = new ResourceExecutableSkill("REX_" + resourceSkill.getTemplateSkill().getName(), 
						resourceSkill, conf, neutralConfiguration, 
						null, null, new ArrayList<PropertyDesignator>(), 
						0, 0, SkillSynchronizationType.NONE);
				
				ResourceExecutableSkill fromNeutralREx = new ResourceExecutableSkill("REX_" + resourceSkill.getTemplateSkill().getName(), 
						resourceSkill, neutralConfiguration, conf, 
						null, null, new ArrayList<PropertyDesignator>(), 
						0, 0, SkillSynchronizationType.NONE);
				
				Set<ResourceExecutableSkill> toNeutralRExSkills = new HashSet<>();
				toNeutralRExSkills.add(toNeutralREx);
				
				Set<ResourceExecutableSkill> fromNeutralRExSkills = new HashSet<>();
				fromNeutralRExSkills.add(fromNeutralREx);
				
				ExecutableSkill toNeutralEx = new ExecutableSkill("EX_" + resourceSkill.getTemplateSkill().getName(), null, toNeutralRExSkills);
				toNeutralEx.setId(toNeutralEx.getName() + "_toNeutral_" + id);
				
				ExecutableSkill fromNeutralEx = new ExecutableSkill("EX_" + resourceSkill.getTemplateSkill().getName(), null, fromNeutralRExSkills);
				fromNeutralEx.setId(fromNeutralEx.getName() + "_fromNeutral_" + id);
				
				id++;
				
				if (!executableSkills.contains(toNeutralEx)) {
					executableSkills.add(toNeutralEx);
				}
				
				if (!executableSkills.contains(fromNeutralEx)) {
					executableSkills.add(fromNeutralEx);
				}
				
			}
			
		}
		
		return executableSkills;
	}
	
	//<resourceName, resourceSkillString>
	private Map<String, String> createToFromNeutralResourceMapping() {
		Map<String, String> toFromNeutralResourceMapping = new HashMap<>();
		
		toFromNeutralResourceMapping.put("MobilePlatform", "Transport3DXYYaw");
		toFromNeutralResourceMapping.put("UR5", "Transport6D");
		toFromNeutralResourceMapping.put("KR5", "Transport6DToPlatform");
		
		return toFromNeutralResourceMapping;
	}
	
	//<resourceName, duration>
	private Map<String, Integer> createDurationResourceMapping() {
		Map<String, Integer> durationResourceMapping = new HashMap<>();

		durationResourceMapping.put("MobilePlatform", 15);
		
		return durationResourceMapping;
	}
	
	//<resourceSkillName, duration>
	private Map<String, Integer> createDurationResourceSkillMapping() {
		Map<String, Integer> durationResourceSkillMapping = new HashMap<>();

		durationResourceSkillMapping.put("Transport3DXYYaw", 20);
		durationResourceSkillMapping.put("OperateMilling", 60);
		
		return durationResourceSkillMapping;
	}
	
}
