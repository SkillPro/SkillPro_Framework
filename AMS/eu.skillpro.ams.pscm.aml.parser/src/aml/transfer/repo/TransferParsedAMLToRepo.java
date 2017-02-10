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

package aml.transfer.repo;

import java.util.Set;

import aml.amlparser.AMLParser;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.transformation.providers.IAMLProvider;
import aml.transformation.service.AMLTransformationService;

public class TransferParsedAMLToRepo {
	private static final TransferParsedAMLToRepo INSTANCE = new TransferParsedAMLToRepo();
	
	private TransferParsedAMLToRepo() {
	}
	
	public void transferToAMLRepoFromAMLParser() {
		Set<Object> parsedObjects = AMLParser.getInstance().getParsedObjects();
		transferToAMLRepo(parsedObjects);
	}
	
	public static TransferParsedAMLToRepo getInstance() {
		return INSTANCE;
	}

	private void transferToAMLRepo(Set<Object> objects) {
		IAMLProvider amlProvider = AMLTransformationService.getAMLProvider();
		for (Object obj : objects) {
			if (obj instanceof Interface) {
				amlProvider.storeInterface((Interface) obj);
			} else if (obj instanceof Role) {
				amlProvider.storeRole((Role) obj);
			} else if (obj instanceof InternalElement) {
				amlProvider.storeInternalElement((InternalElement) obj);
			} else if (obj instanceof SystemUnit) {
				amlProvider.storeSystemUnit((SystemUnit) obj);
			} else if (obj instanceof Root<?>) {
				amlProvider.storeRoot((Root<?>) obj);
			} else if (obj instanceof Hierarchy<?>) {
				amlProvider.storeHierarchy((Hierarchy<?>) obj);
			} else {
				//do nothing
			}
		}
	}
}
