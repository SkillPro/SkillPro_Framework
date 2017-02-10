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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ontology.model.OntologyClass;
import ontology.model.property.DataProperty;
import ontology.model.property.ObjectProperty;
import ontology.model.property.PropertyConstraint;
import ontology.model.property.PropertyConstraintNominal;
import ontology.model.property.PropertyConstraintOrdinal;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import skillpro.model.service.SkillproService;
import eu.skillpro.ams.ontology.util.OntologyUtil;


public class TBoxExporter {
	private OWLOntology ontology;
	private OWLOntologyManager ontologyManager;
	private OWLDataFactory dataFactory;
	
	public TBoxExporter() {
		dataFactory = OntologyUtil.getInstance().getDataFactory();
		ontology = OntologyUtil.getInstance().getOntology();
		ontologyManager = OntologyUtil.getInstance().getOntologyManager();
		

	}
	
	private void exportOntologyClasses(List<OntologyClass> ontologyClasses) {
		for (OntologyClass ontClass : ontologyClasses) {
			if (!ontClass.getParents().isEmpty()) {
				for (OntologyClass parent : ontClass.getParents()) {
					OntologyUtil.convertToOntologyClass(ontClass, parent);
				}
			} else {
				OntologyUtil.convertToOntologyClass(ontClass, null);
			}
		}
	}
	
	private void exportObjectProperties(List<ObjectProperty> objectProperties) {
		for (ObjectProperty objectProperty : objectProperties) {
			if (!objectProperty.getParents().isEmpty()) {
				List<OWLOntologyChange> changes = new ArrayList<>();
				OWLObjectProperty owlObjectProp = OntologyUtil.getOWLObjectProperty(objectProperty);
				
				for (OntologyClass domain : objectProperty.getDomains()) {
					OWLAxiom attPropAxiom = dataFactory.getOWLObjectPropertyDomainAxiom(owlObjectProp, OntologyUtil.getOWLClass(domain));
					AddAxiom addPropAxiom = new AddAxiom(ontology, attPropAxiom);
					changes.add(addPropAxiom);
				}
				for (ObjectProperty parent : objectProperty.getParents()) {
					OWLAxiom subPropOfAxiom = dataFactory.getOWLSubObjectPropertyOfAxiom(owlObjectProp, OntologyUtil.getOWLObjectProperty(parent));
					AddAxiom addSubPropAxiom = new AddAxiom(ontology, subPropOfAxiom);
					ontologyManager.applyChange(addSubPropAxiom);
				}
				
				ontologyManager.applyChanges(changes);
			}
		}
	}
	
	private void exportDataProperties(List<DataProperty> dataProperties) {
		for (DataProperty dataProperty : dataProperties) {
			if (!dataProperty.getParents().isEmpty()) {
				List<OWLOntologyChange> changes = new ArrayList<>();
				OWLDataProperty owlDataProp = OntologyUtil.getOWLDataProperty(dataProperty);
				for (OntologyClass domain : dataProperty.getDomains()) {
					OWLAxiom attPropAxiom = dataFactory.getOWLDataPropertyDomainAxiom(owlDataProp, OntologyUtil.getOWLClass(domain));
					AddAxiom addPropAxiom = new AddAxiom(ontology, attPropAxiom);
					changes.add(addPropAxiom);
					
				}
				//creates the sub properties for def
				OWLDataRange dataRange = OntologyUtil.getOWLDataType(dataProperty);
				OWLAxiom dataAxiom = dataFactory.getOWLDataPropertyRangeAxiom(owlDataProp, dataRange);
				AddAxiom addDataRangeAxiom = new AddAxiom(ontology, dataAxiom);
				changes.add(addDataRangeAxiom);
				if (!dataProperty.getPropertyConstraints().isEmpty()) {
					Set<OWLLiteral> literals = getOWLPossibleValues(dataProperty);
					if (!literals.isEmpty()) {
						OWLDataRange literalRange = dataFactory.getOWLDataOneOf(literals);
						OWLAxiom literalRangeAxiom = dataFactory.getOWLDataPropertyRangeAxiom(owlDataProp, literalRange);
						AddAxiom addLiteralRangeAxiom = new AddAxiom(ontology, literalRangeAxiom);
						
						changes.add(addLiteralRangeAxiom);
					}
				}
				ontologyManager.applyChanges(changes);
				
				for (DataProperty parent : dataProperty.getParents()) {
					OWLAxiom subPropOfAxiom = dataFactory.getOWLSubDataPropertyOfAxiom(owlDataProp, OntologyUtil.getOWLDataProperty(parent));
					AddAxiom addSubPropAxiom = new AddAxiom(ontology, subPropOfAxiom);
					ontologyManager.applyChange(addSubPropAxiom);
				}
			}
		}
	}
	
	private Set<OWLLiteral> getOWLPossibleValues(DataProperty dataProperty) {
		Set<OWLLiteral> owlPossibleValues = new HashSet<>();
		
		for (PropertyConstraint cons : dataProperty.getPropertyConstraints()) {
			if (cons instanceof PropertyConstraintNominal) {
				for (String value : ((PropertyConstraintNominal) cons).getValues()) {
					owlPossibleValues.add(OntologyUtil.getOWLLiteral(value));
				}
			} else if (cons instanceof PropertyConstraintOrdinal) {
				//FIXME do something
				System.out.println("Please implement property constraint ordinal");
			}
		}
		return owlPossibleValues;
	}
	
	public void exportToOntology() {
		System.out.println("Exporting ontology classes");
		exportOntologyClasses(SkillproService.getOntologyProvider().getOntologyClassRepo().getEntities());
		System.out.println("Exporting data properties");
		exportDataProperties(SkillproService.getOntologyProvider().getDataPropertyRepo().getEntities());
		System.out.println("Exporting object properties");
		exportObjectProperties(SkillproService.getOntologyProvider().getObjectPropertyRepo().getEntities());
	}
}
