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

package skillpro.asset.views.dialogs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.assets.SEE;
import skillpro.view.impl.CreateSEEComposite;

public class CreateSEEDialog extends SelectionDialog {
	private static final String TITLE = "Create a new SEE";
	private CreateSEEComposite createSEEComposite;
	
	public CreateSEEDialog(Shell parentShell) {
		super(parentShell);
		setTitle(TITLE);
	}
	
	public SEE getSEE() {
		SEE see = new SEE();
		see.setAmlDescription("");
		see.setSimulation(createSEEComposite.isSimulation());
		see.addNotRegisteredResource(createSEEComposite.getResource());
		see.setAMSCommunication(createSEEComposite.getAmsCommunication());
		see.setMESCommunication(createSEEComposite.getMesCommunication());
		see.setESCommunication(createSEEComposite.getEsCommunication());
		see.setDefaultInputProductQuantities(createSEEComposite.getDefaultProductQuantities());
		see.setDefaultResourceConfigurationID(createSEEComposite.getDefaultResourceConfigurationID());
		see.setSEEType(createSEEComposite.getSEEType());
		return see;
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		parent.setLayout(GridLayoutFactory.fillDefaults().create());
		parent.setLayoutData(GridDataFactory.fillDefaults().create());
		createSEEComposite = new CreateSEEComposite(parent, null, true) {
			@Override
			public boolean validate() {
				if (getResource() != null && getDefaultResourceConfigurationID() != null && !getDefaultResourceConfigurationID().isEmpty() 
						&& getDefaultProductQuantities() != null && !getDefaultProductQuantities().isEmpty()
						&& getAmsCommunication() != null && getAmsCommunication().getFirstElement() != null
						&& getAmsCommunication().getSecondElement() != null && getMesCommunication() != null
						&& getMesCommunication().getFirstElement() != null && getMesCommunication().getSecondElement() != null
						&& !getMesCommunication().getSecondElement().isEmpty() && !getAmsCommunication().getSecondElement().isEmpty()
						&& getEsCommunication() != null && getEsCommunication().getFirstElement() != null 
						&& getEsCommunication().getSecondElement() != null && !getEsCommunication().getSecondElement().isEmpty()) {
					getOkButton().setEnabled(true);
					return true;
				} else {
					getOkButton().setEnabled(false);
					return false;
				}
			}
		};
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createSEEComposite.validate();
	}
}
