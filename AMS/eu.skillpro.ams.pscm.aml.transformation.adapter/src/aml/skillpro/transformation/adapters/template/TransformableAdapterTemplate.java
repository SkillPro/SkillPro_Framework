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

package aml.skillpro.transformation.adapters.template;

import java.util.List;
import java.util.Map;
import java.util.Set;

import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyConstraintNominal;
import skillpro.model.properties.PropertyConstraintOrdinal;
import transformation.interfaces.ITransformable;
import aml.domain.Domain;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.model.NominalConstraint;
import aml.model.OrdinalConstraint;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

public abstract class TransformableAdapterTemplate {
	
	private static final List<Hierarchy<Role>> DEFAULT_ROLE_HIERARCHIES = AMLTransformationService.getAMLProvider()
			.getAMLModelRepo(Role.class).getFlattenedHierarchies();
	private static final List<Hierarchy<Interface>> DEFAULT_INTERFACE_HIERARCHIES = AMLTransformationService.getAMLProvider()
			.getAMLModelRepo(Interface.class).getFlattenedHierarchies();
	//no other way.. or maybe there is?
	private static List<Hierarchy<Role>> currentRoleHierarchies = DEFAULT_ROLE_HIERARCHIES;
	private static List<Hierarchy<Interface>> currentInterfaceHierarchies = DEFAULT_INTERFACE_HIERARCHIES;
	
	protected static final TransformationRepo TRANSFORMATION_REPO = AMLTransformationService.getTransformationProvider()
			.getTransformationRepo();
	protected static Map<Object, ITransformable> transformedObjectsMap = TRANSFORMATION_REPO.getTransformedObjectsMap();
	protected static Map<Object, Class<? extends ITransformable>> interfaceTransformablesMapping = TRANSFORMATION_REPO.getInterfaceTransformablesMapping();
	protected static Map<Object, Class<? extends ITransformable>> adapterTransformablesMapping = TRANSFORMATION_REPO.getAdapterTransformablesMapping();
	protected static Map<ITransformable, Object> reverseTransformedObjectsMap = TRANSFORMATION_REPO.getReverseTransformedObjectsMap();
	protected Map<Class<? extends ITransformable>, Object> inversedPivotAdapterMapping = TRANSFORMATION_REPO.getInvertedPivotAdapterTransformableMapping();
	
	protected final Hierarchy<Role> templateSkillHierarchy = findHierarchy("TemplateSkill", currentRoleHierarchies);
	protected final Hierarchy<Role> resourceConfigurationTypeHierarchy = findHierarchy("ResourceConfigurationType", currentRoleHierarchies);
	protected final Hierarchy<Role> processStructureHierarchy = findHierarchy("ProcessStructure", currentRoleHierarchies);
	protected final Hierarchy<Role> productStructureHierarchy = findHierarchy("ProductStructure", currentRoleHierarchies);
	protected final Hierarchy<Interface> productionSkillConnectorHierarchy = findHierarchy("ProductionSkillConnector", currentInterfaceHierarchies);
	
	protected PropertyConstraint convertConstraint(Constraint cons) {
		if (cons instanceof NominalConstraint) {
			NominalConstraint nominalConstraint = (NominalConstraint) cons;
			return new PropertyConstraintNominal(cons.getName(), nominalConstraint.getValues());
		} else if (cons instanceof OrdinalConstraint) {
			OrdinalConstraint ordinalConstraint = (OrdinalConstraint) cons;
			return new PropertyConstraintOrdinal(cons.getName(), ordinalConstraint.getMaxValue()
					, ordinalConstraint.getMinValue(), ordinalConstraint.getRequiredValue());
		}
		return null;
	}
	
	protected Constraint convertConstraint(PropertyConstraint cons) {
		if (cons instanceof PropertyConstraintNominal) {
			PropertyConstraintNominal nominalConstraint = (PropertyConstraintNominal) cons;
			return new NominalConstraint(cons.getName(), nominalConstraint.getValues());
		} else if (cons instanceof PropertyConstraintOrdinal) {
			PropertyConstraintOrdinal ordinalConstraint = (PropertyConstraintOrdinal) cons;
			return new OrdinalConstraint(cons.getName(), ordinalConstraint.getMaxValue(), 
					ordinalConstraint.getMinValue(), ordinalConstraint.getRequiredValue());
		}
		return null;
	}
	
	protected void addAttribute(InternalElement ie, String attributeName, String attributeType, String unit, String value) {
		Attribute att = new Attribute(attributeName, attributeType, unit);
		ie.addAttribute(att);
		AttributeDesignator designatorByAttribute = ie.getDesignatorByAttribute(att);
		att.addDesignator(designatorByAttribute);
		designatorByAttribute.setValue(value);
	}
	
	protected void addAttribute(InternalElement ie, Attribute attribute, String value) {
		ie.addAttribute(attribute);
		AttributeDesignator designatorByAttribute = ie.getDesignatorByAttribute(attribute);
		attribute.addDesignator(designatorByAttribute);
		if (value != null && !value.isEmpty()) {
			designatorByAttribute.setValue(value);
		}
	}
	
	protected static <D extends Domain> Hierarchy<D> findHierarchy(String name, List<Hierarchy<D>> flattenedHierarchies) {
		for (Hierarchy<D> hie : flattenedHierarchies) {
			if (hie.getName().equals(name)) {
				return hie;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected static <D extends Domain> Hierarchy<D> findActualHierarchy(D actualElement, Set<Object> context) {
		for (Object object : context) {
			if (object instanceof Hierarchy<?>) {
				Hierarchy<?> hie = (Hierarchy<?>) object;
				if (hie.getActualElement().equals(actualElement)) {
					//if actual Element is of type D, hierarchy will also be of type D
					return (Hierarchy<D>) hie;
				}
			}
		}
		return null;
	}
	
	public static void setCurrentRoleHierarchies(List<Hierarchy<Role>> roleHierarchies) {
		currentRoleHierarchies = roleHierarchies;
	}
	
	public static void setCurrentInterfaceHierarchies(List<Hierarchy<Interface>> interfaceHierarchies) {
		currentInterfaceHierarchies = interfaceHierarchies;
	}
	
	public static void revertRoleHierarchies() {
		currentRoleHierarchies = DEFAULT_ROLE_HIERARCHIES;
	}
	
	public static void revertInterfaceHierarchies() {
		currentInterfaceHierarchies = DEFAULT_INTERFACE_HIERARCHIES;
	}
	
	public static void revertEverything() {
		revertInterfaceHierarchies();
		revertRoleHierarchies();
	}
}
