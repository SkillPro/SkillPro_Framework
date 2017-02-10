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

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class RoomBorder extends AbstractBorder {
	@Override
	public Insets getInsets(IFigure figure) {
		return new Insets(5);
	}

	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		Rectangle rect = figure.getBounds();
		tempRect.setBounds(getPaintRectangle(figure, insets));
		tempRect.width--;
		tempRect.height--;
		graphics.drawRectangle(tempRect);
		graphics.drawRectangle(figure.getClientArea());
		for (int i = 0; i < rect.width; i = i + 5) {
			graphics.drawLine(new Point(rect.x + i, rect.y), new Point(rect.x
					+ i + 5, rect.y + 5));
			graphics.drawLine(new Point(rect.x + i, rect.y + rect.height - 5),
					new Point(rect.x + i + 4, rect.y + rect.height - 1));
		}
		for (int i = 5; i < rect.height - 10; i = i + 5) {
			graphics.drawLine(new Point(rect.x, rect.y + i), new Point(
					rect.x + 5, rect.y + i + 5));
			graphics.drawLine(new Point(rect.x + rect.width - 5, rect.y + i),
					new Point(rect.x + rect.width - 1, rect.y + i + 4));
		}

	}

}
