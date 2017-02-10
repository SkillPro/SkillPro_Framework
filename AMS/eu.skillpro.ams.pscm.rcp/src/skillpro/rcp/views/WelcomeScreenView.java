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

package skillpro.rcp.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import skillpro.rcp.activator.SkillproActivator;

public class WelcomeScreenView extends ViewPart {
	public static final String ID = WelcomeScreenView.class.getName();
	private Composite container;

	@Override
	public void createPartControl(Composite parent) {
		container = initializeContainer(parent);
	}

	@Override
	public void setFocus() {
		container.setFocus();
	}

	private Composite initializeContainer(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(GridLayoutFactory.swtDefaults().numColumns(1).equalWidth(false).create());
		top.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		Composite center = new Composite(top, SWT.NONE);
		center.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).create());
		center.setLayoutData(GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).create());
		Label label = new Label(center, SWT.NONE);
		label.setLayoutData(GridDataFactory.fillDefaults().create());
		label.setImage(SkillproActivator.getImageDescriptor("icons/skillpro-logo-middle.png").createImage());
		return center;
	}
}
