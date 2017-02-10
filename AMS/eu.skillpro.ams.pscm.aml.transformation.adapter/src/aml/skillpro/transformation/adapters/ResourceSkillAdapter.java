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

package aml.skillpro.transformation.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import skillpro.model.assets.Resource;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IPrePostRequirementTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;

public class ResourceSkillAdapter extends TransformableAdapterTemplate implements IResourceSkillTransformable {
	private ResourceSkill resourceSkill;
	
	public ResourceSkillAdapter() {
		this(new ResourceSkill());
	}
	
	public ResourceSkillAdapter(ResourceSkill resourceSkill) {
		this.resourceSkill = resourceSkill;
	}
	
	@Override
	public ResourceSkill getElement() {
		return resourceSkill;
	}
	
	@Override
	public List<Object> getAMLElements() {
		return resourceSkill.getAmlElements();
	}
	
	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context) throws InstantiationException, IllegalAccessException {
		//ritual start
		if (!context.contains(object)) {
			return null;
		}
		//returns a transformed element if it has already been transformed before.
		InternalElement ie = object.getElement();
		if (transformedObjectsMap.containsKey(ie)) {
			//pre post requirements
			ITransformable iTransformable = (ITransformable) transformedObjectsMap.get(ie);
			ResourceSkill resourceSkill = (ResourceSkill) iTransformable.getElement();
			List<PrePostRequirement> prePostRequirements = new ArrayList<>(resourceSkill.getPrePostRequirements());
			if (prePostRequirements.isEmpty()) {
				for (Hierarchy<InternalElement> child : object.getChildren()) {
					Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
					if (IPrePostRequirementTransformable.class.isAssignableFrom(transClass)) {
						ITransformable transformedConf = ((IPrePostRequirementTransformable) transClass.newInstance())
								.transform(child, context);
						PrePostRequirement requirementPair = (PrePostRequirement) transformedConf.getElement();
						requirementPair.getPreRequirement().setMainResourceSkill(resourceSkill);
						requirementPair.getPostRequirement().setMainResourceSkill(resourceSkill);
						prePostRequirements.add(requirementPair);
					}
				}
				if (!prePostRequirements.isEmpty()) {
					resourceSkill.setPrePostRequirements(prePostRequirements);
				}
				
			}
			return iTransformable;
		}
		
		//ritual end
		
		resourceSkill.setName(ie.getName());
		resourceSkill.setId(ie.getId());
		
		//asset
		if (object.getActualElement() == ie) {
			Hierarchy<InternalElement> parentElement = object.getParent();
			Class<? extends ITransformable> transClass = adapterTransformablesMapping
					.get(parentElement.getElement());
			ITransformable transformedAsset = ((IFactoryNodeTransformable) transClass.newInstance())
					.transform(parentElement, context);
			
			resourceSkill.setResource((Resource) transformedAsset.getElement());
		} else {
			//this means that the parent of the element is a configuration, so that means, to get the resource
			//we have to get the parent of the configuration.
			//it is also possible to skip this section and just hope that the real resource skill
			//instead of the mirror, will be transformed. Instead of hoping, I've decided to just
			//implement it like this
			Hierarchy<InternalElement> assetElement = findActualHierarchy(object.getElement(), context).getParent();
			Class<? extends ITransformable> transClass = adapterTransformablesMapping
					.get(assetElement.getElement());
			ITransformable transformedAsset = ((IFactoryNodeTransformable) transClass.newInstance())
					.transform(assetElement, context);
			
			resourceSkill.setResource((Resource) transformedAsset.getElement());
		}
		
		//template skills
		if (ie.getSupportedRoles() == null) {
			throw new IllegalArgumentException("Template can't be null, Resource: " + resourceSkill.getName());
		}
		
		for (Role supportedRole : ie.getSupportedRoles()) {
			//it's clear that TemplateSkillAdapter is the only one used to transform
			//TemplateSkills, that's why it's not needed to do the same method as the one we used for
			//FactoryNode's transformation.
			//it's of course possible to do this like the others, however Role and Adapter will have to be added
			//to the adapterTransformablesMapping, meaning that the transformation mapping,
			//its parser, exporter, and other related stuff will also need to be changed.
			ITransformable transformedTempSkill = new TemplateSkillAdapter().transform
					(supportedRole, context);
			if (transformedTempSkill == null) {
				throw new IllegalArgumentException("TemplateSkill cannot be transformed for this Role: " 
						+ supportedRole.getName());
			}
			resourceSkill.setTemplateSkill((TemplateSkill) transformedTempSkill.getElement());
		}
		
		for (AttributeDesignator des : ie.getDesignators()) {
			String desName = des.getAttribute().getName();
			List<PropertyConstraint> propertyConstraints = new ArrayList<>();
			List<Constraint> constraints = des.getConstraints();
			for (Constraint cons : constraints) {
				propertyConstraints.add(convertConstraint(cons));
			}
			
			PropertyDesignator propDes = new PropertyDesignator(findProperty(desName), resourceSkill, ""
					, propertyConstraints);
			if (propDes.getProperty() != null) {
				resourceSkill.addPropertyDesignator(propDes);
			}
		}

		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object reverseTransform() {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		Hierarchy<InternalElement> resourceHie = (Hierarchy<InternalElement>) getTransformableResource().reverseTransform();
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		//add supportedRole
		internalElement.addSupportedRole(((Hierarchy<Role>) getTransformableTemplateSkill().reverseTransform()).getElement());
		
		//add AttributeDesignators
		for (PropertyDesignator des : resourceSkill.getPropertyDesignators()) {
			//constraints
			List<Constraint> constraints = new ArrayList<>();
			List<PropertyConstraint> propertyConstraints = des.getConstraints();
			for (PropertyConstraint cons : propertyConstraints) {
				constraints.add(convertConstraint(cons));
			}
			
			Attribute attribute = new PropertyAdapter(des.getProperty()).reverseTransform();
			if (attribute == null) {
				throw new IllegalArgumentException("Cannot reverse transform attribute: " + des.getProperty().getName());
			}
			AttributeDesignator attDes = new AttributeDesignator(attribute, 
				internalElement, constraints, des.getValue());
			internalElement.addDesignator(attDes);
			attDes.getAttribute().addDesignator(attDes);
		}
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		objectHie.setParent(resourceHie);
		
		//avoid loop
		reverseTransformedObjectsMap.put(this, objectHie);

		//add default children
		for (Hierarchy<InternalElement> child : childrenOfResourceSkillHierarchy()) {
			objectHie.addChild(child);
		}
		//add aml elements
		for (Object obj : getAMLElements()) {
			objectHie.addChild((Hierarchy<InternalElement>) obj);
		}
		
		//start ritual
		return objectHie;
		//end ritual
	}
	
	@SuppressWarnings("unchecked")
	private List<Hierarchy<InternalElement>> childrenOfResourceSkillHierarchy() {
		List<Hierarchy<InternalElement>> children = new ArrayList<>();
		
		InternalElement preConfiguration = new InternalElement(UUID.randomUUID().toString(), "Pre-Configuration");
		preConfiguration.setRequiredRole((Role) inversedPivotAdapterMapping.get(PreConfigurationAdapter.class));
		Hierarchy<InternalElement> preConfigurationHierarchy = new Hierarchy<InternalElement>(preConfiguration.getName(), preConfiguration);
		if (getTransformablePreConfiguration() != null) {
			Hierarchy<InternalElement> resourceConfiguration = (Hierarchy<InternalElement>) getTransformablePreConfiguration().reverseTransform();
			InternalElement actualPreConfiguration = new InternalElement(UUID.randomUUID().toString(), resourceConfiguration.getName());
			actualPreConfiguration.setReferencedInternalElement(resourceConfiguration.getElement());
			Hierarchy<InternalElement> actualPreConfigurationHierarchy = new Hierarchy<InternalElement>(actualPreConfiguration.getName(), actualPreConfiguration);
			actualPreConfigurationHierarchy.setParent(preConfigurationHierarchy);
		}
		
		InternalElement postConfiguration = new InternalElement(UUID.randomUUID().toString(), "Post-Configuration");
		postConfiguration.setRequiredRole((Role) inversedPivotAdapterMapping.get(PostConfigurationAdapter.class));
		Hierarchy<InternalElement> postConfigurationHierarchy = new Hierarchy<InternalElement>(postConfiguration.getName(), postConfiguration);
		if (getTransformablePostConfiguration() != null) {
			Hierarchy<InternalElement> resourceConfiguration = (Hierarchy<InternalElement>) getTransformablePostConfiguration().reverseTransform();
			InternalElement actualPostConfiguration = new InternalElement(UUID.randomUUID().toString(), resourceConfiguration.getName());
			actualPostConfiguration.setReferencedInternalElement(resourceConfiguration.getElement());
			Hierarchy<InternalElement> actualPostConfigurationHierarchy = new Hierarchy<InternalElement>(actualPostConfiguration.getName(), actualPostConfiguration);
			actualPostConfigurationHierarchy.setParent(postConfigurationHierarchy);
		}
		
		//add pre post requirements
		for (IPrePostRequirementTransformable trans : getTransformablePrePostRequirements()) {
			children.add((Hierarchy<InternalElement>) trans.reverseTransform());
		}
		children.add(preConfigurationHierarchy);
		children.add(postConfigurationHierarchy);
		
		return children;
	}
	
	private Property findProperty(String name) {
		for (Property prop : resourceSkill.getProperties()) {
			if (prop.getName().equals(name)) {
				return prop;
			}
		}
		return null;
	}
	
	@Override
	public IResourceTransformable getTransformableResource() {
		if (resourceSkill.getResource() == null) {
			throw new IllegalArgumentException("Resource is null for this ResourceSkill: " + getTransformableName());
		}
		return new ResourceAdapter(resourceSkill.getResource());
	}

	@Override
	public String getTransformableName() {
		return resourceSkill.getName();
	}

	@Override
	public String getTransformableID() {
		return resourceSkill.getId();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		//no children
		return null;
	}

	@Override
	public ITransformable getTransformableParent() {
		//no parent
		return null;
	}
	
	@Override
	public ITemplateSkillTransformable getTransformableTemplateSkill() {
		if (resourceSkill.getTemplateSkill() == null) {
			throw new IllegalArgumentException("A ResourceSkill without a TemplateSkill?? IMPOSSIBRU: " + getTransformableName() + ":" + getTransformableID());
		}
		return new TemplateSkillAdapter(resourceSkill.getTemplateSkill());
	}
	
	@Override
	public List<IPrePostRequirementTransformable> getTransformablePrePostRequirements() {
		List<IPrePostRequirementTransformable> temps = new ArrayList<>();
		for (PrePostRequirement pair : resourceSkill.getPrePostRequirements()) {
			temps.add(new PrePostRequirementAdapter(pair));
		}
		return temps;
	}
	
	@Override
	public IResourceConfigurationTransformable getTransformablePostConfiguration() {
		if (resourceSkill.getPostConfiguration() == null) {
			return null;
		}
		return new ResourceConfigurationAdapter(resourceSkill.getPostConfiguration());
	}
	
	@Override
	public IResourceConfigurationTransformable getTransformablePreConfiguration() {
		if (resourceSkill.getPreConfiguration() == null) {
			return null;
		}
		return new ResourceConfigurationAdapter(resourceSkill.getPreConfiguration());
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceSkill);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceSkillAdapter other = (ResourceSkillAdapter) obj;
		return Objects.equals(resourceSkill, other.resourceSkill);
	}
}
