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

package eu.skillpro.ams.ontology;

import java.util.ArrayList;
import java.util.List;

import ontology.model.Individual;
import ontology.model.OntologyClass;
import ontology.model.property.DataPropertyDesignator;
import ontology.model.property.ObjectPropertyDesignator;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import skillpro.model.service.SkillproService;
import eu.skillpro.ams.ontology.util.OntologyUtil;


public class ABoxExporter {
	private OWLOntologyManager ontologyManager;
	private OWLDataFactory dataFactory;
	private OWLOntology ontology;
	
	public ABoxExporter() {
		dataFactory = OntologyUtil.getInstance().getDataFactory();
		ontology = OntologyUtil.getInstance().getOntology();
		ontologyManager = OntologyUtil.getInstance().getOntologyManager();
	}
	
	public void exportIndividuals() {
		for (Individual individual : SkillproService.getOntologyProvider().getIndividualRepo()) {
			assignClassesToIndividual(individual);
			assignDataPropertyAssertions(individual);
			assignObjectPropertyAssertions(individual);
		}
	}
	
	private void assignDataPropertyAssertions(Individual individual) {
		List<OWLAxiom> axioms = new ArrayList<>();
		for (DataPropertyDesignator des : individual.getDataPropertyDesignators()) {
			if (des.getProperty() != null) {
				OWLAxiom valueAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(
						OntologyUtil.getOWLDataProperty(des.getProperty()),
						OntologyUtil.getOWLNamedIndividual(individual), OntologyUtil.getOWLLiteral(des.getValue(), des.getProperty().getPropertyType()));
				
				axioms.add(valueAxiom);
			}
		}
		
		List<AddAxiom> addAxioms = new ArrayList<>();
		for (OWLAxiom axiom : axioms) {
			addAxioms.add(new AddAxiom(ontology, axiom));
		}
		
		ontologyManager.applyChanges(addAxioms);
	}
	
	private void assignObjectPropertyAssertions(Individual individual) {
		List<OWLAxiom> axioms = new ArrayList<>();
		for (ObjectPropertyDesignator des : individual.getObjectPropertyDesignators()) {
			OWLAxiom valueAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(
					OntologyUtil.getOWLObjectProperty(des.getProperty()),
					OntologyUtil.getOWLNamedIndividual(individual), OntologyUtil.getOWLNamedIndividual(des.getValue()));
			
			axioms.add(valueAxiom);
		}
		List<AddAxiom> addAxioms = new ArrayList<>();
		for (OWLAxiom axiom : axioms) {
			addAxioms.add(new AddAxiom(ontology, axiom));
		}
		ontologyManager.applyChanges(addAxioms);
	}
	
	private void assignClassesToIndividual(Individual individual) {
		List<OWLAxiom> axioms = new ArrayList<>();
		for (OntologyClass type : individual.getTypes()) {
			OWLAxiom axiom = dataFactory.getOWLClassAssertionAxiom(OntologyUtil.getOWLClass(type), OntologyUtil.getOWLNamedIndividual(individual));
			axioms.add(axiom);
		}
		List<AddAxiom> addAxioms = new ArrayList<>();
		for (OWLAxiom axiom : axioms) {
			addAxioms.add(new AddAxiom(ontology, axiom));
		}
		
		ontologyManager.applyChanges(addAxioms);
	}
}
