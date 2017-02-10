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
import java.util.UUID;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.assets.State;
import skillpro.model.products.ProductConfiguration;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IFactoryTransformable;
import aml.skillpro.transformation.interfaces.IProductConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTypeTransformable;
import aml.skillpro.transformation.interfaces.IResourceTransformable;

public class FactoryNodeAdapter extends TransformableAdapterTemplate implements IFactoryNodeTransformable {
	private FactoryNode factoryNode;
	
	public FactoryNodeAdapter() {
		this(new FactoryNode());
	}
	
	public FactoryNodeAdapter(FactoryNode factoryNode) {
		this.factoryNode = factoryNode;
	}
	
	@Override
	public FactoryNode getElement() {
		return factoryNode;
	}
	
	@Override
	public List<Object> getAMLElements() {
		return factoryNode.getAmlElements();
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
		
		//attributes and stuff
		double x = Double.parseDouble(ie.getDesignatorByName("XCoordinate").getValue());
		double y = Double.parseDouble(ie.getDesignatorByName("YCoordinate").getValue());
		double z = Double.parseDouble(ie.getDesignatorByName("ZCoordinate").getValue());
		double length = Double.parseDouble(ie.getDesignatorByName("AssetLength").getValue());
		double width = Double.parseDouble(ie.getDesignatorByName("AssetWidth").getValue());
		double height = Double.parseDouble(ie.getDesignatorByName("AssetHeight").getValue());
		factoryNode.setCurrentCoordinates(x, y, z);
		factoryNode.setSize(length, width, height);
		factoryNode.setInitialCoordinates(x, y, z);
		factoryNode.setName(ie.getName());
		factoryNode.setNodeID(ie.getId());
		
		if (factoryNode.getLength() >= 0 && factoryNode.getWidth() >= 0) {
			factoryNode.setLayoutable(true);
		} else if (factoryNode instanceof Resource) {
			factoryNode.setLength(100);
			factoryNode.setWidth(100);
			if (ie.getDesignatorByName("CurrentState") != null) {
				String currentStateString = ie.getDesignatorByName("CurrentState").getValue();
				for (State state : State.values()) {
					if (state.toString().equals(currentStateString)) {
						((Resource) factoryNode).setState(state);
						break;
					}
				}
			}
			//add ResourceConfigurationTypes from supportedRoles
			for (Role supportedRole : ie.getSupportedRoles()) {
				ITransformable transformedResourceConfiguration = new ResourceConfigurationTypeAdapter().transform
						(supportedRole, context);
				if (transformedResourceConfiguration == null) {
					throw new IllegalArgumentException("ResourceConfigurationType cannot be transformed for this Role: " 
							+ supportedRole.getName());
				}
				((Resource) factoryNode).addResourceConfigurationType((ResourceConfigurationType) transformedResourceConfiguration.getElement());
			}
		}
		
		//set parent
		FactoryNode parentElement = null;
		Hierarchy<InternalElement> parent = object.getParent();
		if (parent != null && context.contains(parent)) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping
					.get(parent.getElement());
			if (IFactoryNodeTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedParent = ((IFactoryNodeTransformable) transClass.newInstance())
						.transform(parent, context);
				parentElement = (FactoryNode) transformedParent.getElement();
			}
		}
		if (parentElement == null || parentElement instanceof FactoryNode) {
			if (parentElement instanceof Resource) {
				throw new IllegalArgumentException("Resource can't be a parent: " + parentElement);
			}
			factoryNode.setParent(parentElement);
		} else {
			throw new IllegalArgumentException("Parent: " + parentElement + ", is not from type FactoryNode");
		}
		
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
			if (transClass != null && IProductConfigurationTransformable.class.isAssignableFrom(transClass)) {
				ITransformable transformedConf = ((IProductConfigurationTransformable) transClass.newInstance())
						.transform(child, context);
				((Resource) getElement()).setCurrentProductConfiguration((ProductConfiguration) transformedConf.getElement());
			}
			
		}
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}

	@Override
	public Object reverseTransform() {
		return reverseTransform(true);
	}
	
	@Override
	public Object reverseTransformEntity() {
		return reverseTransform(false);
	}
	
	@SuppressWarnings("unchecked")
	private Object reverseTransform(boolean parent) {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		Hierarchy<InternalElement> parentHie = null;
		if (parent) {
			if (getTransformableParent() != null) {
				parentHie = (Hierarchy<InternalElement>) getTransformableParent().reverseTransform();
			}
		}
		
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
		objectHie.setParent(parentHie);
		if (this instanceof IResourceTransformable) {
			//add ResourceConfigurations as children of Resource
			IResourceTransformable iResourceTransformable = (IResourceTransformable) this;
			for (IResourceConfigurationTransformable trans : iResourceTransformable.getTransformableResourceConfigurations()) {
				objectHie.addChild((Hierarchy<InternalElement>) trans.reverseTransform());
			}
			//ResourceConfigurationTypes as supported roles
			for (IResourceConfigurationTypeTransformable trans : iResourceTransformable
					.getTransformableResourceConfigurationTypes()) {
				internalElement.addSupportedRole(((Hierarchy<Role>) trans.reverseTransform()).getElement());
			}
			
			if (iResourceTransformable.getTransformableProductConfiguration() != null) {
				objectHie.addChild((Hierarchy<InternalElement>) iResourceTransformable
						.getTransformableProductConfiguration().reverseTransform());
			}
			
		} else if (this instanceof IFactoryTransformable) {
			//add default children
			for (Hierarchy<InternalElement> child : childrenOfFactoryHierarchy()) {
				objectHie.addChild(child);
			}
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
	
	private void addDesignators(InternalElement ie) {
		addAttribute(ie, "XCoordinate", "Double", "cm", getXCoordinate() + "");
		addAttribute(ie, "YCoordinate", "Double", "cm", getYCoordinate() + "");
		addAttribute(ie, "ZCoordinate", "Double", "cm", getZCoordinate() + "");
		addAttribute(ie, "AssetLength", "Double", "cm", getAssetLength() + "");
		addAttribute(ie, "AssetWidth", "Double", "cm", getAssetWidth() + "");
		addAttribute(ie, "AssetHeight", "Double", "cm", getAssetHeight() + "");
		addAttribute(ie, "AssetHeight", "Double", "cm", getAssetHeight() + "");
		if (factoryNode instanceof Resource) {
			addAttribute(ie, "CurrentState", "String", "", ((Resource) factoryNode).getState().toString());
		}
	}
	
	private List<Hierarchy<InternalElement>> childrenOfFactoryHierarchy() {
		List<Hierarchy<InternalElement>> children = new ArrayList<>();
		
		InternalElement products = new InternalElement(UUID.randomUUID().toString(), "Products");
		products.setRequiredRole(productStructureHierarchy.getElement());
		InternalElement productionSkills = new InternalElement(UUID.randomUUID().toString(), "ProductionSkills");
		productionSkills.setRequiredRole(processStructureHierarchy.getElement());
		
		children.add(new Hierarchy<InternalElement>(products.getName(), products));
		children.add(new Hierarchy<InternalElement>(productionSkills.getName(), productionSkills));
		
		
		return children;
	}
	
	//other methods
	@Override
	public double getAssetLength() {
		return factoryNode.getLength();
	}
	
	@Override
	public double getAssetWidth() {
		return factoryNode.getWidth();
	}
	
	@Override
	public double getAssetHeight() {
		return factoryNode.getHeight();
	}
	
	@Override
	public double getXCoordinate() {
		return factoryNode.getCurrentCoordinates().x;
	}
	
	@Override
	public double getYCoordinate() {
		return factoryNode.getCurrentCoordinates().y;
	}
	
	@Override
	public double getZCoordinate() {
		return factoryNode.getCurrentCoordinates().z;
	}
	
	@Override
	public String getTransformableName() {
		return factoryNode.getName();
	}
	
	@Override
	public String getTransformableID() {
		return factoryNode.getNodeID();
	}
	
	@Override
	public List<ITransformable> getTransformableChildren() {
		List<ITransformable> transformableChildren = new ArrayList<>();
		List<FactoryNode> subNodes = factoryNode.getSubNodes();
		if (factoryNode instanceof Resource && subNodes != null && !subNodes.isEmpty()) {
			throw new IllegalArgumentException("Resources are not allowed to have children" +
				", size: " + subNodes.size());
		} else {
			subNodes = (subNodes == null) ? new ArrayList<FactoryNode>() : subNodes;
			for (FactoryNode node : subNodes) {
				transformableChildren.add(new FactoryNodeAdapter(node));
			}
		}
		
		return transformableChildren;
	}
	
	public IFactoryNodeTransformable getTransformableParent() {
		IFactoryNodeTransformable transformableParent = null;
		if (factoryNode.getParent() != null) {
			if (factoryNode instanceof Factory) {
				throw new IllegalArgumentException("Factories are not allowed to have a parent: " + factoryNode.getParent());
			}
			if (factoryNode.getParent() instanceof Factory) {
				transformableParent = new FactoryAdapter((Factory) factoryNode.getParent());
			} else {
				transformableParent = new FactoryNodeAdapter(factoryNode.getParent());
			}
		}
		return transformableParent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(factoryNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactoryNodeAdapter other = (FactoryNodeAdapter) obj;
		return Objects.equals(factoryNode, other.factoryNode);
	}
}
