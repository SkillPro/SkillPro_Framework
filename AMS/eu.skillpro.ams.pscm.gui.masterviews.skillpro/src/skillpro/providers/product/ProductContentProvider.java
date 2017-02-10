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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.repo.resource.ProductRepo;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;

public class ProductContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null) {
			return new Object[] {};
		} else if (inputElement instanceof ProductRepo) {
			return ((ProductRepo) inputElement).getRootProducts().toArray();
		} else if (inputElement instanceof Collection<?>)
			return ((Collection<?>) inputElement).toArray();
		else
			return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		//FIXME ProductionQuantity instead of Product??
		if (parentElement instanceof Product) {
			return SkillproService.getSkillproProvider()
					.getProductRepo().getContributingProductionSkills((Product) parentElement).toArray();
		} else if (parentElement instanceof ProductionSkill) {
			Set<ProductQuantity> quantities = ((ProductionSkill) parentElement).getInputConfiguration().getProductQuantities();
			return quantities.toArray();
		} else if (parentElement instanceof ProductQuantity) {
			return getChildren(((ProductQuantity) parentElement).getProduct());
		} 
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Product) {
			List<ProductionSkill> skills = SkillproService.getSkillproProvider()
					.getProductRepo().getContributingProductionSkills((Product) element);
			return skills != null && !skills.isEmpty();
		} else if (element instanceof ProductionSkill) {
			Set<ProductQuantity> quantities = ((ProductionSkill) element).getInputConfiguration().getProductQuantities();
			return quantities != null && !quantities.isEmpty();
		} else if (element instanceof ProductQuantity) {
			return hasChildren(((ProductQuantity) element).getProduct());
		}
		return false;
	}

}
