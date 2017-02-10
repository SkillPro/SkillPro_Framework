/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 * 
 * Module: AMS (Asset Management System)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the AMS (Asset Management System), which has been developed
 * at the PDE department of the FZI, Karlsruhe. It is part of the SkillPro Framework,
 * which is is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733). *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  * 
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework. If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

package skillpro.model.products;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ProductConfiguration {
	private String id;
	private Set<ProductQuantity> productQuantities = new HashSet<>();
	
	public ProductConfiguration() {
		id = UUID.randomUUID().toString();
	}
	
	public ProductConfiguration(String id, Set<ProductQuantity> productQuantities) {
		this.id = id;
		this.productQuantities.addAll(productQuantities);
	}

	public String getId() {
		return id;
	}
	
	public Set<ProductQuantity> getProductQuantities() {
		return productQuantities;
	}
	
	public boolean addProductQuantity(ProductQuantity productQuantity) {
		if (productQuantity != null && !productQuantities.contains(productQuantity)) {
			return productQuantities.add(productQuantity);
		}
		return false;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return Arrays.toString(productQuantities.toArray());
	}

	@Override
	public int hashCode() {
		return Objects.hash(productQuantities);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass())  {
			return false;
		}
		ProductConfiguration other = (ProductConfiguration) obj;
		return Objects.equals(productQuantities, other.productQuantities);
	}
}
