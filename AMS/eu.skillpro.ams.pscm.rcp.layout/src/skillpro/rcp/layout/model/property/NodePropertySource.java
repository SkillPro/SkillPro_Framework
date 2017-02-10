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

package skillpro.rcp.layout.model.property;

import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import skillpro.rcp.layout.model.GEFNode;
import skillpro.rcp.layout.model.GEFWorkingPlace;

public class NodePropertySource implements IPropertySource {
	private GEFNode node;

	public NodePropertySource(GEFNode node) {
		this.node = node;
	}

	/**
	 * Returns the property value when this property source is used as a value.
	 * We can return <tt>null</tt> here
	 */
	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();

		properties.add(new TextPropertyDescriptor(GEFNode.PROPERTY_XCOORD,
				"x-Coordinate"));
		properties.add(new TextPropertyDescriptor(GEFNode.PROPERTY_YCOORD,
				"y-Coordinate"));
		properties.add(new TextPropertyDescriptor(GEFNode.PROPERTY_WIDTH,
				"Width"));
		properties.add(new TextPropertyDescriptor(GEFNode.PROPERTY_HEIGHT,
				"Height"));
		properties.add(new TextPropertyDescriptor(GEFNode.PROPERTY_RENAME,
				"Name"));

		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(GEFNode.PROPERTY_RENAME))
			return node.getName();
		if (id.equals(GEFNode.PROPERTY_XCOORD))
			return Integer.toString(node.getLayout().getTopLeft().x());
		if (id.equals(GEFNode.PROPERTY_YCOORD))
			return Integer.toString(node.getLayout().getTopLeft().y());
		if (id.equals(GEFNode.PROPERTY_WIDTH))
			return Integer.toString(node.getLayout().width);
		if (id.equals(GEFNode.PROPERTY_HEIGHT))
			return Integer.toString(node.getLayout().height);
		if (id.equals(GEFWorkingPlace.PROPERTY_COLOR))
			return ((GEFWorkingPlace) node).getColor().getRGB();
		return null;
	}

	/**
	 * Returns if the property with the given id has been changed since its
	 * initial default value. We do not handle default properties, so we return
	 * <tt>false</tt>.
	 */
	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	/**
	 * Reset a property to its default value. Since we do not handle default
	 * properties, we do nothing.
	 */
	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(GEFNode.PROPERTY_XCOORD)) {
			try {
				int x = Integer.parseInt((String) value);
				int y = node.getLayout().getTopLeft().y();
				int width = node.getLayout().width;
				int height = node.getLayout().height;

				node.setLayout(new Rectangle(x, y, width, height));
			} catch (NumberFormatException e) {
			}
		}
		if (id.equals(GEFNode.PROPERTY_WIDTH)) {
			try {
				int x = node.getLayout().getTopLeft().x();
				int y = node.getLayout().getTopLeft().y();
				int width = Integer.parseInt((String) value);
				int height = node.getLayout().height;

				node.setLayout(new Rectangle(x, y, width, height));
			} catch (NumberFormatException e) {
			}
		}
		if (id.equals(GEFNode.PROPERTY_HEIGHT)) {
			try {
				int x = node.getLayout().getTopLeft().x();
				int y = node.getLayout().getTopLeft().y();
				int width = node.getLayout().width;
				int height = Integer.parseInt((String) value);

				node.setLayout(new Rectangle(x, y, width, height));
			} catch (NumberFormatException e) {
			}
		}
		if (id.equals(GEFNode.PROPERTY_YCOORD)) {
			try {
				int x = node.getLayout().getTopLeft().x();
				int y = Integer.parseInt((String) value);
				int width = node.getLayout().width;
				int height = node.getLayout().height;

				node.setLayout(new Rectangle(x, y, width, height));
			} catch (NumberFormatException e) {
			}
		}
		if (id.equals(GEFWorkingPlace.PROPERTY_COLOR)) {
			((GEFWorkingPlace) node).setColor(new Color(null, (RGB) value));
		}
		if (id.equals(GEFNode.PROPERTY_RENAME))
			node.setName((String) value);
	}
}
