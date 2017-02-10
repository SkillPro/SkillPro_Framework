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

package aml.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Interface extends Domain {
	private Interface referencedInterface;
	private List<Interface> children = new ArrayList<>();
	
	public Interface(String name, Interface referencedInterface) {
		super(name);
		setReferencedInterface(referencedInterface);
	}
	
	public void setReferencedInterface(Interface referencedInterface) {
		this.referencedInterface = referencedInterface;
		if (referencedInterface != null) {
			referencedInterface.addChild(this);
		}
	}
	
	public List<Interface> getChildren() {
		return children;
	}
	
	public boolean addChild(Interface interfais) {
		if (interfais != null && !children.contains(interfais)) {
			return children.add(interfais);
		}
		return false;
	}
	
	public Interface getReferencedInterface() {
		return referencedInterface;
	}

	@Override
	public int hashCode() {
		return Objects.hash(referencedInterface);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interface other = (Interface) obj;
		return Objects.equals(referencedInterface, other.referencedInterface);
	}
}
