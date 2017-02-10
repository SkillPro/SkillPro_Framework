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

package aml.skillpro.transformation.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.skills.ProductionSkill;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IPostProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IProductQuantityTransformable;
import aml.skillpro.transformation.interfaces.IProductionSkillTransformable;

public class PostProductConfigurationAdapter extends TransformableAdapterTemplate implements IPostProductConfigurationTransformable {
	private ProductConfiguration productConfiguration;

	public PostProductConfigurationAdapter() {
		this(new ProductConfiguration());
	}
	
	public PostProductConfigurationAdapter(ProductConfiguration productConfiguration) {
		this.productConfiguration = productConfiguration;
	}
	
	@Override
	public String getTransformableName() {
		return "OutputProductConfiguration";
	}

	@Override
	public String getTransformableID() {
		return productConfiguration.getId();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		return null;
	}

	@Override
	public ITransformable getTransformableParent() {
		return null;
	}

	@Override
	public Object getElement() {
		return productConfiguration;
	}

	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context) throws InstantiationException,
			IllegalAccessException {
		//ritual start
		if (!context.contains(object)) {
			return null;
		}
		//returns a transformed element if it has already been transformed before.
		InternalElement ie = object.getElement();
		if (transformedObjectsMap.containsKey(ie)) {
			return (ITransformable) transformedObjectsMap.get(ie);
		}
		//ritual end
		
		productConfiguration.setId(ie.getId());
		
		//add to production skill's output
		ProductionSkill productionSkill = null;
		Hierarchy<InternalElement> parent = object.getParent();
		if (parent != null && context.contains(parent)) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(parent.getElement());
			ITransformable transformedOwner = ((IProductionSkillTransformable) transClass.newInstance())
					.transform(parent, context);
			productionSkill = (ProductionSkill) transformedOwner.getElement();
		}
		
		if (productionSkill == null) {
			throw new IllegalArgumentException("ProductionSkill cannot be found for this product configuration: " + getTransformableID());
		}
		productionSkill.setOutputConfiguration(productConfiguration);
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object reverseTransform() {
		//does not search for existing product configurations, because each product configurations
		//are different
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		//add product quantities as children
		if (getTransformableProductQuantities().isEmpty()) {
			throw new IllegalArgumentException("ProductConfiguration is empty: " + getTransformableID());
		}
		for (IProductQuantityTransformable trans : getTransformableProductQuantities()) {
			Hierarchy<InternalElement> productHie = (Hierarchy<InternalElement>) trans.reverseTransform();
			productHie.setParent(objectHie);
		}
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}

	@Override
	public List<IProductQuantityTransformable> getTransformableProductQuantities() {
		List<IProductQuantityTransformable> transformables = new ArrayList<>();
		for (ProductQuantity pq : productConfiguration.getProductQuantities()) {
			transformables.add(new ProductQuantityAdapter(pq));
		}
		return transformables;
	}

	@Override
	public int hashCode() {
		return Objects.hash(productConfiguration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PostProductConfigurationAdapter other = (PostProductConfigurationAdapter) obj;
		return Objects.equals(productConfiguration, other.productConfiguration);
	}
}
