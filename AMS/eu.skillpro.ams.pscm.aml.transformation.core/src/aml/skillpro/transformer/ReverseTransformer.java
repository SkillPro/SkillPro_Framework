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

package aml.skillpro.transformer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.SEE;
import skillpro.model.assets.Setup;
import skillpro.model.products.Product;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import aml.skillpro.transformation.adapters.ExecutableSkillAdapter;
import aml.skillpro.transformation.adapters.FactoryAdapter;
import aml.skillpro.transformation.adapters.FactoryNodeAdapter;
import aml.skillpro.transformation.adapters.ProductAdapter;
import aml.skillpro.transformation.adapters.ProductionSkillAdapter;
import aml.skillpro.transformation.adapters.ResourceAdapter;
import aml.skillpro.transformation.adapters.ResourceExecutableSkillAdapter;
import aml.skillpro.transformation.adapters.ResourceSkillAdapter;
import aml.skillpro.transformation.adapters.SEEAdapter;
import aml.skillpro.transformation.adapters.SetupAdapter;
import aml.skillpro.transformation.adapters.TemplateSkillAdapter;
import aml.skillpro.transformation.adapters.dummy.ExecutableSkillDummyAdapter;
import aml.skillpro.transformation.interfaces.IExecutableSkillTransformable;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IProductQuantityTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;
import aml.skillpro.transformation.interfaces.IProductionSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceExecutableSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.ISEETransformable;
import aml.skillpro.transformation.interfaces.ISetupTransformable;
import aml.skillpro.transformation.interfaces.ISkillConfigurationTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

public class ReverseTransformer {
	private final static ReverseTransformer INSTANCE = new ReverseTransformer();
	
	private final TransformationRepo transformationRepo;
	
	public ReverseTransformer() {
		transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
	}
	
	public static ReverseTransformer getInstance() {
		return INSTANCE;
	}
	
	public void reverseTransform() {
		Set<Object> toTransform = new HashSet<>();
		for (FactoryNode obj : SkillproService.getSkillproProvider().getAssetRepo()) {
			if (obj instanceof Factory) {
				toTransform.add(new FactoryAdapter((Factory) obj));
			} else if (obj instanceof Resource) {
				toTransform.add(new ResourceAdapter((Resource) obj));
			} else{
				toTransform.add(new FactoryNodeAdapter(obj));
			}
		}
		for (Setup obj : SkillproService.getSkillproProvider().getSetupRepo()) {
			toTransform.add(new SetupAdapter(obj));
		}
		for (Product obj : SkillproService.getSkillproProvider().getProductRepo()) {
			toTransform.add(new ProductAdapter(obj));
		}
		for (ProductionSkill obj : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			toTransform.add(new ProductionSkillAdapter(obj));
		}
		for (ResourceSkill obj : SkillproService.getSkillproProvider().getResourceSkillRepo()) {
			toTransform.add(new ResourceSkillAdapter(obj));
		}
		for (TemplateSkill obj : SkillproService.getSkillproProvider().getTemplateSkillRepo()) {
			toTransform.add(new TemplateSkillAdapter(obj));
		}
		for (SEE obj : SkillproService.getSkillproProvider().getSEERepo()) {
			toTransform.add(new SEEAdapter((SEE) obj));
		}
		//TODO should properties be here too?
		reverseTransform(toTransform);
	}
	
	public void reverseTransformSEE(SEE see) {
		Set<Object> toTransform = new HashSet<>();
		toTransform.add(new SEEAdapter(see));
		reverseTransform(toTransform);
	}
	
	public void reverseTransformExecutableSkills(List<ExecutableSkill> executableSkills) {
		Set<Object> toTransform = new HashSet<>();
		for (ExecutableSkill executableSkill : executableSkills) {
			toTransform.add(new ExecutableSkillAdapter(executableSkill));
		}
		reverseTransform(toTransform);
	}
	
	public void reverseTransformResourceExecutableSkills(List<ResourceExecutableSkill> rexSkills) {
		Set<Object> toTransform = new HashSet<>();
		for (ResourceExecutableSkill rexSkill : rexSkills) {
			toTransform.add(new ResourceExecutableSkillAdapter(rexSkill));
		}
		reverseTransform(toTransform);
	}
	
	public void reverseTransformExecutableSkillDummies(List<ExecutableSkillDummy> dummies) {
		Set<Object> toTransform = new HashSet<>();
		for (ExecutableSkillDummy dummy : dummies) {
			toTransform.add(new ExecutableSkillDummyAdapter(dummy));
		}
		reverseTransform(toTransform);
	}
	
	public void reverseTransformExecutableSkill(ExecutableSkill executableSkill) {
		Set<Object> toTransform = new HashSet<>();
		toTransform.add(new ExecutableSkillAdapter(executableSkill));
		reverseTransform(toTransform);
	}
	
	public void reverseTransformResourceExecutableSkill(ResourceExecutableSkill resourceExecutableSkill) {
		Set<Object> toTransform = new HashSet<>();
		toTransform.add(new ResourceExecutableSkillAdapter(resourceExecutableSkill));
		reverseTransform(toTransform);
	}
	
	public void reverseTransform(Set<Object> toTransform) {
		//before transforming, always wipe the transformedObjectsMapping
		transformationRepo.getReverseTransformedObjectsMap().clear();

		for (Object obj : toTransform) {
			if (obj instanceof IFactoryNodeTransformable) {
				((IFactoryNodeTransformable) obj).reverseTransform();
			} else if (obj instanceof IProductTransformable) {
				((IProductTransformable) obj).reverseTransform();
			} else if (obj instanceof IResourceSkillTransformable) {
				((IResourceSkillTransformable) obj).reverseTransform();
			} else if (obj instanceof IProductionSkillTransformable) {
				((IProductionSkillTransformable) obj).reverseTransform();
			} else if (obj instanceof ISetupTransformable) {
				((ISetupTransformable) obj).reverseTransform();
			} else if (obj instanceof IResourceConfigurationTransformable) {
				((IResourceConfigurationTransformable) obj).reverseTransform();
			} else if (obj instanceof ISkillConfigurationTransformable) {
				((ISkillConfigurationTransformable) obj).reverseTransform();
			} else if (obj instanceof ISEETransformable) {
				((ISEETransformable) obj).reverseTransform();
			} else if (obj instanceof IProductQuantityTransformable) {
				((IProductQuantityTransformable) obj).reverseTransform();
			} else if (obj instanceof IProductConfigurationTransformable) {
				((IProductConfigurationTransformable) obj).reverseTransform();
			} else if (obj instanceof ITemplateSkillTransformable) {
				((ITemplateSkillTransformable) obj).reverseTransform();
			} else if (obj instanceof IExecutableSkillTransformable) {
				((IExecutableSkillTransformable) obj).reverseTransform();
			} else if (obj instanceof IResourceExecutableSkillTransformable) {
				((IResourceExecutableSkillTransformable) obj).reverseTransform();
			} else {
				throw new IllegalArgumentException("Please add the transformable interface of this adapter " +
						"into Transformer: " + obj.getClass().getSimpleName());
			}
		}
	}
}
