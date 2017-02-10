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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import aml.domain.Domain;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.AttributeDesignator;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.model.InterfaceDesignator;
import aml.model.InternalLink;
import aml.model.NominalConstraint;
import aml.model.OrdinalConstraint;
import aml.model.Root;
import aml.transformation.service.AMLTransformationService;

public class AMLExporter {
	private static AMLExporter INSTANCE = new AMLExporter();

	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private List<Root<InternalElement>> instanceHierarchies = new ArrayList<>();
	private List<Root<Role>> roleClassLibs = new ArrayList<>();
	private List<Root<Interface>> interfaceClassLibs = new ArrayList<>();
	private List<Root<SystemUnit>> systemUnitClassLibs = new ArrayList<>();
	private List<InterfaceDesignator> interfaceDesignators = new ArrayList<>();
	
	private Map<Object, String> pathsMap = new HashMap<>();
	
	private AMLExporter() {
	}
	
	public static AMLExporter getInstance() {
		return INSTANCE;
	}
	
	private void initializeAllInputs() {
		instanceHierarchies.addAll(AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).getEntities());
		roleClassLibs.addAll(AMLTransformationService.getAMLProvider().getAMLModelRepo(Role.class).getEntities());
		interfaceClassLibs.addAll(AMLTransformationService.getAMLProvider().getAMLModelRepo(Interface.class).getEntities());
		systemUnitClassLibs.addAll(AMLTransformationService.getAMLProvider().getAMLModelRepo(SystemUnit.class).getEntities());
		
		List<Root<?>> roots = new ArrayList<>();
		roots.addAll(instanceHierarchies);
		roots.addAll(roleClassLibs);
		roots.addAll(interfaceClassLibs);
		roots.addAll(systemUnitClassLibs);
		
		INSTANCE.initializePathsFromRoots(roots);
	}

	private void wipeData() {
		instanceHierarchies.clear();
		roleClassLibs.clear();
		interfaceClassLibs.clear();
		systemUnitClassLibs.clear();
		interfaceDesignators.clear();
		pathsMap.clear();
	}
	
	public static void saveFile(String filepath) throws TransformerException {
		INSTANCE.wipeData();
		INSTANCE.initializeAllInputs();
		
		Document doc = INSTANCE.initializeDoc(filepath, false);
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		StreamSource source = new StreamSource(new ByteArrayInputStream(doc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(new File(filepath));
		
		transformer.transform(source, result);
		
		System.out.println("File saved to: " + filepath);
	}
	
	public static String getExportedAsStringWithoutIndent() {
		INSTANCE.wipeData();
		INSTANCE.initializeAllInputs();
		
		Document doc = INSTANCE.initializeDoc("AMLDescription.aml", false);
		return doc.toXML();
	}
	
	public static String getExportedAsString() throws TransformerException, IOException {
		INSTANCE.wipeData();
		INSTANCE.initializeAllInputs();
		
		Document doc = INSTANCE.initializeDoc("AMLDescription.aml", false);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		File tempFile = File.createTempFile("AMLDescription", ".aml");
		StreamSource source = new StreamSource(new ByteArrayInputStream(doc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(tempFile);
		
		transformer.transform(source, result);
		FileInputStream fis = new FileInputStream(tempFile);
		String xml = INSTANCE.convertStreamToString(fis);
		tempFile.delete();
		fis.close();
		return xml;
	}
	
	public static String getExportedAsString(Document doc) throws TransformerException, IOException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		File tempFile = File.createTempFile("AMLDescription", ".aml");
		StreamSource source = new StreamSource(new ByteArrayInputStream(doc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(tempFile);
		
		transformer.transform(source, result);
		FileInputStream fis = new FileInputStream(tempFile);
		String xml = INSTANCE.convertStreamToString(fis);
		tempFile.delete();
		fis.close();
		return xml;
	}
	
	public static Document getInternalElementsOnly(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootIEs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("InstanceHierarchy")) {
				rootIEs.add(childOfRoot);
			}
		}
		if (rootIEs.size() != 1) {
			throw new IllegalArgumentException("Has to have 1 InstanceHierarchy");
		}
		Document hieDoc = null;
		for (Element rootIE : rootIEs) {
			Elements childElements = rootIE.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				hieDoc = new Document((Element) childElements.get(i).copy());
			}
		}
		return hieDoc;
	}

	public static Document getInstanceHierarchiesOnly(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootIEs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("InstanceHierarchy")) {
				rootIEs.add(childOfRoot);
			}
		}
		Element caexRoot = new Element("CAEXFile");
		Document hieDoc = new Document(caexRoot);
		for (Element rootIE : rootIEs) {
				caexRoot.appendChild((Element) rootIE.copy());
		}
		return hieDoc;
	}
	
	public static Map<String, Document> getChildrenOfDocAsDocs(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootIEs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("InstanceHierarchy")) {
				rootIEs.add(childOfRoot);
			}
		}
		if (rootIEs.size() != 1) {
			throw new IllegalArgumentException("Has to have 1 InstanceHierarchy");
		}
		Map<String, Document> documentsMap = new HashMap<>();
		for (Element rootIE : rootIEs) {
			Elements childElements = rootIE.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				Element element = childElements.get(i);
				Document hieDoc = new Document((Element) element.copy());
				if (element.getLocalName().equalsIgnoreCase("InternalElement")) {
					String id = element.getAttribute("ID").getValue();
					documentsMap.put(id, hieDoc);
				}
			}
		}
		return documentsMap;
	}
	
	public static Map<String, Document> getSecondChildrenOfDocAsDocs(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootIEs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("InstanceHierarchy")) {
				rootIEs.add(childOfRoot);
			}
		}
		if (rootIEs.size() != 1) {
			throw new IllegalArgumentException("Has to have 1 InstanceHierarchy");
		}
		Map<String, Document> documentsMap = new HashMap<>();
		for (Element rootIE : rootIEs) {
			Elements childElements = rootIE.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				Elements childElements2 = ((Element) childElements.get(i)).getChildElements();
				for (int j = 0; j < childElements2.size(); j++) {
					Element element = childElements2.get(j);
					Document hieDoc = new Document((Element) element.copy());
					if (element.getLocalName().equalsIgnoreCase("InternalElement")) {
						String id = element.getAttribute("ID").getValue();
						documentsMap.put(id, hieDoc);
					}
				}
			}
		}
		return documentsMap;
	}
	
	public static Document excludeChildrenFromDocuments(Document doc, List<String> excludedStrings) {
		Document copyDoc = (Document) doc.copy();
		Element root = copyDoc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootLibs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			rootLibs.add(childrenOfRoot.get(i));
		}
		if (rootLibs.size() != 1) {
			throw new IllegalArgumentException("Has to have 1 InstanceHierarchy");
		}
		for (Element rootLib : rootLibs) {
			Elements childElements = rootLib.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				Element element = childElements.get(i);
				String name = element.getAttribute("Name").getValue();
				boolean shouldBeExcluded = false;
				for (String toExclude : excludedStrings) {
					if (name.contains(toExclude)) {
						shouldBeExcluded = true;
						break;
					}
				}
				if (shouldBeExcluded) {
					rootLib.removeChild(element);
				}
			}
		}
		return copyDoc;
	}
	
	private String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static Document getExportedAsDoc(Set<Object> objects, boolean shortVersion) {
		INSTANCE.wipeData();
		INSTANCE.initializeInputsFromObjectSet(objects);
		
		Document doc = INSTANCE.initializeDoc("AMLDescription.aml", shortVersion);
		return doc;
	}
	
	public static void saveFile(String filepath, Document doc) throws TransformerException {
		doc.getRootElement().addAttribute(new Attribute("FileName", filepath));
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamSource source = new StreamSource(new ByteArrayInputStream(doc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(new File(filepath));
		
		transformer.transform(source, result);
		
		System.out.println("File saved to: " + filepath);
	}
	
	@SuppressWarnings("unchecked")
	private void initializeInputsFromObjectSet(Set<Object> objects) {
		for (Object obj : objects) {
			if (obj instanceof Root && ((Root<?>) obj).getDomainClass().isAssignableFrom(InternalElement.class)) {
				instanceHierarchies.add((Root<InternalElement>) obj);
			} else if (obj instanceof Root && ((Root<?>) obj).getDomainClass().isAssignableFrom(Role.class)) {
				roleClassLibs.add((Root<Role>) obj);
			} else if (obj instanceof Root && ((Root<?>) obj).getDomainClass().isAssignableFrom(Interface.class)) {
				interfaceClassLibs.add((Root<Interface>) obj);
			} else if (obj instanceof Root && ((Root<?>) obj).getDomainClass().isAssignableFrom(SystemUnit.class)) {
				systemUnitClassLibs.add((Root<SystemUnit>) obj);
			}
		}
		
		List<Root<?>> roots = new ArrayList<>();
		roots.addAll(instanceHierarchies);
		roots.addAll(roleClassLibs);
		roots.addAll(interfaceClassLibs);
		roots.addAll(systemUnitClassLibs);
		
		INSTANCE.initializePathsFromRoots(roots);
	}
	
	private Document initializeDoc(String filepath, boolean shortVersion) {
		Element rootElement = INSTANCE.createRootElement(filepath);
		Document doc = new Document(rootElement);
		if (!shortVersion) {
			//add short additional information
			Element shortInformation = new Element("AdditionalInformation");
			shortInformation.addAttribute(new Attribute("AutomationMLVersion", "2.0"));
			
			rootElement.appendChild(shortInformation);
			//add longer additional information
			rootElement.appendChild(INSTANCE.createAdditionalInformation());
		}
		for (Root<InternalElement> root : INSTANCE.instanceHierarchies) {
			rootElement.appendChild(INSTANCE.createInstanceHierarchy(root));
		}
		
		for (Root<Interface> root : INSTANCE.interfaceClassLibs) {
			rootElement.appendChild(INSTANCE.createInterfaceClassLib(root));
		}
		
		for (Root<Role> root : INSTANCE.roleClassLibs) {
			rootElement.appendChild(INSTANCE.createRoleClassLib(root));
		}
		
		for (Root<SystemUnit> root : INSTANCE.systemUnitClassLibs) {
			rootElement.appendChild(INSTANCE.createSystemUnitClassLib(root));
		}
		return doc;
	}
	
	public static String saveFile() throws TransformerException {
		INSTANCE.wipeData();
		INSTANCE.initializeAllInputs();
		// root elements
		String pathname = "dummy.aml";
		Element rootElement = INSTANCE.createRootElement(pathname);
		Document doc = new Document(rootElement);
		//add short additional information
		Element shortInformation = new Element("AdditionalInformation");
		shortInformation.addAttribute(new Attribute("AutomationMLVersion", "2.0"));
		
		rootElement.appendChild(shortInformation);
		//add longer additional information
		rootElement.appendChild(INSTANCE.createAdditionalInformation());
		
		for (Root<InternalElement> root : INSTANCE.instanceHierarchies) {
			rootElement.appendChild(INSTANCE.createInstanceHierarchy(root));
		}
		
		for (Root<Interface> root : INSTANCE.interfaceClassLibs) {
			rootElement.appendChild(INSTANCE.createInterfaceClassLib(root));
		}
		
		for (Root<Role> root : INSTANCE.roleClassLibs) {
			rootElement.appendChild(INSTANCE.createRoleClassLib(root));
		}
		
		for (Root<SystemUnit> root : INSTANCE.systemUnitClassLibs) {
			rootElement.appendChild(INSTANCE.createSystemUnitClassLib(root));
		}
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		StreamSource source = new StreamSource(new ByteArrayInputStream(doc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(new File(pathname));
		
		transformer.transform(source, result);
		
		return AMLParser.getInstance().getContentStringFromFile(pathname);
	}
	
	private void initializePathsFromRoots(List<Root<?>> roots) {
		for (Root<?> root : roots) {
			String path = root.getName();
			pathsMap.put(root, path);
			traversePathsFromHierarchies(root.getChildren(), path);
		}
	}
	
	private void traversePathsFromHierarchies(List<?> list, String previousPath) {
		for (Object obj : list) {
			if (obj instanceof Hierarchy<?>) {
				Hierarchy<?> hie = (Hierarchy<?>) obj;
				String currentPath = previousPath + "/" + hie.getElement().getName();
				pathsMap.put(hie, currentPath);
				pathsMap.put(hie.getElement(), currentPath);
				//recursion
				traversePathsFromHierarchies(hie.getChildren(), currentPath);
			}
		}
	}
	
	private Element createRootElement(String filename) {
		Element root = new Element("CAEXFile");
		String[] tokens = filename.split("\\\\+");
		String nameOfTheFile = tokens[tokens.length - 1];
		root.addAttribute(new Attribute("FileName", nameOfTheFile));
		root.addAttribute(new Attribute("SchemaVersion", "2.15"));
		root.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.addAttribute(new Attribute("xsi:noNamespaceSchemaLocation", "http://www.w3.org/2001/XMLSchema-instance", "CAEX_ClassModel_V2.15.xsd"));
		return root;
	}
	
	private Element createAdditionalInformation() {
		Element additionalInformation = new Element("AdditionalInformation");
		//1st level children
		Element writerHeader = new Element("WriterHeader");
		//2nd level children
		Element writerName = new Element("WriterName");
		writerName.appendChild("unspecified");
		
		Element writerID = new Element("WriterID");
		writerID.appendChild("AutomationML Editor");
		
		Element writerVendor = new Element("WriterVendor");
		writerVendor.appendChild("unspecified");
		
		Element writerVendorURL = new Element("WriterVendorURL");
		writerVendorURL.appendChild("www.AutomationML.org");
		
		Element writerVersion = new Element("WriterVersion");
		writerVersion.appendChild("3.0.0");
		
		Element writerRelease = new Element("WriterRelease");
		writerRelease.appendChild("unspecified");
		
		Element lastWritingDateTime = new Element("LastWritingDateTime");
		lastWritingDateTime.appendChild("unspecified");
		
		Element writerProjectTitle = new Element("WriterProjectTitle");
		writerProjectTitle.appendChild("unspecified");
		
		Element writerProjectID = new Element("WriterProjectID");
		writerProjectID.appendChild("unspecified");
		
		writerHeader.appendChild(writerName);
		writerHeader.appendChild(writerID);
		writerHeader.appendChild(writerVendor);
		writerHeader.appendChild(writerVendorURL);
		writerHeader.appendChild(writerVersion);
		writerHeader.appendChild(writerRelease);
		writerHeader.appendChild(lastWritingDateTime);
		writerHeader.appendChild(writerProjectTitle);
		writerHeader.appendChild(writerProjectID);
		
		additionalInformation.appendChild(writerHeader);
		
		return additionalInformation;
	}
	
	private Element createInstanceHierarchy(Root<InternalElement> root) {
		Element instanceHierarchy = new Element("InstanceHierarchy");
		instanceHierarchy.addAttribute(new Attribute("Name", root.getName()));
		
		for (Hierarchy<InternalElement> child : root.getChildren()) {
			instanceHierarchy.appendChild(createInternalElement(child));
		}
		return instanceHierarchy;
	}
	
	private Element createInternalElement(Hierarchy<InternalElement> hie) {
		if (hie.getActualElement().getReferencedInternalElement() != null) {
			return createReferencedElement(hie);
		}
		
		Element element = new Element("InternalElement");
		InternalElement ie = hie.getElement();
		element.addAttribute(new Attribute("Name", ie.getName()));
		element.addAttribute(new Attribute("ID", ie.getId()));
		
		List<Element> normalAttributes = extractAttributesFromDomain(ie);
		for (Element att : normalAttributes) {
			element.appendChild(att);
		}
		
		//external interfaces
		for (InterfaceDesignator des : ie.getInterfaceDesignators()) {
			element.appendChild(doExternalnterface(des));
		}
		
		//children
		for (Hierarchy<InternalElement> child : hie.getChildren()) {
			element.appendChild(createInternalElement(child));
		}
		
		//supported roles
		for (Role supportedRole : ie.getSupportedRoles()) {
			Element supportedRoleClass = new Element("SupportedRoleClass");
			supportedRoleClass.addAttribute(new Attribute("RefRoleClassPath", pathsMap.get(supportedRole)));
			element.appendChild(supportedRoleClass);
		}
		
		//internal links
		for (InternalLink link : ie.getInternalLinks()) {
			//append to element
			element.appendChild(doInternalLink(link));
		}
		
		//RoleRequirements
		Role requiredRole = ie.getRequiredRole();
		if (requiredRole != null) {
			element.appendChild(doRoleRequirements(requiredRole));
		}
		
		return element;
	}
	
	
	
	private Element createReferencedElement(Hierarchy<InternalElement> hie) {
			Element element = new Element("InternalElement");
			InternalElement ie = hie.getElement();
			element.addAttribute(new Attribute("Name", ie.getName()));
			element.addAttribute(new Attribute("ID", hie.getActualElement().getId()));
			element.addAttribute(new Attribute("RefBaseSystemUnitPath", ie.getId()));
			//mirrored elements should not possess any children
			return element;
	}
	
	
	private List<Element> extractAttributesFromDomain(Domain domain) {
		List<Element> attributes = new ArrayList<>();
		for (AttributeDesignator des : domain.getDesignators()) {
			if (des.getAttribute().getParent() == null) {
				attributes.add(doAttributeDesignator(des));
			}
		}
		return attributes;
	}

	private Element createInterfaceClassLib(Root<Interface> root) {
		Element interfaceClassLib = new Element("InterfaceClassLib");
		interfaceClassLib.addAttribute(new Attribute("Name", root.getName()));
		
		//default lib
		if (root.getName().equalsIgnoreCase("AutomationMLInterfaceClassLib")) {
			Element version = new Element("Version");
			version.appendChild("2.2.0");
			interfaceClassLib.appendChild(version);
		}
		
		for (Hierarchy<Interface> child : root.getChildren()) {
			interfaceClassLib.appendChild(createInterfaceElement(child));
		}
		return interfaceClassLib;
	}
	
	private Element createInterfaceElement(Hierarchy<Interface> hie) {
		Element element = new Element("InterfaceClass");
		Interface interfais = hie.getElement();
		element.addAttribute(new Attribute("Name", interfais.getName()));
		Interface referencedInterface = interfais.getReferencedInterface();
		if (referencedInterface != null) {
			element.addAttribute(new Attribute("RefBaseClassPath", pathsMap.get(referencedInterface)));
		}
		
		List<Element> normalAttributes = extractAttributesFromDomain(interfais);
		for (Element att : normalAttributes) {
			element.appendChild(att);
		}
		//children
		for (Hierarchy<Interface> child : hie.getChildren()) {
			element.appendChild(createInterfaceElement(child));
		}
		return element;
	}

	private Element createRoleClassLib(Root<Role> root) {
		Element roleClassLib = new Element("RoleClassLib");
		
		if (root.getName().equalsIgnoreCase("AutomationMLBaseRoleClassLib")) {
			Element version = new Element("Version");
			version.appendChild("2.2.0");
			roleClassLib.appendChild(version);
		}
		roleClassLib.addAttribute(new Attribute("Name", root.getName()));
		
		for (Hierarchy<Role> child : root.getChildren()) {
			roleClassLib.appendChild(createRoleElement(child));
		}
		
		return roleClassLib;
	}
	
	private Element createRoleElement(Hierarchy<Role> hie) {
		Element element = new Element("RoleClass");
		Role role = hie.getElement();
		element.addAttribute(new Attribute("Name", role.getName()));
		Role referencedRole = role.getReferencedRole();
		if (referencedRole != null) {
			element.addAttribute(new Attribute("RefBaseClassPath", pathsMap.get(referencedRole)));
		}
		
		//description
		String description = role.getDescription();
		if (description != null && !description.equals("")) {
			Element descriptionElement = new Element("Description");
			descriptionElement.appendChild(description);
			element.appendChild(descriptionElement);
		}
		
		List<Element> normalAttributes = extractAttributesFromDomain(role);
		for (Element att : normalAttributes) {
			//append to element
			element.appendChild(att);
		}
		
		for (InterfaceDesignator des : role.getInterfaceDesignators()) {
			Element externalInterface = new Element("ExternalInterface");
			
			externalInterface.addAttribute(new Attribute("Name", des.getName()));
			if (des.getId() != null) {
				externalInterface.addAttribute(new Attribute("ID", des.getId()));
			}
			externalInterface.addAttribute(new Attribute("RefBaseClassPath", pathsMap.get(des.getBaseInterface())));
			//append to element
			element.appendChild(externalInterface);
		}
		
		//children
		for (Hierarchy<Role> child : hie.getChildren()) {
			element.appendChild(createRoleElement(child));
		}
				
		
		return element;
	}

	private Element createSystemUnitClassLib(Root<SystemUnit> root) {
		Element systemUnitClassLib = new Element("SystemUnitClassLib");
		
		systemUnitClassLib.addAttribute(new Attribute("Name", root.getName()));
		
		for (Hierarchy<SystemUnit> child : root.getChildren()) {
			systemUnitClassLib.appendChild(createSystemUnitClass(child));
		}
		
		return systemUnitClassLib;
	}
	
	private Element createSystemUnitClass(Hierarchy<SystemUnit> hie) {
		Element element = new Element("SystemUnitClass");
		SystemUnit systemUnit = hie.getElement();
		element.addAttribute(new Attribute("Name", systemUnit.getName()));
		if (systemUnit.getReferencedSystemUnit() != null) {
			element.addAttribute(new Attribute("RefBaseClassPath", pathsMap.get(systemUnit.getReferencedSystemUnit())));
		}
		List<Element> normalAttributes = extractAttributesFromDomain(systemUnit);
		for (Element att : normalAttributes) {
			element.appendChild(att);
		}
		//external interfaces
		for (InterfaceDesignator des : systemUnit.getInterfaceDesignators()) {
			element.appendChild(doExternalnterface(des));
		}
		
		for (Hierarchy<InternalElement> ieHie : systemUnit.getIeHierarchies()) {
			element.appendChild(createInternalElement(ieHie));
		}
		//supported roles
		for (Role supportedRole : systemUnit.getSupportedRoles()) {
			Element supportedRoleClass = new Element("SupportedRoleClass");
			supportedRoleClass.addAttribute(new Attribute("RefRoleClassPath", pathsMap.get(supportedRole)));
			element.appendChild(supportedRoleClass);
		}
		//internal links
		for (InternalLink link : systemUnit.getInternalLinks()) {
			element.appendChild(doInternalLink(link));
		}
		
		for (Hierarchy<SystemUnit> child : hie.getChildren()) {
			element.appendChild(createSystemUnitClass(child));
			
		}
		
		return element;
	}
	
	private Element doInternalLink(InternalLink link) {
		Element internalLink = new Element("InternalLink");
		
		internalLink.addAttribute(new Attribute("Name", link.getName()));
		internalLink.addAttribute(new Attribute("RefPartnerSideA",((InternalElement) link.getRefA().getDomain()).getId()
				+ ":" + link.getRefA().getName()));
		
		internalLink.addAttribute(new Attribute("RefPartnerSideB", ((InternalElement) link.getRefB().getDomain()).getId() + ":" 
				+ link.getRefB().getName()));
		return internalLink;
	}
	
	private Element doExternalnterface(InterfaceDesignator interDes) {
		Element externalInterface = new Element("ExternalInterface");
		
		externalInterface.addAttribute(new Attribute("Name", interDes.getName()));
		externalInterface.addAttribute(new Attribute("ID", interDes.getId()));
		externalInterface.addAttribute(new Attribute("RefBaseClassPath", pathsMap.get(interDes.getBaseInterface())));
		
		for (AttributeDesignator attDes : interDes.getDesignators()) {
			externalInterface.appendChild(doAttributeDesignator(attDes));
		}
		
		return externalInterface;
	}
	
	private Element doAttributeDesignator(AttributeDesignator des) {
		aml.model.Attribute attributeFromDomain = des.getAttribute();
		Element attribute = doAttribute(attributeFromDomain);
		//value
		if (des.getValue() != null && !des.getValue().equals("")) {
			Element value = new Element("Value");
			value.appendChild(des.getValue());
			attribute.appendChild(value);
		}
		//constraints
		for (Constraint con : des.getConstraints()) {
			attribute.appendChild(doConstraint(con));
		}
		for (aml.model.Attribute sub : attributeFromDomain.getSubAttributes()) {
			for (AttributeDesignator subDes : sub.getDesignators()) {
				if (des.getAMLObject().equals(subDes.getAMLObject())) {
					attribute.appendChild(doAttributeDesignator(subDes));
				}
			}
		}
		return attribute;
	}
	
	private Element doRoleRequirements(Role requiredRole) {
		Element roleRequirement = new Element("RoleRequirements");
		roleRequirement.addAttribute(new Attribute("RefBaseRoleClassPath", pathsMap.get(requiredRole)));
		
		return roleRequirement;
	}
	
	private Element doConstraint(Constraint con) {
		Element constraint = new Element("Constraint");
		constraint.addAttribute(new Attribute("Name", con.getName()));
		if (con instanceof OrdinalConstraint) {
			Element ordinal = new Element("OrdinalScaledType");
			String requiredValue = ((OrdinalConstraint) con).getRequiredValue();
			
			Element maxElement = new Element("RequiredMaxValue");
			maxElement.appendChild("" + ((OrdinalConstraint) con).getMaxValue());
			//append max value
			ordinal.appendChild(maxElement);
			if (requiredValue != null && !requiredValue.equals("")) {
				Element req = new Element("RequiredValue");
				req.appendChild(requiredValue);
				//append required value
				ordinal.appendChild(req);
			}
			
			Element minElement = new Element("RequiredMinValue");
			minElement.appendChild("" + ((OrdinalConstraint) con).getMinValue());
			//append min value
			ordinal.appendChild(minElement);
			//append ordinal to constraint
			constraint.appendChild(ordinal);
		} else if (con instanceof NominalConstraint) {
			Element nominal = new Element("NominalScaledType");
			
			for (String value : ((NominalConstraint) con).getValues()) {
				Element req = new Element("RequiredValue");
				req.appendChild(value);
				nominal.appendChild(req);
			}
			//append nominal to constraint
			constraint.appendChild(nominal);
		}
		
		return constraint;
	}

	private Element doAttribute(aml.model.Attribute attributeFromDomain) {
		Element attribute = new Element("Attribute");
		Attribute name = new Attribute("Name", attributeFromDomain.getName());
		attribute.addAttribute(name);
		if (attributeFromDomain.getUnit() != null && !attributeFromDomain.getUnit().equals("")) {
			Attribute unit = new Attribute("Unit", attributeFromDomain.getUnit());
			attribute.addAttribute(unit);
		}
		
		if (attributeFromDomain.getAttributeType() != null) {
			Attribute type = new Attribute("AttributeDataType", "xs:" + attributeFromDomain.getAttributeType().toString());
			attribute.addAttribute(type);
		}
		
		if (attributeFromDomain.getDescription() != null && !attributeFromDomain.getDescription().equals("")) {
			Element description = new Element("Description");
			//add the description
			description.appendChild(attributeFromDomain.getDescription());
			attribute.appendChild(description);
		}
		
		return attribute;
	}
	
}
