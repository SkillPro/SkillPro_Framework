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

import java.util.List;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.AttributeDesignator;
import aml.model.Hierarchy;
import aml.model.Root;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class DomainLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider{
	private Image rootIcon;
	private Image hierarchyIcon;
	private Image roleIcon;
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Root<?>) {
			return getRootImage();
		} else if (element instanceof Hierarchy<?>) {
			return getHierarchyImage();
		} else if (element instanceof Role) {
			return getRoleImage();
		}
		
		return null;
	}
	
	private Image getRoleImage() {
		if (roleIcon == null) {
			roleIcon = IconActivator.getImageDescriptor("icons/aml/role.png").createImage();
		}
		return roleIcon;
	}

	private Image getHierarchyImage() {
		if (hierarchyIcon == null) {
			hierarchyIcon = IconActivator.getImageDescriptor("icons/aml/hierarchy.png").createImage();
		}
		return hierarchyIcon;
	}

	private Image getRootImage() {
		if (rootIcon == null) {
			rootIcon = IconActivator.getImageDescriptor("icons/aml/root.png").createImage();
		}
		return rootIcon;
	}

	@Override
	public String getText(Object element) {
		if (element == null) {
			return "";
		}
		if (element instanceof Root<?>) {
			return ((Root<?>) element).toString();
		} else if (element instanceof Hierarchy<?>) {
			Hierarchy<?> hierarchy = (Hierarchy<?>) element;
			List<AttributeDesignator> designators = hierarchy.getElement().getDesignators();
			if (designators != null) {
				return hierarchy.getName() + ": " + designators.size();
			}
			return "Null designators";
		} else if (element instanceof Role) {
			return ((Role) element).getName();
		} else if (element instanceof Interface) {
			return ((Interface) element).getName();
		} else if (element instanceof InternalElement) {
			System.err.println("Wow, a wild internal element has appeared!");
			return ((InternalElement) element).getName();
		} else if (element instanceof SystemUnit) {
			return ((SystemUnit) element).getName();
			
		}
		return super.getText(element);
	}

	@Override
	public Font getFont(Object element) {
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getImage(element);
		default:
			break;
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getText(element);
		case 2:
			return getID(element);
		default:
			break;
		}
		return null;
	}

	private String getID(Object element) {
		if (element == null) {
			return "";
		}
		if (element instanceof Root<?>) {
			return "";
		} else if (element instanceof Hierarchy<?>) {
			Hierarchy<?> hierarchy = (Hierarchy<?>) element;
			List<AttributeDesignator> designators = hierarchy.getElement().getDesignators();
			if (designators != null) {
				return hierarchy.getName() + ": " + designators.size();
			}
			return "Null designators";
		} else if (element instanceof Role) {
			return "";
		} else if (element instanceof Interface) {
			return "";
		} else if (element instanceof InternalElement) {
			return ((InternalElement) element).getId();
		} else if (element instanceof SystemUnit) {
			return "";
		}
		return "";
	}
}
