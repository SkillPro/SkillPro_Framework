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

package aml.transformation.repo.aml;

import java.util.ArrayList;
import java.util.List;

import aml.domain.Domain;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.transformation.repo.Repo;

public class AMLModelRepo<D extends Domain> implements Repo {
	private Class<D> clazz;
	private List<Hierarchy<D>> flattenedHierarchies = new ArrayList<>();
	private List<Root<D>> entities = new ArrayList<>();
	
	public AMLModelRepo(Class<D> clazz) {
		this.clazz = clazz;
	}
	
	public List<Hierarchy<D>> getFlattenedHierarchies() {
		return flattenedHierarchies;
	}
	
	@SuppressWarnings("unchecked")
	public void addToFlattenedHierarchies(Hierarchy<?> hie) {
		if (hie.getElement() == null) {
			throw new IllegalArgumentException("A hierarchy must always be accompanied with an element.");
		}
		if (clazz.isAssignableFrom(hie.getElement().getClass())) {
			flattenedHierarchies.add((Hierarchy<D>) hie);
		}
	}
	
	@Override
	public List<Root<D>> getEntities() {
		return entities;
	}

	@Override
	public int size() {
		return getEntities().size();
	}
	
	@Override
	public void wipeAllData() {
		entities.clear();
		flattenedHierarchies.clear();
	}
}
