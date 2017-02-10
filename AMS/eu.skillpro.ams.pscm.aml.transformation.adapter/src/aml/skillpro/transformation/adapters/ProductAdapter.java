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

import skillpro.model.assets.Factory;
import skillpro.model.products.Product;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IFactoryTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;

public class ProductAdapter extends TransformableAdapterTemplate implements IProductTransformable {
	private Product product;
	
	public ProductAdapter() {
		this(new Product());
	}
	
	public ProductAdapter(Product product) {
		this.product = product;
	}
	
	@Override
	public Product getElement() {
		return product;
	}
	
	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context) throws InstantiationException, IllegalAccessException {
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
		
		product.setName(ie.getName());
		product.setProductTypeID(ie.getId());
		
		//factory
		Factory factory = null;
		Hierarchy<InternalElement> topMost = findTopMostElement(object);
		if (topMost != null && context.contains(topMost)) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(topMost.getElement());
			ITransformable transformedOwner = ((IFactoryNodeTransformable) transClass.newInstance())
					.transform(topMost, context);
			factory = (Factory) transformedOwner.getElement();
		}
		if (factory == null) {
			throw new IllegalArgumentException("Factory can't be null for this product: " + ie.getName() + "!");
		}
		product.setFactory(factory);
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object reverseTransform() {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		Hierarchy<InternalElement> parentHie = null;
		Hierarchy<InternalElement> factoryHie = (Hierarchy<InternalElement>) getTransformableFactory().reverseTransform();
		for (Hierarchy<InternalElement> child : factoryHie.getChildren()) {
			if (child.getName().equals("Products")) {
				parentHie = child;
				break;
			}
		}
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		objectHie.setParent(parentHie);
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}
	
	private Hierarchy<InternalElement> findTopMostElement(Hierarchy<InternalElement> hie) {
		if (hie.getParent() != null) {
			return findTopMostElement(hie.getParent());
		}
		return hie;
	}
	
	@Override
	public IFactoryTransformable getTransformableFactory() {
		if (product.getFactory() == null) {
			throw new IllegalArgumentException("Factory can't be null for this product: " + product.getName());
		}
		return new FactoryAdapter(product.getFactory());
	}
	
	@Override
	public String getTransformableName() {
		return product.getName();
	}

	@Override
	public String getTransformableID() {
		return product.getProductTypeID();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		List<ITransformable> transformables = new ArrayList<>();
		//no children
		return transformables;
	}

	@Override
	public ITransformable getTransformableParent() {
		//no parent
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(product);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductAdapter other = (ProductAdapter) obj;
		return Objects.equals(product, other.product);
	}
}
