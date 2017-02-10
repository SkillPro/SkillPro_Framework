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

package skillpro.model.repo.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import skillpro.model.products.Product;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.repo.Repo;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;

public class ProductRepo extends Repo<Product> {
	
	public List<Product> getRootProducts() {
		List<Product> entities = new ArrayList<>();
		entities.addAll(getEntities());
		for (ProductionSkill skill : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			if (skill.getInputConfiguration() != null) {
				for (ProductQuantity quantity : skill.getInputConfiguration().getProductQuantities()) {
					Product productFromQuantity = quantity.getProduct();
					entities.remove(productFromQuantity);
				}
			}
		}
		return entities;
	}
	
	public Product findProduct(String name) {
		for (Product product : list) {
			if (product.getName().equals(name)) {
				return product;
			}
		}
		return null;
	}
	
	public List<ProductionSkill> getContributingProductionSkills(Product product) {
		List<ProductionSkill> contributing = new ArrayList<>();
		for (ProductionSkill prodSkill : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			ProductConfiguration input = prodSkill.getInputConfiguration();
			ProductConfiguration output = prodSkill.getOutputConfiguration();
			if (output != null) {
				boolean createsProduct = false;
				for (ProductQuantity quantity : output.getProductQuantities()) {
					createsProduct |= quantity.getProduct().equals(product);
				}
				if (createsProduct && !input.getProductQuantities().equals(output.getProductQuantities())) {
					contributing.add(prodSkill);
				}
			}
		}
				
		return contributing;
	}
	
	public List<Product> getInBetweenProducts(Product product) {
		List<Product> inBetweens = new ArrayList<>();
		addInBetweenProductsToList(product, inBetweens);
		return inBetweens;
	}
	
	private void addInBetweenProductsToList(Product product, List<Product> inBetweens) {
		List<ProductionSkill> contributingProductionSkills = getContributingProductionSkills(product);
		for (ProductionSkill productionSkill : contributingProductionSkills) {
			Set<ProductQuantity> productQuantities = productionSkill.getInputConfiguration().getProductQuantities();
			for (ProductQuantity productQuantity : productQuantities) {
				Product p = productQuantity.getProduct();
				if (!inBetweens.contains(p)) {
					inBetweens.add(p);
				}
				addInBetweenProductsToList(p, inBetweens);
			}
		}
	}
	
	public List<ProductionSkill> getTransportProductionSkills(Product product) {
		List<ProductionSkill> skills = new ArrayList<>();
		for (ProductionSkill productionSkill : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			ProductConfiguration inputConfiguration = productionSkill.getInputConfiguration();
			// Check if the inputs and outputs quantities are equals and if the first and only product
			// equals the given product, then add the skill
			if (inputConfiguration.getProductQuantities().equals(productionSkill.getOutputConfiguration().getProductQuantities())
					&& inputConfiguration.getProductQuantities().size() == 1
					&& inputConfiguration.getProductQuantities().iterator().next().getProduct().equals(product)) {
				skills.add(productionSkill);
			}
		}
		
		return skills;
	}
}
