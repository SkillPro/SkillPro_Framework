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



public class Pair<N, M> {
	private N firstElement;
	private M secondElement;
	
	public Pair(N firstElement, M secondElement) {
		this.firstElement = firstElement;
		this.secondElement = secondElement;
	}
	
	public Pair(Pair<N, M> pair){
		this(pair.firstElement, pair.secondElement);
	}

	public N getFirstElement() {
		return firstElement;
	}
	
	public M getSecondElement() {
		return secondElement;
	}
	
	public void setFirstElement(N firstElement) {
		this.firstElement = firstElement;
	}
	
	public void setSecondElement(M secondElement) {
		this.secondElement = secondElement;
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstElement, secondElement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		return Objects.equals(firstElement, other.firstElement)
				&& Objects.equals(secondElement, other.secondElement);
	}
	
	@Override
	public String toString() {
		return "(" + firstElement + "; " + secondElement + ")";
	}
}
