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

import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import eu.skillpro.ams.ontology.util.OntologyUtil;


public class OntologyExporter {
	private static final OntologyExporter INSTANCE = new OntologyExporter();
	
	private OntologyExporter() {
	}
	
	public static OntologyExporter getInstance() {
		return INSTANCE;
	}
	
	public void export(String filepath) {
		System.out.println("===========================");
		System.out.println("Resetting ontology");
		System.out.println("===========================");
		OntologyUtil.resetOntology();
		System.out.println("Ontology will be saved at: " + filepath);
		OntologyUtil.setNewIRI(filepath);
		
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
			OntologyUtil.saveOntology();
			System.out.println("Ontology saved.");
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
}
