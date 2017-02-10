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

package skillpro.rcp.layout.editpolicies;


import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import skillpro.rcp.layout.commands.AbstractLayoutCommand;
import skillpro.rcp.layout.commands.RoomChangeLayoutCommand;
import skillpro.rcp.layout.commands.WorkingPlaceChangeLayoutCommand;
import skillpro.rcp.layout.commands.WorkingPlaceCreateCommand;
import skillpro.rcp.layout.figure.WorkingPlaceFigure;
import skillpro.rcp.layout.part.RoomPart;
import skillpro.rcp.layout.part.WorkingPlacePart;


public class AppEditLayoutPolicy extends XYLayoutEditPolicy {
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		AbstractLayoutCommand command = null;
		if (child instanceof WorkingPlacePart) {
			command = new WorkingPlaceChangeLayoutCommand();
		    command.setModel(child.getModel());
		    command.setConstraint((Rectangle)constraint);
		}
		if (child instanceof RoomPart) {
			command = new RoomChangeLayoutCommand();
		    command.setModel(child.getModel());
		    command.setConstraint((Rectangle)constraint);
		}
	    return command;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if (request.getType() == REQ_CREATE && getHost() instanceof RoomPart) {
			WorkingPlaceCreateCommand cmd = new WorkingPlaceCreateCommand();
			
			cmd.setRoom(getHost().getModel());
			cmd.setWorkingPlace(request.getNewObject());
			
			Rectangle constraint = (Rectangle)getConstraintFor(request);
			constraint.x = (constraint.x < 0) ? 0 : constraint.x;
			constraint.y = (constraint.y < 0) ? 0 : constraint.y;
			constraint.width = (constraint.width <= 0) ? WorkingPlaceFigure.WP_FIGURE_DEFWIDTH :  constraint.width;
			constraint.height = (constraint.height <= 0) ? WorkingPlaceFigure.WP_FIGURE_DEFHEIGHT : constraint.height;
			cmd.setLayout(constraint);
			return cmd;			
		}
		return null;
	}
}
