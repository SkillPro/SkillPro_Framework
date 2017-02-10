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

package skillpro.rcp.layout.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;

import skillpro.model.assets.FactoryNode;
import skillpro.rcp.layout.model.property.NodePropertySource;

public class GEFNode implements IAdaptable {
	private String name;
	private Rectangle layout;
	private List<GEFNode> children;
	private GEFNode parent;
	private PropertyChangeSupport listeners;
	private IPropertySource propertySource;
	private FactoryNode node;
	private double rotation;

	public static final String PROPERTY_XCOORD = "NodeXCoord";
	public static final String PROPERTY_YCOORD = "NodeYCoord";
	public static final String PROPERTY_WIDTH = "NodeWidth";
	public static final String PROPERTY_HEIGHT = "NodeHeight";
	public static final String PROPERTY_LAYOUT = "NodeLayout";
	public static final String PROPERTY_ADD = "NodeAddChild";
	public static final String PROPERTY_REMOVE = "NodeRemoveChild";
	public static final String PROPERTY_RENAME = "NodeRename";

	public GEFNode(FactoryNode node) {
		this.layout = new Rectangle(0, 0, 0, 0);
		this.children = new ArrayList<GEFNode>();
		this.parent = null;
		this.listeners = new PropertyChangeSupport(this);
		this.propertySource = null;
		this.name = "";
		this.node = node;
		this.rotation = 0.0;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		getListeners().firePropertyChange(PROPERTY_RENAME, oldName, this.name);
	}

	public FactoryNode getFactoryNode() {
		return this.node;
	}

	public void setFactoryNode(FactoryNode node) {
		this.node = node;
	}

	public String getName() {
		return this.name;
	}

	public void rotate(double rotation) {
		// TODO implement this function
	}

	public double getRotation() {
		return this.rotation;
	}

	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = this.layout;
		this.layout = newLayout;
		node.getCurrentCoordinates().x = newLayout.x;
		node.getCurrentCoordinates().y = newLayout.y;
		node.setLength(newLayout.width);
		node.setWidth(newLayout.height);
		getListeners()
				.firePropertyChange(PROPERTY_LAYOUT, oldLayout, newLayout);
	}

	public Rectangle getLayout() {
		return this.layout;
	}

	public boolean addChild(GEFNode child) {
		boolean b = this.children.add(child);
		if (b) {
			child.setParent(this);
			getListeners().firePropertyChange(PROPERTY_ADD, null, child);
		}
		return b;
	}

	public boolean removeChild(GEFNode child) {
		boolean b = this.children.remove(child);
		if (b)
			getListeners().firePropertyChange(PROPERTY_REMOVE, child, null);
		return b;
	}

	public List<GEFNode> getChildrenArray() {
		return this.children;
	}

	public void removeAllChildren() {
		while (this.children.size() > 0) {
			this.removeChild(this.children.get(0));
		}
	}

	public void setParent(GEFNode parent) {
		this.parent = parent;
	}

	public GEFNode getParent() {
		return this.parent;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	public PropertyChangeSupport getListeners() {
		return listeners;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null)
				propertySource = new NodePropertySource(this);
			return propertySource;
		}
		return null;
	}

	public boolean contains(GEFNode child) {
		return children.contains(child);
	}

	public boolean isPlaceable(GEFNode child, Rectangle newLayout) {
		boolean placeable = true;
		int i = 0;
		while ((placeable) && (i < this.children.size())) {
			if (this.children.get(i) != child) {
				placeable = !this.children.get(i).collides(newLayout);
			}
			i++;
		}
		return placeable;
	}

	private boolean collides(Rectangle newLayout) {
		return this.layout.intersects(newLayout);
	}
}
