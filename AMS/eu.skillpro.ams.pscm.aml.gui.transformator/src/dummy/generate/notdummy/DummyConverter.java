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

package dummy.generate.notdummy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.Setup;
import skillpro.model.products.Product;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyType;
import skillpro.model.repo.Repo;
import skillpro.model.repo.resource.AssetRepo;
import skillpro.model.repo.resource.ProductRepo;
import skillpro.model.repo.resource.ResourceSkillRepo;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.RequirementProductConfigType;
import skillpro.model.skills.RequirementResourceConfigType;
import skillpro.model.skills.RequirementSkillType;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.skills.dummy.Condition;
import skillpro.model.skills.dummy.ConditionConfiguration;
import skillpro.model.skills.dummy.ConfigurationSet;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.PropertyDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import skillpro.model.utils.Pair;

public class DummyConverter {
	
	private static final Factory FACTORY = new Factory("Factory");
	private static final DummyConverter INSTANCE = new DummyConverter();
	
	private DummyConverter() {
//		FACTORY.setNodeID(UUID.randomUUID().toString());
		SkillproService.getSkillproProvider().getAssetRepo().add(FACTORY);
	}

	public static DummyConverter getInstance() {
		return INSTANCE;
	}
	
	public void convertToSkillproRepo(ExecutableSkillDummy exDummy) {
		ResourceExecutableSkillDummy mainDummy = exDummy.getMainDummy();
		
		Resource mainResource = undummifyResourceDummy(mainDummy.getResource());
		List<PropertyDummy> mainPropDummies = mainDummy.getPropertyDummies();
		TemplateSkill mainTemplateSkill = undummifyTemplateSkill(mainDummy.getTemplateSkill(), mainPropDummies);
		
		//RSkill and PSkill are automatically added into the Repo
		ResourceSkill mainResourceSkill = undummifyResourceSkill(mainResource, mainTemplateSkill, mainPropDummies,
				mainDummy.getPreCondition(), mainDummy.getPostCondition());
		if (exDummy.getProducer() != null) {
			//TODO fix this to get the correct input and output configurations
			ResourceExecutableSkillDummy producer = exDummy.getProducer();
			List<PropertyDummy> prodPropDummies = producer.getPropertyDummies();
			TemplateSkill producerTemplateSkill = undummifyTemplateSkill(producer.getTemplateSkill(), prodPropDummies);
			undummifyProductionSkill(mainDummy.getName(), producerTemplateSkill, prodPropDummies,
					producer.getPreCondition(), producer.getPostCondition());
			
		}
		for (ResourceExecutableSkillDummy rexDummy : exDummy.getDummies()) {
			//create Requirements for the main ResourceSkill
			//+ create ResourceSkills for each Reqs
			if (!rexDummy.equals(mainDummy)) {
				ResourceDummy resourceDummy = rexDummy.getResource();
				String templateSkillDummy = rexDummy.getTemplateSkill();
				List<PropertyDummy> propertyDummies = rexDummy.getPropertyDummies();
				Condition preConditionDummy = rexDummy.getPreCondition();
				Condition postConditionDummy = rexDummy.getPostCondition();
				
				Resource undummifiedResource = undummifyResourceDummy(resourceDummy);
				TemplateSkill undummifiedTemplateSkill = undummifyTemplateSkill(templateSkillDummy, propertyDummies);
				ResourceSkill undummifiedResourceSkill = undummifyResourceSkill(undummifiedResource, undummifiedTemplateSkill, 
						propertyDummies, preConditionDummy, postConditionDummy);
				
				Requirement preRequirement = undummifyRequirement(undummifiedResourceSkill, preConditionDummy);
				Requirement postRequirement = undummifyRequirement(undummifiedResourceSkill, postConditionDummy);
				preRequirement.setMainResourceSkill(mainResourceSkill);
				postRequirement.setMainResourceSkill(mainResourceSkill);
				//FIXME postRequirements have to always have the SAME SkillType for now
				postRequirement.setSkillType(RequirementSkillType.SAME);
				//set product config to the prePostReqs
				
				Pair<ProductConfiguration, ProductConfiguration> prePostProductConfiguration = undummifyPrePostProductConfiguration(postConditionDummy);
				ProductConfiguration preProductConf = prePostProductConfiguration.getFirstElement();
				ProductConfiguration postProductConf = prePostProductConfiguration.getSecondElement();

				preRequirement.setProductConfigType(RequirementProductConfigType.SPECIFIC);
				preRequirement.setRequiredProductConfiguration(preProductConf);
				postRequirement.setProductConfigType(RequirementProductConfigType.SPECIFIC);
				postRequirement.setRequiredProductConfiguration(postProductConf);
				if (preProductConf == null) {
					//FIXME I don't know, this is too weird
					//what does {} mean? Is it empty or it doesn't matter what it is
					preRequirement.setProductConfigType(RequirementProductConfigType.ANY);
				}
				
				if (postProductConf == null && preProductConf != null) {
					postRequirement.setProductConfigType(RequirementProductConfigType.EMPTY);
				}
				
				if (preProductConf == postProductConf) {
					postRequirement.setProductConfigType(RequirementProductConfigType.SAME);
				}
				
				//set ResourceConfigType
				if (preRequirement.getRequiredResourceConfiguration() == postRequirement.getRequiredResourceConfiguration()) {
					postRequirement.setResourceConfigType(RequirementResourceConfigType.SAME);
				}
				//adding pre post requirements to the main resource skill
				mainResourceSkill.addPrePostRequirement(new PrePostRequirement(preRequirement, postRequirement));
				
			}
			
			
		}
	}
	
	private Requirement undummifyRequirement(ResourceSkill resourceSkill, Condition conditionDummy) {
		Resource resource = resourceSkill.getResource();
		
		List<ResourceConfiguration> undummifiedResourceConfigurations = 
				undummifyResourceConfigurations(conditionDummy, resource);
		
		
		ResourceConfiguration resourceConfiguration = getSingleResourceConfigurationFromList(undummifiedResourceConfigurations);
		
		//FIXME probably need a constructor
		Requirement requirement = new Requirement();
		if (resourceConfiguration == null) {
			requirement.setResourceConfigType(RequirementResourceConfigType.ANY);
		} else {
			requirement.setResourceConfigType(RequirementResourceConfigType.SPECIFIC);
		}
		//Product config will be set somewhere else
		requirement.setRequiredResourceConfiguration(resourceConfiguration);
		requirement.setSkillType(RequirementSkillType.RESOURCE_SKILL);
		requirement.setRequiredResourceSkill(resourceSkill);
		return requirement;
	}
	
	private Product undummifyProduct(String productString) {
		//example of a product string from a product configuration
		//'{ManualInspectedPCB-prodB': [(">=", 1), ("<", 65536)]
		
		char singleQuote = '\'';
		
		boolean firstQuote = false;
		String productName = "";

		for (int i = 0; i < productString.length(); i++) {
			if (firstQuote) {
				if (productString.charAt(i) == singleQuote) {
					break;
				}
				productName += productString.charAt(i);
			}
			
			if (!firstQuote && productString.charAt(i) == singleQuote) {
				firstQuote = true;
			}
		}
		
		ProductRepo allProducts = SkillproService.getSkillproProvider().getProductRepo();
		for (Product prod : allProducts) {
			//dummy's id = name
			if (prod.getName().equals(productName)) {
				return prod;
			}
		}
		
		Product product = new Product(productName, null, FACTORY);
		allProducts.add(product);
		
		return product;
	}
	
	private Pair<ProductConfiguration, ProductConfiguration> undummifyPrePostProductConfiguration(Condition conditionDummy) {
		//only postProduct from rexDummy is relevant
		//pre product only has the min max limitations which is pretty useless
		//example of how the post product looks like {'twoLayerPCB-prodB': -1, 'THTassembledPCB-prodB': 1}
		//negative quantity means that it is being used as the input
		//positive of course means that it's the output that we get after we execute it
		String allConfString = conditionDummy.getSecondElement();
		//FIXME check correctness
		String[] productConfigurations = allConfString.split(", ");
		
		Set<ProductQuantity> inputQuantities = new HashSet<>();
		Set<ProductQuantity> outputQuantities = new HashSet<>();
		for (String pqString : productConfigurations) {
			//FIXME Default quantity = 1?
			if (!pqString.isEmpty() && !pqString.equals("{}")) {
				pqString = pqString.replace('{', ' ');
				pqString = pqString.replace('}', ' ');
				String[] pqTokens = pqString.split(": ");
				//first token = product, second = quantity
				if (pqTokens.length != 2) {
					throw new IllegalArgumentException("Unknown PQString format");
				}
				int quantity = Integer.parseInt(pqTokens[1].replaceAll("\\s+", ""));
				if (quantity < 0) {
					inputQuantities.add(new ProductQuantity(undummifyProduct(pqTokens[0]), Math.abs(quantity)));
				} else {
					outputQuantities.add(new ProductQuantity(undummifyProduct(pqTokens[0]), 1));
				}
			}
		}
		//FIXME random id?
		ProductConfiguration inputConfiguration = new ProductConfiguration(UUID.randomUUID().toString(), inputQuantities);
		ProductConfiguration outputConfiguration = new ProductConfiguration(UUID.randomUUID().toString(), outputQuantities);
		if (inputQuantities.isEmpty()) {
			//Empty Configuration
			inputConfiguration = null;
		} 
		
		if (outputQuantities.isEmpty()) {
			//empty configuration
			outputConfiguration = null;
		}
		return new Pair<ProductConfiguration, ProductConfiguration>(inputConfiguration, outputConfiguration);
	}
	
	
	private TemplateSkill undummifyTemplateSkill(String templateSkillDummy, List<PropertyDummy> propertyDummies) {
		Repo<TemplateSkill> templateSkills = SkillproService.getSkillproProvider().getTemplateSkillRepo();
		for (TemplateSkill tSkill : templateSkills) {
			//dummy's id = name
			if (tSkill.getName().equals(templateSkillDummy)) {
				return tSkill;
			}
		}
		List<Property> properties = new ArrayList<>();
		for (PropertyDummy propDum : propertyDummies) {
			Property undummifiedProperty = undummifyProperty(propDum);
			if (!properties.contains(undummifiedProperty)) {
				properties.add(undummifiedProperty);
			}
		}
		
		TemplateSkill tSkill = new TemplateSkill(templateSkillDummy, properties);
		if (!templateSkills.contains(tSkill)){
			templateSkills.add(tSkill);
		}
		
		return tSkill;
	}
	
	private Property undummifyProperty(PropertyDummy propertyDummy) {
		for (Property property : SkillproService.getSkillproProvider().getPropertyRepo()) {
			if (property.getName().equals(propertyDummy.getName())
					&& property.getType() == getPropertyTypeFromString(propertyDummy.getDataType())) {
				return property;
			}
		}
		
		return new Property(propertyDummy.getName(),
				getPropertyTypeFromString(propertyDummy.getDataType()), propertyDummy.getUnit());
	}
	
	private PropertyType getPropertyTypeFromString(String type) {
		for (PropertyType e : PropertyType.values()) {
			if (e.name().equalsIgnoreCase(type)) {
				return e;
			}
		}
		
		if (type.equals("")) {
			//FIXME isn't this a huge problem? Empty types?
			return PropertyType.STRING;
		}
		
		throw new IllegalArgumentException("Can't find PropertyType: " + type);
	}
	
	private List<ResourceConfiguration> undummifyResourceConfigurations(Condition condition, Resource resource) {
		List<ResourceConfiguration> resourceConfigurations = new ArrayList<>();
		ConfigurationSet confSet = condition.getFirstElement();
		for (ConditionConfiguration conf : confSet) {
			resourceConfigurations.add(undummifyResourceConfiguration(conf, resource));
		}
		return resourceConfigurations;
	}
	
	private ResourceConfiguration undummifyResourceConfiguration(ConditionConfiguration dummyConf, Resource resource) {
		List<ResourceConfiguration> configurations = resource.getResourceConfigurations();
		//FIXME should ID and Name be the same?
		ResourceConfiguration conf = new ResourceConfiguration(dummyConf.getName() + ":" + resource.getName(), dummyConf.getName(), resource);
		if (!configurations.contains(conf)) {
			//add conf into the list of configurations that this resource has
			configurations.add(conf);
		}
		
		return conf;
	}
	
	private ResourceSkill undummifyResourceSkill(Resource resource, TemplateSkill templateSkill,
			List<PropertyDummy> propertyDummies, Condition preCondition, Condition postCondition) {
		//FIXME propertyDummies are not yet used. Constraints are not defined in a ResourceExecutableSkillDummy
		ResourceSkillRepo resourceSkills = SkillproService.getSkillproProvider().getResourceSkillRepo();
		
		for (ResourceSkill rSkill : resourceSkills) {
			if (rSkill.getResource().equals(resource) && rSkill.getTemplateSkill().equals(templateSkill)) {
				return rSkill;
			}
		}
		
		ResourceSkill rSkill = new ResourceSkill("RSkill: " + templateSkill.getName(), templateSkill, resource);
		
		//add rSkill to default setup if it exists
		
		List<Setup> setups = resource.getSetups();
		if (setups.isEmpty()) {
			Setup setup = new Setup("Default Setup", resource);
			resource.setCurrentSetup(setup);
			SkillproService.getSkillproProvider().getSetupRepo().getEntities().add(setup);
			setups.add(setup);
		} 
		
		//add the resource skill to one of the setups (should be default setup)
		Setup supposedlyDefaultSetup = setups.get(0);
		if (!supposedlyDefaultSetup.getResourceSkills().contains(rSkill)) {
			supposedlyDefaultSetup.addResourceSkill(rSkill);
		}
		
		resourceSkills.add(rSkill);
		
		//FIXME Pre- Post- ResourceConfigurations
		rSkill.setPreConfiguration(getSingleResourceConfigurationFromList
				(undummifyResourceConfigurations(preCondition, resource)));
		rSkill.setPostConfiguration(getSingleResourceConfigurationFromList
				(undummifyResourceConfigurations(postCondition, resource)));
		//Add self Requirements
		Requirement preRequirement = undummifyRequirement(rSkill, preCondition);
		Requirement postRequirement = undummifyRequirement(rSkill, postCondition);
		preRequirement.setMainResourceSkill(rSkill);
		postRequirement.setMainResourceSkill(rSkill);
		postRequirement.setSkillType(RequirementSkillType.SAME);
		//set product config to the prePostReqs
		
		Pair<ProductConfiguration, ProductConfiguration> prePostProductConfiguration = undummifyPrePostProductConfiguration(postCondition);
		ProductConfiguration preProductConf = prePostProductConfiguration.getFirstElement();
		ProductConfiguration postProductConf = prePostProductConfiguration.getSecondElement();

		preRequirement.setProductConfigType(RequirementProductConfigType.SPECIFIC);
		preRequirement.setRequiredProductConfiguration(preProductConf);
		postRequirement.setProductConfigType(RequirementProductConfigType.SPECIFIC);
		postRequirement.setRequiredProductConfiguration(postProductConf);
		if (preProductConf == null) {
			//FIXME I don't know, this is too weird
			//what does {} mean? Is it empty or it doesn't matter what it is
			preRequirement.setProductConfigType(RequirementProductConfigType.ANY);
		}
		
		if (postProductConf == null && preProductConf != null) {
			postRequirement.setProductConfigType(RequirementProductConfigType.EMPTY);
		}
		
		if (preProductConf == postProductConf) {
			postRequirement.setProductConfigType(RequirementProductConfigType.SAME);
		}
		
		//ResourceConfigType
		//set ResourceConfigType
		if (preRequirement.getRequiredResourceConfiguration() == null) {
			//FIXME I don't know, this is too weird
			//what does {} mean? Is it empty or it doesn't matter what it is
			preRequirement.setResourceConfigType(RequirementResourceConfigType.ANY);
		}
		
		
		if (preRequirement.getRequiredResourceConfiguration() == postRequirement.getRequiredResourceConfiguration()) {
			postRequirement.setResourceConfigType(RequirementResourceConfigType.SAME);
		}
		
		if (postRequirement.getRequiredResourceConfiguration()== null) {
			postRequirement.setResourceConfigType(RequirementResourceConfigType.ANY);
		}
		//adding pre post requirements to the main resource skill
		rSkill.addPrePostRequirement(new PrePostRequirement(preRequirement, postRequirement));
		
		return rSkill;
		 
	}
	
	private ResourceConfiguration getSingleResourceConfigurationFromList(List<ResourceConfiguration> resourceConfigurations) {
		if (resourceConfigurations.size() == 0) {
			return null;
		} else if (resourceConfigurations.size() == 1) {
			return resourceConfigurations.get(0);
		} else {
			throw new IllegalArgumentException("How to get through this dilemma: confDummies has more than 1 conf");
		}
	}
	
	private ProductionSkill undummifyProductionSkill(String mainSkillName, TemplateSkill templateSkill, List<PropertyDummy> propertyDummies,
			Condition preCondition, Condition postCondition) {
		Repo<ProductionSkill> productionSkills = SkillproService.getSkillproProvider().getProductionSkillRepo();
		
		String name = "PSkill: " + mainSkillName + "-" + templateSkill.getName();
		for (ProductionSkill pSkill : productionSkills) {
			//FIXME check correctness, does it need another if statement?s
			if (pSkill.getName().equals(name)) {
				return pSkill;
			}
		}
		Pair<ProductConfiguration, ProductConfiguration> prePostProductConfiguration = undummifyPrePostProductConfiguration(postCondition);
		ProductionSkill pSkill = new ProductionSkill(name, templateSkill,
				prePostProductConfiguration.getFirstElement(), prePostProductConfiguration.getSecondElement());
		productionSkills.add(pSkill);
		
		return pSkill;
	}
	
	private Resource undummifyResourceDummy(ResourceDummy dummy) {
		AssetRepo assets = SkillproService.getSkillproProvider().getAssetRepo();
		for (FactoryNode fn : assets) {
			//dummy's id = name
			if (fn.getName().equals(dummy.getResourceId())) {
				if (fn instanceof Resource) {
					return (Resource) fn;
				} else {
					throw new IllegalArgumentException("FN has a similar name as the dummy resource");
				}
			}
		}
		Resource resource = new Resource(dummy.getResourceId(), new ArrayList<Setup>(), FACTORY);
		assets.add(resource);
		//create AnyOrIdle dummyConfs
		undummifyResourceConfiguration(ConditionConfiguration.ANY_OR_IDLE, resource);
		
		return resource;
		
		
	}
}
