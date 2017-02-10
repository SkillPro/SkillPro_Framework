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

public class Triple<N, M, P>{
	public N e1;
	public M e2;
	public P e3;
	
	public Triple(N e1, M e2, P e3) {
		this.e1 = e1;
		this.e2 = e2;
		this.e3 = e3;
	}
	
	public N getFirstElement() {
		return e1;
	}
	
	public M getSecondElement() {
		return e2;
	}
	
	public P getThirdElement() {
		return e3;
	}
	
	public void setFirstElement(N e1) {
		this.e1 = e1;
	}
	
	public void setSecondElement(M e2) {
		this.e2 = e2;
	}
	
	public void setThirdElement(P e3) {
		this.e3 = e3;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(e1, e2, e3);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		} else {
			Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
			return Objects.equals(e1, other.e1)
					&& Objects.equals(e2, other.e2)
					&& Objects.equals(e3, other.e3);
		}
	}
	
	@Override
	public String toString() {
		return "(" + e1 + "; " + e2 + "; " + e3 + ")";
	}
}
