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

package skillpro.providers.product;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class ProductLabelProvider extends LabelProvider {
	private Image endProductIcon ;
	private Image productionSkillIcon;
	private Image intermediateProductIcon;
	private Image rawProductIcon;
	
	@Override
	public String getText(Object element) {
		if (element instanceof Product) {
			Product node = (Product) element;
			return node.getName();
		} else if (element instanceof ProductionSkill) {
			return ((ProductionSkill) element).getName()+": ["+((ProductionSkill) element).getTemplateSkill().getName()+"]";
		} else if (element instanceof ProductQuantity) {
			ProductQuantity pq = (ProductQuantity) element;
			Product product = pq.getProduct();
			String result = product.getName()+": "+pq.getQuantity();
			List<ProductionSkill> transportProductionSkills = SkillproService.getSkillproProvider().getProductRepo().getTransportProductionSkills(product);
			if (!transportProductionSkills.isEmpty()) {
				result += " [";
				for (int i = 0; i < transportProductionSkills.size(); i++) {
					if (i == transportProductionSkills.size() - 1) {
						result += transportProductionSkills.get(i).getName();
					} else {
						result += transportProductionSkills.get(i).getName() + ", ";
					}
				}
				result += "]";
			}
			
			
			return result;
		}
		
		return null;
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Product) {
			return getEndProductIcon();
		} else if (element instanceof ProductionSkill) {
			return getProductionSkillIcon();
		} else if (element instanceof ProductQuantity) {
			List<ProductionSkill> prodSkills = SkillproService.getSkillproProvider()
				.getProductRepo().getContributingProductionSkills(((ProductQuantity) element).getProduct());
			if (prodSkills == null || prodSkills.isEmpty()) {
				return getRawProductIcon();
			} else {
				return getIntermediateProductIcon();
			}
		}
		return null;
	}
	
	private Image getRawProductIcon() {
		if (rawProductIcon == null) {
			rawProductIcon = IconActivator.getImageDescriptor("icons/product/raw.png").createImage();
		}
		return rawProductIcon;
	}

	private Image getIntermediateProductIcon() {
		if (intermediateProductIcon == null) {
			intermediateProductIcon = IconActivator.getImageDescriptor("icons/product/interprod.png").createImage();
		}
		return intermediateProductIcon;
	}

	private Image getProductionSkillIcon() {
		if (productionSkillIcon == null) {
			productionSkillIcon = IconActivator.getImageDescriptor("icons/skill/ps.png").createImage();
		}
		return productionSkillIcon;
	}

	protected Image getEndProductIcon() {
		if (endProductIcon == null) {
			endProductIcon = IconActivator.getImageDescriptor("icons/product/product.png").createImage();
		}
		return endProductIcon;
	}
}
