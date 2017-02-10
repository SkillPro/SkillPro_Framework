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

package skillpro.model.utils;

import java.util.Objects;

public class ShiftSchedule {
	private String name;
	private double hoursPerDay;
	private double shiftsPerDay;
	private double daysPerPeriod;
	
	public ShiftSchedule(String name, double hoursPerDay, double shiftsPerDay, double daysPerPeriod) {
		this.name = name;
		this.hoursPerDay = hoursPerDay;
		this.shiftsPerDay = shiftsPerDay;
		this.daysPerPeriod = daysPerPeriod;
	}
	
	public String getName() {
		return name;
	}

	public double getHoursPerDay() {
		return hoursPerDay;
	}
	
	public double getShiftsPerDay() {
		return shiftsPerDay;
	}
	
	public double getDaysPerPeriod() {
		return daysPerPeriod;
	}

	@Override
	public int hashCode() {
		return Objects.hash(daysPerPeriod, hoursPerDay, name, shiftsPerDay);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShiftSchedule other = (ShiftSchedule) obj;
		return daysPerPeriod == other.daysPerPeriod
				&& hoursPerDay == other.hoursPerDay
				&& Objects.equals(name, other.name)
				&& shiftsPerDay == other.shiftsPerDay;
	}
}
