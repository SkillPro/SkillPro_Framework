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

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import skillpro.model.assets.FactoryNode;
import skillpro.model.repo.resource.AssetRepo;

public class AssetOnlyTreeContentProvider implements ITreeContentProvider {
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
		if (parentElement instanceof FactoryNode) {
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
		if (element instanceof FactoryNode) {
			List<FactoryNode> subNodes = ((FactoryNode) element).getSubNodes();
			return subNodes != null && !subNodes.isEmpty();
		}
		return false;
	}

}