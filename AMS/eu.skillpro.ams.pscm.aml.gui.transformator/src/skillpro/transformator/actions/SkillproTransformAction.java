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

package skillpro.transformator.actions;

import org.eclipse.jface.action.Action;

import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import aml.skillpro.transformation.transfer.TransferToSkillpro;
import aml.skillpro.transformer.Transformer;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

/**
 * @author Kevin Nicholas Arbai
 * @author Abteilung ISPE/PDE, FZI Forschungszentrum Informatik 
 *
 * 30 Mar 2014
 *
 */
public class SkillproTransformAction extends Action {
	private static final String TOOLTIP = "Transform AML data to Skillpro";
	private static final String ICON = "icons/transform.png";
	
	protected final TransformationRepo transformationRepo;
	
	public SkillproTransformAction() {
		setToolTipText(TOOLTIP);
		setImageDescriptor(eu.skillpro.ams.pscm.gui.masterviews.Activator.getImageDescriptor(ICON));
		transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
	}

	@Override
	public void run() {
		try {
			Transformer.getInstance().transform();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		TransferToSkillpro.transferToSkillproRepo();
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
	}
}
