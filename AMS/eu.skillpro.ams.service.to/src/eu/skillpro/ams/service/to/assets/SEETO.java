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

package eu.skillpro.ams.service.to.assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author caliqi
 * @date 10.03.2014
 * 
 */
public class SEETO {
	// configurationId
	private String seeID;
	private List<String> assetTypeNames = new ArrayList<String>();
	// nodeId and nameSpace are runtimeId
	private String identifier;
	private String nameSpace;
	private String opcuaAddress;
	private String amlDescription;
	private boolean newAsset;
	private boolean simulation;

	public SEETO() {
	}

	public SEETO(String configurationId, List<String> assetTypeNames,
			String identifier, String nameSpace, String opcuaAddress,
			String amlDescription, boolean newAsset, boolean simulation) {
		super();
		this.seeID = configurationId;
		this.assetTypeNames.addAll(assetTypeNames);
		this.identifier = identifier;
		this.nameSpace = nameSpace;
		this.opcuaAddress = opcuaAddress;
		this.amlDescription = amlDescription;
		this.newAsset = newAsset;
		this.simulation = simulation;
	}

	/**
	 * @return the guid
	 */
	public String getSeeID() {
		return seeID;
	}

	/**
	 * @param configurationId
	 *            the guid to set
	 */
	public void setSeeID(String seeID) {
		this.seeID = seeID;
	}

	public List<String> getAssetTypeNames() {
		return assetTypeNames;
	}

	/**
	 * @param assetTypeNames
	 *            the list of names of the asset types that are gonna be set
	 */
	public void setAssetTypeNames(List<String> assetTypeNames) {
		this.assetTypeNames = assetTypeNames;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the nameSpace
	 */
	public String getNameSpace() {
		return nameSpace;
	}

	/**
	 * @param nameSpace
	 *            the nameSpace to set
	 */
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	/**
	 * @return the newAsset
	 */
	public boolean isNewAsset() {
		return newAsset;
	}

	/**
	 * @param newAsset
	 *            the newAsset to set
	 */
	public void setNewAsset(boolean newAsset) {
		this.newAsset = newAsset;
	}

	public String getOpcuaAddress() {
		return opcuaAddress;
	}

	public void setOpcuaAddress(String opcuaAddress) {
		this.opcuaAddress = opcuaAddress;
	}

	public boolean isSimulation() {
		return simulation;
	}

	public void setSimulation(boolean simulation) {
		this.simulation = simulation;
	}

	public String getAmlDescription() {
		return amlDescription;
	}

	public void setAmlDescription(String amlDescription) {
		this.amlDescription = amlDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SEETO [seeID=" + seeID + ", assetTypeName="
				+ Arrays.toString(assetTypeNames.toArray()) + ", identifier="
				+ identifier + ", nameSpace=" + nameSpace + ", opcuaAddress="
				+ opcuaAddress + ", newAsset=" + newAsset + ", simulation="
				+ simulation + ", amlFile=" + amlDescription + "]";
	}
}
