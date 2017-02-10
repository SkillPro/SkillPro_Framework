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

package aml.skillpro.transformation.transfer;

import java.util.Collection;

import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.SEE;
import skillpro.model.assets.Setup;
import skillpro.model.products.Product;
import skillpro.model.repo.Repo;
import skillpro.model.repo.resource.AssetRepo;
import skillpro.model.repo.resource.ProductRepo;
import skillpro.model.repo.resource.ResourceSkillRepo;
import skillpro.model.repo.resource.SEERepo;
import skillpro.model.repo.resource.SetupRepo;
import skillpro.model.resourceprovider.ISkillproProvider;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;
import transformation.interfaces.ITransformable;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;
import aml.skillpro.transformation.interfaces.IProductionSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.ISEETransformable;
import aml.skillpro.transformation.interfaces.ISetupTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;
import aml.transformation.service.AMLTransformationService;

public class TransferToSkillpro {
	private final static TransferToSkillpro INSTANCE = new TransferToSkillpro();
	
	private TransferToSkillpro() {
	}
	
	public static TransferToSkillpro getInstance() {
		return INSTANCE;
	}
	
	public static void transferToSkillproRepo() {
		ISkillproProvider p = SkillproService.getSkillproProvider();
		Repo<TemplateSkill> templateSkills = p.getTemplateSkillRepo();
		AssetRepo repo = p.getAssetRepo();
		Repo<ProductionSkill> productionSkills = p.getProductionSkillRepo();
		ResourceSkillRepo resourceSkillsInRepo = p.getResourceSkillRepo();
		SetupRepo realConfigs = p.getSetupRepo();
		ProductRepo productRepo = p.getProductRepo();
		SEERepo seeRepo = p.getSEERepo();
		Collection<ITransformable> transformedObjects = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getTransformedObjectsMap().values();
		for (ITransformable trans : transformedObjects) {
			if (trans instanceof ITemplateSkillTransformable) {
				if (!templateSkills.contains(trans.getElement())) {
					ITransformable transformableParent = trans.getTransformableParent();
					if (transformableParent != null && templateSkills.contains(transformableParent.getElement())) {
						TemplateSkill parent = null;
						for (TemplateSkill tSkill : templateSkills) {
							if (tSkill.equals(transformableParent.getElement())) {
								parent = tSkill;
								break;
							}
						}
						if (parent == null) {
							throw new IllegalArgumentException("Please fix implementation, parent TSkill can't be found: " 
									+ transformableParent.getTransformableName());
						}
						((TemplateSkill) trans.getElement()).setParent(parent);
					}
					templateSkills.add((TemplateSkill) trans.getElement());
				}
			} else if (trans instanceof IFactoryNodeTransformable) {
					repo.addIfAbsent((FactoryNode) trans.getElement());
			} else if (trans instanceof IResourceSkillTransformable) {
					resourceSkillsInRepo.addIfAbsent((ResourceSkill) trans.getElement());
			} else if (trans instanceof IProductionSkillTransformable) {
					productionSkills.addIfAbsent((ProductionSkill) trans.getElement());
			} else if (trans instanceof ISetupTransformable) {
					realConfigs.addIfAbsent((Setup) trans.getElement());
			} else if (trans instanceof IProductTransformable) {
					productRepo.addIfAbsent((Product) trans.getElement());
			} else if (trans instanceof ISEETransformable) {
					seeRepo.addIfAbsent((SEE) trans.getElement());
			}
		}
	}
}
