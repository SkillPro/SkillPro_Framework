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

import skillpro.rcp.layout.model.GEFWorkingPlace;

public class WorkingPlaceChangeLayoutCommand extends AbstractLayoutCommand {
	private GEFWorkingPlace model;
	private Rectangle layout;
	private Rectangle oldLayout;

	@Override
	public void execute() {
		this.model.setLayout(layout);
	}

	@Override
	public void setConstraint(Rectangle rect) {
		this.layout = rect;
	}

	@Override
	public boolean canExecute() {
		if ((model != null)) {
			return model.getParent().isPlaceable(model, layout);
		}
		return false;
	}

	@Override
	public void setModel(Object model) {
		this.model = (GEFWorkingPlace) model;
		this.oldLayout = ((GEFWorkingPlace) model).getLayout();
	}

	@Override
	public void undo() {
		this.model.setLayout(this.oldLayout);
	}
}
