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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.Node;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import eu.skillpro.ams.ontology.util.OntologyUtil;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class RulesExecutor {

    protected final OntologyUtil ontologyContext;

    public RulesExecutor(OntologyUtil ontologyContext) {
        this.ontologyContext = ontologyContext;
    }

    protected Set<OWLClass> inferenceClasses() {
        Set<OWLClass> result = new HashSet<OWLClass>();

        for (SWRLRule rule : ontologyContext.getRules()) {
            for (SWRLAtom swrlAtom : rule.getHead()) {
                if (swrlAtom instanceof SWRLClassAtom) {
                    SWRLClassAtom classAtom = (SWRLClassAtom) swrlAtom;
                    result.add(classAtom.getClassesInSignature().iterator().next());
                }
            }
        }

        return result;
    }

    public List<ClassMembership> inferClassMemberships() {
        List<ClassMembership> classMemberships = new ArrayList<ClassMembership>();

        PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(ontologyContext.getOntology());
        reasoner.precomputeInferences();

        for (OWLClass inferenceClass : inferenceClasses()) {
            for (Node<OWLNamedIndividual> individualNode : reasoner.getInstances(inferenceClass, true)) {
                for (OWLNamedIndividual individual : individualNode.getEntities()) {
                    classMemberships.add(new ClassMembership(individual, inferenceClass));
                }
            }
        }

        return classMemberships;
    }
}
