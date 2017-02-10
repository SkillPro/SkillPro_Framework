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

package skillpro.providers.property;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;

public class PropertyTableLabelProvider extends LabelProvider implements ITableLabelProvider {
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			if (element instanceof Property) {
				return ((Property) element).getName();
			} else if (element instanceof PropertyDesignator) {
				return ((PropertyDesignator) element).getProperty().getName();
			} else {
				return "unknown";
			}
		case 1:
			if (element instanceof Property) {
				return ((Property) element).getType().name();
			} else if (element instanceof PropertyDesignator) {
				return ((PropertyDesignator) element).getProperty().getType().name();
			}
		case 2:
			if (element instanceof Property) {
				return "";
			} else if (element instanceof PropertyDesignator) {
				return ((PropertyDesignator) element).getValue();
			}
		case 3:
			if (element instanceof Property) {
				return ((Property) element).getUnit();
			} else if (element instanceof PropertyDesignator) {
				return ((PropertyDesignator) element).getProperty().getUnit();
			}
		case 4:
			if (element instanceof Property) {
				return "";
			} else if (element instanceof PropertyDesignator) {
				String constraint = constraintsToString(((PropertyDesignator) element).getConstraints());
				if (constraint == null || constraint.isEmpty()) {
					return "No constraints";
				} else {
					return constraint;
				}
			}
		}
		return null;
	}
	
	private String constraintsToString(List<PropertyConstraint> constraints) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < (constraints.size() - 1)) {
			sb.append(constraints.get(i).toString());
			sb.append(", ");
			i++;
		}
		if (i < constraints.size()) {
			sb.append(constraints.get(i).toString());
		}
		
		return sb.toString();
	}
}
