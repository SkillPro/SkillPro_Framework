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

import skillpro.model.assets.FactoryNode;

public class ProductRatio {
	private Product product;
	private FactoryNode factoryNode;
	private double ratio;
	
	public ProductRatio(Product product, FactoryNode factoryNode) {
		this.product = product;
		this.factoryNode = factoryNode;
	}
	
	public ProductRatio(Product product, FactoryNode factoryNode, double ratio) {
		this(product, factoryNode);
		this.ratio = ratio;
	}
	
	public double getRatio() {
		return ratio;
	}
	
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public FactoryNode getFactoryNode() {
		return factoryNode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(factoryNode, product, ratio);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductRatio other = (ProductRatio) obj;
		return Objects.equals(factoryNode, other.factoryNode)
				&& Objects.equals(product, other.product)
				&& ratio == other.ratio;
	}
}
