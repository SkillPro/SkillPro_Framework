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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.jface.action.Action;

import skillpro.model.assets.Factory;
import skillpro.model.assets.Resource;
import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import transformation.interfaces.ITransformable;
import aml.skillpro.mapping.TransformationMappingParser;
import aml.skillpro.transformation.transfer.TransferToAML;
import aml.skillpro.transformer.ReverseTransformer;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

public class SkillproReverseTransformAction extends Action {
	private static final String TOOLTIP = "Transform Skillpro to AML";
	private static final String ICON = "icons/sync.png";
	
	protected final TransformationRepo transformationRepo;
	protected final Map<Object, Class<? extends ITransformable>> transformables;
	protected final Map<Object, Class<? extends ITransformable>> elementTransformables;
	
	public SkillproReverseTransformAction() {
		setToolTipText(TOOLTIP);
		setImageDescriptor(eu.skillpro.ams.pscm.gui.masterviews.Activator.getImageDescriptor(ICON));
		transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
		transformables = transformationRepo.getInterfaceTransformablesMapping();
		elementTransformables = transformationRepo.getAdapterTransformablesMapping();
	}

	@Override
	public void run() {
		//delete old transformation data
		System.out.println("Wiping to prepare for the reverse transformation");
		transformationRepo.wipeAllData();
		//set transformation data
		System.out.println("set transformation mapping to default mapping");
		try {
			TransformationMappingParser.loadConfiguration("DefaultMapping.xml");
		} catch (ValidityException e1) {
			e1.printStackTrace();
		} catch (ParsingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		//begin
		System.out.println("==========================");
		System.out.println("Transforming from Skillpro into AML: ".toUpperCase());
		System.out.println("==========================");
		
		//import roles
		ReverseTransformer rev = ReverseTransformer.getInstance();
		TransformationRepo transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
		if (!transformationRepo.getInterfaceTransformablesMapping().isEmpty() 
				&& !transformationRepo.getPivotElementToAdapterTransformableMapping().isEmpty()) {
			rev.reverseTransform();
		} else {
			throw new IllegalArgumentException("Please load a transformation mapping before trying to perform reverse transformation.");
		}
		//transfer to aml
		TransferToAML.transferToAMLRepo();
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		System.out.println("==========================");
		System.out.println("FINISHED TRANSFERRING");
		System.out.println("==========================");
	}
	
	public void reverseTransform(List<Factory> factories) {
		System.out.println("Wiping to prepare for the reverse transformation");
		transformationRepo.wipeAllData();
		//begin
		System.out.println("==========================");
		System.out.println("Transforming from Skillpro into AML: ".toUpperCase());
		System.out.println("==========================");
		
		//FIXME check correctness
		//this function has not been used in a long time.
		Set<Object> toReverse = new HashSet<>();
		toReverse.addAll(factories);
		ReverseTransformer.getInstance().reverseTransform(toReverse);
		
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		System.out.println("==========================");
		System.out.println("FINISHED TRANSFERRING");
		System.out.println("==========================");
	}
	
	public void reverseTransformResources(List<Resource> resources) {
		System.out.println("Wiping to prepare for the reverse transformation");
		transformationRepo.wipeAllData();
		
		//begin
		System.out.println("==========================");
		System.out.println("Transforming from Skillpro into AML: ".toUpperCase());
		System.out.println("==========================");
		
		//FIXME check correctness
		//this function has not been used in a long time.
		Set<Object> toReverse = new HashSet<>();
		toReverse.addAll(resources);
		ReverseTransformer.getInstance().reverseTransform(toReverse);
		
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		System.out.println("==========================");
		System.out.println("FINISHED TRANSFERRING");
		System.out.println("==========================");
	}
}