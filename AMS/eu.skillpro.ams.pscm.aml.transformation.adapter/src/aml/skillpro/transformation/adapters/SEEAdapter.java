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

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import skillpro.model.assets.AMSCommType;
import skillpro.model.assets.ESCommType;
import skillpro.model.assets.Factory;
import skillpro.model.assets.MESCommType;
import skillpro.model.assets.Resource;
import skillpro.model.assets.SEE;
import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.utils.Pair;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IResourceTransformable;
import aml.skillpro.transformation.interfaces.ISEETransformable;

public class SEEAdapter extends TransformableAdapterTemplate implements ISEETransformable {
	private SEE see;
	
	public SEEAdapter() {
		see = new SEE();
	}
	
	public SEEAdapter(SEE see) {
		this.see = see;
	}
	
	@Override
	public String getTransformableName() {
		return see.getResource().getName();
	}

	@Override
	public String getTransformableID() {
		return see.getSeeID();
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
		return see;
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
		
		//trivial attributes
		see.setSeeID(ie.getId());
		see.setAmlObject(object);
		//"Attributes" of SEE
		AttributeDesignator seeTypeDesignator = ie.getDesignatorByName("SEEType");
		Attribute seeTypeAtt = seeTypeDesignator.getAttribute();
		String seeTypeString = seeTypeDesignator.getValue();
		if (seeTypeAtt.getSubAttributes().size() > 1) {
			throw new IllegalArgumentException("Unknown sub attributes from the attribute: " 
					+ seeTypeAtt.getName());
		}
		//TODO uncomment this if needed
//		boolean isSimulated = false;
//		if (seeTypeAtt.getSubAttributes().size() == 1) {
//			Attribute simulatedAtt = seeTypeAtt.getSubAttributes().get(0);
//			if (simulatedAtt != null) {
//				String simulatedString = ie.getDesignatorByAttribute(simulatedAtt).getValue();
//				isSimulated = Boolean.parseBoolean(simulatedString);
//			}
//		}
		//ams and mes
		AttributeDesignator amsCommTypeDes = ie.getDesignatorByName("AMSCommType");
		Attribute amsCommTypeAtt = amsCommTypeDes.getAttribute();
		String amsCommTypeString = amsCommTypeDes.getValue();
		if (amsCommTypeAtt.getSubAttributes().size() > 1) {
			throw new IllegalArgumentException("Unknown sub attributes from the attribute: " 
					+ amsCommTypeAtt.getName());
		}
		String amsURI = getSubAttributeValue(amsCommTypeAtt, "uri", ie);
		
		AttributeDesignator mesCommTypeDes = ie.getDesignatorByName("MESCommType");
		Attribute mesCommTypeAtt = mesCommTypeDes.getAttribute();
		String mesCommTypeString = mesCommTypeDes.getValue();
		if (mesCommTypeAtt.getSubAttributes().size() > 2) {
			throw new IllegalArgumentException("Unknown sub attributes from the attribute: " 
					+ mesCommTypeAtt.getName());
		}
		String mesURI = getSubAttributeValue(mesCommTypeAtt, "uri", ie);
		
		AttributeDesignator esCommTypeDes = ie.getDesignatorByName("ESCommType");
		Attribute esCommTypeAtt = esCommTypeDes.getAttribute();
		String esCommTypeString = esCommTypeDes.getValue();
		if (esCommTypeAtt.getSubAttributes().size() > 2) {
			throw new IllegalArgumentException("Unknown sub attributes from the attribute: " 
					+ esCommTypeAtt.getName());
		}
		String esURI = getSubAttributeValue(esCommTypeAtt, "uri", ie);
		
		AMSCommType amsCommType = null;
		MESCommType mesCommType = null;
		ESCommType esCommType = null;
		for (Enum<AMSCommType> e : AMSCommType.values()) {
			if (e.toString().equalsIgnoreCase(amsCommTypeString)) {
				amsCommType = (AMSCommType) e;
			}
		}
		
		for (Enum<MESCommType> e : MESCommType.values()) {
			if (e.toString().equalsIgnoreCase(mesCommTypeString)) {
				mesCommType = (MESCommType) e;
			}
		}
		
		for (Enum<ESCommType> e : ESCommType.values()) {
			if (e.toString().equalsIgnoreCase(esCommTypeString)) {
				esCommType = (ESCommType) e;
			}
		}
		//FIXME optional?
		AttributeDesignator customerNameDes = ie.getDesignatorByName("CustomerName");
		if (customerNameDes != null) {
			see.setCustomerName(customerNameDes.getValue());
		}
		
		see.setAMSCommunication(new Pair<AMSCommType, String>(amsCommType, amsURI));
		see.setMESCommunication(new Pair<MESCommType, String>(mesCommType, mesURI));
		see.setESCommunication(new Pair<ESCommType, String>(esCommType, esURI));
		see.setSEEType(seeTypeString);
		//default ids
		Attribute defaultCondition = ie.getDesignatorByName("Default_Condition").getAttribute();
		String defaultResourceConfigurationID = getSubAttributeValue(defaultCondition, "Configuration", ie);;
		String defaultProductConfigurationID = getSubAttributeValue(defaultCondition, "Product", ie);;

		JsonReader jsonReader = Json.createReader(new StringReader(defaultProductConfigurationID));
	    JsonObject jsonObject = jsonReader.readObject();
	    jsonReader.close();
	    
	    Set<ProductQuantity> productQuantities = new HashSet<>();
	    
	    for (String key : jsonObject.keySet()) {
	    	Hierarchy<InternalElement> foundHie = findProduct(key, context);
	    	Product foundProduct = null;
	    	if (foundHie != null) {
	    		foundProduct = (Product) new ProductAdapter(new Product()).transform(foundHie, context).getElement();
	    	} else {
	    		//no factory cos this isn't done correctly
	    		foundProduct = new Product(key, null, new Factory("DummyFactory"));
	    	}
	    	
	    	int quantity = Integer.parseInt(jsonObject.get(key).toString());
		    
	    	if (quantity < 0) {
	    		throw new IllegalArgumentException("Couldn't create product quantity, please check input format.");
	    	}
	    	productQuantities.add(new ProductQuantity(foundProduct, quantity));
	    }
		see.setDefaultInputProductQuantities(productQuantities);
		see.setDefaultResourceConfigurationID(defaultResourceConfigurationID);
		
		//Assets managed by the SEE
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping
					.get(child.getElement());
			ITransformable transformedAsset = ((IFactoryNodeTransformable) transClass.newInstance())
					.transform(child, context);
			//adds the assets as not_registered
			see.addNotRegisteredResource((Resource) transformedAsset.getElement());
			((Resource) transformedAsset.getElement()).setResponsibleSEE(see);
		}
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}
	
	private String getSubAttributeValue(Attribute parentAttribute, String attributeName, InternalElement ie) {
		String result = "";
		boolean foundAlready = false;
		for (Attribute subAtt : parentAttribute.getSubAttributes()) {
			if (subAtt != null && subAtt.getName().equalsIgnoreCase(attributeName)) {
				if (foundAlready) {
					throw new IllegalArgumentException("Duplicate " + attributeName.toUpperCase() + " attribute!!");
				}
				result = ie.getDesignatorByAttribute(subAtt).getValue();
				foundAlready = true;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Hierarchy<InternalElement> findProduct(String productName, Set<Object> context) {
		//FIXME will not work if there are multiple products
		for (Object obj : context) {
			if (obj instanceof Hierarchy<?>) {
				if (((Hierarchy<?>) obj).getElement() != null && ((Hierarchy<?>) obj).getElement().getName().equals(productName)) {
					if (((Hierarchy<?>) obj).getElement() instanceof InternalElement) {
						return (Hierarchy<InternalElement>) obj;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public Object reverseTransformEntity() {
		return reverseTransform(true);
	}
	
	@Override
	public Object reverseTransform() {
		return reverseTransform(false);
	}
	
	@SuppressWarnings("unchecked")
	private Object reverseTransform(boolean single) {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		//add default attributes
		addDesignators(internalElement);
		
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		
		if (single) {
			Hierarchy<InternalElement> resourceHie = (Hierarchy<InternalElement>) getTransformableResource().reverseTransformEntity();
			objectHie.addChild(resourceHie);
			
		} else {
			Hierarchy<InternalElement> resourceHie = (Hierarchy<InternalElement>) getTransformableResource().reverseTransform();
			InternalElement actualResource = new InternalElement(UUID.randomUUID().toString(), resourceHie.getName());
			actualResource.setReferencedInternalElement(resourceHie.getElement());
			Hierarchy<InternalElement> actualResourceHierarchy = new Hierarchy<InternalElement>(actualResource.getName(), actualResource);
			actualResourceHierarchy.setParent(objectHie);
		}
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}
	
	private void addDesignators(InternalElement ie) {
		//default condition: resource configuration and product configuration
		Attribute condition = new Attribute("Default_Condition", AttributeType.COMPLEX_TYPE, "");
		addAttribute(ie, condition, ""); 
		
		Attribute configuration = new Attribute("Configuration", AttributeType.STRING, "");
		configuration.setDescription("Default resource configuration");
		condition.addAttribute(configuration);
		addAttribute(ie, configuration, see.getDefaultResourceConfigurationID());
		
		Attribute product = new Attribute("Product", AttributeType.STRING, "");
		product.setDescription("Default product configuration");
		condition.addAttribute(product);
		JsonObjectBuilder preReqJsonOB = Json.createObjectBuilder();
		Set<ProductQuantity> defaultInputProductQuantities = see.getDefaultInputProductQuantities();
		for (ProductQuantity input : defaultInputProductQuantities) {
			preReqJsonOB.add(input.getProduct().getName(), input.getQuantity());
		}
		addAttribute(ie, product, preReqJsonOB.build().toString());
		
		//MESCommType
		Attribute mesCommType = new Attribute("MESCommType", AttributeType.STRING, "");
		addAttribute(ie, mesCommType, see.getMESCommunication().getFirstElement().toString());
		
		Attribute mesUri = new Attribute("uri", AttributeType.STRING, "");
		mesCommType.addAttribute(mesUri);
		addAttribute(ie, mesUri, see.getMESCommunication().getSecondElement());
		
		Attribute nodeId = new Attribute("nodeId", AttributeType.STRING, "");
		mesCommType.addAttribute(nodeId);
		addAttribute(ie, nodeId, (see.getMESNodeID() == null 
				|| see.getMESNodeID().isEmpty()) ? "ns=;i=" : see.getMESNodeID());
		
		//ESCommType
		Attribute esCommType = new Attribute("ESCommType", AttributeType.STRING, "");
		addAttribute(ie, esCommType, see.getESCommunication().getFirstElement().toString());
		
		Attribute esUri = new Attribute("uri", AttributeType.STRING, "");
		esCommType.addAttribute(esUri);
		addAttribute(ie, esUri, see.getESCommunication().getSecondElement());
		
		Attribute esNodeId = new Attribute("nodeId", AttributeType.STRING, "");
		esCommType.addAttribute(esNodeId);
		addAttribute(ie, esNodeId, (see.getESNodeID() == null 
				|| see.getESNodeID().isEmpty()) ? "ns=;i=" : see.getESNodeID());
		
		//AMSCommType
		Attribute amsCommType = new Attribute("AMSCommType", AttributeType.STRING, "");
		addAttribute(ie, amsCommType, see.getAMSCommunication().getFirstElement().toString());
		
		Attribute amsUri = new Attribute("uri", AttributeType.STRING, "");
		amsCommType.addAttribute(amsUri);
		addAttribute(ie, amsUri, see.getAMSCommunication().getSecondElement());
		
		//SEEType
		Attribute seeType = new Attribute("SEEType", AttributeType.STRING, "");
		addAttribute(ie, seeType, see.getSEEType());
		
		Attribute simulated = new Attribute("simulated", AttributeType.STRING, "");
		seeType.addAttribute(simulated);
		addAttribute(ie, simulated, false + "");
		
		//customer name
		if (see.getCustomerName() != null) {
			Attribute customerName = new Attribute("CustomerName", AttributeType.STRING, "");
			addAttribute(ie, customerName, see.getCustomerName());
		}
	}
	
	@Override
	public IResourceTransformable getTransformableResource() {
		if (see.getResource() == null) {
			throw new IllegalArgumentException("Resource is null for this see: " + getTransformableID());
		}
		return new ResourceAdapter(see.getResource());
	}

	@Override
	public int hashCode() {
		return Objects.hash(see);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SEEAdapter other = (SEEAdapter) obj;
		return Objects.equals(see, other.see);
	}
}
