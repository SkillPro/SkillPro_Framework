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

import java.util.Objects;
import java.util.UUID;

import skillpro.model.assets.Factory;

public class Product {
	private String name;
	private Factory factory;
	private Supply supply;
	private boolean isDisposable = false;
	private boolean isPurchasable = false;
	private String productTypeID;
	
	//should not be used explicitly.
	public Product() {
		super();
		productTypeID = UUID.randomUUID().toString();
	}
	
	/**
	 * creates a Product instance. Adds the product to the product lists of the given {@link Factory}.
	 * 
	 * @param name
	 * @param supply
	 * @param factory
	 */
	public Product(String name, Supply supply, Factory factory) {
		this();
		this.name = name;
		this.supply = supply;
		setFactory(factory);
	}
	
	public Product(String name, Supply supply, Factory factory, boolean isDisposable, boolean isPurchasable) {
		this(name, supply, factory);
		this.isDisposable = isDisposable;
		this.isPurchasable = isPurchasable;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isDisposable() {
		return isDisposable;
	}
	
	public boolean isPurchasable() {
		return isPurchasable;
	}
	
	public void setDisposable(boolean isDisposable) {
		this.isDisposable = isDisposable;
	}
	
	public void setPurchasable(boolean isPurchasable) {
		this.isPurchasable = isPurchasable;
	}
	
	public Supply getSupply() {
		return supply;
	}
	
	public Factory getFactory() {
		return factory;
	}
	
	public void setFactory(Factory factory) {
		this.factory = factory;
		factory.addProduct(this);
	}

	public String getProductTypeID() {
		return productTypeID;
	}
	
	public void setProductTypeID(String productTypeID) {
		this.productTypeID = productTypeID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(productTypeID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		return Objects.equals(productTypeID, other.productTypeID);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
