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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import skillpro.model.assets.Factory;
import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.TemplateSkill;
import transformation.interfaces.ITransformable;
import aml.domain.Domain;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.model.InterfaceDesignator;
import aml.model.InternalLink;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryTransformable;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;
import aml.skillpro.transformation.interfaces.IProductionSkillTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;

public class ProductionSkillAdapter extends TransformableAdapterTemplate implements IProductionSkillTransformable {
	private ProductionSkill productionSkill;
	private static Set<InternalLink> internalLinks;
	private static Map<InternalElement, Integer> inputCountMap = new HashMap<>();
	private static Map<InternalElement, Integer> outputCountMap = new HashMap<>();
	
	
	public ProductionSkillAdapter() {
		this(new ProductionSkill());
	}
	
	public ProductionSkillAdapter(ProductionSkill productionSkill) {
		this.productionSkill = productionSkill;
	}
	
	@Override
	public List<Object> getAMLElements() {
		return productionSkill.getAmlElements();
	}
	
	@Override
	public ProductionSkill getElement() {
		return productionSkill;
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
		
		//attributes and stuff
		this.productionSkill.setName(ie.getName());
		this.productionSkill.setId(ie.getId());
		
		if (ie.getSupportedRoles().size() != 1) {
			throw new IllegalArgumentException("This ProductionSkill: " + productionSkill + " has to have exactly 1 SupportedRole");
		}
		for (Role supportedRole : ie.getSupportedRoles()) {
			//it's clear that TemplateSkillAdapter is the only one used to transform
			//TemplateSkills, that's why it's not needed to do the same method as FactoryNode's transformation
			ITransformable transformedTempSkill = new TemplateSkillAdapter().transform
					(supportedRole, context);
			if (transformedTempSkill == null) {
				throw new IllegalArgumentException("TemplateSkill has not been transformed for this Role: " 
						+ supportedRole.getName());
			}
			productionSkill.setTemplateSkill((TemplateSkill) transformedTempSkill.getElement());
		}
		
		//property designators
		List<AttributeDesignator> normalDesignators = ie.getDesignators();
		for (AttributeDesignator des : normalDesignators) {
			String desName = des.getAttribute().getName();
			PropertyDesignator propDes = new PropertyDesignator(findProperty(desName), this.productionSkill, des.getValue());
			if (propDes.getProperty() != null) {
				this.productionSkill.addPropertyDesignator(propDes);
			}
		}
		
		//inputs and outputs InternalLink version
		//try ProductConfiguration version if it's available for use
		if (object.getChildren().size() == 0) {
			Map<IProductTransformable, InterfaceDesignator> inputProducts = new HashMap<>();
			Map<IProductTransformable, InterfaceDesignator> outputProducts = new HashMap<>();
			for (InternalLink link : getInternalLinks(context)) {
				InterfaceDesignator refA = link.getRefA();
				String refAName = refA.getName().toLowerCase();
				InterfaceDesignator refB = link.getRefB();
				String refBName = refB.getName().toLowerCase();
				Domain refADomain = refA.getDomain();
				Domain refBDomain = refB.getDomain();
				if (domainExistsInContext(refADomain, context) && domainExistsInContext(refBDomain, context)) {
					if (refAName.contains("production") && (refBName.contains("input") || refB.getBaseInterface().getName().toLowerCase().contains("input"))) {
						Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(refADomain);
						ITransformable transformedProduct = ((IProductTransformable) transClass.newInstance())
								.transform(getHierarchyFromDomain(refADomain, context), context);
						if (refBDomain.equals(object.getElement())) {
							inputProducts.put((IProductTransformable) transformedProduct, refA);
						}
					} else if (refAName.contains("production") && (refBName.contains("output") || refB.getBaseInterface().getName().toLowerCase().contains("output"))) {
						Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(refADomain);
						ITransformable transformedProduct = ((IProductTransformable) transClass.newInstance())
								.transform(getHierarchyFromDomain(refADomain, context), context);
						if (refBDomain.equals(object.getElement())) {
							outputProducts.put((IProductTransformable) transformedProduct, refA);
						}
					} else if (refBName.contains("production") && (refAName.contains("input") || refA.getBaseInterface().getName().toLowerCase().contains("input"))) {
						Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(refBDomain);
						ITransformable transformedProduct = ((IProductTransformable) transClass.newInstance())
								.transform(getHierarchyFromDomain(refBDomain, context), context);
						if (refADomain.equals(object.getElement())) {
							inputProducts.put((IProductTransformable) transformedProduct, refB);
						}
					} else if (refBName.contains("production") && (refAName.contains("output") || refA.getBaseInterface().getName().toLowerCase().contains("output"))) {
						Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(refBDomain);
						ITransformable transformedProduct = ((IProductTransformable) transClass.newInstance())
								.transform(getHierarchyFromDomain(refBDomain, context), context);
						if (refADomain.equals(object.getElement())) {
							outputProducts.put((IProductTransformable) transformedProduct, refB);
						}
					}
				}
			}
			
			for (IProductTransformable trans : inputProducts.keySet()) {
				InterfaceDesignator des = inputProducts.get(trans);
				String value = null;
				if (des.getDesignatorByName("quantity") != null) {
					value = des.getDesignatorByName("quantity").getValue();
				}
				if (value == null || value.isEmpty()) {
					productionSkill.getInputConfiguration().getProductQuantities().add(new ProductQuantity((Product) trans.getElement(), 1));
				} else {
					productionSkill.getInputConfiguration().getProductQuantities().add(new ProductQuantity((Product) trans.getElement(), Integer.parseInt(value)));
				}
			}
			
			if (productionSkill.getName().equalsIgnoreCase("plate milling")) {
				System.err.println("Found input: " + productionSkill.getInputConfiguration());
			}
			
			for (IProductTransformable trans : outputProducts.keySet()) {
				InterfaceDesignator des = outputProducts.get(trans);
				String value = null;
				if (des.getDesignatorByName("quantity") != null) {
					value = des.getDesignatorByName("quantity").getValue();
				}
				if (value == null || value.isEmpty()) {
					productionSkill.getOutputConfiguration().getProductQuantities().add(new ProductQuantity((Product) trans.getElement(), 1));
				} else {
					productionSkill.getOutputConfiguration().getProductQuantities().add(new ProductQuantity((Product) trans.getElement(), Integer.parseInt(value)));
				}
			}
		}
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
			if (child.getName().equals("ProductionSkills")) {
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
		
		//add supportedRole
		internalElement.addSupportedRole(((Hierarchy<Role>) getTransformableTemplateSkill().reverseTransform()).getElement());
		
		//add AttributeDesignators
		for (PropertyDesignator des : productionSkill.getPropertyDesignators()) {
			Attribute attribute = new PropertyAdapter(des.getProperty()).reverseTransform();
			if (attribute == null) {
				throw new IllegalArgumentException("Cannot reverse transform attribute: " + des.getProperty().getName());
			}
			AttributeDesignator attDes = new AttributeDesignator(attribute, 
					internalElement, new ArrayList<Constraint>(), des.getValue());
			internalElement.addDesignator(attDes);
			attDes.getAttribute().addDesignator(attDes);
		}
		
		//do internalLink <- discuss whether this is still needed or not because the same thing can be achieved by using
		//ProductConfiguration
		doInternalLinkMethod(internalElement, factoryHie);
		
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		objectHie.setParent(parentHie);
		
		//add default children
		IProductConfigurationTransformable inputProductConfigurationTransformable = getTransformableInputProductConfiguration();
		if (inputProductConfigurationTransformable != null) {
			objectHie.addChild((Hierarchy<?>) inputProductConfigurationTransformable.reverseTransform());
		}
		IProductConfigurationTransformable outputProductConfigurationTransformable = getTransformableOutputProductConfiguration();
		if (outputProductConfigurationTransformable != null) {
			objectHie.addChild((Hierarchy<?>) outputProductConfigurationTransformable.reverseTransform());
		}
		//add aml elements
		for (Object obj : getAMLElements()) {
			objectHie.addChild((Hierarchy<InternalElement>) obj);
		}
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}
	
	@SuppressWarnings("unchecked")
	private void doInternalLinkMethod(InternalElement internalElementPSkill, Hierarchy<InternalElement> factoryHie) {
		Set<ProductQuantity> inputs = productionSkill.getInputConfiguration().getProductQuantities();
		Set<ProductQuantity> outputs = productionSkill.getOutputConfiguration().getProductQuantities();
		
		if (productionSkillConnectorHierarchy == null) {
			throw new IllegalArgumentException("ProductionSkillConnector doesn't exist");
		}
		Interface productionSkillConnector = productionSkillConnectorHierarchy.getElement();
		//get input and output interface designators from production skill element (from the requiredRole)
		InterfaceDesignator inputDes = internalElementPSkill.getInterfaceDesignatorByName("InputProductInterface");
		InterfaceDesignator outputDes = internalElementPSkill.getInterfaceDesignatorByName("OutputProductInterface");

		String productionSkillInterface = "ProductionSkillInterface";
		Attribute quantityAttribute = new Attribute("Quantity", AttributeType.INTEGER, "");
		for (ProductQuantity input : inputs) {
			InternalElement productElement = ((Hierarchy<InternalElement>) new ProductAdapter(input.getProduct()).reverseTransform()).getElement();
			
			InterfaceDesignator prodDes = null;
			if ((inputCountMap.get(productElement) == null && outputCountMap.get(productElement) == null)) {
				prodDes = productElement.getInterfaceDesignatorByName(productionSkillInterface);
				inputCountMap.put(productElement, 1);
			} else {
				int oldCount = 0;
				if (inputCountMap.get(productElement) != null) {
					oldCount = inputCountMap.get(productElement);
				}
				int totalCount = oldCount;
				if (outputCountMap.get(productElement) != null) {
					totalCount += outputCountMap.get(productElement);
				}
				prodDes = new InterfaceDesignator(UUID.randomUUID().toString(),
						productionSkillInterface + totalCount, productElement, productionSkillConnector);
				productElement.addInterfaceDesignator(prodDes);
				inputCountMap.put(productElement, oldCount++);
			}
			AttributeDesignator designator = new AttributeDesignator(quantityAttribute, prodDes, new ArrayList<Constraint>(),
					"" + input.getQuantity());
			prodDes.addDesignator(designator);
			quantityAttribute.addDesignator(designator);
			
			InternalLink internalLink = new InternalLink("New Link " + factoryHie.getElement().getInternalLinks().size() + 1, 
					prodDes, inputDes);
			factoryHie.getElement().addInternalLink(internalLink);
		}
		
		for (ProductQuantity output : outputs) {
			InternalElement productElement = ((Hierarchy<InternalElement>) new ProductAdapter(output.getProduct()).reverseTransform()).getElement();
			InterfaceDesignator prodDes = null;
			
			if ((inputCountMap.get(productElement) == null && outputCountMap.get(productElement) == null)) {
				prodDes = productElement.getInterfaceDesignatorByName(productionSkillInterface);
				outputCountMap.put(productElement, 1);
			} else {
				int oldCount = 0;
				if (outputCountMap.get(productElement) != null) {
					oldCount = outputCountMap.get(productElement);
				}
				int totalCount = oldCount;
				if (inputCountMap.get(productElement) != null) {
					totalCount += inputCountMap.get(productElement);
				}
				prodDes = new InterfaceDesignator(UUID.randomUUID().toString(), 
						productionSkillInterface + totalCount, productElement, productionSkillConnector);
				productElement.addInterfaceDesignator(prodDes);
				outputCountMap.put(productElement, oldCount++);
			}
			AttributeDesignator designator = new AttributeDesignator(quantityAttribute, prodDes, new ArrayList<Constraint>(), 
					"" + output.getQuantity());
			prodDes.addDesignator(designator);
			quantityAttribute.addDesignator(designator);
			
			InternalLink internalLink = new InternalLink("New Link " + factoryHie.getElement().getInternalLinks().size() + 1, 
					prodDes, outputDes);
			factoryHie.getElement().addInternalLink(internalLink);
		}
	}

	private Set<InternalLink> getInternalLinks(Set<Object> context) {
		if (internalLinks == null) {
			Set<InternalLink> internalLinks = new HashSet<>();
			for (Object obj : context) {
				if (obj instanceof Hierarchy<?> && ((Hierarchy<?>) obj).getElement() instanceof InternalElement) {
					@SuppressWarnings("unchecked")
					InternalElement ie = ((Hierarchy<InternalElement>) obj).getElement();
					internalLinks.addAll(ie.getInternalLinks());
				}
			}
			ProductionSkillAdapter.internalLinks = internalLinks;
		}
		return internalLinks;
	}
	
	private boolean domainExistsInContext(Domain domain, Set<Object> context) {
		if (domain instanceof InternalElement) {
			for (Object obj : context) {
				if (obj instanceof Hierarchy<?> && ((Hierarchy<?>) obj).getElement() == domain) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public IFactoryTransformable getTransformableFactory() {
		Factory factory = null;
		
		for (ProductQuantity pq : productionSkill.getInputConfiguration().getProductQuantities()) {
			Factory productFactory = pq.getProduct().getFactory();
			if (factory != null && !factory.equals(productFactory)) {
				throw new IllegalArgumentException("Inconsistent data. Multiple factories are found in the " +
						"products of this ProductionSkill: " + getTransformableName());
			} else if (factory == null) {
				factory = productFactory;
			}
		}
		for (ProductQuantity pq : productionSkill.getOutputConfiguration().getProductQuantities()) {
			Factory productFactory = pq.getProduct().getFactory();
			if (factory != null && !factory.equals(productFactory)) {
				throw new IllegalArgumentException("Inconsistent data. Multiple factories are found in the " +
						"products of this ProductionSkill: " + getTransformableName());
			} else if (factory == null) {
				factory = productFactory;
			}
		}
		if (factory == null) {
			throw new IllegalArgumentException("Cannot find Factory for this ProductionSkill: " + getTransformableName());
		}
		
		return new FactoryAdapter(factory);
	}
	
	@SuppressWarnings("unchecked")
	private Hierarchy<InternalElement> getHierarchyFromDomain(Domain domain, Set<Object> context) {
		if (domain instanceof InternalElement) {
			for (Object obj : context) {
				if (obj instanceof Hierarchy<?> && ((Hierarchy<?>) obj).getElement() == domain) {
					return (Hierarchy<InternalElement>) obj;
				}
			}
		}
		return null;
	}
	
	private Property findProperty(String name) {
		for (Property prop : this.productionSkill.getProperties()) {
			if (prop.getName().equals(name)) {
				return prop;
			}
		}
		return null;
	}

	@Override
	public String getTransformableName() {
		return this.productionSkill.getName();
	}

	@Override
	public String getTransformableID() {
		return this.productionSkill.getId();
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
	public ITemplateSkillTransformable getTransformableTemplateSkill() {
		if (productionSkill.getTemplateSkill() == null) {
			throw new IllegalArgumentException("A ProductionSkill without a TemplateSkill?? IMPOSSIBRU: " + getTransformableName() + ":" + getTransformableID());
		}
		return new TemplateSkillAdapter(productionSkill.getTemplateSkill());
	}

	@Override
	public IProductConfigurationTransformable getTransformableInputProductConfiguration() {
		if (productionSkill.getInputConfiguration() == null) {
			return null;
		}
		return new PreProductConfigurationAdapter(productionSkill.getInputConfiguration());
	}
	
	@Override
	public IProductConfigurationTransformable getTransformableOutputProductConfiguration() {
		if (productionSkill.getOutputConfiguration() == null) {
			return null;
		}
		return new PostProductConfigurationAdapter(productionSkill.getOutputConfiguration());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(productionSkill);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductionSkillAdapter other = (ProductionSkillAdapter) obj;
		return Objects.equals(productionSkill, other.productionSkill);
	}
}
