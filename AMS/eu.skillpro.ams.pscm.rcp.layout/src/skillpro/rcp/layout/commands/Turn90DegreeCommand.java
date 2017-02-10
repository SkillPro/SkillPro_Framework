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

package skillpro.rcp.layout.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import skillpro.rcp.layout.model.GEFNode;

public class Turn90DegreeCommand extends Command {
	private GEFNode model;
	private Rectangle layout;
	private double angle;

	public void execute() {
		this.model.rotate(90.00);
	}

	public void setConstraint() {
		this.angle = this.model.getRotation() + 90.00;
		this.layout = model.getLayout();
		if (this.angle == 90.00) {
			layout = new Rectangle(layout.x, layout.y - layout.width,
					layout.height, layout.width);
		} else if (this.angle == 180.00) {
			layout = new Rectangle(layout.x - layout.width, layout.y
					- layout.height, layout.width, layout.height);
		} else if (this.angle == 270.00) {
			layout = new Rectangle(layout.x - layout.height, layout.y,
					layout.height, layout.width);
		}
	}

	@Override
	public boolean canExecute() {
		if (layout != null && model != null && model.getParent() != null) {
			return model.getParent().isPlaceable(model, layout);
		}
		return false;
	}

	public void setModel(Object model) {
		this.model = (GEFNode) model;
	}

	@Override
	public void undo() {
		this.model.rotate(-90.00);
	}
}
