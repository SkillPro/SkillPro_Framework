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
import java.util.List;

import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.repo.Repo;
import skillpro.model.service.SkillproService;

public class AssetRepo extends Repo<FactoryNode> {
	
	public List<FactoryNode> getRootAssets() {
		List<FactoryNode> rootEntities = new ArrayList<>();
		for (FactoryNode entity : list) {
			if (entity.getParent() == null) {
				rootEntities.add(entity);
			}
		}
		return rootEntities;
	}
	
	public List<Resource> getAllAssignedResources() {
		List<Resource> resources = getAllResources();
		resources.retainAll(list);
		return resources;
	}
	
	private List<Resource> getAllResources() {
		List<Resource> resources = new ArrayList<>();
		for (FactoryNode node : getRootAssets()) {
			recursiveResourceSearch(resources, node);
		}
		//dunno, should we just add catalogs here?
		resources.addAll(SkillproService.getSkillproProvider().getCatalogRepo().getEntities());
		return resources;
	}
	
	private static void recursiveResourceSearch(List<Resource> result, FactoryNode node) {
		if (node instanceof Resource) {
			result.add((Resource) node);
		} else {
			for (FactoryNode subNode : node.getSubNodes()) {
				recursiveResourceSearch(result, subNode);
			}
		}
	}
}
