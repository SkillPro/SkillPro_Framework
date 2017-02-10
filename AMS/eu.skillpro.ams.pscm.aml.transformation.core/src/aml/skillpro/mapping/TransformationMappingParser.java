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

package aml.skillpro.mapping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.util.TransformationUtil;
import aml.transformation.service.AMLTransformationService;

public class TransformationMappingParser {
	
	private static final TransformationMappingParser INSTANCE = new TransformationMappingParser();
	private final Map<String, Role> nameRoleMap = new HashMap<>();
	
	private TransformationMappingParser() {
	}
	
	
	
	public static void loadConfiguration(String filepath) throws ValidityException, ParsingException, IOException {
		INSTANCE.doLoad(filepath);
		
	}
	
	@SuppressWarnings("unchecked")
	private void doLoad(String filepath) throws ValidityException, ParsingException, IOException {
		initNameRolesMapFromRepo();
		Document doc = getDocumentFromFile(filepath);
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		Set<Class<?>> transformables = TransformationUtil.getAllTransformables();
		Set<Class<?>> adapters = getAllModelsImplementingTransformable();
		
		Map<Object, Class<? extends ITransformable>> interfaceMapping = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getInterfaceTransformablesMapping();
		Map<Object, Class<? extends ITransformable>> adapterMapping = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getAdapterTransformablesMapping();
		Map<Object, Class<? extends ITransformable>> pivotToAdapterMapping = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getPivotElementToAdapterTransformableMapping();
		
		List<Hierarchy<InternalElement>> allHierarchies = AMLTransformationService
				.getAMLProvider().getAMLModelRepo(InternalElement.class)
				.getFlattenedHierarchies();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			String roleName = getNameFromElement(childOfRoot);
			String transformableName = getTransformableFromElement(childOfRoot);
			String defaultClassName = getDefaultClassFromElement(childOfRoot);
			Role role = nameRoleMap.get(roleName);
			//the classes that are implementing the transformables
			if (transformableName != null && role != null) {
				Class<?> transformable = findClassFromSet(transformables, transformableName);
				interfaceMapping.put(role, (Class<? extends ITransformable>) transformable);
			}
			//default class is not a transformable. It's in the form of an adapter
			if (defaultClassName != null) {
				Class<?> defaultClass = findClassFromSet(adapters, defaultClassName);
				if (defaultClass != null) {
					pivotToAdapterMapping.put(role, (Class<? extends ITransformable>) defaultClass);
				}
				for (Hierarchy<?> hie : allHierarchies) {
					Role requiredRole = ((InternalElement) hie.getElement()).getRequiredRole();
					InternalElement element = (InternalElement) hie.getElement();
					if (requiredRole != null && requiredRole.equals(role) && defaultClass != null) {
						adapterMapping.put(element, (Class<? extends ITransformable>) defaultClass);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Role, Class<? extends ITransformable>> getTransformableInterfaceMapping(String filepath) 
			throws ValidityException, ParsingException, IOException {
		INSTANCE.initNameRolesMapFromRepo();
		Map<Role, Class<? extends ITransformable>> defaultMapping = new HashMap<>();
		Document doc = INSTANCE.getDocumentFromFile(filepath);
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		Set<Class<?>> transformables = TransformationUtil.getAllTransformables();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			String roleName = INSTANCE.getNameFromElement(childOfRoot);
			String transformableString = INSTANCE.getTransformableFromElement(childOfRoot);
			Role role = INSTANCE.nameRoleMap.get(roleName);
			//the classes that are implementing the Transformables
			//default class is not a transformable. It's in the form of an adapter
			if (transformableString != null && role != null) {
				Class<?> transformable = INSTANCE.findClassFromSet(transformables, transformableString);
				defaultMapping.put(role, (Class<? extends ITransformable>) transformable);
			}
		}
		return defaultMapping;
	}
	
	@SuppressWarnings("unchecked")
	public static void loadTransformationMapping(String filepath, Set<Role> roles,
			Set<Hierarchy<InternalElement>> allHierarchies)
			throws ValidityException, ParsingException, IOException {
		INSTANCE.initNameRolesMapFromSet(roles);
		Document doc = INSTANCE.getDocumentFromFile(filepath);
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		Set<Class<?>> transformables = TransformationUtil.getAllTransformables();
		Set<Class<?>> adapters = getAllModelsImplementingTransformable();
		
		Map<Object, Class<? extends ITransformable>> interfaceMapping = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getInterfaceTransformablesMapping();
		Map<Object, Class<? extends ITransformable>> adapterMapping = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getAdapterTransformablesMapping();
		Map<Object, Class<? extends ITransformable>> pivotToAdapterMapping = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getPivotElementToAdapterTransformableMapping();
		
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			String roleName = INSTANCE.getNameFromElement(childOfRoot);
			String defaultClassName = INSTANCE.getDefaultClassFromElement(childOfRoot);
			String transformableString = INSTANCE.getTransformableFromElement(childOfRoot);
			Role role = INSTANCE.nameRoleMap.get(roleName);
			//the classes that are implementing the transformables
			//default class is not a transformable. It's in the form of an adapter
			if (transformableString != null && role != null) {
				Class<?> transformable = INSTANCE.findClassFromSet(transformables, transformableString);
				interfaceMapping.put(role, (Class<? extends ITransformable>) transformable);
			}
			
			if (defaultClassName != null) {
				Class<?> defaultClass = INSTANCE.findClassFromSet(adapters, defaultClassName);
				if (defaultClass != null) {
					pivotToAdapterMapping.put(role, (Class<? extends ITransformable>) defaultClass);
				}
				for (Hierarchy<?> hie : allHierarchies) {
					Role requiredRole = ((InternalElement) hie.getElement()).getRequiredRole();
					InternalElement element = (InternalElement) hie.getElement();
					if (requiredRole != null && requiredRole.equals(role) && defaultClass != null) {
						adapterMapping.put(element, (Class<? extends ITransformable>) defaultClass);
					}
				}
			}
		}
	}
	
	public static Set<Class<?>> getAllModelsImplementingTransformable() {
		Set<Class<?>> result = new HashSet<>();
		//should be correct, because every transformable adapters are subclasses of ITransformable
		result.addAll(TransformationUtil.getAllModelsImplementingTransformable(ITransformable.class));
		
		return result;
	}
	
	private Class<?> findClassFromSet(Set<Class<?>> transformables, String name) {
		for (Class<?> cls : transformables) {
			if (cls.getSimpleName().equalsIgnoreCase(name)) {
				return cls;
			}
		}
		return null;
	}
	
	private void initNameRolesMapFromSet(Set<Role> roles) {
		INSTANCE.nameRoleMap.clear();
		for (Role role : roles) {
			if (nameRoleMap.get(role.getName()) != null) {
				throw new IllegalArgumentException("Duplicate roles: " + role.getName() + ": old: " + nameRoleMap.get(role.getName()) + ", new: " + role);
			}
			nameRoleMap.put(role.getName(), role);
		}
	}
	
	private void initNameRolesMapFromRepo() {
		INSTANCE.nameRoleMap.clear();
		for (Role role : getAllRolesFromRepo()) {
			if (nameRoleMap.get(role.getName()) != null) {
				throw new IllegalArgumentException("Duplicate roles: " + role.getName() + ": old: " + nameRoleMap.get(role.getName()) + ", new: " + role);
			}
			nameRoleMap.put(role.getName(), role);
		}
	}
	
	private List<Role> getAllRolesFromRepo() {
		List<Role> allRoles = new ArrayList<>();
		for (Role role : AMLTransformationService.getAMLProvider().getAMLRoleRepo().getEntities()) {
			allRoles.add(role);
			allRoles.addAll(getAllFlattenedChildrenRoles(role));
		}
		return allRoles;
	}
	
	private List<Role> getAllFlattenedChildrenRoles(Role role) {
		List<Role> roles = new ArrayList<>();
		for (Role child : role.getChildren()) {
			roles.add(child);
			if (child.getChildren() != null && !child.getChildren().isEmpty()) {
				roles.addAll(getAllFlattenedChildrenRoles(child));
			}
		}
		return roles;
	}
	
	private String getTransformableFromElement(Element element) {
		if (element.getAttribute("Transformable") != null) {
			return element.getAttribute("Transformable").getValue();
		}
		return null;
	}
	
	private String getDefaultClassFromElement(Element element) {
		if (element.getAttribute("DefaultClass") != null) {
			return element.getAttribute("DefaultClass").getValue();
		}
		return null;
	}
	
	private String getNameFromElement(Element element) {
		if (element.getAttribute("Name") != null) {
			return element.getAttribute("Name").getValue();
		}
		return null;
	}
	
	private Document getDocumentFromFile(String filepath) throws ValidityException, ParsingException, IOException {
		Builder parser = new Builder();
		Document result = parser.build(new File(filepath));
		return result;
	}
	
	public static void wipeData() {
		INSTANCE.nameRoleMap.clear();
	}
}
