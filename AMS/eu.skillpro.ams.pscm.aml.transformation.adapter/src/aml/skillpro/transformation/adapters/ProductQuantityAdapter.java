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
import java.util.Set;
import java.util.UUID;

import skillpro.model.products.Product;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IProductQuantityTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;

public class ProductQuantityAdapter extends TransformableAdapterTemplate implements IProductQuantityTransformable {
	private ProductQuantity productQuantity;

	public ProductQuantityAdapter() {
		this(new ProductQuantity(null, 0));
	}
	
	public ProductQuantityAdapter(ProductQuantity productQuantity) {
		this.productQuantity = productQuantity;
	}
	
	@Override
	public String getTransformableName() {
		return "ProductQuantity";
	}

	@Override
	public String getTransformableID() {
		return UUID.randomUUID().toString();
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
		return productQuantity;
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
		
		//add product to quantity
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
			productQuantity.setProduct((Product) ((IProductTransformable) transClass.newInstance())
					.transform(child, context).getElement());
		}
		//add "quantity"
		productQuantity.setQuantity(Integer.parseInt(ie.getDesignatorByName("Quantity").getValue()));
		
		//add to production configurations's output
		ProductConfiguration productConfiguration = null;
		Hierarchy<InternalElement> parent = object.getParent();
		if (parent != null && context.contains(parent)) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(parent.getElement());
			ITransformable transformedOwner = ((IProductConfigurationTransformable) transClass.newInstance())
					.transform(parent, context);
			productConfiguration = (ProductConfiguration) transformedOwner.getElement();
		}
		
		if (productConfiguration == null) {
			throw new IllegalArgumentException("ProductiConfiguration cannot be found for this product quantity: " + getTransformableID());
		}
		productConfiguration.addProductQuantity(productQuantity);
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object reverseTransform() {
		//does not search for existing product quantities, because each PQs are different
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		//add product child
		if (getTransformableProduct() != null) {
			Hierarchy<InternalElement> productHie = (Hierarchy<InternalElement>) getTransformableProduct().reverseTransform();
			InternalElement actualProduct = new InternalElement(UUID.randomUUID().toString(), productHie.getName());
			actualProduct.setReferencedInternalElement(productHie.getElement());
			Hierarchy<InternalElement> actualProductHierarchy = new Hierarchy<InternalElement>(actualProduct.getName(), actualProduct);
			actualProductHierarchy.setParent(objectHie);

			//add attribute
			Attribute quantityAttribute = new Attribute("Quantity", AttributeType.INTEGER, "");
			AttributeDesignator quantityDes = new AttributeDesignator(quantityAttribute, internalElement, new ArrayList<Constraint>(), 
					productQuantity.getQuantity() + "");
			internalElement.addDesignator(quantityDes);
			quantityAttribute.addDesignator(quantityDes);
		}
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}

	@Override
	public IProductTransformable getTransformableProduct() {
		if (productQuantity.getProduct() == null) {
			throw new IllegalArgumentException("Product is null for this ProductQuantity: " + getTransformableName());
		}
		return new ProductAdapter(productQuantity.getProduct()); 
	}
}
