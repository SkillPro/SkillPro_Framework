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

package skillpro.model.resourceprovider;

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
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.utils.ShiftSchedule;

public interface ISkillproProvider {
	//read
	public AssetRepo getAssetRepo();
	public SetupRepo getSetupRepo();
	public Repo<Property> getPropertyRepo();
	public ProductRepo getProductRepo();
	public Repo<Order> getOrderRepo();
	public ResourceSkillRepo getResourceSkillRepo();
	public Repo<ProductionSkill> getProductionSkillRepo();
	public SkillRepo getSkillRepo();
	public Repo<TemplateSkill> getTemplateSkillRepo();
	public SEERepo getSEERepo();
	public CatalogRepo getCatalogRepo();
	public ProblemRepo getProblemRepo();
	//runtime repo
	public KPIRepo getKPIRepo();
	//create
	public FactoryNode createFactoryNode(String name, FactoryNode parent);
	public Factory createFactory(String name, List<Product> products,
			ShiftSchedule shiftPlan, Order order);
	public Factory createFactory(Factory factory);
	public Resource createResource(String name, List<Setup> setups, FactoryNode parent);
	public Resource copyResourceToCatalog(Resource resource);
	public Setup createSetup(String name, Resource resource);
	public Tool createTool(String name);

	public TemplateSkill createTemplateSkill(String name,
			List<Property> properties);
	public ResourceSkill createResourceSkill(String name,
			TemplateSkill templateSkill, Resource asset);
	public ResourceSkill createResourceSkill(String name, TemplateSkill templateSkill, Resource asset,
			List<PrePostRequirement> prePostRequirements);
	
	public ProductionSkill createProductionSkill(String name,
			TemplateSkill templateSkill,
			Set<ProductQuantity> inputs, Set<ProductQuantity> outputs);
	public ExecutableSkill createExecutableSkill(String name, Resource resource,
			TemplateSkill templateSkill,
			ResourceConfiguration preResourceConfiguration,
			ResourceConfiguration postResourceConfiguration,
			ProductConfiguration preProductConfiguration,
			ProductConfiguration postProductConfiguration,
			List<PropertyDesignator> designators, int slack, int duration, Cost cost);
	public ExecutableSkill createExecutableSkill(String name, Cost cost, Set<ResourceExecutableSkill> resourceExecutableSkills);
	
	public Product createProduct(String name, Supply supply, Factory factory);
	public Supply createSupply(String name, Supplier supplier);
	public Supplier createSupplier(String name);
	
	public Property createProperty(String name, PropertyType type, String unit);
	public PropertyDesignator createPropertyDesignator(Property property,
			Skill skill, String value);
	public PropertyDesignator createPropertyDesignator(Property property,
			Skill skill, String value, List<PropertyConstraint> constraints);
	
	public SEE createSEE();
	public SEE createSEE(String confString);
	
	public void addFactoryNodeAndDependencies(FactoryNode factoryNode);
	public void addResourceAndItsSetupsAndResourceSkills(Resource resource);
	public void addSkillAndDependencies(Skill skill);
	//remove
	public void removeFactoryNode(FactoryNode factoryNode);
	public void removeSkill(Skill skill);
	public void removeProduct(Product product);
	public void removeProperty(Property property);
	public void removeSEE(SEE see);
	
	public void removeFactoryNodeAndDependencies(FactoryNode factoryNode);
	public void removeResourceAndItsSetupsAndResourceSkills(Resource resource);
	public void removeSkillAndDependencies(Skill skill);
	//update
	public void updateFactoryNode(FactoryNode factoryNode);
	public void updateSkill(Skill skill);
	public void updateProduct(Product product);
	public void updateProperty(Property property);
	public void updateSEE(SEE see);

	//etc
	public boolean isDirty();
	public void refreshAssetRepo();
	public void refreshProductRepo();
	public void refreshSkillRepo();
	public void refreshProperyRepo();
	public void refreshSEERepo();
	
	public void wipeAllData();
}
