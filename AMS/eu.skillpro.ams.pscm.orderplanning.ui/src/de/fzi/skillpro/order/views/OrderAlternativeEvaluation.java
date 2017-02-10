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

package de.fzi.skillpro.order.views;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.products.Order;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.Skill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.view.impl.OrderAlternativeTableComposite;

public class OrderAlternativeEvaluation extends ViewPart implements Updatable {
	public static final String ID = OrderAlternativeEvaluation.class.getName();
	private OrderAlternativeTableComposite alternativeTableComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		alternativeTableComposite = new OrderAlternativeTableComposite(parent, SWT.NONE);
		alternativeTableComposite.setInput(initInput());
		SkillproService.getUpdateManager().registerUpdatable(this, Order.class);
		SkillproService.getUpdateManager().registerUpdatable(this, Skill.class);
	}

	private Set<Entry<Order, Set<List<ExecutableSkill>>>> initInput() {
		return SkillproService.getSkillproProvider().getSkillRepo().getPossibleOrderedExSkillsBasedOnOrder().entrySet();
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void update(UpdateType type) {
		switch (type) {
		case KPI_UPDATED:
			alternativeTableComposite.disposeAllItems();
			alternativeTableComposite.setInput(initInput());
			break;
		default:
			break;
		}
		
	}
}
