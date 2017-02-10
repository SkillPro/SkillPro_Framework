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

package amltransformation.providers.aml;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import aml.domain.Interface;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.transformation.repo.Repo;

public class DomainContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			return ((Collection<?>) inputElement).toArray();
		} else if (inputElement instanceof Repo) {
			return ((Repo) inputElement).getEntities().toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Interface) {
			return ((Interface) parentElement).getChildren().toArray();
		} else if (parentElement instanceof Role) {
			return ((Role) parentElement).getChildren().toArray();
		} else if (parentElement instanceof SystemUnit) {
			return new Object[] {};
		} else if (parentElement instanceof Hierarchy) {
			return ((Hierarchy<?>) parentElement).getChildren().toArray();
		} else if (parentElement instanceof Root) {
			return ((Root<?>) parentElement).getChildren().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Interface) {
			List<Interface> children = ((Interface) element).getChildren();
			return children != null && !children.isEmpty();
		} else if (element instanceof Role) {
			List<Role> children = ((Role) element).getChildren();
			return children != null && !children.isEmpty();
		} else if (element instanceof SystemUnit) {
			return false;
		} else if (element instanceof Hierarchy<?>) {
			List<?> children = ((Hierarchy<?>) element).getChildren();
			return children != null && !children.isEmpty();
		} else if (element instanceof Root<?>) {
			List<?> children = ((Root<?>) element).getChildren();
			return children != null && !children.isEmpty();
		}
		return false;
	}
}
