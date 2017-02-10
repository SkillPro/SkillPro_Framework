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

package skillpro.model.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyDesignator;

public class ProductionSkill extends Skill {
	private TemplateSkill templateSkill;
	private ProductConfiguration inputConfiguration;
	private ProductConfiguration outputConfiguration;
	private List<PropertyDesignator> propertyDesignators = new ArrayList<>();
	
	public ProductionSkill() {
		super("");
		setId(UUID.randomUUID().toString());
		inputConfiguration = new ProductConfiguration();
		outputConfiguration = new ProductConfiguration();
	}
	
	public ProductionSkill(String name, TemplateSkill templateSkill, ProductConfiguration inputConfiguration, ProductConfiguration outputConfiguration) {
		super(name);
		setId(UUID.randomUUID().toString());
		this.templateSkill = templateSkill;
		this.inputConfiguration = inputConfiguration;
		this.outputConfiguration = outputConfiguration;
	}
	
	public ProductionSkill(String name, TemplateSkill templateSkill, Set<ProductQuantity> inputs, Set<ProductQuantity> outputs) {
		super(name);
		setId(UUID.randomUUID().toString());
		this.templateSkill = templateSkill;
		inputConfiguration = new ProductConfiguration(UUID.randomUUID().toString(), inputs);
		outputConfiguration = new ProductConfiguration(UUID.randomUUID().toString(), outputs);
	}
	
	public List<PropertyDesignator> getEmptyAndFilledDesignators() {
		for (Property property : getProperties()) {
			boolean exists = false;
			for (PropertyDesignator designator : propertyDesignators) {
				if (designator.getProperty().equals(property)) {
					exists = true;
				}
			}
			if (!exists) {
				propertyDesignators.add(new PropertyDesignator(property, this, "-"));
			}
		}
		return propertyDesignators;
	}
	
	public PropertyDesignator getPropertyDesignator(Property property) {
		PropertyDesignator propertyDesignator = null;
		if (getProperties().contains(property)) {
			propertyDesignator = new PropertyDesignator(property, this, "-");
		}
		
		for (PropertyDesignator designator : propertyDesignators) {
			if (designator.getProperty().equals(property)) {
				propertyDesignator = designator;
			}
		}
		return propertyDesignator;
	}
	
	public void setTemplateSkill(TemplateSkill templateSkill) {
		this.templateSkill = templateSkill;
	}
	
	@Override
	public List<Property> getProperties() {
		return templateSkill.getProperties();
	}
	
	public ProductConfiguration getInputConfiguration() {
		return inputConfiguration;
	}
	
	public ProductConfiguration getOutputConfiguration() {
		return outputConfiguration;
	}
	
	public void setInputConfiguration(ProductConfiguration inputConfiguration) {
		this.inputConfiguration = inputConfiguration;
	}
	
	public void setOutputConfiguration(ProductConfiguration outputConfiguration) {
		this.outputConfiguration = outputConfiguration;
	}
	
	public TemplateSkill getTemplateSkill() {
		return templateSkill;
	}

	public List<PropertyDesignator> getPropertyDesignators() {
		return propertyDesignators;
	}
	
	public boolean addPropertyDesignator(PropertyDesignator designator) {
		if (designator != null && !propertyDesignators.contains(designator)) {
			return propertyDesignators.add(designator);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), inputConfiguration, outputConfiguration, templateSkill);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductionSkill other = (ProductionSkill) obj;
		return Objects.equals(inputConfiguration, other.inputConfiguration)
				&& Objects.equals(outputConfiguration, other.outputConfiguration)
				&& Objects.equals(templateSkill, other.templateSkill);
	}

	@Override
	public String toString() {
		return "ProductionSkill: " + getName() + ", input: " + inputConfiguration
				+ ", output: " + outputConfiguration;
	}
}
