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

package aml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import aml.domain.Domain;
import aml.domain.InternalElement;

public class Hierarchy<D extends Domain> {
	private String name;
	private D element;
	private Hierarchy<D> parent;
	private List<Hierarchy<D>> children = new ArrayList<>();
	
	public Hierarchy(String name, D element) {
		this.name = name;
		this.element = element;
	}
	
	public Hierarchy<D> getParent() {
		return parent;
	}
	
	public void setParent(Hierarchy<D> parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public List<Hierarchy<D>> getChildren() {
		return children;
	}
	
	public String getName() {
		return name;
	}
	/**
	 *
	 * @return the element contained in this Hierarchy. If the element is an InternalElement
	 * and if it is only a mirror of another InternalElement then that other InternalElement
	 * will be returned instead. To get the mirror Element, use getActualElement().
	 */
	@SuppressWarnings("unchecked")
	public D getElement() {
		if (element instanceof InternalElement) {
			InternalElement ie = (InternalElement) element;
			if (ie.getReferencedInternalElement() != null) {
				ie = ie.getReferencedInternalElement();
			}
			return (D) ie;
		}
		return element;
	}
	
	public D getActualElement() {
		return element;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addChild(Hierarchy<?> child) {
		if (child != null && !children.contains(child)) {
			if (child.getElement().getClass().equals(getElement().getClass())) {
				if (children.add((Hierarchy<D>) child)) {
					if (child.getParent() == null) {
						((Hierarchy<D>) child).setParent(this);
					}
					return true;
				}
				return true;
			} else {
				throw new IllegalArgumentException("Cannot add this child: " + child);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element, name, parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hierarchy<?> other = (Hierarchy<?>) obj;
		return Objects.equals(element, other.element)
				&& Objects.equals(name, other.name)
				&& Objects.equals(parent, other.parent);
	}
}
