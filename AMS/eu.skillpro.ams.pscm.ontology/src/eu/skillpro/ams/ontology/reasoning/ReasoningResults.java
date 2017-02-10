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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author <a href="mailto:kiril.tonev@kit.edu">Kiril Tonev</a>
 */
public class ReasoningResults {
    protected final Map<OWLNamedIndividual, List<String>> results;

    public ReasoningResults() {
        this.results = new HashMap<OWLNamedIndividual, List<String>>();
    }

    protected boolean addResult(OWLNamedIndividual individual, String message) {
        boolean mapModified;
        List<String> bucket;
        if (results.containsKey(individual)) {
            bucket = results.get(individual);
            mapModified = false;
        } else {
            bucket = new ArrayList<String>();
            results.put(individual, bucket);
            mapModified = true;
        }
        bucket.add(message);
        return mapModified;
    }

    public Map<OWLNamedIndividual, List<String>> getResults() {
        return Collections.unmodifiableMap(results);
    }

    public Set<OWLNamedIndividual> getIndividuals() {
        return results.keySet();
    }

    public List<String> getMessages(OWLIndividual individual) {
        if (results.containsKey(individual)) {
            return Collections.unmodifiableList(results.get(individual));
        } else {
            return Collections.emptyList();
        }
    }
}
