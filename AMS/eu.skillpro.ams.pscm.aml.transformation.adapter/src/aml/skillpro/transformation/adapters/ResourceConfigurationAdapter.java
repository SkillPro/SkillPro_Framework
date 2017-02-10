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

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTypeTransformable;

public class ResourceConfigurationAdapter extends TransformableAdapterTemplate implements IResourceConfigurationTransformable {
	private ResourceConfiguration resourceConfiguration;
	
	public ResourceConfigurationAdapter() {
		this(new ResourceConfiguration("", "", null));
	}
	
	public ResourceConfigurationAdapter(ResourceConfiguration configuration) {
		this.resourceConfiguration = configuration;
	}
	
	@Override
	public String getTransformableName() {
		return resourceConfiguration.getName();
	}

	@Override
	public String getTransformableID() {
		return resourceConfiguration.getId();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		return null;
	}

	@Override
	public ITransformable getTransformableParent() {
		return null;
	}

	@Override
	public ResourceConfiguration getElement() {
		return resourceConfiguration;
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
			return (ITransformable) transformedObjectsMap.get(ie);
		}
		//ritual end
		
		resourceConfiguration.setName(ie.getName());
		resourceConfiguration.setId(ie.getId());
		
		//get supportedRoles
		if (ie.getSupportedRoles().size() > 1) {
			throw new IllegalArgumentException("This ResourceConfiguration may only possess at most 1 supportedRole: " + resourceConfiguration.getName());
		}
		
		for (Role supportedRole : ie.getSupportedRoles()) {
			ITransformable transformedResourceConfiguration = new ResourceConfigurationTypeAdapter().transform
					(supportedRole, context);
			if (transformedResourceConfiguration == null) {
				throw new IllegalArgumentException("ResourceConfigurationType cannot be transformed for this Role: " 
						+ supportedRole.getName());
			}
			resourceConfiguration.setResourceConfigurationType((ResourceConfigurationType) transformedResourceConfiguration.getElement());
		}
		
		for (AttributeDesignator des : ie.getDesignators()) {
			String desName = des.getAttribute().getName();
			List<PropertyConstraint> propertyConstraints = new ArrayList<>();
			List<Constraint> constraints = des.getConstraints();
			for (Constraint cons : constraints) {
				propertyConstraints.add(convertConstraint(cons));
			}
			
			PropertyDesignator propDes = new PropertyDesignator(findProperty(desName), null, des.getValue(), propertyConstraints);
			if (propDes.getProperty() != null) {
				resourceConfiguration.updateDesignator(propDes);
			}
		}
		
		//owner
		Resource owner = null;
		if (object.getActualElement() == ie) {
			Hierarchy<InternalElement> parent = object.getParent();
			if (parent != null && context.contains(parent)) {
				Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(parent.getElement());
				ITransformable transformedOwner = ((IFactoryNodeTransformable) transClass.newInstance())
						.transform(parent, context);
				owner = (Resource) transformedOwner.getElement();
			}
		} else {
			Hierarchy<InternalElement> parent = findActualHierarchy(object.getElement(), context).getParent();
			if (parent != null && context.contains(parent)) {
				Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(parent.getElement());
				ITransformable transformedOwner = ((IFactoryNodeTransformable) transClass.newInstance())
						.transform(parent, context);
				owner = (Resource) transformedOwner.getElement();
			}
		}
		
		if (owner == null) {
			throw new IllegalArgumentException("This ResourceConfiguration: " + resourceConfiguration + ", has to "
					+ "have an owner");
		}
		owner.addResourceConfiguration(resourceConfiguration);
		
		Attribute currentConfAtt = new Attribute("Current Configuration", AttributeType.BOOLEAN, "");
		AttributeDesignator currentConfDes = ie.getDesignatorByAttribute(currentConfAtt);
		if (currentConfDes != null) {
			if (currentConfDes.getValue().equalsIgnoreCase("true")) {
				owner.setCurrentResourceConfiguration(resourceConfiguration);
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
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		//add AttributeDesignators
		for (PropertyDesignator des : resourceConfiguration.getPropertyDesignators()) {
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

		//add supportedRole
		if (getTransformableResourceConfigurationType() != null) {
			internalElement.addSupportedRole(((Hierarchy<Role>) getTransformableResourceConfigurationType().reverseTransform()).getElement());
		}
		
		if (resourceConfiguration.equals(resourceConfiguration.getResource().getCurrentResourceConfiguration())) {
			Attribute currentConfAtt = new Attribute("Current Configuration", AttributeType.BOOLEAN, "");
			addAttribute(internalElement, currentConfAtt, "true");
		}
		
		//parent will be set in FactoryNodeAdapter
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}
	
	private Property findProperty(String name) {
		for (Property prop : resourceConfiguration.getProperties()) {
			if (prop.getName().equals(name)) {
				return prop;
			}
		}
		return null;
	}
	
	@Override
	public IResourceConfigurationTypeTransformable getTransformableResourceConfigurationType() {
		if (resourceConfiguration.getResourceConfigurationType() == null) {
			//FIXME IllegalArgumentException?
			return null;
		}
		return new ResourceConfigurationTypeAdapter(resourceConfiguration.getResourceConfigurationType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceConfiguration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceConfigurationAdapter other = (ResourceConfigurationAdapter) obj;
		return Objects.equals(resourceConfiguration, other.resourceConfiguration);
	}
}
