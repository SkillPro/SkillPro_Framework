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

package skillpro.model.products;

import java.util.List;

public class CustomerRequest {
	private String customerName;
	private String orderID;
	private List<String> products;
	
	private boolean humanSEE;
	private String seeID;
	
	public CustomerRequest(String customerName, String orderID, List<String> products, boolean humanSEE, String seeID) {
		this.customerName = customerName;
		this.products = products;
		this.orderID = orderID;
		this.humanSEE = humanSEE;
		this.seeID = seeID;
	}

	public String getCustomerName() {
		return customerName;
	}
	
	public String getOrderID() {
		return orderID;
	}
	
	public List<String> getProducts() {
		return products;
	}
	
	public boolean isHumanSEE() {
		return humanSEE;
	}
	
	public String getSeeID() {
		return seeID;
	}
}
