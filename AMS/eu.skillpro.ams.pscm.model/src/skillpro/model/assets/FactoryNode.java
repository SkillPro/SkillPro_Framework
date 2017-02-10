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

package skillpro.model.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import skillpro.model.utils.Vector3d;

public class FactoryNode extends Asset {
	private Factory factory;
	private FactoryNode parent;
	private List<FactoryNode> subNodes = new ArrayList<>();
	/** initial coordinates (synced coordinates) */
	private Vector3d initialCoordinates;
	/** current coordinates (not synced) */
	private Vector3d currentCoordinates;
	/** The size of the object, in order length, width, height. */
	private Vector3d size;
	private boolean layoutable;
	private String nodeID;
	
	public FactoryNode() {
		super("");
		initialCoordinates = new Vector3d();
		this.currentCoordinates = new Vector3d();
		this.size = new Vector3d();
	}
	
	public FactoryNode(String name) {
		this(name, true);
	}
	
	public FactoryNode(String name, boolean layoutable) {
		super(name);
		setNodeID(UUID.randomUUID().toString());
		this.layoutable = layoutable;
		initialCoordinates = new Vector3d();
		this.currentCoordinates = new Vector3d();
		if (layoutable) {
			this.size = new Vector3d(100, 100, 100);
		} else {
			this.size = new Vector3d(-1, -1, -1);
		}
	}

	/**
	 * @param name
	 * @param parent
	 * @param layoutable if the node is logical and doesn't have representation or not
	 */
	public FactoryNode(String name, FactoryNode parent, boolean layoutable) {
		this(name, layoutable);
		setParent(parent);
	}
	
	public Factory getFactory() {
		return factory;
	}
	
	public void setFactory(Factory factory) {
		this.factory = factory;
	}
	
	public FactoryNode getParent() {
		return parent;
	}
	
	public void setParent(FactoryNode parent) {
		this.parent = parent;
		if (parent != null && !parent.getSubNodes().contains(this)) {
			parent.addSubNode(this);
		}
	}
	
	public List<FactoryNode> getSubNodes() {
		return subNodes;
	}
	
	public boolean addSubNode(FactoryNode node) {
		if (node != null && !subNodes.contains(node)) {
			if (subNodes.add(node)) {
				node.setParent(this);
				return true;
			}
		}
		return false;
	}
	
	public String getId() {
		return nodeID;
	}
	
	public List<FactoryNode> ascendants() {
        return getAncestors(false);
    }
	
	public List<FactoryNode> reversedAscendants() {
        return getAncestors(true);
    }
	
	public List<FactoryNode> ascendantsAndSelf() {
        List<FactoryNode> ascendants = new ArrayList<FactoryNode>();
        ascendants.addAll(getAncestors(false));
        ascendants.add(this);
        return ascendants;
    }
	
	public List<FactoryNode> reversedAscendantsAndSelf() {
        List<FactoryNode> ascendants = new ArrayList<FactoryNode>();
        ascendants.add(this);
        ascendants.addAll(getAncestors(true));
        return ascendants;
    }
	
	private List<FactoryNode> getAncestors(boolean reverse) {
        List<FactoryNode> ancestors = new ArrayList<FactoryNode>();
        FactoryNode parent = getParent();
        while (parent != null) {
            ancestors.add(parent);
            parent = parent.getParent();
        }
        if (!reverse) {
            Collections.reverse(ancestors);
        }
        return ancestors;
    }

	public Vector3d getCurrentCoordinates() {
		return currentCoordinates;
	}

	public void setCurrentCoordinates(double x, double y, double z) {
		currentCoordinates.x = x;
		currentCoordinates.y = y;
		currentCoordinates.z = z;
	}

	public void setCurrentCoordinates(Vector3d currentCoordinates) {
		setCurrentCoordinates(currentCoordinates.x, currentCoordinates.y, currentCoordinates.z);
	}
	
	/** The size of the object, in order length, width, height. */
	public Vector3d getSize() {
		return size;
	}

	public void setSize(double length, double width, double height) {
		size.x = length;
		size.y = width;
		size.z = height;
	}

	public void setSize(Vector3d size) {
		setSize(size.x, size.y, size.z);
	}
	
	public void setLength(double length) {
		size.x = length;
	}
	
	public void setWidth(double width) {
		size.y = width;
	}
	
	public void setHeight(double height) {
		size.z = height;
	}
	
	public double getLength() {
		return size.x;
	}
	
	public double getWidth() {
		return size.y;
	}
	
	public double getHeight() {
		return size.z;
	}

	public void setInitialCoordinates(double x, double y, double z) {
		initialCoordinates.x = x;
		initialCoordinates.y = y;
		initialCoordinates.z = z;
	}

	public void setInitialCoordinates(Vector3d initialCoordinates) {
		setInitialCoordinates(initialCoordinates.x, initialCoordinates.y, initialCoordinates.z);
	}
	
	public boolean isLayoutable() {
		return layoutable;
	}
	
	public void setLayoutable(boolean layoutable) {
		this.layoutable = layoutable;
	}
	
	public String getNodeID() {
		return nodeID;
	}
	
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	public Vector3d getInitialCoordinates() {
		return initialCoordinates;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeID, parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactoryNode other = (FactoryNode) obj;
		return Objects.equals(nodeID, other.nodeID)
				&& Objects.equals(parent, other.parent);
	}
}
