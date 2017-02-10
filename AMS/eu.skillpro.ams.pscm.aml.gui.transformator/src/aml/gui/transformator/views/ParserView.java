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

package aml.gui.transformator.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.service.SkillproService;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import aml.domain.Domain;
import aml.model.Root;
import aml.transformation.service.AMLTransformationService;
import amltransformation.composites.ParserTreeComposite;

public class ParserView extends ViewPart implements Updatable {
	public static final String ID = ParserView.class.getName();
	private ParserTreeComposite parserTreeComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		SkillproService.getUpdateManager().registerUpdatable(this, Root.class);
	}
	
	private void createViewer(Composite parent) {
		parserTreeComposite =  new ParserTreeComposite(parent, SWT.NULL);
		parserTreeComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		parserTreeComposite.setInput(new ArrayList<>());
		parserTreeComposite.getTreeViewer().setInput(initInput());
	}
	
	private List<?> initInput() {
		Collection<Class<? extends Domain>> domainClasses = AMLTransformationService.getAMLProvider().getAllDomains();
		List<Root<?>> input = new ArrayList<>();
		for (Class<? extends Domain> domainClass : domainClasses) {
			input.addAll(AMLTransformationService.getAMLProvider().getAMLModelRepo(domainClass).getEntities());
		}
		
		return input;
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void update(UpdateType type) {
		switch(type) {
		case NEW_DATA_IMPORTED:
			parserTreeComposite.disposeAllItems();
			parserTreeComposite.getTreeViewer().setInput(initInput());
			parserTreeComposite.getTreeViewer().refresh();
			break;
		case AML_MODEL_CREATED:
			parserTreeComposite.disposeAllItems();
			parserTreeComposite.getTreeViewer().setInput(initInput());
			parserTreeComposite.getTreeViewer().refresh();
			break;
		case AML_MODEL_UPDATED:
			parserTreeComposite.disposeAllItems();
			parserTreeComposite.getTreeViewer().setInput(initInput());
			parserTreeComposite.getTreeViewer().refresh();
			break;
		case AML_DOMAIN_CREATED:
			parserTreeComposite.disposeAllItems();
			parserTreeComposite.getTreeViewer().setInput(initInput());
			parserTreeComposite.getTreeViewer().refresh();
			break;
		case AML_DOMAIN_UPDATED:
			parserTreeComposite.disposeAllItems();
			parserTreeComposite.getTreeViewer().setInput(initInput());
			parserTreeComposite.getTreeViewer().refresh();
			break;
		case AML_DOMAIN_DELETED:
			parserTreeComposite.disposeAllItems();
			parserTreeComposite.getTreeViewer().setInput(initInput());
			parserTreeComposite.getTreeViewer().refresh();
			break;
		default:
			break;
		}
	}
}
