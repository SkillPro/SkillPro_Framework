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

package skillpro.rcp.layout.part;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import skillpro.rcp.layout.editpolicies.AppDeletePolicy;
import skillpro.rcp.layout.figure.WorkingPlaceFigure;
import skillpro.rcp.layout.model.GEFNode;
import skillpro.rcp.layout.model.GEFWorkingPlace;

public class WorkingPlacePart extends AppAbstractEditPart {
	@Override
	protected IFigure createFigure() {
		IFigure figure = new WorkingPlaceFigure();
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new AppDeletePolicy());
	}

	@Override
	protected void refreshVisuals() {
		WorkingPlaceFigure figure = (WorkingPlaceFigure) getFigure();
		GEFWorkingPlace model = (GEFWorkingPlace) getModel();

		figure.setID(model.getName());
		figure.setLayout(model.getLayout(), model.getRotation());
		figure.setBackgroundColor(model.getColor());
	}

	@Override
	public List<GEFNode> getModelChildren() {
		return new ArrayList<GEFNode>();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GEFNode.PROPERTY_LAYOUT))
			refreshVisuals();
		if (evt.getPropertyName().equals(GEFWorkingPlace.PROPERTY_COLOR))
			refreshVisuals();
	}
}
