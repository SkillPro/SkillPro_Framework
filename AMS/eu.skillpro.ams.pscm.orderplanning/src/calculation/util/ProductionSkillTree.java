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

package calculation.util;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.skills.ProductionSkill;

public class ProductionSkillTree {
	
	private List<ProductionSkillNode> possibleRoots;
	
	public ProductionSkillTree(Product outputProduct) {
		possibleRoots = createTreeStructure(outputProduct, null);
	}
	
	public ProductionSkillTree(List<ProductionSkillNode> possibleRoots) {
		this.possibleRoots = possibleRoots;
	}
	
	private List<ProductionSkillNode> createTreeStructure(Product outputProduct, ProductionSkillNode parent) {
		List<ProductionSkillNode> possibleNodes = new ArrayList<>();
		for (ProductionSkill pSkill : Utility.getProductionSkillsRelatedToOutput(outputProduct)) {
			possibleNodes.add(new ProductionSkillNode(pSkill, parent));
		}
		
		for (ProductionSkillNode node : possibleNodes) {
			for (ProductQuantity inputPQ : node.getProductionSkill().getInputConfiguration().getProductQuantities()) {
				List<ProductionSkillNode> childStructure = createTreeStructure(inputPQ.getProduct(), node);
				if (!childStructure.isEmpty()) {
					node.addChild(new ProductionSkillTree(childStructure));
				}
			}
		}
		
		return possibleNodes;
	}
	
	/**
	 * @return all possible production paths with the order of Material -> End Product
	 */
	public List<ProductionPath> getPossibleProductionPaths() {
		List<ProductionPath> realProductionPaths = new ArrayList<>();
		for (ProductionSkillNode root : possibleRoots) {
			List<ProductionPath> productionPaths = new ArrayList<>();
			for (ProductionSkillTree child : root.getChildren()) {
				List<ProductionPath> possibleProductionPaths = child.getPossibleProductionPaths();
				productionPaths.addAll(possibleProductionPaths);
			}
			
			for (ProductionPath path : productionPaths) {
				path.getProductionNodes().add(root);
			}
			
			if (productionPaths.isEmpty()) {
				List<ProductionSkillNode> nodes = new ArrayList<>();
				nodes.add(root);
				productionPaths.add(new ProductionPath(nodes));
			}
			realProductionPaths.addAll(productionPaths);
		}
		
		return realProductionPaths;
	}
	
	public List<ProductionSkillNode> getPossibleRoots() {
		return possibleRoots;
	}
}
