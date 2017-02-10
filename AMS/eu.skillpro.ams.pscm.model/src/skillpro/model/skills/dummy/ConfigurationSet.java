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

package skillpro.model.skills.dummy;

import java.util.HashSet;
import java.util.Iterator;

public class ConfigurationSet extends HashSet<ConditionConfiguration>{
	
	private static final long serialVersionUID = 4157528826876934431L;

	public static ConfigurationSet of(ConditionConfiguration... elements) {
		ConfigurationSet result = new ConfigurationSet();
		for (ConditionConfiguration e : elements) {
			result.add(e);
		}
		return result;
	}
	
	@Override
	public String toString() {
		if (isEmpty()) {
			return "{}";
		} else if (size() == 1) {
			return iterator().next().getName();
		} else {
			StringBuilder sb = new StringBuilder();
			Iterator<ConditionConfiguration> iterator = iterator();
			sb.append("{" + iterator.next().getName());
			while (iterator.hasNext()) {
				sb.append(",");
				sb.append(iterator.next().getName());
			}
			sb.append("}");
			return sb.toString();
		}
	}
}
