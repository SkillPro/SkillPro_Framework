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

package skillpro.model.resourceprovider;

import ontology.model.Individual;
import ontology.model.OntologyClass;
import ontology.model.property.DataProperty;
import ontology.model.property.DataPropertyType;
import ontology.model.property.ObjectProperty;
import skillpro.model.repo.Repo;

public interface IOntologyProvider {
	public Repo<OntologyClass> getOntologyClassRepo();
	public Repo<DataProperty> getDataPropertyRepo();
	public Repo<ObjectProperty> getObjectPropertyRepo();
	public Repo<Individual> getIndividualRepo();
	
	//create
	public OntologyClass createOntologyClass(String name);
	public DataProperty createDataProperty(String name, DataPropertyType propertyType);
	public ObjectProperty createObjectProperty(String name);
	public Individual createIndividual(String name);

	//remove
	public void removeOntologyClass(OntologyClass ontologyClass);
	public void removeDataProperty(DataProperty dataProperty);
	public void removeObjectProperty(ObjectProperty objectProperty);
	public void removeIndividual(Individual individual);

	//update
	public void updateOntologyClass(OntologyClass ontologyClass);
	public void updateDataProperty(DataProperty dataProperty);
	public void updateObjectProperty(ObjectProperty objectProperty);
	public void updateIndividual(Individual individual);
	
	//etc
	public boolean isDirty();
	public void wipeAllData();
}
