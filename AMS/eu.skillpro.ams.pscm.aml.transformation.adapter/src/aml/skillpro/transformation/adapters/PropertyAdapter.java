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

import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyType;
import transformation.interfaces.ITransformable;
import aml.model.Attribute;
import aml.model.AttributeType;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IPropertyTransformable;

public class PropertyAdapter extends TransformableAdapterTemplate implements IPropertyTransformable {
	private Property property;
	
	public PropertyAdapter() {
		this(new Property());
	}
	
	public PropertyAdapter(Property property) {
		this.property = property;
	}
	
	@Override
	public Property getElement() {
		return property;
	}
	
	@Override
	public IPropertyTransformable transform(Attribute attribute) {
		//ritual start
		if (transformedObjectsMap.containsKey(attribute)) {
			return (IPropertyTransformable) transformedObjectsMap.get(attribute);
		}
		//ritual end
		
		Attribute parentAttribute = attribute.getParent();
		if (parentAttribute != null) {
			IPropertyTransformable transformedParent = new PropertyAdapter().transform(parentAttribute);
			property.setParent((Property) transformedParent.getElement());
		}
		property.setName(attribute.getName());
		property.setDescription(attribute.getDescription());
		property.setUnit(attribute.getUnit());
		property.setType(convertFromAttributeType(attribute.getAttributeType()));
		
		//ritual start
		transformedObjectsMap.put(attribute, this);
		return this;
		//ritual end
	}
	
	@Override
	public Attribute reverseTransform() {
		//ritual start
//		if (reverseTransformedObjectsMap.containsKey(this)) {
//			return (Attribute) reverseTransformedObjectsMap.get(this);
//		}
		//ritual end
		
		Attribute attribute = new Attribute(getTransformableName(), getPropertyType(), getPropertyUnit());
		attribute.setDescription(getPropertyDescription());
		if (getTransformableParent() != null) {
			Attribute parentAttribute = getTransformableParent().reverseTransform();
			attribute.setParent(parentAttribute);
		}
		
		//ritual start
		reverseTransformedObjectsMap.put(this, attribute);
		
		return attribute;
	}
	
	private PropertyType convertFromAttributeType(AttributeType type) {
		PropertyType propType;
		switch (type) {
		case BOOLEAN:
			propType = PropertyType.BOOLEAN;
			break;
		case DOUBLE:
			propType = PropertyType.DOUBLE;
			break;
		case INTEGER:
			propType = PropertyType.INTEGER;
			break;
		case STRING:
			propType = PropertyType.STRING;
			break;
		default:
			propType = PropertyType.STRING;
			break;
		}
		return propType;
	}
	
	@Override
	public String getTransformableName() {
		return property.getName();
	}

	@Override
	public String getTransformableID() {
		return property.getRandomID();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		List<ITransformable> transformables = new ArrayList<>();
		for (Property sub : property.getSubProperties()) {
			transformables.add(new PropertyAdapter(sub));
		}
		return transformables;
	}

	@Override
	public IPropertyTransformable getTransformableParent() {
		IPropertyTransformable transformableParent = null;
		if (property.getParent() != null) {
			transformableParent = new PropertyAdapter(property.getParent());
		}
		return transformableParent;
	}

	@Override
	public String getPropertyDescription() {
		return property.getDescription();
	}
	
	@Override
	public String getPropertyUnit() {
		return property.getUnit();
	}

	@Override
	public String getPropertyType() {
		return property.getType().name();
	}

	@Override
	public int hashCode() {
		return Objects.hash(property);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyAdapter other = (PropertyAdapter) obj;
		return Objects.equals(property, other.property);
	}
}
