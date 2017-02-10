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

package skillpro.asset.views.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import skillpro.asset.views.wizardpages.ConfirmationPage;
import skillpro.asset.views.wizardpages.ManualSEEPage;
import skillpro.asset.views.wizardpages.MethodSelectionPage;
import skillpro.asset.views.wizardpages.SEECreationMethod;
import skillpro.asset.views.wizardpages.SEEResultPage;

public class CreateSEEWizard extends Wizard {
	private ManualSEEPage manualPage;
	private ManualSEEPage editPage;
	private SEEResultPage resultPage;
	private ConfirmationPage confirmationPage;
	private MethodSelectionPage selectionPage;
	private IWizardPage selectedPage;
	
	public CreateSEEWizard() {
		super();
		selectionPage = new MethodSelectionPage("Create AML description of an SEE");
		selectedPage = selectionPage;
	}
	
	@Override
	public boolean performFinish() {
		if (canFinish()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canFinish() {
		if ((selectedPage == resultPage || selectedPage == confirmationPage) && selectedPage.isPageComplete()) {
			return true;
		}
		return false;
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page.equals(selectionPage)) {
			if (selectionPage.getCreationMethod() == SEECreationMethod.MANUAL) {
				manualPage = new ManualSEEPage("Manually create an SEE", selectionPage.usesDefaultAddresses());
				manualPage.setTransformationMappingPath(selectionPage.getTransformationMappingPath());
				addPage(manualPage);
				editPage = null;
				selectedPage = manualPage;
			} else if (selectionPage.getCreationMethod() == SEECreationMethod.LOAD_SINGLE) {
				editPage = new ManualSEEPage("Edit SEE", selectionPage.getSEE(), selectionPage.getSEEFileInput(),
						selectionPage.getTransformationMappingPath(), selectionPage.usesDefaultAddresses());
				addPage(editPage);
				selectedPage = editPage;
			} else if (selectionPage.getCreationMethod() == SEECreationMethod.LOAD_BATCH) {
				confirmationPage = new ConfirmationPage("Confirm SEEs to save/upload", selectionPage.getBatchFilesMapping().values(),
						selectionPage.getTransformationMappingPath(), selectionPage.usesDefaultAddresses());
				addPage(confirmationPage);
				selectedPage = confirmationPage;
			} else {
				throw new IllegalArgumentException("A new implementation is needed.");
			}
		} else if (page instanceof ManualSEEPage) {
			ManualSEEPage manualPage = (ManualSEEPage) page;
			resultPage = new SEEResultPage("AML Description of the SEE", manualPage.getAMLInputs(), manualPage.getSEE(), manualPage.getTransformationMappingPath());
			addPage(resultPage);
			selectedPage = resultPage;
		} else {
			return null;
		}
		return selectedPage;
	}
	
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage previousPage = null;
		if (page.equals(editPage) || page.equals(manualPage) || page.equals(confirmationPage)) {
			selectedPage = selectionPage;
			previousPage = selectionPage;
		} else if (page.equals(resultPage)) {
			if (editPage != null) {
				selectedPage = editPage;
				previousPage = editPage;
			} else {
				selectedPage = manualPage;
				previousPage = manualPage;
			}
		}
		return previousPage;
	}
	
	@Override
	public boolean needsPreviousAndNextButtons() {
		return true;
	}
	
	@Override
	public void addPages() {
		addPage(selectionPage);
	}
}