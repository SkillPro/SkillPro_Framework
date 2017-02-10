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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PropertyConstraintNominal extends PropertyConstraint {
	private List<String> values = new ArrayList<>();
	
	public PropertyConstraintNominal(String name, List<String> values) {
		super(name);
		this.values = values;
	}
	
	/**
	 * A copy constructor.
	 * @param pcn a PropertyConstraintNominal
	 */
	public PropertyConstraintNominal(PropertyConstraintNominal pcn) {
		super(pcn);
		this.values.addAll(pcn.values);
	}
	
	public List<String> getValues() {
		return values;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append("=");
		sb.append("{");
		int i = 0;
		while (i < (values.size() - 1)) {
			sb.append(values.get(i).toString());
			sb.append(", ");
			i++;
		}
		if (i < values.size()) {
			sb.append(values.get(i).toString());
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyConstraintNominal other = (PropertyConstraintNominal) obj;
		return Objects.equals(values, other.values);
	}

	@Override
	public PropertyConstraintNominal clone() {
		return new PropertyConstraintNominal(this);
	}
}
