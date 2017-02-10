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

package skillpro.rcp.layout.part.tree;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import skillpro.rcp.layout.model.GEFNode;
import skillpro.rcp.layout.model.Hall;

public class HallTreeEditPart extends AppAbstractTreeEditPart {
	@Override
	protected List<GEFNode> getModelChildren() {
		return ((Hall) getModel()).getChildrenArray();
	}

	@Override
	public void refreshVisuals() {
		Hall model = (Hall) getModel();
		setWidgetText(model.getName());
		setWidgetImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJ_ELEMENT));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GEFNode.PROPERTY_ADD))
			refreshChildren();
		if (evt.getPropertyName().equals(GEFNode.PROPERTY_REMOVE))
			refreshChildren();
		if (evt.getPropertyName().equals(GEFNode.PROPERTY_RENAME))
			refreshVisuals();
	}
}
