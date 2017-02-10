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
import java.util.UUID;

import eu.skillpro.ams.service.utils.ScenarioType;

public class CustomerRequestTO {
	private String customerName;

	private boolean humanSEE;
	private String orderID;
	private String seeID;
	private ScenarioType scenarioType;
	private List<String> products;
	private int count = 1;

	private boolean newRequest;

	public CustomerRequestTO() {
		this.orderID = UUID.randomUUID().toString();
		this.count = 1;
		this.newRequest = true;
		this.products = new ArrayList<String>();
	}

	public CustomerRequestTO(String customerName, boolean humanSEE,
			String seeID, ScenarioType scenarioType, String... products) {
		this();
		this.customerName = customerName;
		this.humanSEE = humanSEE;
		this.scenarioType = scenarioType;
		this.seeID = seeID;
		this.products = Arrays.asList(products);
	}

	public String getCustomerName() {
		return customerName;
	}

	public boolean isNewRequest() {
		return newRequest;
	}

	public List<String> getProducts() {
		return products;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public void setNewRequest(boolean newRequest) {
		this.newRequest = newRequest;
	}

	public void setProducts(List<String> products) {
		this.products = products;
	}

	public boolean isHumanSEE() {
		return humanSEE;
	}

	public void setHumanSEE(boolean humanSEE) {
		this.humanSEE = humanSEE;
	}

	public String getOrderID() {
		return orderID;
	}

	public void setSeeID(String seeID) {
		this.seeID = seeID;
	}

	public String getSeeID() {
		return seeID;
	}

	public String getFormattedProducts() {
		String res = "";
		for (String s : products) {
			res += s + ", ";
		}
		return res.substring(0, res.length() - 2);
	}

	public ScenarioType getScenarioType() {
		return scenarioType;
	}

	public void setScenarioType(ScenarioType scenarioType) {
		this.scenarioType = scenarioType;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "CustomerRequestTO: [scenarioType=" + scenarioType
				+ "customerName=" + customerName + ", orderID=" + orderID
				+ ", products=" + Arrays.toString(products.toArray())
				+ ", humanSEE=" + humanSEE + ", seeID=" + seeID + "] ";
	}
}
