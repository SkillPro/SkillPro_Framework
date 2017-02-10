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

package skillpro.model.update;

public enum UpdateType {
	NEW_DB_CONNECTION,
	NEW_DATA_IMPORTED,
	
	AML_MODEL_CREATED, 
	AML_MODEL_UPDATED, 
	AML_DOMAIN_CREATED, 
	AML_DOMAIN_DELETED, 
	AML_DOMAIN_UPDATED, 

	ASSET_CREATED, 
	ASSET_UPDATED, 
	ASSET_DELETED, 
	CONFIGURATION_UPDATED, 
	TOOL_UPDATED, 
	CATALOG_UPDATED,

	SKILL_CREATED, 
	SKILL_UPDATED, 
	SKILL_DELETED, 

	PRODUCT_CREATED,
	PRODUCT_UPDATED, 
	PRODUCT_DELETED, 

	PROPERTY_CREATED, 
	PROPERTY_UPDATED,
	
	ORDER_CREATED,
	ORDER_DELETED,
	
	EXECUTABLE_SKILLS_GENERATION_TRIED,
	EXECUTABLE_SKILLS_GENERATED,
	EXECUTABLE_SKILLS_TESTED,
	/**
	 * the REPO is completely updated with new entities
	 */
	SEE_IMPORTED, 
	
	/**
	 * new SEE are added to the REPO
	 */
	SEE_ADDED, 
	
	//old types
	// project related values
	/**
	 * Project loaded from file
	 */
	PROJECT_LOADED,
	/**
	 * Project saved to file, all dirty bits should be reset
	 */
	PROJECT_SAVED,
	
	/**
	 * The current project is switched. 
	 * Views should be updated!
	 */
	PROJECT_SWITCHED,
	
	/**
	 * LivingSolid Data loaded from XML-file
	 */
	LSDATA_LOADED,
	
	/**
	 * New factory copy is created and set active
	 */
	FACTORY_COPY_CREATED,
	/**
	 * The active factory copy is modified. 
	 * All views that show information about it should be updated! 
	 */
	FACTORY_COPY_MODIFIED,
	
	/**
	 * The layout of the active factory copy is modified. 
	 * All layout related views should be updated!
	 */
	FACTORY_COPY_LAYOUT_MODIFIED,
	
	/**
	 * The active factory copy is switched. 
	 * Views should be updated!
	 */
	FACTORY_COPY_SWITCHED, 
	
	/**
	 * The Perspective is switched. when using this UpdateType you should always give the current project as associated object
	 * so the views in the switched perspective should check if it has changed or not
	 */
	PERSPECTIVE_SWITCHED,
	
	KPI_UPDATED;
}
