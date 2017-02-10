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

package aml.amlparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import aml.domain.Domain;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.model.InterfaceDesignator;
import aml.model.InternalLink;
import aml.model.NominalConstraint;
import aml.model.OrdinalConstraint;
import aml.model.Root;

public class AMLParser {
	private static final String NOMINAL_SCALED_TYPE = "NominalScaledType";
	private static final String ORDINAL_SCALED_TYPE = "OrdinalScaledType";
	private static final String SYSTEM_UNIT_CLASS = "SystemUnitClass";
	private static final String SYSTEM_UNIT_CLASS_LIB = "SystemUnitClassLib";
	private static final String ATTRIBUTE = "Attribute";
	private static final String ROLE_CLASS = "RoleClass";
	private static final String INTERFACE_CLASS = "InterfaceClass";
	private static final String ROLE_CLASS_LIB = "RoleClassLib";
	private static final String INTERFACE_CLASS_LIB = "InterfaceClassLib";
	private static final String INSTANCE_HIERARCHY = "InstanceHierarchy";
	private static final String INTERNAL_ELEMENT = "InternalElement";
	private Document doc;
	
	private final List<Root<InternalElement>> instanceHierarchies = new ArrayList<>();
	private final List<Root<Role>> roleClassLibs = new ArrayList<>();
	private final List<Root<Interface>> interfaceClassLibs = new ArrayList<>();
	private final List<Root<SystemUnit>> systemUnitClassLibs = new ArrayList<>();
	private final List<InterfaceDesignator> interfaceDesignators = new ArrayList<>();
	private final List<InternalElement> flattenedInternalElements = new ArrayList<>();
	
	//Maps
	private final Map<Interface, String> notReferencedInterfaces = new HashMap<>();
	private final Map<Role, String> notReferencedRoles = new HashMap<>();
	private final Map<SystemUnit, String> notReferencedSystemUnits = new HashMap<>();
	private final Map<InternalElement, String> notReferencedRefBaseSystemUnitPath = new HashMap<>();
	private final Map<InternalElement, Element> unconvertedInternalLinks = new HashMap<>();
	private final Map<SystemUnit, Element> unconvertedInternalSystemUnitLinks = new HashMap<>();
	
	private Set<Object> parsedObjects = new HashSet<>();
	
	private static final AMLParser INSTANCE = new AMLParser();
	
	private AMLParser() {
	}
	
	private void parse() {
		//doc contains the AML description
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootIEs = new ArrayList<>();
		List<Element> rootRoles = new ArrayList<>();
		List<Element> rootInterfaces = new ArrayList<>();
		List<Element> rootSystems = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase(INSTANCE_HIERARCHY)) {
				rootIEs.add(childOfRoot);
			} else if (childOfRoot.getLocalName().equalsIgnoreCase(INTERFACE_CLASS_LIB)) {
				rootInterfaces.add(childOfRoot);
			} else if (childOfRoot.getLocalName().equalsIgnoreCase(ROLE_CLASS_LIB)) {
				rootRoles.add(childOfRoot);
			} else if (childOfRoot.getLocalName().equalsIgnoreCase(SYSTEM_UNIT_CLASS_LIB)) {
				rootSystems.add(childOfRoot);
			}
		}
		
		//order = Interfaces -> Roles -> SystemUnits -> InternalElements
		for (Element element : rootInterfaces) {
			interfaceClassLibs.add(doInterfaceClassLib(element));
		}
		//assigning references to interfaces
		
		assignReferencesForInterfaces();
		
		for (Element element : rootRoles) {
			roleClassLibs.add(doRoleClassLib(element));
		}
		
		//assigning references to roles");
		assignReferencesForRoles();
		
		for (Element element : rootSystems) {
			systemUnitClassLibs.add(doSystemUnitClassLib(element));
		}

		assignReferencesForSystemUnits();
		
		for (Element element : rootIEs) {
			instanceHierarchies.add(doInstanceHierarchy(element));
		}
		//assign resource skills for the configuration
		assignElementsForNotReferencedRefSystemUnits();
		
		
		//Building internal links
		Set<Entry<InternalElement, Element>> internalEntrySet = unconvertedInternalLinks.entrySet();
		for (Entry<InternalElement, Element> entry : internalEntrySet) {
			InternalElement ie = entry.getKey();
			Element value = entry.getValue();
			String childName = getNameFromElement(value);
			String refAPath = getRefSideAFromElement(value);
			String refBPath = getRefSideBFromElement(value);
			InternalLink internalLink = createInternalLink(childName, refAPath, refBPath);
			//add internal links
			ie.addInternalLink(internalLink);
		}
		
		Set<Entry<SystemUnit, Element>> internalSystemUnitEntrySet = unconvertedInternalSystemUnitLinks.entrySet();
		for (Entry<SystemUnit, Element> entry : internalSystemUnitEntrySet) {
			Element element = entry.getValue();
			SystemUnit systemUnit = entry.getKey();
			String childName = getNameFromElement(element);
			String refAPath = getRefSideAFromElement(element);
			String refBPath = getRefSideBFromElement(element);
			InternalLink internalLink = createInternalLink(childName, refAPath, refBPath);
			//add internal links
			systemUnit.addInternalLink(internalLink);
		}
	}
	
	private void parseLibrariesOnly() {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootRoles = new ArrayList<>();
		List<Element> rootInterfaces = new ArrayList<>();
		List<Element> rootSystems = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase(INTERFACE_CLASS_LIB)) {
				rootInterfaces.add(childOfRoot);
			} else if (childOfRoot.getLocalName().equalsIgnoreCase(ROLE_CLASS_LIB)) {
				rootRoles.add(childOfRoot);
			} else if (childOfRoot.getLocalName().equalsIgnoreCase(SYSTEM_UNIT_CLASS_LIB)) {
				rootSystems.add(childOfRoot);
			}
		}
		
		//order = Interfaces -> Roles -> SystemUnits -> InternalElements
		for (Element element : rootInterfaces) {
			interfaceClassLibs.add(doInterfaceClassLib(element));
		}
		//assigning references to interfaces
		
		assignReferencesForInterfaces();
		
		for (Element element : rootRoles) {
			roleClassLibs.add(doRoleClassLib(element));
		}
		
		//assigning references to roles");
		assignReferencesForRoles();
		
		for (Element element : rootSystems) {
			systemUnitClassLibs.add(doSystemUnitClassLib(element));
		}

		assignReferencesForSystemUnits();
		
		//assign resource skills for the configuration
		assignElementsForNotReferencedRefSystemUnits();
		//Building internal links
		Set<Entry<SystemUnit, Element>> internalSystemUnitEntrySet = unconvertedInternalSystemUnitLinks.entrySet();
		for (Entry<SystemUnit, Element> entry : internalSystemUnitEntrySet) {
			Element element = entry.getValue();
			SystemUnit systemUnit = entry.getKey();
			String childName = getNameFromElement(element);
			String refAPath = getRefSideAFromElement(element);
			String refBPath = getRefSideBFromElement(element);
			InternalLink internalLink = createInternalLink(childName, refAPath, refBPath);
			//add internal links
			systemUnit.addInternalLink(internalLink);
		}
	}
	
	public void wipeData() {
		instanceHierarchies.clear();
		roleClassLibs.clear();
		interfaceClassLibs.clear();
		systemUnitClassLibs.clear();
		interfaceDesignators.clear();
		flattenedInternalElements.clear();
		notReferencedInterfaces.clear();
		notReferencedRoles.clear();
		unconvertedInternalLinks.clear();
		notReferencedRefBaseSystemUnitPath.clear();
		parsedObjects.clear();
	}
	
	public List<String> getExternalReferencesFromString(String input) {
		try {
			doc = getDocumentFromString(input);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> references = new ArrayList<>();
		
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> referenceElements = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("ExternalReference")) {
				referenceElements.add(childOfRoot);
			} 
		}
		
		for (Element element : referenceElements) {
			references.add(getPathFromElement(element));
		}
		return references;
	}
	
	public void parseAMLFromFile(String filepath) {
		try {
			doc = getDocumentFromFile(filepath);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parse();
	}
	
	public void parseAMLFromFile(File file) {
		try {
			doc = getDocumentFromFile(file);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parse();
	}
	
	public void parseAMLFromString(String input) {
		try {
			doc = getDocumentFromString(input);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parse();
	}
	
	public void parseAMLLibrariesFromFilepath(String filepath) {
		try {
			doc = getDocumentFromFile(filepath);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parseLibrariesOnly();
	}
	
	public void parseAMLLibrariesFromFile(File file) {
		try {
			doc = getDocumentFromFile(file);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parseLibrariesOnly();
	}
	
	public void parseAMLLibrariesFromString(String input) {
		try {
			doc = getDocumentFromString(input);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parseLibrariesOnly();
	}
	
	public String getContentStringFromFile(String filepath) {
		try {
			Document doc = getDocumentFromFile(filepath);
			return doc.toXML();
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getContentStringFromFile(File file) {
		try {
			Document doc = getDocumentFromFile(file);
			return doc.toXML();
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public List<Root<InternalElement>> getInstanceHierarchies() {
		return instanceHierarchies;
	}
	
	public List<Root<Interface>> getInterfaceClassLibs() {
		return interfaceClassLibs;
	}
	
	public List<Root<Role>> getRoleClassLibs() {
		return roleClassLibs;
	}
	
	public List<Root<SystemUnit>> getSystemUnitClassLibs() {
		return systemUnitClassLibs;
	}
	
	public static AMLParser getInstance() {
		return INSTANCE;
	}
	
	private String getNameFromElement(Element element) {
		if (element.getAttribute("Name") != null) {
			return element.getAttribute("Name").getValue();
		}
		return null;
	}
	
	private String getRefIDFromElement(Element element) {
		if (element.getAttribute("RefBaseSystemUnitPath") != null) {
			return element.getAttribute("RefBaseSystemUnitPath").getValue();
		}
		return null;
	}
	
	private String getPathFromElement(Element element) {
		if (element.getAttribute("Path") != null) {
			return element.getAttribute("Path").getValue();
		}
		return null;
	}
	
	private String getIDFromElement(Element element) {
		if (element.getAttribute("ID") != null) {
			return element.getAttribute("ID").getValue();
		}
		return null;
	}
	
	private String getRolePathFromElement(Element element) {
		if (element.getAttribute("RefRoleClassPath") != null) {
			return element.getAttribute("RefRoleClassPath").getValue();
		}
		return null;
	}
	
	private String getBaseRolePathFromElement(Element element) {
		if (element.getAttribute("RefBaseRoleClassPath") != null) {
			return element.getAttribute("RefBaseRoleClassPath").getValue();
		}
		return null;
	}
	
	private String getBasePathFromElement(Element element) {
		if (element.getAttribute("RefBaseClassPath") != null) {
			return element.getAttribute("RefBaseClassPath").getValue();
		}
		return null;
	}
	
	private String getRefSideAFromElement(Element element) {
		if (element.getAttribute("RefPartnerSideA") != null) {
			String refA = element.getAttribute("RefPartnerSideA").getValue();
			refA = refA.replace("{", "");
			refA = refA.replace("}", "");
			return refA;
		}
		return null;
	}
	
	private String getRefSideBFromElement(Element element) {
		if (element.getAttribute("RefPartnerSideB") != null) {
			String refB = element.getAttribute("RefPartnerSideB").getValue();
			refB = refB.replace("{", "");
			refB = refB.replace("}", "");
			return refB;
		}
		return null;
	}
	
	private String getAttributeDataTypeFromElement(Element element) {
		if (element.getAttribute("AttributeDataType") != null) {
			return element.getAttribute("AttributeDataType").getValue();
		}
		return null;
	}
	
	private String getUnitFromElement(Element element) {
		if (element.getAttribute("Unit") != null) {
			return element.getAttribute("Unit").getValue();
		}
		return null;
	}
	
	private Object addToParsedObjects(Object obj) {
		parsedObjects.add(obj);
		return obj;
	}


	@SuppressWarnings("unchecked")
	private Root<SystemUnit> doSystemUnitClassLib(Element childOfRoot) {
		Root<SystemUnit> root = (Root<SystemUnit>) addToParsedObjects(new Root<>(getNameFromElement(childOfRoot), SystemUnit.class));
		Elements children = childOfRoot.getChildElements();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			//root can't have attributes.
			Hierarchy<?> transfChild = doUntilLastChild(null, child, SYSTEM_UNIT_CLASS);
			root.addChild((Hierarchy<SystemUnit>) transfChild);
		}
		return root;
	}

	@SuppressWarnings("unchecked")
	private Root<InternalElement> doInstanceHierarchy(Element childOfRoot) {
		Root<InternalElement> root = (Root<InternalElement>) addToParsedObjects(new Root<>(getNameFromElement(childOfRoot), InternalElement.class));
		Elements children = childOfRoot.getChildElements();	
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			Hierarchy<?> transfChild = doUntilLastChild(null, child, INTERNAL_ELEMENT);
			root.addChild((Hierarchy<InternalElement>) transfChild);
		}
		return root;
	}


	@SuppressWarnings("unchecked")
	private Root<Interface> doInterfaceClassLib(Element childOfRoot) {
		Root<Interface> root = (Root<Interface>) addToParsedObjects(new Root<>(getNameFromElement(childOfRoot), Interface.class));
		Elements children = childOfRoot.getChildElements();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			Hierarchy<?> transfChild = doUntilLastChild(null, child, INTERFACE_CLASS);
			root.addChild((Hierarchy<Interface>) transfChild);
		}
		return root;
	}


	@SuppressWarnings("unchecked")
	private Root<Role> doRoleClassLib(Element childOfRoot) {
		Root<Role> root = (Root<Role>) addToParsedObjects(new Root<>(getNameFromElement(childOfRoot), Role.class));
		Elements children = childOfRoot.getChildElements();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			Hierarchy<?> transfChild = doUntilLastChild(null, child, ROLE_CLASS);
			root.addChild((Hierarchy<Role>) transfChild);
		}
		return root;
		
	}
	
	private Hierarchy<?> doUntilLastChild(Hierarchy<?> parent, Element element, String elementName) {
		String nameFromElement = getNameFromElement(element);
		Hierarchy<?> transformedElement = null;
		Domain domain = null;
		if (element.getLocalName().equalsIgnoreCase(elementName)) {
			if (element.getLocalName().equalsIgnoreCase(INTERNAL_ELEMENT)) {
				domain = doInternalElement(element, parent);
				if (domain != null) {
					flattenedInternalElements.add((InternalElement) domain);
				}
			} else if (element.getLocalName().equalsIgnoreCase(ROLE_CLASS)) {
				domain = doRole(element, parent);
			} else if (element.getLocalName().equalsIgnoreCase(INTERFACE_CLASS)) {
				domain = doInterface(element, parent);
			} else if (element.getLocalName().equalsIgnoreCase(SYSTEM_UNIT_CLASS)) {
				domain = doSystemUnit(element, parent);
			}		}
		if (domain != null && element.getLocalName().equalsIgnoreCase(elementName)) {
			transformedElement = (Hierarchy<?>) addToParsedObjects(new Hierarchy<>(nameFromElement, domain));
			Elements children = element.getChildElements();
			if (children != null) {
				for (int i = 0; i < children.size(); i++) {
					Element child = children.get(i);
					transformedElement.addChild(doUntilLastChild(transformedElement, child, elementName));
				}
				return transformedElement;
			}
		} else if (element.getLocalName().equalsIgnoreCase(ATTRIBUTE)) {
			doAttribute(element, parent.getElement(), null);
		}
		return transformedElement;
	}
	
	private Interface doInterface(Element element, Hierarchy<?> parent) {
		String refBasePath = getBasePathFromElement(element);
		Interface interfaceFromPath = getInterfaceFromPath(refBasePath, interfaceClassLibs, parent);
		Interface inter = (Interface) addToParsedObjects(new Interface(getNameFromElement(element), interfaceFromPath));
		
		if (refBasePath != null && interfaceFromPath == null) {
			//map
			notReferencedInterfaces.put(inter, refBasePath);
		}
		return inter;
	}
	
	private void assignReferencesForInterfaces() {
		Set<Interface> keys = new HashSet<>(notReferencedInterfaces.keySet());
		for (Interface key : keys) {
			Interface interfaceFromPath = getInterfaceFromPath(notReferencedInterfaces.get(key), interfaceClassLibs, null);
			if (interfaceFromPath != null) {
				notReferencedInterfaces.remove(key);
				key.setReferencedInterface(interfaceFromPath);
			}
		}
	}
	
	private Role doRole(Element element, Hierarchy<?> parent) {
		String refBasePath = getBasePathFromElement(element);
		Role roleFromPath = getRoleFromPath(refBasePath, roleClassLibs, parent);
		Role role = (Role) addToParsedObjects(new Role(getNameFromElement(element), roleFromPath));

		Elements children = element.getChildElements();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Element child = children.get(i);
				if (child.getLocalName().equalsIgnoreCase("ExternalInterface")) {
					doExternalInterface(child, role, parent);
				} else if (child.getLocalName().equalsIgnoreCase("Description")) {
					role.setDescription(child.getValue());
				}
				
			} 
		}
		if (refBasePath != null && role.getReferencedRole() == null) {
			//map
			notReferencedRoles.put(role, refBasePath);
		}
		return role;
	}
	
	/**
	 * sets reference roles, when these not known at the time of parsing
	 */
	private void assignReferencesForRoles() {
		Set<Role> keys = new HashSet<>(notReferencedRoles.keySet());
		for (Role key : keys) {
			Role roleReference = getRoleFromPath(notReferencedRoles.get(key), roleClassLibs, null);
			if (roleReference != null) {
				notReferencedRoles.remove(key);
				key.setReferencedRole(roleReference);
			}
		}
	}
	
	private void assignReferencesForSystemUnits() {
		Set<SystemUnit> keys = new HashSet<>(notReferencedSystemUnits.keySet());
		for (SystemUnit key : keys) {
			SystemUnit systemUnitReference = getSystemUnitFromPath(notReferencedSystemUnits.get(key), systemUnitClassLibs, null);
			if (systemUnitReference != null) {
				notReferencedSystemUnits.remove(key);
				key.setReferencedSystemUnit(systemUnitReference);
			}
		}
	}
	
	private void assignElementsForNotReferencedRefSystemUnits() {
		Iterator<Entry<InternalElement, String>> it = notReferencedRefBaseSystemUnitPath.entrySet().iterator();
		while (it.hasNext()) {
			Entry<InternalElement, String> entry = it.next();
			InternalElement refElement = searchFlattenedInternalElementsByID(entry.getValue());
			if (refElement != null) {
				entry.getKey().setReferencedInternalElement(refElement);
				it.remove();
			}
		}
	}
	
	//Interfaces and Roles have to be initialized first.
	private InternalElement doInternalElement(Element element, Hierarchy<?> parent) {
		Elements children = element.getChildElements();
		String idFromElement = getIDFromElement(element);
		
		String refBaseSystemUnitPath = getRefIDFromElement(element);
		if (refBaseSystemUnitPath != null && refBaseSystemUnitPath.split("\\/").length == 1) {
			InternalElement refElement = searchFlattenedInternalElementsByID(refBaseSystemUnitPath);
			InternalElement ie = (InternalElement) addToParsedObjects(new InternalElement(idFromElement, getNameFromElement(element)));
			if (refElement == null) {
				notReferencedRefBaseSystemUnitPath.put(ie, refBaseSystemUnitPath);
			} else {
				ie.setReferencedInternalElement(refElement);
			}
			return ie;
		}
		InternalElement found = searchFlattenedInternalElementsByID(idFromElement);
		if (found != null) {
			System.err.println("Error! At least 2 elements possess the same ID: " + idFromElement);
			return found;
		}
		InternalElement ie = (InternalElement) addToParsedObjects(new InternalElement(idFromElement, getNameFromElement(element)));
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Element child = children.get(i);
				if (child.getLocalName().equalsIgnoreCase("RoleRequirements")) {
					String baseRolePath = getBaseRolePathFromElement(child);
					Role requiredRole = getRoleFromPath(baseRolePath, roleClassLibs, parent);
					Elements roleRequirementsChildren = child.getChildElements();
					if (roleRequirementsChildren.size() > 0) {
						for (int j = 0; j < roleRequirementsChildren.size(); j++) {
							Element rElement = roleRequirementsChildren.get(j);
							if (rElement.getLocalName().equalsIgnoreCase(ATTRIBUTE)) {
								//if an attribute is found, creates designators for the ie and the attributes.
								doAttribute(rElement, ie, null);
							} else if (rElement.getLocalName().equalsIgnoreCase("ExternalInterface")) {
								doExternalInterface(rElement, ie, parent);
							}
						}
					}
					//add the required role
					ie.setRequiredRole(requiredRole);
				} else if (child.getLocalName().equalsIgnoreCase("SupportedRoleClass")) {
					String rolePath = getRolePathFromElement(child);
					Role supportedRole = getRoleFromPath(rolePath, roleClassLibs, parent);
					//add supported roles
					ie.addSupportedRole(supportedRole);
				} else if (child.getLocalName().equalsIgnoreCase("ExternalInterface")) {
					doExternalInterface(child, ie, parent);
				} else if (child.getLocalName().equalsIgnoreCase("InternalLink")) {
					unconvertedInternalLinks.put(ie, child);
				}
			}
		}
		List<Root<?>> systemUnitLibs = new ArrayList<>();
		systemUnitLibs.addAll(systemUnitClassLibs);
		if (refBaseSystemUnitPath != null && refBaseSystemUnitPath.split("\\/").length > 1) {
			SystemUnit systemUnit = (SystemUnit) getDomainFromFullPath(refBaseSystemUnitPath, systemUnitLibs);
			if (systemUnit != null) {
				ie.setSystemUnit(systemUnit);
			} else {
				throw new IllegalArgumentException("Cannot find referenced SystemUnit: " + refBaseSystemUnitPath);
			}
		}
		return ie;
	}
	
	private void doExternalInterface(Element element, Domain domain, Hierarchy<?> parent) {
		String name = getNameFromElement(element);
		String id = getIDFromElement(element);
		String refPath = getBasePathFromElement(element);
		InterfaceDesignator interfaceDesignator = (InterfaceDesignator) addToParsedObjects(new InterfaceDesignator(id,
				name, domain, getInterfaceFromPath(refPath, interfaceClassLibs, parent)));
		//add the interface designators to the list of designators (the list is flat)
		interfaceDesignators.add(interfaceDesignator);
		//add interface designators
		if (domain instanceof InternalElement) {
			((InternalElement) domain).addInterfaceDesignator(interfaceDesignator);
		} else if (domain instanceof SystemUnit) {
			((SystemUnit) domain).addInterfaceDesignator(interfaceDesignator);
		} else if (domain instanceof Role) {
			((Role) domain).addInterfaceDesignator(interfaceDesignator);
		} else {
			System.err.println("Domain can't add interface designator");
		}
		
		Elements children = element.getChildElements();
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			if (child.getLocalName().equalsIgnoreCase(ATTRIBUTE)) {
				doExternalAttribute(child, interfaceDesignator);
			} else {
				System.err.println("Unexpected local name: " + child.getLocalName());
			}
		}
	}
	
	private InternalElement searchFlattenedInternalElementsByID(String idFromElement) {
		for (InternalElement ie : flattenedInternalElements) {
			if (ie.getId().equals(idFromElement)) {
				return ie;
			}
		}
		return null;
	}


	@SuppressWarnings("unchecked")
	private SystemUnit doSystemUnit(Element element, Hierarchy<?> parent) {
		String refBasePath = getBasePathFromElement(element);
		SystemUnit systemUnitFromPath = getSystemUnitFromPath(refBasePath, systemUnitClassLibs, parent);
		SystemUnit systemUnit = (SystemUnit) addToParsedObjects(new SystemUnit(getNameFromElement(element), systemUnitFromPath));
		Elements children = element.getChildElements();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Element child = children.get(i);
				if (child.getLocalName().equalsIgnoreCase("SupportedRoleClass")) {
					String rolePath = getRolePathFromElement(child);
					Role supportedRole = getRoleFromPath(rolePath, roleClassLibs, parent);
					//add supported roles
					systemUnit.addSupportedRole(supportedRole);
				} else if (child.getLocalName().equalsIgnoreCase("ExternalInterface")) {
					doExternalInterface(child, systemUnit, parent);
				} else if (child.getLocalName().equalsIgnoreCase("InternalLink")) {
					unconvertedInternalSystemUnitLinks.put(systemUnit, child);
					
				} else if (child.getLocalName().equalsIgnoreCase("InternalElement")) {
					Hierarchy<InternalElement> hieChild = (Hierarchy<InternalElement>) doUntilLastChild(null, child, INTERNAL_ELEMENT);
					systemUnit.addIEHierarchy(hieChild);
				}
			}
		}
	
		if (refBasePath != null && systemUnitFromPath == null) {
			//map
			notReferencedSystemUnits.put(systemUnit, refBasePath);
		}
		return systemUnit;
	}
	
	private InternalLink createInternalLink(String name, String refAPath, String refBPath) {
		InterfaceDesignator refA = getInterfaceDesignatorFromPath(refAPath);
		InterfaceDesignator refB = getInterfaceDesignatorFromPath(refBPath);
		InternalLink result = (InternalLink) addToParsedObjects(new InternalLink(name, refA, refB));
		return result;
	}
	
	private InterfaceDesignator getInterfaceDesignatorFromPath(String path) {
		if (path == null) {
			return null;
		}
		String[] tokens = path.split("\\:");
		InterfaceDesignator designator = null;
		if (tokens.length == 2) {
			for (InternalElement ie : flattenedInternalElements) {
				if (ie.getId() != null && ie.getId().equals(tokens[0])) {
					
					for (InterfaceDesignator des : ie.getInterfaceDesignators()) {
						if (des.getName().equalsIgnoreCase(tokens[1])) {
							return des;
						}
					}
				}
			}
		} else if (tokens.length == 1) {
			for (InterfaceDesignator des : interfaceDesignators) {
				if (des.getId().equals(tokens[0])) {
					return des;
				}
			}
		}
		return designator;
	}
	
	private Domain getDomainFromPath(String path, List<Root<?>> libs, Hierarchy<?> parent) {
		if (path == null || path.equals("")) {
			return null;
		}
		
		String[] tokens = path.split("\\/");
		if (tokens.length == 1) {
			return getDomainFromSinglePath(parent);
		} else {
			return getDomainFromFullPath(path, libs);
		}
	}
	
	private Domain getDomainFromSinglePath(Hierarchy<?> parent) {
		return parent.getElement();
	}
	
	private Domain getDomainFromFullPath(String path, List<Root<?>> libs) {
		if (path == null) {
			return null;
		}
		String[] refTokens = path.split("\\@");
		String realPath = "";
		if (refTokens.length == 1) {
			realPath = path;
		} else if (refTokens.length > 1) {
			realPath = path.replace(refTokens[0] + "@", "");
		}
		String[] tokens = realPath.split("\\/");
		Domain domain = null;
		Root<?> root = null;
			
		if (tokens.length > 0) {
			for (Root<?> toor : libs) {
				if (toor.getName().equalsIgnoreCase(tokens[0])) {
					root = toor;
					break;
				}
			}
		}
		if (root == null) {
			return null;
		}
		int i = 1;
		Hierarchy<?> hierarchy = null;
		List<Hierarchy<?>> children = new ArrayList<>();
		children.addAll(root.getChildren());
		while (i < tokens.length) {
			for (Hierarchy<?> hie : children) {
				if (hie.getName().equalsIgnoreCase(tokens[i])) {
					hierarchy = hie;
					children = new ArrayList<>();
					children.addAll(hie.getChildren());
					break;
				}
			}
			i++;
		}
		domain = hierarchy.getElement();
		return domain;
	}
	
	private Interface getInterfaceFromPath(String path, List<Root<Interface>> libs, Hierarchy<?> parent) {
		List<Root<?>> newLibs = new ArrayList<>();
		newLibs.addAll(libs);
		return (Interface) getDomainFromPath(path, newLibs, parent);
	}
	
	private Role getRoleFromPath(String path, List<Root<Role>> libs, Hierarchy<?> parent) {
		List<Root<?>> newLibs = new ArrayList<>();
		newLibs.addAll(libs);
		return (Role) getDomainFromPath(path, newLibs, parent);
	}
	
	private SystemUnit getSystemUnitFromPath(String path, List<Root<SystemUnit>> libs, Hierarchy<?> parent) {
		List<Root<?>> newLibs = new ArrayList<>();
		newLibs.addAll(libs);
		return (SystemUnit) getDomainFromPath(path, newLibs, parent);
	}

	private Constraint checkConstraint(Element child) {
		Elements children = child.getChildElements();
		Constraint constraint = null;
		if (children != null) {
			for (int j = 0; j < children.size(); j++) {
				Element constraintTypeElement = children.get(j);
				Elements constraintTypeChildren = constraintTypeElement.getChildElements();
				String constraintTypeName = constraintTypeElement.getLocalName();
				if (constraintTypeName != null) {
					if (constraintTypeName.equalsIgnoreCase(NOMINAL_SCALED_TYPE)) {
						NominalConstraint nominal = new NominalConstraint(getNameFromElement(child));
						for (int k = 0; k < constraintTypeChildren.size(); k++) {
							nominal.addValue(constraintTypeChildren.get(k).getValue());
						}
						constraint = nominal;
					} else if (constraintTypeName.equalsIgnoreCase(ORDINAL_SCALED_TYPE)) {
						double maxValue = Double.MAX_VALUE;
						double minValue = Double.MIN_VALUE;
						String requiredValue = "";
						for (int k = 0; k < constraintTypeChildren.size(); k++) {
							String valueTypeName = constraintTypeChildren.get(k).getLocalName();
							if (valueTypeName.equalsIgnoreCase("RequiredMaxValue")) {
								maxValue = Double.parseDouble(constraintTypeChildren.get(k).getValue());
							} else if (valueTypeName.equalsIgnoreCase("RequiredMinValue")) {
								minValue = Double.parseDouble(constraintTypeChildren.get(k).getValue());
							} else if (valueTypeName.equalsIgnoreCase("RequiredValue")) {
								requiredValue = constraintTypeChildren.get(k).getValue();
							}
						}
						constraint = new OrdinalConstraint(getNameFromElement(child), maxValue, minValue, requiredValue);
					} else {
						throw new IllegalArgumentException("Unsupported type of constraint: " + constraintTypeName);
					}
				} else {
					throw new IllegalArgumentException("Constraint type name is null!");
				}
			}
		}
		return constraint;
	}

	private Attribute doAttribute(Element element, Domain domain, Attribute parent) {
		String nameFromElement = getNameFromElement(element);
		
		Attribute attribute = (Attribute) addToParsedObjects(new Attribute(nameFromElement, getAttributeDataTypeFromElement(element), getUnitFromElement(element)));
		Elements children = element.getChildElements();
		List<Constraint> constraints = new ArrayList<>();
		String value = "";
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Element currentElement = children.get(i);
				if (currentElement.getLocalName().equalsIgnoreCase("Constraint")) {
					constraints.add(checkConstraint(currentElement));
				} else if (currentElement.getLocalName().equalsIgnoreCase("Attribute")) {
					doAttribute(currentElement, domain, attribute);
				} else if (currentElement.getLocalName().equalsIgnoreCase("Description")) {
					attribute.setDescription(currentElement.getValue());
				} else if (currentElement.getLocalName().equalsIgnoreCase("Value")) {
					value = currentElement.getValue();
				} else if (currentElement.getLocalName().equalsIgnoreCase("DefaultValue")) {
					if (value == null || value.isEmpty()) {
						value = currentElement.getValue();
					}
				} else {
					System.err.println("This element cannot be parsed: " + element.getLocalName() + ": " + element.getValue());
				}
			}
		}
		if (parent != null) {
			parent.addAttribute(attribute);
		}
		AttributeDesignator designator = (AttributeDesignator) addToParsedObjects(new AttributeDesignator(attribute, domain, constraints, value));
		attribute.addDesignator(designator);
		domain.addDesignator(designator);
		return attribute;
	}
	
	private Attribute doExternalAttribute(Element element, InterfaceDesignator externalInterface) {
		String nameFromElement = getNameFromElement(element);
		
		Attribute attribute = (Attribute) addToParsedObjects(new Attribute(nameFromElement, getAttributeDataTypeFromElement(element), getUnitFromElement(element)));
		Elements children = element.getChildElements();
		List<Constraint> constraints = new ArrayList<>();
		String value = "";
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Element currentElement = children.get(i);
				if (currentElement.getLocalName().equalsIgnoreCase("Constraint")) {
					constraints.add(checkConstraint(currentElement));
				} else if (currentElement.getLocalName().equalsIgnoreCase("Attribute")) {
					Attribute subAttribute = doExternalAttribute(currentElement, externalInterface);
					attribute.addAttribute(subAttribute);
				} else if (currentElement.getLocalName().equalsIgnoreCase("Description")) {
					attribute.setDescription(currentElement.getValue());
				} else if (currentElement.getLocalName().equalsIgnoreCase("Value")) {
					value = currentElement.getValue();
				} else if (currentElement.getLocalName().equalsIgnoreCase("DefaultValue")) {
					if (value == null || value.isEmpty()) {
						value = currentElement.getValue();
					}
				} else {
					System.err.println("This element cannot be parsed: " + element.getLocalName() + ": " + element.getValue());
				}
			}
		}
		AttributeDesignator designator = (AttributeDesignator) addToParsedObjects(new AttributeDesignator(attribute, externalInterface, constraints, value));
		attribute.addDesignator(designator);
		externalInterface.addDesignator(designator);
		return attribute;
	}

	public Document getDocumentFromFile(String filepath) throws ValidityException, ParsingException, IOException {
		Builder parser = new Builder();
		Document result = parser.build(new File(filepath));
		return result;
	}
	
	public Document getDocumentFromFile(File file) throws ValidityException, ParsingException, IOException {
		Builder parser = new Builder();
		Document result = parser.build(file);
		return result;
	}
	
	public Document getDocumentFromString(String input) throws ValidityException, ParsingException, IOException {
		Builder parser = new Builder();
		Document result = parser.build(input, null);
		return result;
	}
	
	public Set<Object> getParsedObjects() {
		return parsedObjects;
	}
}
