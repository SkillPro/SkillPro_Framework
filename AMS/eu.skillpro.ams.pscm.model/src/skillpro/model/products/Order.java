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

import java.util.Calendar;
import java.util.Objects;

public class Order {
	private String orderID;
	private String orderName;
	private ProductQuantity productQuantity;
	private Calendar earliestStartDate;
	private Calendar deadline;
	
	public Order(String orderID, String orderName, ProductQuantity productQuantity, Calendar earliestStartDate, Calendar deadline) {
		this.orderID = orderID;
		this.orderName = orderName;
		this.productQuantity = productQuantity;
		this.earliestStartDate = earliestStartDate;
		this.deadline = deadline;
	}
	
	public Calendar getDeadline() {
		return deadline;
	}
	
	public Calendar getEarliestStartDate() {
		return earliestStartDate;
	}
	
	public ProductQuantity getProductQuantity() {
		return productQuantity;
	}
	
	public String getOrderID() {
		return orderID;
	}
	
	public String getOrderName() {
		return orderName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(orderID, orderName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		return Objects.equals(orderID, other.orderID)
				&& Objects.equals(orderName, other.orderName);
	}
}
