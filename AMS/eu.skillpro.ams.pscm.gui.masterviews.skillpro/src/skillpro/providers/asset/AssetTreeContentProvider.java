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

package skillpro.providers.asset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.Setup;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.repo.resource.AssetRepo;
import skillpro.model.skills.ResourceSkill;

public class AssetTreeContentProvider implements ITreeContentProvider {
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null) {
			return new Object[] {};
		} else if (inputElement instanceof AssetRepo)
			return ((AssetRepo) inputElement).getRootAssets().toArray();
		else if (inputElement instanceof Collection<?>)
			return ((Collection<?>) inputElement).toArray();
		else
			return new Object[] {};
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Resource) {
			List<Object> objects = new ArrayList<>();
			List<Setup> setups = ((Resource) parentElement).getSetups();
			if (setups != null && !setups.isEmpty()) {
				objects.addAll(setups);
			}
			List<ResourceConfiguration> resourceConfigurations = ((Resource) parentElement).getResourceConfigurations();
			if (resourceConfigurations != null && !resourceConfigurations.isEmpty()) {
				objects.addAll(resourceConfigurations);
			}
			
			if (((Resource) parentElement).getCurrentProductConfiguration() != null) {
				objects.add(((Resource) parentElement).getCurrentProductConfiguration());
			}
			
			if (!objects.isEmpty()) {
				return objects.toArray();
			}
		} else if (parentElement instanceof Setup) {
			List<ResourceSkill> resourceSkills = ((Setup) parentElement).getResourceSkills();
			if (resourceSkills != null && !resourceSkills.isEmpty()) {
				return resourceSkills.toArray();
			}
		} else if (parentElement instanceof FactoryNode) {
			FactoryNode parent = (FactoryNode) parentElement;
			if (parent.getSubNodes() != null && !parent.getSubNodes().isEmpty()) {
				return parent.getSubNodes().toArray();
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof FactoryNode) {
			FactoryNode child = (FactoryNode) element;
			return child.getParent();
			
		} else {
			return null;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Resource) {
			Resource resource = (Resource) element;
			List<Setup> setups = resource.getSetups();
			List<ResourceConfiguration> rConfs = resource.getResourceConfigurations();
			ProductConfiguration pConf = resource.getCurrentProductConfiguration();
			return (setups != null && !setups.isEmpty()) || rConfs != null && !rConfs.isEmpty()
					|| (pConf != null && pConf.getProductQuantities() != null && !pConf.getProductQuantities().isEmpty());
		} else if (element instanceof Setup) {
			List<ResourceSkill> resourceSkills = ((Setup) element).getResourceSkills();
			return resourceSkills != null && !resourceSkills.isEmpty();
		} else if (element instanceof FactoryNode) {
			FactoryNode node = (FactoryNode) element;
			return node.getSubNodes() != null && !node.getSubNodes().isEmpty();
		}
		return false;
	}

}
