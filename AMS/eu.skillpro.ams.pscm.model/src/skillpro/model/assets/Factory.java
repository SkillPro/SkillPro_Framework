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

import java.util.ArrayList;
import java.util.List;

import skillpro.model.products.Order;
import skillpro.model.products.Product;
import skillpro.model.utils.ShiftSchedule;


public class Factory extends FactoryNode {
	private List<Product> products = new ArrayList<>();
	private ShiftSchedule shiftPlan;
	private Order order;
	
	public Factory() {
		super("Factory",true);
	}
	
	public Factory(String name) {
		super(name, true);
	}
	
	public Factory(Factory factory) {
		super(factory.getName(), true);
		products.addAll(factory.getProducts());
		this.shiftPlan = factory.getShiftPlan();
		this.order = factory.getOrder();
	}
	
	public Factory(String name, List<Product> products, ShiftSchedule shiftPlan, Order order) {
		super(name, true);
		this.products = products;
		this.shiftPlan = shiftPlan;
		this.order = order;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public ShiftSchedule getShiftPlan() {
		return shiftPlan;
	}
	
	public List<Product> getProducts() {
		return products;
	}
	
	public boolean addProduct(Product prod) {
		if (prod != null && !products.contains(prod)) {
			products.add(prod);
			prod.setFactory(this);
			return true;
		}
		return false;
	}
}
