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

package skillpro.model.properties;

import java.util.Objects;

public class PropertyConstraintOrdinal extends PropertyConstraint {
	private double maxValue;
	private double minValue;
	private String requiredValue;
	
	public PropertyConstraintOrdinal(String name, double maxValue, double minValue) {
		super(name);
		this.maxValue = maxValue;
		this.minValue = minValue;
	}
	
	public PropertyConstraintOrdinal(String name, double maxValue, double minValue, String requiredValue) {
		this(name, maxValue, minValue);
		this.requiredValue = requiredValue;
	}
	
	public PropertyConstraintOrdinal(PropertyConstraintOrdinal pco) {
		super(pco);
		this.maxValue = pco.maxValue;
		this.minValue = pco.minValue;
		this.requiredValue = pco.requiredValue;
	}

	public String getRequiredValue() {
		return requiredValue;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
	
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	
	public void setRequiredValue(String requiredValue) {
		this.requiredValue = requiredValue;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append("=");
		sb.append("{");
		
		sb.append("min=" + minValue + ", ");
		if (requiredValue != null && !requiredValue.isEmpty()) {
			sb.append("required=" + requiredValue + ", ");
		}
		sb.append("max=" + maxValue);
		
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxValue, minValue, requiredValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyConstraintOrdinal other = (PropertyConstraintOrdinal) obj;
		return maxValue == other.maxValue
				&& minValue == other.minValue
				&& Objects.equals(requiredValue, other.requiredValue);
	}

	@Override
	public PropertyConstraintOrdinal clone() {
		return new PropertyConstraintOrdinal(this);
	}	
}
