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

package pscm.resourceprovider.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.SEE;
import skillpro.model.assets.Setup;
import skillpro.model.assets.Tool;
import skillpro.model.costs.Cost;
import skillpro.model.products.Order;
import skillpro.model.products.Product;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.products.Supplier;
import skillpro.model.products.Supply;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.properties.PropertyType;
import skillpro.model.repo.Repo;
import skillpro.model.repo.execution.KPIRepo;
import skillpro.model.repo.resource.AssetRepo;
import skillpro.model.repo.resource.CatalogRepo;
import skillpro.model.repo.resource.ProblemRepo;
import skillpro.model.repo.resource.ProductRepo;
import skillpro.model.repo.resource.ResourceSkillRepo;
import skillpro.model.repo.resource.SEERepo;
import skillpro.model.repo.resource.SetupRepo;
import skillpro.model.repo.resource.SkillRepo;
import skillpro.model.resourceprovider.ISkillproProvider;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.update.UpdateType;
import skillpro.model.utils.ShiftSchedule;

public class LocalSkillproProvider implements ISkillproProvider {
	private AssetRepo assetRepo;
	private SetupRepo setupRepo;
	private Repo<TemplateSkill> templateSkillRepo;
	private ResourceSkillRepo resourceSkillRepo;
	private Repo<ProductionSkill> productionSkillRepo;
	private SkillRepo skillRepo;
	private ProductRepo productRepo;
	private Repo<Order> orderRepo;
	private Repo<Property> propertyRepo;
	private SEERepo seeRepo;
	private CatalogRepo catalogRepo;
	private ProblemRepo problemRepo;
	
	//runtime Repo
	private KPIRepo kpiRepo;
	
	public LocalSkillproProvider() {
	}

	@Override
	public AssetRepo getAssetRepo() {
		if (assetRepo == null) {
			assetRepo = new AssetRepo();
		}
		return assetRepo;
	}

	@Override
	public SetupRepo getSetupRepo() {
		if (setupRepo == null) {
			setupRepo = new SetupRepo();
		}
		return setupRepo;
	}

	@Override
	public Repo<Property> getPropertyRepo() {
		if (propertyRepo == null) {
			propertyRepo = new Repo<>();
		}
		return propertyRepo;
	}

	@Override
	public ProductRepo getProductRepo() {
		if (productRepo == null) {
			productRepo = new ProductRepo();
		}
		return productRepo;
	}

	@Override
	public ResourceSkillRepo getResourceSkillRepo(){
		if (resourceSkillRepo == null) {
			resourceSkillRepo = new ResourceSkillRepo();
		}
		return resourceSkillRepo;
	}

	@Override
	public Repo<TemplateSkill> getTemplateSkillRepo(){
		if (templateSkillRepo == null) {
			templateSkillRepo = new Repo<>();
		}
		return templateSkillRepo;
	}

	@Override
	public SkillRepo getSkillRepo() {
		if (skillRepo == null) {
			skillRepo = new SkillRepo();
		}
		return skillRepo;
	}

	@Override
	public Repo<ProductionSkill> getProductionSkillRepo() {
		if (productionSkillRepo == null) {
			productionSkillRepo = new Repo<>();
		}
		return productionSkillRepo;
	}

	@Override
	public SEERepo getSEERepo() {
		if (seeRepo == null) {
			seeRepo = new SEERepo();
		}
		return seeRepo;
	}
	
	@Override
	public CatalogRepo getCatalogRepo() {
		if (catalogRepo == null) {
			catalogRepo = new CatalogRepo();
		}
		return catalogRepo;
	}

	@Override
	public Repo<Order> getOrderRepo() {
		if (orderRepo == null) {
			orderRepo = new Repo<Order>();
		}
		return orderRepo;
	}
	
	@Override
	public ProblemRepo getProblemRepo() {
		if (problemRepo == null) {
			problemRepo = new ProblemRepo();
		}
		return problemRepo;
	}
	
	@Override
	public KPIRepo getKPIRepo() {
		if (kpiRepo == null) {
			kpiRepo = new KPIRepo();
		}
		return kpiRepo;
	}
	
	@Override
	public FactoryNode createFactoryNode(String name, FactoryNode parent) {
		FactoryNode factoryNode = new FactoryNode(name, parent, true);
		if (parent == null) {
			getAssetRepo().getEntities().add(factoryNode);
		}
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_CREATED,
				FactoryNode.class);
		return factoryNode;
	}

	@Override
	public Factory createFactory(String name, List<Product> products, ShiftSchedule shiftPlan, Order order) {
		Factory factory = new Factory(name, products, shiftPlan, order);
		getAssetRepo().getEntities().add(factory);
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_CREATED,
				FactoryNode.class);
		return factory;
	}

	@Override
	public Factory createFactory(Factory factory) {
		Factory result = new Factory(factory);
		getAssetRepo().getEntities().add(result);
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_CREATED,
				FactoryNode.class);
		return result;
	}

	@Override
	public Resource createResource(String name,
			List<Setup> setups, FactoryNode parent) {
		Resource resource = new Resource(name, setups, parent);
		if (parent == null) {
			getAssetRepo().getEntities().add(resource);
		}
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_CREATED,
				FactoryNode.class);
		return resource;
	}

	@Override
	public Resource copyResourceToCatalog(Resource resource) {
		Resource copy = null;
		List<Setup> newSetups = new ArrayList<>();
		copy = new Resource(resource.getName(), newSetups, null);
		for (Setup config : resource.getSetups()) {
			Setup newConfig = new Setup(config.getName(), copy);
			for (ResourceSkill rs : config.getResourceSkills()) {
				ResourceSkill newResourceSkill = getCatalogRepo().getReferenceResourceSkillsMapping().get(rs);
				if (newResourceSkill == null) {
					newResourceSkill = new ResourceSkill(rs.getName(), rs.getTemplateSkill(), copy
							, rs.getPrePostRequirements());
					getCatalogRepo().getReferenceResourceSkillsMapping().put(rs, newResourceSkill);
				}
				newConfig.addResourceSkill(newResourceSkill);
			}
			//current configuration for the new copied workplace?
			newSetups.add(newConfig);
			getCatalogRepo().getSetups().add(newConfig);
		}
		//catalogs don't have parents.
		for (Setup conf : newSetups) {
			if (conf.getName().equals(resource.getCurrentSetup().getName())) {
				((Resource) copy).setCurrentSetup(conf);
				break;
			}
		}
		copy.setLayoutable(resource.isLayoutable());
		copy.setCurrentCoordinates(resource.getCurrentCoordinates());
		copy.setSize(resource.getSize());
		
		List<Resource> entities = getCatalogRepo().getEntities();
		boolean foundEqualName = false;
		for (Resource entity : entities) {
			if (entity.getName().equals(resource.getName())) {
				foundEqualName = true;
				break;
			}
		}
		if (!foundEqualName) {
			entities.add(copy);
		}
		SkillproService.getUpdateManager().notify(UpdateType.CATALOG_UPDATED,
				FactoryNode.class);
		return copy;
	}

	@Override
	public Setup createSetup(String name, Resource resource) {
		Setup setup = new Setup(name, resource);
		SkillproService.getSkillproProvider().getSetupRepo().getEntities().add(setup);
		setup.setResource(resource);
		SkillproService.getUpdateManager().notify(UpdateType.CONFIGURATION_UPDATED, FactoryNode.class);
		return setup;
	}

	@Override
	public Tool createTool(String name) {
		Tool tool = new Tool(name);
		SkillproService.getUpdateManager().notify(UpdateType.TOOL_UPDATED,
				Skill.class);
		return tool;
	}

	@Override
	public TemplateSkill createTemplateSkill(String name, List<Property> properties) {
		TemplateSkill templateSkill = new TemplateSkill(name, properties);
		List<TemplateSkill> templateSkills = getTemplateSkillRepo().getEntities();
		if (templateSkill.getParent() == null && !templateSkills.contains(templateSkill)) {
			templateSkills.add(templateSkill);
		}
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_CREATED,
				Skill.class);
		return templateSkill;
	}

	@Override
	public ResourceSkill createResourceSkill(String name,
			TemplateSkill templateSkill, Resource asset) {
		ResourceSkill resourceSkill = new ResourceSkill(name, templateSkill,
				asset);
		getResourceSkillRepo().getEntities().add(resourceSkill);
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_CREATED,
				Skill.class);
		return resourceSkill;
	}

	@Override
	public ResourceSkill createResourceSkill(String name, TemplateSkill templateSkills, Resource asset,
			List<PrePostRequirement> prePostRequirements) {
		ResourceSkill resourceSkill = new ResourceSkill(name, templateSkills, asset, prePostRequirements);
		getResourceSkillRepo().getEntities().add(resourceSkill);
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_CREATED, Skill.class);
		return resourceSkill;
	}

	@Override
	public ProductionSkill createProductionSkill(String name,
			TemplateSkill templateSkill, Set<ProductQuantity> inputs,
			Set<ProductQuantity> outputs) {
		ProductionSkill productionSkill = new ProductionSkill(name,
				templateSkill, inputs, outputs);
		getProductionSkillRepo().getEntities().add(productionSkill);
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_CREATED,
				Skill.class);
		return productionSkill;
	}

	@Override
	public ExecutableSkill createExecutableSkill(String name,
			Resource resource, TemplateSkill templateSkill,
			ResourceConfiguration preResourceConfiguration,
			ResourceConfiguration postResourceConfiguration,
			ProductConfiguration preProductConfiguration,
			ProductConfiguration postProductConfiguration,
			List<PropertyDesignator> designators, int slack, int duration,
			Cost cost) {
		//FIXME
		return null;
	}
	
	@Override
	public ExecutableSkill createExecutableSkill(String name, Cost cost,
			Set<ResourceExecutableSkill> resourceExecutableSkills) {
		ExecutableSkill executableSkill = new ExecutableSkill(name, cost);
		for (ResourceExecutableSkill res : resourceExecutableSkills) {
			executableSkill.addResourceExecutableSkill(res);
		}
		getSkillRepo().getExecutableSkills().add(executableSkill);
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_CREATED,
				Skill.class);
		return executableSkill;
	}

	@Override
	public Product createProduct(String name, Supply supply, Factory factory) {
		Product product = new Product(name, supply, factory);
		getProductRepo().getEntities().add(product);
		SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_CREATED,
				Product.class);
		return product;
	}

	@Override
	public Supply createSupply(String name, Supplier supplier) {
		Supply supply = new Supply(name, supplier);

		return supply;
	}

	@Override
	public Supplier createSupplier(String name) {
		Supplier supplier = new Supplier(name);
		return supplier;
	}

	@Override
	public Property createProperty(String name, PropertyType type, String unit) {
		Property property = new Property(name, type, unit);
		getPropertyRepo().getEntities().add(property);
		SkillproService.getUpdateManager().notify(UpdateType.PROPERTY_CREATED,
				Property.class);
		return property;
	}

	@Override
	public PropertyDesignator createPropertyDesignator(Property property, Skill skill, String value){
		PropertyDesignator designator = new PropertyDesignator(property, skill, value);
		return designator;
	}

	@Override
	public PropertyDesignator createPropertyDesignator(Property property, Skill skill, String value, List<PropertyConstraint> constraints) {
		PropertyDesignator designator = new PropertyDesignator(property, skill, value, constraints);
		return designator;
	}

	@Override
	public SEE createSEE() {
		SEE see = new SEE();
		getSEERepo().add(see);
		return see;
	}

	@Override
	public SEE createSEE(String confString) {
		SEE see = new SEE(confString);
		getSEERepo().add(see);
		return see;
	}

	@Override
	public void addFactoryNodeAndDependencies(FactoryNode factoryNode) {
		if (factoryNode instanceof Resource) {
			addResourceAndItsSetupsAndResourceSkills((Resource) factoryNode);
		} else {
			getAssetRepo().add(factoryNode);
			for (FactoryNode child : factoryNode.getSubNodes()) {
				addFactoryNodeAndDependencies(child);
			}
		}
	}
	
	@Override
	public void addResourceAndItsSetupsAndResourceSkills(Resource resource) {
		getAssetRepo().add(resource);
		for (Setup setup : resource.getSetups()) {
			getSetupRepo().add(setup);
			for (ResourceSkill resourceSkill : setup.getResourceSkills()) {
				getResourceSkillRepo().add(resourceSkill);
			}
		}
	}
	
	@Override
	public void addSkillAndDependencies(Skill skill) {
		if (skill instanceof TemplateSkill) {
			getTemplateSkillRepo().remove(skill);
		} else if (skill instanceof ResourceSkill) {
			getResourceSkillRepo().remove(skill);
			List<Setup> setups = ((ResourceSkill) skill).getResource().getSetups();
			getSetupRepo().getEntities().addAll(setups);
		} else if (skill instanceof ProductionSkill) {
			getProductionSkillRepo().remove(skill);
		}
	}
	
	@Override
	public void removeFactoryNode(FactoryNode factoryNode) {
		getAssetRepo().remove(factoryNode);
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_DELETED,
				FactoryNode.class);
	}

	@Override
	public void removeSkill(Skill skill) {
		if (skill instanceof TemplateSkill) {
			getTemplateSkillRepo().remove(skill);
		} else if (skill instanceof ResourceSkill) {
			getResourceSkillRepo().remove(skill);
		} else if (skill instanceof ProductionSkill) {
			getProductionSkillRepo().remove(skill);
		}
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_DELETED,
				Skill.class);
	}

	@Override
	public void removeProduct(Product product) {
		getProductRepo().remove(product);
		SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_DELETED,
				Product.class);
	}

	@Override
	public void removeProperty(Property property) {
		getPropertyRepo().remove(property);
		SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_DELETED,
				Property.class);
	}

	@Override
	public void removeSEE(SEE see) {
		getSEERepo().remove(see);
		// notify who?
	}
	
	@Override
	public void removeFactoryNodeAndDependencies(FactoryNode factoryNode) {
		if (factoryNode instanceof Resource) {
			removeResourceAndItsSetupsAndResourceSkills((Resource) factoryNode);
		} else {
			getAssetRepo().remove(factoryNode);
			for (FactoryNode child : factoryNode.getSubNodes()) {
				removeFactoryNodeAndDependencies(child);
			}
		}
	}
	
	@Override
	public void removeResourceAndItsSetupsAndResourceSkills(Resource resource) {
		getAssetRepo().remove(resource);
		for (Setup setup : resource.getSetups()) {
			getSetupRepo().remove(setup);
			for (ResourceSkill resourceSkill : setup.getResourceSkills()) {
				getResourceSkillRepo().remove(resourceSkill);
			}
		}
	}

	@Override
	public void removeSkillAndDependencies(Skill skill) {
		if (skill instanceof TemplateSkill) {
			getTemplateSkillRepo().remove(skill);
		} else if (skill instanceof ResourceSkill) {
			getResourceSkillRepo().remove(skill);
			List<Setup> setups = ((ResourceSkill) skill).getResource().getSetups();
			getSetupRepo().getEntities().addAll(setups);
			SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
		} else if (skill instanceof ProductionSkill) {
			getProductionSkillRepo().remove(skill);
		} 
	}
	
	@Override
	public void updateFactoryNode(FactoryNode factoryNode) {
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED,
				FactoryNode.class);
	}

	@Override
	public void updateSkill(Skill skill) {
	}

	@Override
	public void updateProduct(Product product) {
		// no need to update, since all products are root elements.
		SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_UPDATED,
				Product.class);

	}

	@Override
	public void updateProperty(Property property) {
		// no need to update, since all properties are root elements.
		SkillproService.getUpdateManager().notify(UpdateType.PROPERTY_UPDATED,
				Property.class);
	}

	@Override
	public void updateSEE(SEE see) {
		// no need to update, since all SEEs are root elements.
	}

	@Override
	public boolean isDirty() {
		return !(
				getAssetRepo().isEmpty() &&
				getSetupRepo().isEmpty() &&
				getProductRepo().isEmpty() &&
				getProductionSkillRepo().isEmpty() &&
				getResourceSkillRepo().isEmpty() &&
				getTemplateSkillRepo().isEmpty() &&
				getSEERepo().isEmpty()
				);
	}

	@Override
	public void wipeAllData() {
		getAssetRepo().wipeAllData();
		getProductRepo().wipeAllData();
		getPropertyRepo().wipeAllData();
		getResourceSkillRepo().wipeAllData();
		getTemplateSkillRepo().wipeAllData();
		getSkillRepo().wipeAllData();
		getProductionSkillRepo().wipeAllData();
		getSEERepo().wipeAllData();
		getCatalogRepo().wipeAllData();
		getOrderRepo().wipeAllData();
		getKPIRepo().wipeAllData();
		getProblemRepo().wipeAllData();
	}

	@Override
	public void refreshAssetRepo() {
	}

	@Override
	public void refreshProductRepo() {
	}

	@Override
	public void refreshSkillRepo() {
	}

	@Override
	public void refreshProperyRepo() {
	}

	@Override
	public void refreshSEERepo() {
	}
}
