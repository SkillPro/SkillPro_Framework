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

import java.util.Iterator;

import org.eclipse.draw2d.geometry.Rectangle;

import skillpro.model.assets.FactoryNode;

public class Room extends GEFNode implements Cloneable{
	public Room(FactoryNode node) {
		super(node);
		if (node != null) {
			int xCoord = (int) node.getCurrentCoordinates().x;
			int yCoord = (int) node.getCurrentCoordinates().x;
			int width = (int) (node.getLength());
			int height = (int) (node.getWidth());
			this.setName(node.getName());
			this.setLayout(new Rectangle(xCoord, yCoord, width, height));
		}
	}

	@Override
	public Room clone() throws CloneNotSupportedException {
		Room room = new Room(this.getFactoryNode());
		room.setName(this.getName());
		room.setParent(this.getParent());
		room.setLayout(new Rectangle(getLayout().x, getLayout().y,
				getLayout().width, getLayout().height));
		Iterator<GEFNode> it = this.getChildrenArray().iterator();
		while (it.hasNext()) {
			GEFNode node = it.next();
			if (node instanceof GEFWorkingPlace) {
				GEFWorkingPlace child = (GEFWorkingPlace) node;
				GEFNode clone = (GEFNode) child.clone();
				room.addChild(clone);
				clone.setLayout(child.getLayout());
			}
		}
		return room;
	}
}
