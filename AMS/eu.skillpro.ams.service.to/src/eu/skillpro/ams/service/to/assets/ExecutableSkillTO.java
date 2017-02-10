/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: AMS Server
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

package eu.skillpro.ams.service.to.assets;

public class ExecutableSkillTO {
	private String resourceExecutableSkillID;
	private String seeID;
	private String amlDescription;

	public ExecutableSkillTO(String resourceExecutableSkillID, String seeID,
			String amlDescription) {
		this.resourceExecutableSkillID = resourceExecutableSkillID;
		this.seeID = seeID;
		this.amlDescription = amlDescription;
	}

	public String getSeeID() {
		return seeID;
	}

	public void setSeeID(String seeID) {
		this.seeID = seeID;
	}

	public String getResourceExecutableSkillID() {
		return resourceExecutableSkillID;
	}

	public void setResourceExecutableSkillID(String resourceExecutableSkillID) {
		this.resourceExecutableSkillID = resourceExecutableSkillID;
	}

	public String getAmlDescription() {
		return amlDescription;
	}

	public void setAmlDescription(String amlDescription) {
		this.amlDescription = amlDescription;
	}

	@Override
	public String toString() {
		return "ExecutableSkillTO: [id=" + resourceExecutableSkillID
				+ ", seeID=" + seeID + ", amlSnippet=" + amlDescription + "]";
	}
}
