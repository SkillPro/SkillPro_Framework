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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.AttributeDesignator;
import aml.model.Hierarchy;
import aml.model.InterfaceDesignator;
import aml.model.Root;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class ParserLabelProvider extends LabelProvider {
	private Image rootIcon;
	private Image attrIcon;
	private Image hierarchyIcon;
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Root<?>) {
			return getRootImage();
		} else if (element instanceof Hierarchy<?>) {
			return getHierarchyImage();
		} else if (element instanceof AttributeDesignator) {
			return getAttributeImage();
		}
		return null;
	}
	
	private Image getAttributeImage() {
		if (attrIcon == null) {
			attrIcon = IconActivator.getImageDescriptor("icons/aml/attribute_value.png").createImage();
		}
		return attrIcon;
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
			return ((Root<?>)element).toString();
		} else if (element instanceof Hierarchy<?>) {
			String result = "";
			Hierarchy<?> hierarchy = (Hierarchy<?>) element;
			result += hierarchy.getName();
			List<AttributeDesignator> designators = hierarchy.getElement().getDesignators();
			if (designators != null) {
				result += ": " + designators.size();
			}
			Object hieElement = ((Hierarchy<?>) element).getElement();
			Role requiredRole = null;
			if (hieElement.getClass().isAssignableFrom(InternalElement.class)) {
				requiredRole = ((InternalElement) hieElement).getRequiredRole();
				result +=  ", reqRole: " + requiredRole.getName();
			}
			return result;
		} else if (element instanceof AttributeDesignator) {
			AttributeDesignator attribute = ((AttributeDesignator) element);
			return attribute.getAttribute().getName();
		} else if (element instanceof InterfaceDesignator) {
			return ((InterfaceDesignator) element).getName();
		}
		return super.getText(element);
	}
}