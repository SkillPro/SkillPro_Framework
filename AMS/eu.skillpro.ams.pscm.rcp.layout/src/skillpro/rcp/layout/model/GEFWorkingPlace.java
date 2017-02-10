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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import skillpro.model.assets.Resource;

public class GEFWorkingPlace extends GEFNode {
	public static final String PROPERTY_COLOR = "WP_Color";
	private Color color;

	public GEFWorkingPlace(Resource node) {
		super(node);
		int xCoord = (int) node.getCurrentCoordinates().x;
		int yCoord = (int) node.getCurrentCoordinates().y;
		int width = (int) node.getLength();
		int height = (int) node.getWidth();
		this.setName(node.getName());
		this.setLayout(new Rectangle(xCoord, yCoord, width, height));
		this.color = ColorConstants.lightBlue;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		GEFWorkingPlace wp = new GEFWorkingPlace(
				(Resource) this.getFactoryNode());
		wp.setName(this.getName());
		wp.setLayout(new Rectangle(getLayout().x + 10, getLayout().y + 10,
				getLayout().width, getLayout().height));
		return wp;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		Color oldColor = this.getColor();
		getListeners().firePropertyChange(PROPERTY_COLOR, oldColor, color);
	}
}
