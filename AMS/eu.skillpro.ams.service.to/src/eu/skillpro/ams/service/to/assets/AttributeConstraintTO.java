/**
 * 
 */
/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: AMS Server
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

package eu.skillpro.ams.service.to.assets;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyConstraintNominal;
import skillpro.model.properties.PropertyConstraintOrdinal;

/**
 * @author caliqi
 * @date 25.09.2014
 * 
 */
public class AttributeConstraintTO {
	private String name;
	private double minValue;
	private double maxValue;
	private String requiredValue;
	private List<String> values = new ArrayList<String>();

	/**
	 * @param pc
	 */
	public AttributeConstraintTO(PropertyConstraint propertyConstraint) {
		this.name = propertyConstraint.getName();
		if (propertyConstraint instanceof PropertyConstraintOrdinal) {
			PropertyConstraintOrdinal pc = (PropertyConstraintOrdinal) propertyConstraint;
			this.minValue = pc.getMinValue();
			this.maxValue = pc.getMaxValue();
			this.requiredValue = pc.getRequiredValue();
		} else if (propertyConstraint instanceof PropertyConstraintNominal) {
			PropertyConstraintNominal pc = (PropertyConstraintNominal) propertyConstraint;
			this.values = pc.getValues();
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the minValue
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the requiredValue
	 */
	public String getRequiredValue() {
		return requiredValue;
	}

	/**
	 * @param requiredValue
	 *            the requiredValue to set
	 */
	public void setRequiredValue(String requiredValue) {
		this.requiredValue = requiredValue;
	}

	/**
	 * @return the values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(List<String> values) {
		this.values = values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AttributeConstraintTO [name=" + name + ", minValue=" + minValue
				+ ", maxValue=" + maxValue + ", requiredValue=" + requiredValue
				+ ", values=" + values + "]";
	}

}
