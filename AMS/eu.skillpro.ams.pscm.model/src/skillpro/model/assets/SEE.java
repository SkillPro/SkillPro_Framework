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

package skillpro.model.assets;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import skillpro.model.products.ProductQuantity;
import skillpro.model.utils.Pair;

/**
 * SEE stands for Skill Execution Engine
 * SEE is responsible for the execution of the resources it's assigned to.
 */
public class SEE {
	/**
	 * the configuration id of this SEE used to identify it before connecting to
	 * the OPC-UA server
	 */
	private String seeID;

	/**
	 * this is the nodeID of the OPC-UA server for the SEE
	 */
	private String mesNodeID;
	
	
	/**
	 * NodeID of the environment server
	 */
	private String esNodeID;

	private Resource resource;
	
	private SEEState seeState;
	
	/**
	 * AML description to be provided to the MES for this SEE
	 */
	private String amlDescription;
	
	/**
	 * the corresponding AML object of this SEE
	 */
	private Object amlObject;
	
	/**
	 * The address of the OPC-UA, for the runtime id. It should be changed to a map (opc-ua-address, runtimeID)
	 */
	private String opcUAAddress;
	
	/**
	 * True if only in simulation (virtual) mode, else false 
	 */
	private boolean simulation = false;
	
	private Pair<AMSCommType, String> amsCommunication;
	
	private Pair<MESCommType, String> mesCommunication;
	private Pair<ESCommType, String> esCommunication;
	//the id of the default resource configuration
	private String defaultResourceConfigurationID;
	//the id of the default production configuration
	private Set<ProductQuantity> defaultInputProductQuantities = new HashSet<>();
	
	private String customerName;
	private String seeType;
	

	/**
	 * default constructor. Use of this constructor is discouraged as the
	 * configurationID should be provided as a parameter by the "real" SEE
	 * after registering to the AMS.
	 */
	public SEE() {
		this.seeID = (UUID.randomUUID().toString());
	}
	
	/**
	 * Clone constructor.
	 * @param see the see that will be cloned.
	 */
	public SEE(SEE see) {
		if (see != null) {
			this.seeID = see.seeID;
			this.resource = see.resource;
			this.seeState = see.seeState;
			this.defaultResourceConfigurationID = see.defaultResourceConfigurationID;
			this.defaultInputProductQuantities.addAll(see.defaultInputProductQuantities);
			if (see.amsCommunication != null) {
				this.amsCommunication = new Pair<>(see.amsCommunication);
			}
			if (see.mesCommunication != null) {
				this.mesCommunication = new Pair<>(see.mesCommunication);
			}
			if (see.esCommunication != null) {
				this.esCommunication = new Pair<>(see.esCommunication);
			}
			this.seeType = see.seeType;
		}
	}

	/**
	 * 
	 * @param seeID the configuration id of this SEE.
	 */
	public SEE(String seeID) {
		this();
		this.seeID = (seeID);
	}
	
	public SEE(Resource resource, String defaultResourceConfigurationID, Set<ProductQuantity> defaultInputProductQuantities, 
			Pair<AMSCommType, String> amsCommunication, Pair<MESCommType, String> mesCommunication,
			Pair<ESCommType, String> esCommunication, String seeType) {
		this();
		addNotRegisteredResource(resource);
		this.defaultResourceConfigurationID = defaultResourceConfigurationID;
		this.defaultInputProductQuantities.addAll(defaultInputProductQuantities);
		this.amsCommunication = new Pair<>(amsCommunication);
		this.mesCommunication = new Pair<>(mesCommunication);
		this.esCommunication = new Pair<>(esCommunication);
		this.seeType = seeType;
	}
	
	public Pair<ESCommType, String> getESCommunication() {
		return esCommunication;
	}
	
	public String getSEEType() {
		return seeType;
	}
	
	public void setESCommunication(Pair<ESCommType, String> esCommunication) {
		this.esCommunication = esCommunication;
	}
	
	public void setSEEType(String seeType) {
		this.seeType = seeType;
	}
	
	public String getCustomerName() {
		return customerName;
	}
	
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	/**
	 * @return the seeID of this SEE as specified by the "real" SEE.
	 */
	public String getSeeID() {
		return seeID;
	}
	
	public void setSeeID(String seeID) {
		this.seeID = seeID;
	}

	public String getMESNodeID() {
		return mesNodeID;
	}

	public void setMESNodeID(String mesNodeID) {
		this.mesNodeID = mesNodeID;
	}
	
	public void setESNodeID(String esNodeID) {
		this.esNodeID = esNodeID;
	}
	
	public String getESNodeID() {
		return esNodeID;
	}
	
	public Set<ProductQuantity> getDefaultInputProductQuantities() {
		return defaultInputProductQuantities;
	}
	
	public String getDefaultResourceConfigurationID() {
		return defaultResourceConfigurationID;
	}
	
	public void setDefaultResourceConfigurationID(String defaultResourceConfigurationID) {
		this.defaultResourceConfigurationID = defaultResourceConfigurationID;
	}

	public void setDefaultInputProductQuantities(Set<ProductQuantity> defaultInputProductQuantities) {
		this.defaultInputProductQuantities = defaultInputProductQuantities;
	}
	
	public String getAmlDescription() {
		return amlDescription;
	}

	public void setAmlDescription(String amlDescription) {
		this.amlDescription = amlDescription;
	}

	public String getOpcUAAddress() {
		return opcUAAddress;
	}

	public void setOpcUAAddress(String opcUAAddress) {
		this.opcUAAddress = opcUAAddress;
	}

	public boolean isSimulation() {
		return simulation;
	}

	public void setSimulation(boolean simulation) {
		this.simulation = simulation;
	}
	
	public Pair<AMSCommType, String> getAMSCommunication() {
		return amsCommunication;
	}
	
	public Pair<MESCommType, String> getMESCommunication() {
		return mesCommunication;
	}
	
	public void setAMSCommunication(Pair<AMSCommType, String> amsCommunication) {
		this.amsCommunication = amsCommunication;
	}
	
	public void setMESCommunication(Pair<MESCommType, String> mesCommunication) {
		this.mesCommunication = mesCommunication;
	}

	public Object getAmlObject() {
		return amlObject;
	}

	public void setAmlObject(Object amlObject) {
		this.amlObject = amlObject;
	}

	public void removeResource() {
		if (resource != null) {
			resource.setResponsibleSEE(null);
			resource = null;
		}
	}
	
	public void addNotRegisteredResource(Resource resource) {
		addResource(resource, SEEState.NOT_REGISTERED);
	}
	
	public void addRegisteredResource(Resource resource) {
		addResource(resource, SEEState.REGISTERED);
	}
	
	private void addResource(Resource resource, SEEState seeState) {
		if (this.resource != null) {
			this.resource.setResponsibleSEE(null);
		}
		this.resource = resource;
		this.seeState = seeState;
		this.resource.setResponsibleSEE(this);
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public SEEState getSEEState() {
		return seeState;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public void setSEEState(SEEState seeState) {
		this.seeState = seeState;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(amsCommunication, defaultInputProductQuantities, defaultResourceConfigurationID, mesCommunication, mesNodeID, seeID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SEE other = (SEE) obj;
		return Objects.equals(amsCommunication, other.amsCommunication)
				&& Objects.equals(defaultInputProductQuantities, other.defaultInputProductQuantities)
				&& Objects.equals(defaultResourceConfigurationID, other.defaultResourceConfigurationID)
				&& Objects.equals(mesCommunication, other.mesCommunication)
				&& Objects.equals(mesNodeID, other.mesNodeID)
				&& Objects.equals(seeID, other.seeID);
	}
}
