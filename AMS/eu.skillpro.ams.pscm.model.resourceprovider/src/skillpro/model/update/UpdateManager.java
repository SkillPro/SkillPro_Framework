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

package skillpro.model.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateManager {
	private Map< Class<?>, List<Updatable>> updatables = new HashMap<Class<?>, List<Updatable>>();
	private List<Updatable> generalUpdatables = new ArrayList<Updatable>(); // for class unspecific messages

	public void registerUpdatable(Updatable u, Class<?> domainClass) {
		if (!updatables.containsKey(domainClass)) {
			updatables.put(domainClass, new ArrayList<Updatable>());
		}
		if (!updatables.get(domainClass).contains(u)) {
			updatables.get(domainClass).add(u);
		}
		if (!generalUpdatables.contains(u)) { // every updateble can be included in this list only once 
			generalUpdatables.add(u);
		}
	}

	/**
	 * Use this method to notify all registered listeners for a given
	 * domainClass about an event.
	 * 
	 * @param type
	 *            - the Type of event that happened
	 * @param domainClass
	 *            - the Class (normally sub-type of {@link Domain}). If null than
	 *            all listeners, independent from the domainClass, will be
	 *            notified.
	 */
	public void notify(UpdateType type, Class<?> domainClass) {
		if (domainClass != null) {
			if (updatables == null || updatables.get(domainClass) == null) {
				return;
			}
			for (Updatable u : updatables.get(domainClass)) {
					u.update(type);
			}
		} else {
			for (Updatable u : generalUpdatables) {
				u.update(type);
			}
		}
	}
}
