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

package eu.skillpro.ams.pscm.ontology.transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ontology.model.Individual;
import ontology.model.OntologyClass;
import ontology.model.property.DataProperty;
import ontology.model.property.DataPropertyDesignator;
import ontology.model.property.DataPropertyType;
import ontology.model.property.ObjectProperty;
import ontology.model.property.ObjectPropertyDesignator;
import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.Setup;
import skillpro.model.products.Product;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyConstraintNominal;
import skillpro.model.properties.PropertyConstraintOrdinal;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.properties.PropertyType;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;

public class SkillproToOntologyTransformer {
	private static final String FACTORY_NODE = "FactoryNode";
	private static final String FACTORY = "Factory";
	private static final String RESOURCE = "Resource";
	private static final String SETUP = "Setup";
	private static final String TEMPLATE_SKILL = "TemplateSkill";
	private static final String RESOURCE_SKILL = "ResourceSkill";
	private static final String PRODUCTION_SKILL = "ProductionSkill";
	private static final String EXECUTABLE_SKILL = "ExecutableSkill";
	private static final String RESOURCE_EXECUTABLE_SKILL = "ResourceExecutableSkill";
	private static final String RESOURCE_CONFIGURATION = "ResourceConfiguration";
	private static final String PRODUCT_CONFIGURATION = "ProductConfiguration";
	private static final String PRODUCT_QUANTITY = "ProductQuantity";
	private static final String PRODUCT = "Product";
	private static final String EMPTY_PRODUCT = "EmptyProduct";
	
	private static final String HAS_RESOURCE_SKILL = "hasResourceSkill";
	private static final String HAS_TEMPLATE_SKILL = "hasTemplateSkill";
	private static final String HAS_PRODUCTION_SKILL = "hasProductionSkill";
	private static final String HAS_INPUT_PRODUCT_CONFIGURATION = "hasInputProductConfiguration";
	private static final String HAS_OUTPUT_PRODUCT_CONFIGURATION = "hasOutputProductConfiguration";
	private static final String HAS_PRE_RESOURCE_CONFIGURATION = "hasPreResourceConfiguration";
	private static final String HAS_PRE_PRODUCT_CONFIGURATION = "hasPreProductConfiguration";
	private static final String HAS_POST_RESOURCE_CONFIGURATION = "hasPostResourceConfiguration";
	private static final String HAS_POST_PRODUCT_CONFIGURATION = "hasPostProductConfiguration";
	private static final String HAS_ASSET = "hasAsset";
	private static final String HAS_SETUP = "hasSetup";
	private static final String HAS_RESOURCE_CONFIGURATION = "hasResourceConfiguration";
	private static final String HAS_PARENT = "hasParent";
	private static final String HAS_PRODUCT = "hasProduct";
	private static final String HAS_PRODUCT_QUANTITY = "hasProductQuantity";
	
	private static final SkillproToOntologyTransformer INSTANCE = new SkillproToOntologyTransformer();
	
	private SkillproToOntologyTransformer() {
	}
	
	public static SkillproToOntologyTransformer getInstance() {
		return INSTANCE;
	}
	
	public void transform() {
		//ontology class
		Set<OntologyClass> ontologyClasses = new HashSet<>();
		ontologyClasses.addAll(createAssetOntologyClasses());
		ontologyClasses.addAll(createSkillOntologyClasses());
		ontologyClasses.addAll(createDinOntologyClasses());
		ontologyClasses.addAll(createProductionOntologyClasses());
		ontologyClasses.addAll(createConfigurationClasses());
		
		//data property
		Set<DataProperty> dataProperties = new HashSet<>();
		for (TemplateSkill tSkill : SkillproService.getSkillproProvider().getTemplateSkillRepo()) {
			for (Property property : tSkill.getProperties()) {
				dataProperties.add(convertToDataProperty(property));
			}
		}
		
		for (ResourceSkill rSkill : SkillproService.getSkillproProvider().getResourceSkillRepo()) {
			for (PropertyDesignator des : rSkill.getPropertyDesignators()) {
				DataProperty converted = convertToDataProperty(des);
				if (converted != null) {
					dataProperties.add(converted);
				}
			}
		}
		
		for (ProductionSkill pSkill : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			for (PropertyDesignator des : pSkill.getPropertyDesignators()) {
				DataProperty converted = convertToDataProperty(des);
				if (converted != null) {
					dataProperties.add(converted);
				}
			}
		}
		//object property
		Set<ObjectProperty> objectProperties = createObjectProperties();
		//individuals
		Set<Individual> individuals = new HashSet<>();
		individuals.addAll(createAssetIndividuals());
		individuals.addAll(createSkillIndividuals());
		individuals.addAll(createProductIndividuals());
		
		SkillproService.getOntologyProvider().getOntologyClassRepo().getEntities().addAll(ontologyClasses);
		SkillproService.getOntologyProvider().getDataPropertyRepo().getEntities().addAll(dataProperties);
		SkillproService.getOntologyProvider().getObjectPropertyRepo().getEntities().addAll(objectProperties);
		SkillproService.getOntologyProvider().getIndividualRepo().getEntities().addAll(individuals);
	}
	
	private Set<OntologyClass> createAssetOntologyClasses() {
		Set<OntologyClass> ontologyClasses = new HashSet<>();
		OntologyClass ontologyClass = new OntologyClass("Asset");
		
		OntologyClass conf = new OntologyClass(SETUP);
		for (Resource resource : SkillproService.getSkillproProvider().getAssetRepo().getAllAssignedResources()) {
			for (Setup setup : resource.getSetups()) {
				OntologyClass setupClass = new OntologyClass(setup.getName());
				setupClass.addParent(conf);
				ontologyClasses.add(setupClass);
			}
		}
		
		OntologyClass factoryNode = new OntologyClass(FACTORY_NODE);
		OntologyClass factory = new OntologyClass(FACTORY);
		OntologyClass workplace = new OntologyClass(RESOURCE);
		//parents
		conf.addParent(ontologyClass);
		factoryNode.addParent(ontologyClass);
		factory.addParent(factoryNode);
		workplace.addParent(factoryNode);
		//disjoints
		conf.addDisjoint(factoryNode);
		factoryNode.addDisjoint(conf);
		factory.addDisjoint(workplace);
		workplace.addDisjoint(factory);
		ontologyClasses.add(ontologyClass);
		ontologyClasses.add(conf);
		ontologyClasses.add(factoryNode);
		ontologyClasses.add(factory);
		ontologyClasses.add(workplace);
		
		return ontologyClasses;
	}
	
	private Set<OntologyClass> createProductionOntologyClasses() {
		Set<OntologyClass> ontologyClasses = new HashSet<>();
		OntologyClass ontologyClass = new OntologyClass("Production");
		
		OntologyClass productQuantity = new OntologyClass(PRODUCT_QUANTITY);
		OntologyClass product = new OntologyClass(PRODUCT);
		OntologyClass emptyProduct = new OntologyClass(EMPTY_PRODUCT);
		//parents
		productQuantity.addParent(ontologyClass);
		product.addParent(ontologyClass);
		emptyProduct.addParent(product);
		
		ontologyClasses.add(ontologyClass);
		ontologyClasses.add(productQuantity);
		ontologyClasses.add(product);
		ontologyClasses.add(emptyProduct);
		return ontologyClasses;
	}
	
	private Set<OntologyClass> createConfigurationClasses() {
		Set<OntologyClass> ontologyClasses = new HashSet<>();
		OntologyClass ontologyClass = new OntologyClass("Configuration");
		
		OntologyClass productConfiguration = new OntologyClass(PRODUCT_CONFIGURATION);
		OntologyClass resourceConfiguration = new OntologyClass(RESOURCE_CONFIGURATION);
		//parents
		productConfiguration.addParent(ontologyClass);
		resourceConfiguration.addParent(ontologyClass);
		//disjoints
		productConfiguration.addDisjoint(resourceConfiguration);
		resourceConfiguration.addDisjoint(productConfiguration);
		
		ontologyClasses.add(ontologyClass);
		ontologyClasses.add(productConfiguration);
		ontologyClasses.add(resourceConfiguration);
		
		return ontologyClasses;
	}
	
	private Set<OntologyClass> createSkillOntologyClasses() {
		Set<OntologyClass> ontologyClasses = new HashSet<>();
		OntologyClass ontologyClass = new OntologyClass("Skill");
		
		OntologyClass template = new OntologyClass(TEMPLATE_SKILL);
		OntologyClass resource = new OntologyClass(RESOURCE_SKILL);
		OntologyClass production = new OntologyClass(PRODUCTION_SKILL);
		OntologyClass executable = new OntologyClass(EXECUTABLE_SKILL);
		OntologyClass resourceExecutable = new OntologyClass(RESOURCE_EXECUTABLE_SKILL);
		//parents
		template.addParent(ontologyClass);
		resource.addParent(ontologyClass);
		production.addParent(ontologyClass);
		executable.addParent(ontologyClass);
		resourceExecutable.addParent(ontologyClass);
		//TemplateSkills
		for (TemplateSkill tSkill : SkillproService.getSkillproProvider().getTemplateSkillRepo()) {
			OntologyClass tSkillClass = new OntologyClass(tSkill.getName());
			tSkillClass.addParent(template);
			ontologyClasses.add(tSkillClass);
		}
		//disjoints
		
		ontologyClasses.add(ontologyClass);
		ontologyClasses.add(template);
		ontologyClasses.add(resource);
		ontologyClasses.add(production);
		ontologyClasses.add(executable);
		ontologyClasses.add(resourceExecutable);
		
		return ontologyClasses;
	}
	
	private Set<OntologyClass> createDinOntologyClasses() {
		Set<OntologyClass> ontologyClasses = new HashSet<>();
		OntologyClass ontologyClass = new OntologyClass("DIN8580");
		
		OntologyClass din8580Min2 = new OntologyClass("DIN8580-2");
		
		din8580Min2.addParent(ontologyClass);
		
		ontologyClasses.add(ontologyClass);
		ontologyClasses.add(din8580Min2);
		
		return ontologyClasses;
	}
	
	private DataProperty convertToDataProperty(Property property) {
		DataProperty dataProperty = new DataProperty(property.getName(), convertToDataPropertyType(property.getType()));
		
		return dataProperty;
	}
	
	private DataProperty convertToDataProperty(PropertyDesignator designator) {
		if (designator.getConstraints() != null && !designator.getConstraints().isEmpty()) {
			DataProperty dataProperty = new DataProperty(designator.getSkill().getName() + "_" + designator.getProperty().getName()
					, convertToDataPropertyType(designator.getProperty().getType()));
			for (PropertyConstraint constraint : designator.getConstraints()) {
				dataProperty.addConstraint(convertToPropertyConstraint(constraint));
			}
		}
		
		return null;
	}

	private DataProperty createDataProperty(String name, DataPropertyType type) {
		return new DataProperty(name, type);
	}
	
	private ontology.model.property.PropertyConstraint convertToPropertyConstraint(
			PropertyConstraint constraint) {
		if (constraint instanceof PropertyConstraintNominal) {
			List<String> values = new ArrayList<>();
			values.addAll(((PropertyConstraintNominal) constraint).getValues());
			return new ontology.model.property.PropertyConstraintNominal(constraint.getName(), values);
		} else if (constraint instanceof PropertyConstraintOrdinal) {
			return new ontology.model.property.PropertyConstraintOrdinal(constraint.getName(),
					((PropertyConstraintOrdinal) constraint).getMaxValue(), ((PropertyConstraintOrdinal) constraint).getMinValue());
		}
		return null;
	}

	private DataPropertyType convertToDataPropertyType(PropertyType type) {
		if (type == PropertyType.BOOLEAN) {
			return DataPropertyType.BOOLEAN;
		} else if (type == PropertyType.DOUBLE) {
			return DataPropertyType.DOUBLE;
		} else if (type == PropertyType.INTEGER) {
			return DataPropertyType.INTEGER;
		} else if (type == PropertyType.STRING) {
			return DataPropertyType.STRING;
		}
		
		return DataPropertyType.STRING;
	}
	
	private Set<ObjectProperty> createObjectProperties() {
		Set<ObjectProperty> objectProperties = new HashSet<>();
		
		objectProperties.add(new ObjectProperty(HAS_PARENT));
		objectProperties.add(new ObjectProperty(HAS_TEMPLATE_SKILL));
		objectProperties.add(new ObjectProperty(HAS_RESOURCE_SKILL));
		objectProperties.add(new ObjectProperty(HAS_PRODUCTION_SKILL));
		objectProperties.add(new ObjectProperty(HAS_ASSET));
		objectProperties.add(new ObjectProperty(HAS_SETUP));
		objectProperties.add(new ObjectProperty(HAS_RESOURCE_CONFIGURATION));
		objectProperties.add(new ObjectProperty(HAS_INPUT_PRODUCT_CONFIGURATION));
		objectProperties.add(new ObjectProperty(HAS_OUTPUT_PRODUCT_CONFIGURATION));
		objectProperties.add(new ObjectProperty(HAS_PRODUCT));
		objectProperties.add(new ObjectProperty(HAS_PRE_PRODUCT_CONFIGURATION));
		objectProperties.add(new ObjectProperty(HAS_PRE_RESOURCE_CONFIGURATION));
		objectProperties.add(new ObjectProperty(HAS_POST_PRODUCT_CONFIGURATION));
		objectProperties.add(new ObjectProperty(HAS_POST_RESOURCE_CONFIGURATION));
		
		return objectProperties;
	}
	
	private Set<Individual> createAssetIndividuals() {
		Set<Individual> individuals = new HashSet<>();
		
		for (FactoryNode node : SkillproService.getSkillproProvider().getAssetRepo()) {
			Individual individual = new Individual(node.getName());
			if (node instanceof Factory) {
				individual.addType(new OntologyClass(FACTORY));
			} else if (node instanceof Resource) {
				individual.addType(new OntologyClass(RESOURCE));
				individual.addType(new OntologyClass(((Resource) node).getState().toString()));
				
				for (Setup conf : ((Resource) node).getSetups()) {
					
					Individual confIndividual = new Individual(conf.getName());
					confIndividual.addType(new OntologyClass(conf.getName()));
					
					individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_SETUP), individual,
							confIndividual));
					//data property?
					individuals.add(confIndividual);
					for (ResourceSkill rSkill : conf.getResourceSkills()) {
						confIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_RESOURCE_SKILL),
								confIndividual, new Individual(createResourceSkillName(rSkill))));
					}
				}
				for (ResourceConfiguration resourceConfiguration : ((Resource) node).getResourceConfigurations()) {
					Individual confIndividual = new Individual(createResourceConfigurationName(resourceConfiguration));
					confIndividual.addType(new OntologyClass(RESOURCE_CONFIGURATION));
					
					individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_RESOURCE_CONFIGURATION), individual,
							confIndividual));
					individuals.add(confIndividual);
				}
			} else {
				individual.addType(new OntologyClass(FACTORY_NODE));
			}
			
			if (node.getParent() != null) {
				individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PARENT), individual,
						new Individual(node.getParent().getName())));
			}
			
			individuals.add(individual);
		}
		
		
		
		return individuals;
	}
	
	private Set<Individual> createProductIndividuals() {
		Set<Individual> individuals = new HashSet<>();
		
		for (Product product : SkillproService.getSkillproProvider().getProductRepo()) {
			
			Individual individual = new Individual(product.getName());
			individual.addType(new OntologyClass(PRODUCT));
			individuals.add(individual);
		}
		
		return individuals;
	}

	private Set<Individual> createSkillIndividuals() {
		Set<Individual> individuals = new HashSet<>();
		//FIXME use only TemplateSkills that are used in any available ResourceSkills?getAvailableTemplateSkills() instead?
		// why do we need to transform the template skills that aren't even used?
		for (TemplateSkill tSkill : SkillproService.getSkillproProvider().getTemplateSkillRepo()) {
			Individual individual = new Individual(tSkill.getName());
			individual.addType(new OntologyClass(tSkill.getName()));
			
			if (tSkill.getParent() != null) {
				individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PARENT), individual,
						new Individual(tSkill.getParent().getName())));
			}
			
			//data property?
			for (Property property : tSkill.getProperties()) {
				individual.addDataPropertyDesignator(new DataPropertyDesignator(convertToDataProperty(property), individual, ""));
			}
			individuals.add(individual);
		}
		
		for (ResourceSkill rSkill : SkillproService.getSkillproProvider().getResourceSkillRepo()) {
			
			Individual individual = new Individual(createResourceSkillName(rSkill));
			
			individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_TEMPLATE_SKILL), individual,
					new Individual(rSkill.getTemplateSkill().getName())));
			
			individual.addType(new OntologyClass(RESOURCE_SKILL));
			
			if (rSkill.getResource() != null) {
				Individual asset = new Individual(rSkill.getResource().getName());
				asset.addType(new OntologyClass(asset.getClass().getSimpleName()));
				individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_ASSET), individual,
						asset));
				individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_TEMPLATE_SKILL), individual,
						new Individual(rSkill.getTemplateSkill().getName())));
				Setup currentSetup = null;
				boolean found = false;
				for (Setup setup : rSkill.getResource().getSetups()) {
					for (ResourceSkill resourceSkill : setup.getResourceSkills()) {
						if (resourceSkill.equals(rSkill)) {
							currentSetup = setup;
							found = true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
				
				for (Resource resource : SkillproService.getSkillproProvider().getAssetRepo().getAllAssignedResources()) {
					if (found) {
						break;
					}
					for (Setup setup : resource.getSetups()) {
						if (found) {
							break;
						}
						for (ResourceSkill resourceSkill : setup.getResourceSkills()) {
							if (resourceSkill.equals(rSkill)) {
								currentSetup = setup;
								found = true;
								break;
							}
						}
					}
					
				}
				individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_SETUP), individual,
						new Individual(currentSetup.getName())));
				
				for (PropertyDesignator des : rSkill.getPropertyDesignators()) {
					individual.addDataPropertyDesignator(new DataPropertyDesignator(convertToDataProperty(des),
							individual, ""));
				}
			} else {
				throw new IllegalArgumentException("Asset can't be null for this resource skill: " + rSkill.getName());
			}
			
			individuals.add(individual);
		}
		
		for (ProductionSkill pSkill : SkillproService.getSkillproProvider().getProductionSkillRepo().getEntities()) {
			
			Individual individual = new Individual(createProductionSkillName(pSkill));

			individual.addType(new OntologyClass(PRODUCTION_SKILL));
			individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_TEMPLATE_SKILL), individual,
					new Individual(pSkill.getTemplateSkill().getName())));
			
			//input ProductConfiguration
			ProductConfiguration inputProductConfiguration = pSkill.getInputConfiguration();
			Individual inputProductConfIndividual = new Individual(inputProductConfiguration.getId());
			inputProductConfIndividual.addType(new OntologyClass(PRODUCT_CONFIGURATION));
			
			individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_INPUT_PRODUCT_CONFIGURATION), individual,
					inputProductConfIndividual));
			//data property?
			individuals.add(inputProductConfIndividual);
			for (ProductQuantity pq : inputProductConfiguration.getProductQuantities()) {
				Individual pqIndidivual = new Individual(createProductQuantityName(pq));
				pqIndidivual.addType(new OntologyClass(PRODUCT_QUANTITY));
				
				inputProductConfIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT_QUANTITY),
						inputProductConfIndividual, pqIndidivual));
				individuals.add(pqIndidivual);
				
				//product + quantity
				pqIndidivual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT),
						pqIndidivual, new Individual(pq.getProduct().getName())));
				pqIndidivual.addDataPropertyDesignator(new DataPropertyDesignator(createDataProperty("Quantity",
						DataPropertyType.INTEGER), pqIndidivual, pq.getQuantity() + ""));
				
			}
			//output ProductConfiguration
			ProductConfiguration outputProductConfiguration = pSkill.getOutputConfiguration();
			Individual outputProductConfIndividual = new Individual(outputProductConfiguration.getId());
			outputProductConfIndividual.addType(new OntologyClass(PRODUCT_CONFIGURATION));
			
			individual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_OUTPUT_PRODUCT_CONFIGURATION), individual,
					outputProductConfIndividual));
			individuals.add(outputProductConfIndividual);
			for (ProductQuantity pq : outputProductConfiguration.getProductQuantities()) {
				Individual pqIndidivual = new Individual(createProductQuantityName(pq));
				pqIndidivual.addType(new OntologyClass(PRODUCT_QUANTITY));
				
				outputProductConfIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT_QUANTITY),
						outputProductConfIndividual, pqIndidivual));
				individuals.add(pqIndidivual);
				
				//product + quantity
				pqIndidivual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT),
						pqIndidivual, new Individual(pq.getProduct().getName())));
				pqIndidivual.addDataPropertyDesignator(new DataPropertyDesignator(createDataProperty("Quantity",
						DataPropertyType.INTEGER), pqIndidivual, pq.getQuantity() + ""));
				
			}
			for (PropertyDesignator des : pSkill.getPropertyDesignators()) {
				individual.addDataPropertyDesignator(new DataPropertyDesignator(convertToDataProperty(des),
						individual, des.getValue()));
			}
			individuals.add(individual);
		}
		
		for (ExecutableSkill exSkill : SkillproService.getSkillproProvider().getSkillRepo().getExecutableSkills()) {
			Individual individual = new Individual(exSkill.getName());

			individual.addType(new OntologyClass(EXECUTABLE_SKILL));
			for (ResourceExecutableSkill rexSkill : exSkill.getResourceExecutableSkills()) {
				Individual rexIndividual = new Individual(rexSkill.getName());

				rexIndividual.addType(new OntologyClass(RESOURCE_EXECUTABLE_SKILL));
				rexIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_TEMPLATE_SKILL), rexIndividual,
						new Individual(rexSkill.getTemplateSkill().getName())));
				rexIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_RESOURCE_SKILL), rexIndividual,
						new Individual(createResourceSkillName(rexSkill.getResourceSkill()))));
				rexIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRE_RESOURCE_CONFIGURATION),
						rexIndividual, new Individual(createResourceConfigurationName(rexSkill.getPreResourceConfiguration()))));
				rexIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_POST_RESOURCE_CONFIGURATION),
						rexIndividual, new Individual(createResourceConfigurationName(rexSkill.getPostResourceConfiguration()))));
				//product configurations
				
				//pre product configuration
				ProductConfiguration preProductConfiguration = rexSkill.getPreProductConfiguration();
				Individual preProductConfIndividual = new Individual(rexSkill.getName() + "_PreProductConfiguration");
				preProductConfIndividual.addType(new OntologyClass(PRODUCT_CONFIGURATION));
				
				individuals.add(preProductConfIndividual);
				//data property?
				if (preProductConfiguration != null && preProductConfiguration.getProductQuantities() != null) {
					for (ProductQuantity pq : preProductConfiguration.getProductQuantities()) {
						Individual pqIndidivual = new Individual(createProductQuantityName(pq));
						pqIndidivual.addType(new OntologyClass(PRODUCT_QUANTITY));
						
						preProductConfIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT_QUANTITY),
								preProductConfIndividual, pqIndidivual));
						individuals.add(pqIndidivual);
						//product + quantity
						pqIndidivual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT),
								pqIndidivual, new Individual(pq.getProduct().getName())));
						pqIndidivual.addDataPropertyDesignator(new DataPropertyDesignator(createDataProperty("Quantity",
								DataPropertyType.INTEGER), pqIndidivual, pq.getQuantity() + ""));
					}
				} else {
					preProductConfIndividual.addType(new OntologyClass(EMPTY_PRODUCT));
				}
				//post product configuration
				ProductConfiguration postProductConfiguration = rexSkill.getPreProductConfiguration();
				Individual postProductConfIndividual = new Individual(rexSkill.getName() + "_PostProductConfiguration");
				postProductConfIndividual.addType(new OntologyClass(PRODUCT_CONFIGURATION));
				
				individuals.add(postProductConfIndividual);
				//data property?
				if (postProductConfiguration != null && postProductConfiguration.getProductQuantities() != null) {
					for (ProductQuantity pq : postProductConfiguration.getProductQuantities()) {
						Individual pqIndidivual = new Individual(createProductQuantityName(pq));
						pqIndidivual.addType(new OntologyClass(PRODUCT_QUANTITY));
						
						postProductConfIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT_QUANTITY),
								postProductConfIndividual, pqIndidivual));
						individuals.add(pqIndidivual);
						//product + quantity
						pqIndidivual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRODUCT),
								pqIndidivual, new Individual(pq.getProduct().getName())));
						pqIndidivual.addDataPropertyDesignator(new DataPropertyDesignator(createDataProperty("Quantity",
								DataPropertyType.INTEGER), pqIndidivual, pq.getQuantity() + ""));
					}
				} else {
					preProductConfIndividual.addType(new OntologyClass(EMPTY_PRODUCT));
				}
				
				rexIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_PRE_PRODUCT_CONFIGURATION),
						rexIndividual, postProductConfIndividual));
				rexIndividual.addObjectPropertyDesignator(new ObjectPropertyDesignator(new ObjectProperty(HAS_POST_PRODUCT_CONFIGURATION),
						rexIndividual, postProductConfIndividual));
				individuals.add(rexIndividual);
			}
			individuals.add(individual);
		}
		return individuals;
	}
	
	private String createResourceSkillName(ResourceSkill rSkill) {
		return rSkill.getResource().getName() + "-" + rSkill.getName();
	}

	private String createResourceConfigurationName(ResourceConfiguration resourceConfiguration) {
		return resourceConfiguration.getResource().getName() + "-" + resourceConfiguration.getName();
	}
	
	private String createProductQuantityName(ProductQuantity productQuantity) {
		return productQuantity.getProduct().getName() + ":" + productQuantity.getQuantity();
	}
	
	private String createProductionSkillName(ProductionSkill productionSkill) {
		return "PSkill:" + productionSkill.getName();
	}
}
