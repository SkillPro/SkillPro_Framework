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

import skillpro.rcp.layout.model.GEFWorkingPlace;
import skillpro.rcp.layout.model.Room;

public class WorkingPlaceCreateCommand extends Command {
	private Room room;
	private GEFWorkingPlace wp;

	public WorkingPlaceCreateCommand() {
		super();
		room = null;
		wp = null;
	}

	public void setRoom(Object room) {
		if (room instanceof Room)
			this.room = (Room) room;
	}

	public void setWorkingPlace(Object wp) {
		if (wp instanceof GEFWorkingPlace)
			this.wp = (GEFWorkingPlace) wp;
	}

	public void setLayout(Rectangle r) {
		if (wp == null)
			return;
		wp.setLayout(r);
	}

	@Override
	public boolean canExecute() {
		if (room == null || wp == null)
			return false;
		return true;
	}

	@Override
	public void execute() {
		room.addChild(wp);
	}

	@Override
	public boolean canUndo() {
		if (room == null || wp == null)
			return false;
		return room.contains(wp);
	}

	@Override
	public void undo() {
		room.removeChild(wp);
	}
}
