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

package de.fzi.skillpro.order.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import skillpro.product.views.ProductTreeView;
import de.fzi.skillpro.order.views.EvaluationView;
import de.fzi.skillpro.order.views.ExecutableSkillView;
import de.fzi.skillpro.order.views.OrderAlternativeEvaluation;
import de.fzi.skillpro.order.views.OrderTreeView;

public class OrderPerspective implements IPerspectiveFactory {
	public static final String ID = OrderPerspective.class.getName();
	public static final String NAME = "Order";
	public static final String GROUP_ID = "Order";
	public static final String IMAGE_PATH = "process2.png";

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		String editorArea = layout.getEditorArea();
		layout.addStandaloneView(OrderTreeView.ID, true, IPageLayout.LEFT, 0.25f, editorArea);
		layout.addStandaloneView(ExecutableSkillView.ID, true, IPageLayout.BOTTOM, 0.4f, OrderTreeView.ID);
		layout.addStandaloneView(EvaluationView.ID, true, IPageLayout.RIGHT, 0.75f, editorArea);
		layout.addStandaloneView(ProductTreeView.ID, true,IPageLayout.RIGHT, 0.6f, EvaluationView.ID);
		layout.addStandaloneView(OrderAlternativeEvaluation.ID, true,IPageLayout.BOTTOM, 0.5f, EvaluationView.ID);
		
		layout.getViewLayout(EvaluationView.ID).setCloseable(false);
		layout.getViewLayout(OrderTreeView.ID).setCloseable(false);
		layout.getViewLayout(ExecutableSkillView.ID).setCloseable(false);
		layout.getViewLayout(OrderAlternativeEvaluation.ID).setCloseable(false);
		layout.getViewLayout(ProductTreeView.ID).setCloseable(false);
	}
}
