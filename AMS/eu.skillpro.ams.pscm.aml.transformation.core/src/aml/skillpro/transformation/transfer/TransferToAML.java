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

package aml.skillpro.transformation.transfer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import transformation.interfaces.ITransformable;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.ISEETransformable;
import aml.transformation.providers.IAMLProvider;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

public class TransferToAML {
	private final static TransferToAML INSTANCE = new TransferToAML();
	
	private TransferToAML() {
	}
	
	public static TransferToAML getInstance() {
		return INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static void transferToAMLRepo() {
		IAMLProvider amlProvider = AMLTransformationService.getAMLProvider();
		Root<InternalElement> defaultRoot = amlProvider.createRoot("Configuration", InternalElement.class);
		Hierarchy<InternalElement> seeGroupHie = INSTANCE.createSEEGroup();
		defaultRoot.addChild(seeGroupHie);
		
		TransformationRepo transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
		Collection<Object> objects = transformationRepo.getReverseTransformedObjectsMap().values();
		Map<Object, Class<? extends ITransformable>> interfaceTransformablesMapping = transformationRepo.getInterfaceTransformablesMapping();

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
				Hierarchy<?> hie = (Hierarchy<?>) obj;
				if (hie.getElement() instanceof InternalElement && hie.getParent() == null) {
					Hierarchy<InternalElement> internalHie = (Hierarchy<InternalElement>) hie;
					Role requiredRole = internalHie.getElement().getRequiredRole();
					if (IFactoryNodeTransformable.class.isAssignableFrom(interfaceTransformablesMapping.get(requiredRole))) {
						defaultRoot.addChild(internalHie);
					} else if (ISEETransformable.class.isAssignableFrom(interfaceTransformablesMapping.get(requiredRole))) {
						seeGroupHie.addChild(internalHie);
					}
				}
				amlProvider.storeHierarchy(hie);
			} else {
				//do nothing
			}
		}
	}
	
	private Hierarchy<InternalElement> createSEEGroup() {
		Hierarchy<Role> processStructureHierarchy = null;
		for (Hierarchy<Role> hie : AMLTransformationService.getAMLProvider().getAMLModelRepo(Role.class).getFlattenedHierarchies()) {
			if (hie.getName().equals("ProcessStructure")) {
				processStructureHierarchy = hie;
			}
		}
		
		InternalElement internalElement = new InternalElement(UUID.randomUUID().toString(), "SEEs");
		internalElement.setRequiredRole(processStructureHierarchy.getElement());
		return new Hierarchy<InternalElement>("SEEs", internalElement);
	}
}
