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

package pscm.resourceprovider.local;

import ontology.model.Individual;
import ontology.model.OntologyClass;
import ontology.model.property.DataProperty;
import ontology.model.property.DataPropertyType;
import ontology.model.property.ObjectProperty;
import skillpro.model.repo.Repo;
import skillpro.model.resourceprovider.IOntologyProvider;

public class LocalOntologyProvider implements IOntologyProvider {
	private Repo<OntologyClass> ontologyClassRepo;
	private Repo<DataProperty> dataPropertyRepo;
	private Repo<ObjectProperty> objectPropertyRepo;
	private Repo<Individual> individualRepo;
	
	@Override
	public Repo<OntologyClass> getOntologyClassRepo() {
		if (ontologyClassRepo == null) {
			ontologyClassRepo = new Repo<>();
		}
		return ontologyClassRepo;
	}

	@Override
	public Repo<DataProperty> getDataPropertyRepo() {
		if (dataPropertyRepo == null) {
			dataPropertyRepo = new Repo<>();
		}
		return dataPropertyRepo;
	}

	@Override
	public Repo<ObjectProperty> getObjectPropertyRepo() {
		if (objectPropertyRepo == null) {
			objectPropertyRepo = new Repo<>();
		}
		return objectPropertyRepo;
	}

	@Override
	public Repo<Individual> getIndividualRepo() {
		if (individualRepo == null) {
			individualRepo = new Repo<>();
		}
		return individualRepo;
	}

	@Override
	public OntologyClass createOntologyClass(String name) {
		OntologyClass ontologyClass = new OntologyClass(name);
		getOntologyClassRepo().add(ontologyClass);
		//no update service
		return ontologyClass;
	}

	@Override
	public DataProperty createDataProperty(String name,
			DataPropertyType propertyType) {
		DataProperty dataProperty = new DataProperty(name, propertyType);
		getDataPropertyRepo().add(dataProperty);
		return dataProperty;
	}

	@Override
	public ObjectProperty createObjectProperty(String name) {
		ObjectProperty objectProperty = new ObjectProperty(name);
		getObjectPropertyRepo().add(objectProperty);
		return objectProperty;
	}

	@Override
	public Individual createIndividual(String name) {
		Individual indivudual = new Individual(name);
		getIndividualRepo().add(indivudual);
		return indivudual;
	}

	@Override
	public void removeOntologyClass(OntologyClass ontologyClass) {
		getOntologyClassRepo().remove(ontologyClass);
		
	}

	@Override
	public void removeDataProperty(DataProperty dataProperty) {
		getDataPropertyRepo().remove(dataProperty);
		
	}

	@Override
	public void removeObjectProperty(ObjectProperty objectProperty) {
		getObjectPropertyRepo().remove(objectProperty);
		
	}

	@Override
	public void removeIndividual(Individual individual) {
		getIndividualRepo().remove(individual);
	}

	@Override
	public void updateOntologyClass(OntologyClass ontologyClass) {
	}

	@Override
	public void updateDataProperty(DataProperty dataProperty) {
	}

	@Override
	public void updateObjectProperty(ObjectProperty objectProperty) {
	}

	@Override
	public void updateIndividual(Individual individual) {
	}

	@Override
	public boolean isDirty() {
		return !getOntologyClassRepo().isEmpty() || !getDataPropertyRepo().isEmpty() 
				|| !getObjectPropertyRepo().isEmpty() || !getIndividualRepo().isEmpty();
	}

	@Override
	public void wipeAllData() {
		getOntologyClassRepo().wipeAllData();
		getDataPropertyRepo().wipeAllData();
		getObjectPropertyRepo().wipeAllData();
		getIndividualRepo().wipeAllData();
	}
}
