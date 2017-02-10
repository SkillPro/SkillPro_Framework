/**
 * 
 */
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

package eu.skillpro.ams.service.to.utility;

import java.util.List;

import eu.skillpro.ams.service.to.assets.AssetTO;

/**
 * @author caliqi
 * @date 17.09.2014
 *
 */
public class PSCConfiguration {
	public enum PSCConfigurationState {
		DIRTY,
		APPROVED,
		NOT_APPROVED;
	}
	
	private String id;
	private List<AssetTO> pscConfiguration;
	private PSCConfigurationState pSCConfigurationState;
	private boolean geoDataHasBeenRead = false;
	
	/**
	 * 
	 */
	public PSCConfiguration() {
	}
	
	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return pscConfiguration as list of Assets
	 */
	public List<AssetTO> getPSCConfiguration() {
		return pscConfiguration;
	}
	
	/**
	 * @param pscConfiguration
	 */
	public void setPSCConfiguration(List<AssetTO> pscConfiguration) {
		this.pscConfiguration = pscConfiguration;
	}
	
	/**
	 * @return the pscConfigurationState
	 */
	public PSCConfigurationState getConfigurationState() {
		return pSCConfigurationState;
	}
	
	/**
	 * @param pSCConfigurationState the pscConfigurationState to set
	 */
	public void setPSCConfigurationState(PSCConfigurationState newPSCConfigurationState) {
		PSCConfigurationState oldCS = this.pSCConfigurationState;
		this.pSCConfigurationState = newPSCConfigurationState;
		if(oldCS == PSCConfigurationState.DIRTY && this.pSCConfigurationState == PSCConfigurationState.APPROVED) {
			this.geoDataHasBeenRead = false;
		}
	}
	
	/**
	 * @return the geoDataHasBeenRead
	 */
	public boolean isGeoDataHasBeenRead() {
		return geoDataHasBeenRead;
	}
	
	/**
	 * @param geoDataHasBeenRead the geoDataHasBeenRead to set
	 */
	public void setGeoDataHasBeenRead(boolean geoDataHasBeenRead) {
		this.geoDataHasBeenRead = geoDataHasBeenRead;
	}
}
