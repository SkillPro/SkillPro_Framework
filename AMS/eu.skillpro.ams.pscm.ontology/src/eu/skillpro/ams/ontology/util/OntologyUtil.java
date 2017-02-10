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

package eu.skillpro.ams.ontology.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ontology.model.Individual;
import ontology.model.OntologyClass;
import ontology.model.property.DataProperty;
import ontology.model.property.DataPropertyType;
import ontology.model.property.ObjectProperty;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import eu.skillpro.ams.ontology.ABoxExporter;
import eu.skillpro.ams.ontology.TBoxExporter;
import eu.skillpro.ams.ontology.reasoning.ClassMembership;
import eu.skillpro.ams.ontology.reasoning.InferencesRenderer;
import eu.skillpro.ams.ontology.reasoning.ReasoningResults;
import eu.skillpro.ams.ontology.reasoning.RulesExecutor;

public class OntologyUtil {
	public static final String REPLACEMENT_FOR_SPACE_CHARACTER = "XXX";
	
	private static final String DEFAULT_PATHNAME = "skillpro.owl";
	private File iriFile;
	private IRI skillproIRI;
	private OWLDataFactory dataFactory;
	private OWLOntologyManager ontologyManager;
	private OWLOntology ontology;
	
	private static final OntologyUtil INSTANCE = new OntologyUtil();
	
	private OntologyUtil() {
		dataFactory = OWLManager.getOWLDataFactory();
		iriFile = createIRIFile(DEFAULT_PATHNAME);
		
		skillproIRI = IRI.create(iriFile);
		ontologyManager = createOntologyManager(new OWLManager());
		try {
			ontology = ontologyManager.createOntology(skillproIRI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public static OntologyUtil getInstance() {
		return INSTANCE;
	}
	
	public static OntologyUtil resetOntology() {
		INSTANCE.dataFactory = OWLManager.getOWLDataFactory();
		INSTANCE.ontologyManager = INSTANCE.createOntologyManager(new OWLManager());
		try {
			INSTANCE.ontology = INSTANCE.ontologyManager.createOntology(INSTANCE.skillproIRI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		return INSTANCE;
	}
	
	 public static void loadAndExport(String filepath) throws OWLOntologyCreationException {
		 resetOntology();
		 INSTANCE.ontologyManager = OWLManager.createOWLOntologyManager();
		 INSTANCE.ontology = INSTANCE.ontologyManager.loadOntologyFromOntologyDocument(new File(filepath));
		 
		 INSTANCE.iriFile = createIRIFile(filepath);
		 INSTANCE.skillproIRI = IRI.create(INSTANCE.iriFile);
		 System.out.println("Exporting TBox");
		 TBoxExporter tBox = new TBoxExporter();
		 tBox.exportToOntology();
		 
		 System.out.println("Exporting ABox");
		 ABoxExporter aBox = new ABoxExporter();
		 aBox.exportIndividuals();
		 
		 System.out.println("no rules yet");
		 //rule here
		 //TODO
		 System.out.println("Saving ontology");
		 try {
			 saveOntology();
			 System.out.println("Ontology saved.");
		 } catch (OWLOntologyStorageException e) {
			 e.printStackTrace();
		 }
	 }
	
	public static void setNewIRI(String pathname) {
		INSTANCE.iriFile = createIRIFile(pathname);
		
		INSTANCE.skillproIRI = IRI.create(INSTANCE.iriFile);
		INSTANCE.ontologyManager = INSTANCE.createOntologyManager(new OWLManager());
		try {
			INSTANCE.ontology = INSTANCE.ontologyManager.createOntology(INSTANCE.skillproIRI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	public File getIriFile() {
		return iriFile;
	}
	
	public IRI getSkillProIRI() {
		return skillproIRI;
	}
	
	public OWLDataFactory getDataFactory() {
		return dataFactory;
	}
	
	public OWLOntologyManager getOntologyManager() {
		return ontologyManager;
	}
	
	public OWLOntology getOntology() {
		return ontology;
	}
	
	private static File createIRIFile(String pathname) {
		File iriFile = new File(pathname);
		if(iriFile.exists()) {
			iriFile.delete();
		}
		try {
			iriFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return iriFile;
	}
	
	private OWLClass getOWLClass(String className) {
		return INSTANCE.dataFactory.getOWLClass(IRI.create(INSTANCE.skillproIRI + "#" + INSTANCE.convertExistenceOperatorToState(INSTANCE.encodeString(className))));
	}
	
	public static OWLClass getOWLClass(OntologyClass ontologyClass) {
		if (ontologyClass == null) {
			return INSTANCE.dataFactory.getOWLThing();
		}
		return INSTANCE.getOWLClass(ontologyClass.getName());
	}
	
	private OWLDataProperty getOWLDataProperty(String dataPropertyName) {
		//done this way cos the attribute assignments having possible values is not possible to be mapped with just 1 single data property.
		return INSTANCE.dataFactory.getOWLDataProperty(IRI.create(INSTANCE.skillproIRI + "#" + INSTANCE.encodeString(dataPropertyName.substring(0, 1).toLowerCase() + 
				dataPropertyName.substring(1))));
	}
	
	public static OWLDataProperty getOWLDataProperty(DataProperty property) {
		//done this way cos the attribute assignments having possible values is not possible to be mapped with just 1 single data property.
		return INSTANCE.getOWLDataProperty(property.getName());
	}
	
	private OWLObjectProperty getOWLObjectProperty(String objectPropertyName) {
		return INSTANCE.dataFactory.getOWLObjectProperty(IRI.create(INSTANCE.skillproIRI + "#" + INSTANCE.encodeString(objectPropertyName.substring(0, 1).toLowerCase() + 
				objectPropertyName.substring(1))));
	}
	
	public static OWLObjectProperty getOWLObjectProperty(ObjectProperty property) {
		return INSTANCE.getOWLObjectProperty(property.getName());
	}
	
	private OWLNamedIndividual getOWLNamedIndividual(String individualName) {
		return INSTANCE.dataFactory.getOWLNamedIndividual(IRI.create(INSTANCE.skillproIRI + "#_" + INSTANCE.encodeString(individualName)));
	}
	
	public static OWLNamedIndividual getOWLNamedIndividual(Individual individual) {
		return INSTANCE.getOWLNamedIndividual(individual.getName());
	}
	
	public static OWLLiteral getOWLLiteral(String value) {
		OWLLiteral literal = null;
		if (INSTANCE.isBoolean(value)) {
			literal = INSTANCE.dataFactory.getOWLLiteral(Boolean.parseBoolean(value));
		} else if (INSTANCE.isInteger(value)) {
			literal = INSTANCE.dataFactory.getOWLLiteral(Integer.parseInt(value));
		} else if (INSTANCE.isDouble(value)) {
			literal = INSTANCE.dataFactory.getOWLLiteral(Double.parseDouble(value));
		} else if (INSTANCE.isFloat(value)) {
			literal = INSTANCE.dataFactory.getOWLLiteral(Double.parseDouble(value));
		} else {
			literal = INSTANCE.dataFactory.getOWLLiteral(INSTANCE.encodeString(value));
			
		}
		return literal;
	}
	
	public static OWLDataRange getOWLDataType(DataProperty prop) {
		DataPropertyType type = prop.getPropertyType();
		if (type.equals(DataPropertyType.BOOLEAN)) {
			return INSTANCE.dataFactory.getBooleanOWLDatatype();
		} else if (type.equals(DataPropertyType.INTEGER)) {
			return INSTANCE.dataFactory.getIntegerOWLDatatype();
		} else if (type.equals(DataPropertyType.DOUBLE)) {
			return INSTANCE.dataFactory.getDoubleOWLDatatype();
		} else {
			return INSTANCE.dataFactory.getOWLDatatype(IRI.create("http://www.w3.org/2001/XMLSchema#string"));
		}
	}
	
	public static OWLLiteral getOWLLiteral(String value, DataPropertyType valueType) {
		OWLLiteral literal = null;
		if (valueType.equals(DataPropertyType.BOOLEAN)) {
			if (value != null && !value.equals("")) {
				literal = INSTANCE.dataFactory.getOWLLiteral(Boolean.parseBoolean(value));
			} else {
				INSTANCE.dataFactory.getOWLLiteral(false);
			}
		} else if (valueType.equals(DataPropertyType.INTEGER)) {
			if (value != null && !value.equals("")) {
				literal = INSTANCE.dataFactory.getOWLLiteral(Integer.parseInt(value));
			} else {
				literal = INSTANCE.dataFactory.getOWLLiteral((Integer) 0);
			}
		} else if (valueType.equals(DataPropertyType.DOUBLE)) {
			if (value != null && !value.equals("")) {
				literal = INSTANCE.dataFactory.getOWLLiteral(Double.parseDouble(value));
			} else {
				literal = INSTANCE.dataFactory.getOWLLiteral(Double.parseDouble("0"));
			}
		} else {
			if (value != null) {
				literal = INSTANCE.dataFactory.getOWLLiteral(INSTANCE.encodeString(value));
			} else {
				literal = INSTANCE.dataFactory.getOWLLiteral(INSTANCE.encodeString(""));
			}
		}
		return literal;
	}
	
	public static IRI getBuiltInOperatorIRI(String operatorName) {
		//only converts the ones with similar names as the built in operators.
		if (operatorName.equalsIgnoreCase("greater_than") || operatorName.equalsIgnoreCase("greaterthan")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#greaterThan");
		} else if (operatorName.equalsIgnoreCase("greater_than_or_equal") || operatorName.equalsIgnoreCase("greaterthanorequal")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#greaterThanOrEqual");
		} else if (operatorName.equalsIgnoreCase("less_than") || operatorName.equalsIgnoreCase("lessthan")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#lessThan");
		} else if (operatorName.equalsIgnoreCase("less_than_or_equal") || operatorName.equalsIgnoreCase("lessthanorequal")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#lessThanOrEqual");
		} else if (operatorName.equalsIgnoreCase("equal")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#equal");
		} else if (operatorName.equalsIgnoreCase("not_equal") || operatorName.equalsIgnoreCase("notequal")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#notEqual");
		} else if (operatorName.equalsIgnoreCase("subtract")) {
			return IRI.create("http://www.w3.org/2003/11/swrlb#subtract");
		} else {
			return IRI.create(INSTANCE.convertExistenceOperatorToState(operatorName));
		}
		
	}
	
	//new util class?
	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private boolean isBoolean(String s) {
		if (s != null && (s.toLowerCase().equals("true") || s.toLowerCase().equals("false"))) {
			return true;
		}
		return false;
	}
	
	public static SWRLVariable getSWRLVariable(String varName) {
		return INSTANCE.dataFactory.getSWRLVariable(IRI.create(INSTANCE.skillproIRI + "#" + INSTANCE.encodeString(varName)));
	}
	
	public static IRI createIRI(String name) {
		
		return IRI.create(INSTANCE.skillproIRI + "#" + INSTANCE.encodeString(name));
	}
	
	public static SWRLVariable getSWRLVariableFromDataProperty(String className, String varName) {
		return INSTANCE.dataFactory.getSWRLVariable(IRI.create(INSTANCE.skillproIRI + "#" + INSTANCE.encodeString(className) 
				+ "_" + INSTANCE.encodeString(varName)));
	}
	
	public static SWRLLiteralArgument getSWRLLiteralArgument(String value, DataPropertyType valueType) {
		return INSTANCE.dataFactory.getSWRLLiteralArgument(getOWLLiteral(value, valueType));
	}
	
	public static OWLClass convertToOntologyClass(OntologyClass ontologyClass, OntologyClass superClass) {
		List<OWLOntologyChange> changes = new ArrayList<>();
		OWLClass ontClass = OntologyUtil.getOWLClass(ontologyClass);
		OWLAxiom axiom = INSTANCE.dataFactory
				.getOWLSubClassOfAxiom(ontClass, OntologyUtil.getOWLClass(superClass));
		OWLAxiom thing = INSTANCE.dataFactory.getOWLSubClassOfAxiom(ontClass,
				INSTANCE.dataFactory.getOWLThing());
		AddAxiom addAxiom = new AddAxiom(INSTANCE.ontology, axiom);
		changes.add(addAxiom);
		if (superClass != null) {
			RemoveAxiom removeAxiom = new RemoveAxiom(INSTANCE.ontology, thing);
			changes.add(removeAxiom);
		}
		INSTANCE.ontologyManager.applyChanges(changes);
		return ontClass;
	}
	
	public static void saveOntology() throws OWLOntologyStorageException {
		OWLOntologyFormat format = INSTANCE.ontologyManager.getOntologyFormat(INSTANCE.ontology);
		RDFXMLOntologyFormat rdfxmlFormat = new RDFXMLOntologyFormat();
        if (format.isPrefixOWLOntologyFormat()) {
            rdfxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        INSTANCE.ontologyManager.saveOntology(INSTANCE.ontology, rdfxmlFormat, INSTANCE.skillproIRI);
	}
	
	public static void saveOntology(IRI iri) throws OWLOntologyStorageException {
		OWLOntologyFormat format = INSTANCE.ontologyManager.getOntologyFormat(INSTANCE.ontology);
		RDFXMLOntologyFormat rdfxmlFormat = new RDFXMLOntologyFormat();
        if (format.isPrefixOWLOntologyFormat()) {
            rdfxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        INSTANCE.ontologyManager.saveOntology(INSTANCE.ontology, rdfxmlFormat, iri);
	}

	private OWLOntologyManager createOntologyManager(OWLManager manager) {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		ontologyManager.addIRIMapper(new AutoIRIMapper(new File(
				"materializedOntologies"), true));
		return ontologyManager;
	}

	public Set<SWRLRule> getRules() {
        Set<SWRLRule> rules = new HashSet<SWRLRule>();
        for (OWLAxiom axiom : ontology.getAxioms()) {
            if (axiom instanceof SWRLRule) {
                rules.add((SWRLRule) axiom);
            }
        }
        return rules;
    }
	
	private String encodeString(String s) {
		String encodedString = "";
		encodedString = s.replaceAll("\\s+", REPLACEMENT_FOR_SPACE_CHARACTER);
		return encodedString;
	}
	
	private String convertExistenceOperatorToState(String name) {
		if (name.equalsIgnoreCase("exists")) {
			return "isActive";
		} else if (name.equalsIgnoreCase("does_not_exist")) {
			return "isInactive";
		} else if (name.equalsIgnoreCase("rejects")) {
			return "Rejected";
		
		} else if (name.equalsIgnoreCase("requires")) {
			return "Required";
		
		} else {
			return name;
		}
	}
	
	public ReasoningResults interpretOntology(InferencesRenderer renderer) {
		RulesExecutor rulesExecutor = new RulesExecutor(this);
		List<ClassMembership> classMemberships = rulesExecutor.inferClassMemberships();
        ReasoningResults reasoningResults = renderer.renderInferences(classMemberships);
		return reasoningResults;
	}
}
