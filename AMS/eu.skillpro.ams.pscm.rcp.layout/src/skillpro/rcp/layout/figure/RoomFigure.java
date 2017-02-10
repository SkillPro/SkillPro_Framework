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

package skillpro.rcp.layout.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class RoomFigure extends Figure {

	public static final int ROOM_FIGURE_DEFHEIGHT = 500;
	public static final int ROOM_FIGURE_DEFWIDTH = 700;

	public RoomFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		setForegroundColor(ColorConstants.black);
		setOpaque(true);
		setBorder(new RoomBorder());
	}

	public void setLayout(Rectangle rect, double rotation) {
		Rectangle newLayout = rect;
		if (rotation == 90.00) {
			newLayout = new Rectangle(rect.x, rect.y - rect.width, rect.height,
					rect.width);
		} else if (rotation == 180.00) {
			newLayout = new Rectangle(rect.x - rect.width,
					rect.y - rect.height, rect.width, rect.height);
		} else if (rotation == 270.00) {
			newLayout = new Rectangle(rect.x - rect.height, rect.y,
					rect.height, rect.width);
		}
		getParent().setConstraint(this, newLayout);
	}
}
