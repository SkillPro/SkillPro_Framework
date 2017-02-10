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

package skillpro.model.repo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Repo<R> implements Iterable<R>{
	protected List<R> list = new ArrayList<>();
	
	public List<R> getEntities(){
		return list;
	}
	
	public int size(){
		return list.size();
	}
	
	public void wipeAllData(){
		list.clear();
	}

	public boolean isEmpty(){
		return list.isEmpty();
	}

	@Override
	public Iterator<R> iterator(){
		return getEntities().iterator();
	}
	
	public boolean contains(Object element){
		return list.contains(element);
	}
	
	public boolean add(R element){
		return list.add(element);
	}
	
	public boolean addIfAbsent(R element){
		return !contains(element) && add(element);
	}

	public void remove(Object toDelete){
		list.remove(toDelete);
	}
}
