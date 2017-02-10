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

package eu.skillpro.ams.ontology.reasoning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * Encapsulates the objects relevant for ontology manipulation and query.
 * <p>
 * Exposes a manager, factory and (a structural) reasoner associated with an ontology.
 * </p>
 * <p>
 * Provides shorthands for accessing individuals, properties and classes in the namespace of the ontology document.
 * </p>
 *
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 * @see org.semanticweb.owlapi.model.OWLOntology
 * @see org.semanticweb.owlapi.model.OWLOntologyManager
 * @see org.semanticweb.owlapi.model.OWLDataFactory
 * @see org.semanticweb.owlapi.reasoner.OWLReasoner
 */
public class OntologyContext {

    private final IRI ontologyIRI;
    private final IRI documentIRI;
    private final OWLOntology ontology;
    private final OWLOntologyManager manager;
    private final OWLDataFactory factory;

    private OWLReasoner reasoner;

    /**
     * Default constructor.
     *
     * @param ontologyFile ontology document.
     * @throws OWLOntologyCreationException in case of ontology parsing error.
     */
    public static OntologyContext load(File ontologyFile) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        return new OntologyContext(ontologyFile, manager, ontology);
    }

    public static OntologyContext create(File ontologyFile, IRI ontologyIRI) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI documentIRI = IRI.create(ontologyFile);
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
        manager.addIRIMapper(mapper);
        return new OntologyContext(ontologyFile, manager, manager.createOntology(ontologyIRI));
    }

    private OntologyContext(File ontologyFile, OWLOntologyManager manager, OWLOntology ontology) {
        this.manager = manager;
        this.ontology = ontology;
        this.factory = manager.getOWLDataFactory();
        this.reasoner = new StructuralReasonerFactory().createReasoner(ontology);
        this.ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        this.documentIRI = IRI.create(ontologyFile);
    }

    public IRI getOntologyIRI() {
        return ontologyIRI;
    }

    public IRI getDocumentIRI() {
        return documentIRI;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public OWLDataFactory getFactory() {
        return factory;
    }

    public void setReasoner(OWLReasonerFactory factory) {
        this.reasoner = factory.createReasoner(ontology);
    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public Set<String> individuals() {
        Set<String> individualShortNames = new HashSet<String>();

        for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
            individualShortNames.add(individual.getIRI().getFragment());
        }

        return individualShortNames;
    }

    public boolean containsIndividual(String name) {
        IRI individualIRI = IRI.create(ontologyIRI.toString(), resource(name));
        return ontology.containsIndividualInSignature(individualIRI);
    }

    /**
     * Returns the first class in the declaration of an individual.
     *
     * @param shortName <em>short</em> individual name. Must not be <code>null</code>.
     * @return <code>null</code>, if the individual could not be found in the ontology signature, or no types declared.
     * Else the short class name.
     */
    public String getRepresentativeClass(String shortName) {
        if (containsIndividual(shortName)) {
            Set<OWLClass> classes = reasoner.getTypes(factory.getOWLNamedIndividual(iri(shortName)), true).getFlattened();
            if (classes.isEmpty()) {
                return null;
            } else {
                return classes.iterator().next().getIRI().getFragment();
            }
        } else {
            return null;
        }
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

    public OWLNamedIndividual getIndividual(String name) {
        return factory.getOWLNamedIndividual(iri(name));
    }

    public OWLDataProperty getDataProperty(String name) {
        return factory.getOWLDataProperty(iri(name));
    }

    public OWLObjectProperty getObjectProperty(String name) {
        return factory.getOWLObjectProperty(iri(name));
    }

    public OWLClass getOWLClass(String name) {
        return factory.getOWLClass(iri(name));
    }

    public List<OWLOntologyChange> addRule(SWRLRule rule) {
        return manager.applyChange(new AddAxiom(ontology, rule));
    }

    /**
     * Persists changes in the ontology.
     *
     * @throws OWLOntologyStorageException
     */
    public void saveOntology() throws OWLOntologyStorageException {
        manager.saveOntology(ontology);
    }

    /**
     * Saves the ontology into another file.
     *
     * @param file
     * @throws OWLOntologyStorageException
     * @throws FileNotFoundException       if the given file is a directory, and not a file.
     */
    public void saveOntologyAs(File file) throws OWLOntologyStorageException, FileNotFoundException {
        saveOntologyAs(file, manager.getOntologyFormat(ontology));
    }

    /**
     * Saves the ontology into another file.
     *
     * @param file   output file path.
     * @param format the ontology format to use.
     * @throws OWLOntologyStorageException
     * @throws FileNotFoundException       if the given file is a directory, and not a file.
     */
    public void saveOntologyAs(File file, OWLOntologyFormat format) throws OWLOntologyStorageException, FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(file);
        manager.saveOntology(ontology, format, outputStream);
    }

    /**
     * Returns an IRI formed by prepending the ontology IRI to the resource name.
     *
     * @param resourceName short name of the resource (e.g. class, property, individual).
     * @return resource IRI.
     */
    public IRI iri(String resourceName) {
        return IRI.create(ontologyIRI.toString(), resource(resourceName));
    }

    private String resource(String resourceName) {
        return "#" + resourceName;
    }

    public OWLOntologyFormat getOntologyFormat() {
        return manager.getOntologyFormat(ontology);
    }

    public String getOntologyNamespace() {
        return getOntologyIRI().toString() + "#";
    }
}
