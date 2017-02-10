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
import java.util.Map;
import java.util.Set;

import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.ResourceConfigurationTypeAdapter;
import aml.skillpro.transformation.adapters.TemplateSkillAdapter;
import aml.skillpro.transformation.interfaces.IAMLElementTransformable;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IPrePostRequirementTransformable;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IProductQuantityTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;
import aml.skillpro.transformation.interfaces.IProductionSkillTransformable;
import aml.skillpro.transformation.interfaces.IRequirementTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTypeTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.ISEETransformable;
import aml.skillpro.transformation.interfaces.ISetupTransformable;
import aml.skillpro.transformation.interfaces.ISkillConfigurationTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

public class Transformer {
	private final static Transformer INSTANCE = new Transformer();
	
	private final TransformationRepo transformationRepo;
	private final Map<Object, Class<? extends ITransformable>> adapterTransformables;
	
	public Transformer() {
		transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
		adapterTransformables = transformationRepo.getAdapterTransformablesMapping();
	}
	
	public static Transformer getInstance() {
		return INSTANCE;
	}
	
	public void transform() throws InstantiationException, IllegalAccessException {
		Set<Object> toTransform = new HashSet<>();
		for (Object object : transformationRepo.getInterfaceTransformablesMapping().keySet()) {
			Class<? extends ITransformable> cls = transformationRepo.getInterfaceTransformablesMapping().get(object);
			if (cls != null && ITemplateSkillTransformable.class.isAssignableFrom(cls)) {
				toTransform.add(object);
			} else if (cls != null && IResourceConfigurationTypeTransformable.class.isAssignableFrom(cls)) {
				toTransform.add(object);
			}
		}
		for (Object obj : transformationRepo.getAdapterTransformablesMapping().keySet()) {
			if (obj instanceof InternalElement) {
				for (Hierarchy<InternalElement> hie : getHierarchies((InternalElement) obj)) {
					toTransform.add(hie);
				}
			}
		}
		
		toTransform.addAll(toTransform);
		
		transform(toTransform);
	}
	
	@SuppressWarnings("unchecked")
	public void transform(Set<Object> toTransform) throws InstantiationException, IllegalAccessException {
		//before transforming, always wipe the transformedObjectsMapping
		transformationRepo.getTransformedObjectsMap().clear();

		for (Object obj : toTransform) {
			if (obj instanceof Hierarchy<?> && ((Hierarchy<?>) obj).getElement() instanceof InternalElement) {
				Hierarchy<InternalElement> hie = (Hierarchy<InternalElement>) obj;
				Class<? extends ITransformable> clazz = adapterTransformables.get(hie.getElement());
				if (clazz == null) {
					throw new IllegalArgumentException("Cannot transform: " + hie);
				} else {
					if (IFactoryNodeTransformable.class.isAssignableFrom(clazz)) {
						((IFactoryNodeTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IProductTransformable.class.isAssignableFrom(clazz)) {
						((IProductTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IResourceSkillTransformable.class.isAssignableFrom(clazz)) {
						((IResourceSkillTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IProductionSkillTransformable.class.isAssignableFrom(clazz)) {
						((IProductionSkillTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (ISetupTransformable.class.isAssignableFrom(clazz)) {
						((ISetupTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IRequirementTransformable.class.isAssignableFrom(clazz)) {
						((IRequirementTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IResourceConfigurationTransformable.class.isAssignableFrom(clazz)) {
						((IResourceConfigurationTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (ISkillConfigurationTransformable.class.isAssignableFrom(clazz)) {
						((ISkillConfigurationTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IAMLElementTransformable.class.isAssignableFrom(clazz)) {
						((IAMLElementTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (ISEETransformable.class.isAssignableFrom(clazz)) {
						((ISEETransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IProductQuantityTransformable.class.isAssignableFrom(clazz)) {
						((IProductQuantityTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IProductConfigurationTransformable.class.isAssignableFrom(clazz)) {
						((IProductConfigurationTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else if (IPrePostRequirementTransformable.class.isAssignableFrom(clazz)) {
						((IPrePostRequirementTransformable) clazz.newInstance()).transform(hie, toTransform);
					} else {
						throw new IllegalArgumentException("Please add the transformable interface of this adapter " +
								"into Transformer: " + clazz.getSimpleName());
					}
				}
			} else if (obj instanceof Role) {
				Class<? extends ITransformable> cls = transformationRepo.getInterfaceTransformablesMapping().get(obj);
				if (cls != null && ITemplateSkillTransformable.class.isAssignableFrom(cls)) {
					Class<?> impClass = TemplateSkillAdapter.class;
					((ITemplateSkillTransformable) impClass.newInstance()).transform((Role) obj, toTransform);
				} else if (cls != null && IResourceConfigurationTypeTransformable.class.isAssignableFrom(cls)) {
					Class<?> impClass = ResourceConfigurationTypeAdapter.class;
					((IResourceConfigurationTypeTransformable) impClass.newInstance()).transform((Role) obj, toTransform);
				}
			} else {
				throw new IllegalArgumentException("Unidentified object: " + obj);
			}
		}
	}
	
	private Set<Hierarchy<InternalElement>> getHierarchies(InternalElement ie) {
		Set<Hierarchy<InternalElement>> hierarchies = new HashSet<>();
		for (Hierarchy<InternalElement> hie : AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(InternalElement.class).getFlattenedHierarchies()) {
			if (hie.getElement() == ie) {
				hierarchies.add(hie);
			}
		}
		return hierarchies;
	}
}
