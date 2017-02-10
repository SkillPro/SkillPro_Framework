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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.transformation.service.AMLTransformationService;

public class TransformationMappingExporter {
	private static final TransformationMappingExporter INSTANCE = new TransformationMappingExporter();

	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private TransformationMappingExporter() {
	}
	
	public static void saveConfiguration(String filepath) throws TransformerException {
		INSTANCE.saveFile(filepath);
	}
	
	public static void addToExistingConfiguration(String filepath) throws ValidityException, TransformerException, ParsingException, IOException {
		INSTANCE.addToExistingFile(filepath);
	}
	
	private void addToExistingFile(String filepath) throws TransformerException, ValidityException, ParsingException, IOException {
		Map<String, Element> mappedRoles = new HashMap<>();
		
		Document loadDoc = getDocumentFromFile(filepath);
		Element loadRoot = loadDoc.getRootElement();
		Elements childrenOfRoot = loadRoot.getChildElements();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			String roleName = getNameFromElement(childOfRoot);
			mappedRoles.put(roleName, childOfRoot);
			
		}
		
		Map<Object, Class<? extends ITransformable>> transformables = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping();
		for (Object object : transformables.keySet()) {
			if (object instanceof Role) {
				Role role = (Role) object;
				Element mappedElement = mappedRoles.get(role.getName());
				if (mappedElement == null) {
					Element roleElement = new Element("RuleElement");
					roleElement.addAttribute(new Attribute("Name", role.getName()));
					roleElement.addAttribute(new Attribute("Transformable", transformables.get(role).getSimpleName()));
					loadRoot.appendChild(roleElement);
					mappedRoles.put(role.getName(), roleElement);
				} else {
					mappedElement.getAttribute("Transformable").setValue(transformables.get(role).getSimpleName());
				}
			} else {
				throw new IllegalArgumentException("Type not expected: " + object);
			}
		}
		
		Map<Role, Map<Class<? extends ITransformable>, Integer>> outerMap = new HashMap<>();
		Map<Object, Class<? extends ITransformable>> elementTransformables = AMLTransformationService.getTransformationProvider().getTransformationRepo().getAdapterTransformablesMapping();
		for (Object object : elementTransformables.keySet()) {
			if (object instanceof InternalElement) {
				InternalElement ie = (InternalElement) object;
				Role requiredRole = ie.getRequiredRole();
				Map<Class<? extends ITransformable>, Integer> innerMap = outerMap.get(requiredRole);
				if (innerMap != null) {
					Integer numberOfOccurrence = innerMap.get(elementTransformables.get(ie));
					if (numberOfOccurrence != null) {
						innerMap.put(elementTransformables.get(ie), numberOfOccurrence + 1);
					} else {
						innerMap.put(elementTransformables.get(ie), 1);
					}
				} else {
					innerMap = new HashMap<>();
					innerMap.put(elementTransformables.get(ie), 1);
					outerMap.put(requiredRole, innerMap);
				}
			} else {
				throw new IllegalArgumentException("Type not expected: " + object);
			}
			
		}
		
		for (Role role : outerMap.keySet()) {
			Element mappedElement = mappedRoles.get(role.getName());
			if (mappedElement == null) {
				throw new IllegalArgumentException("Role hasn't been mapped");
			} else {
				Attribute defaultClassAttribute = mappedElement.getAttribute("DefaultClass");
				if (defaultClassAttribute == null) {
					mappedElement.addAttribute(new Attribute("DefaultClass", findMostOftenUsedTransformable(outerMap.get(role)).getSimpleName()));
				} else {
					defaultClassAttribute.setValue(findMostOftenUsedTransformable(outerMap.get(role)).getSimpleName());
					
				}
			}
		}
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		StreamSource source = new StreamSource(new ByteArrayInputStream(loadDoc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(new File(filepath));
		
		// Output to console for testing
		transformer.transform(source, result);
	}
	
	private Document getDocumentFromFile(String filepath) throws ValidityException, ParsingException, IOException {
		Builder parser = new Builder();
		Document result = parser.build(new File(filepath));
		return result;
	}
	
	private void saveFile(String filepath) throws TransformerException {
		// root elements
		Element rootElement = new Element("Configuration");
		Document doc = new Document(rootElement);
		List<Element> children = new ArrayList<>();
		
		Map<Object, Class<? extends ITransformable>> transformables = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping();
		for (Object object : transformables.keySet()) {
			if (object instanceof Role) {
				Role role = (Role) object;
				Element roleElement = new Element("RuleElement");
				roleElement.addAttribute(new Attribute("Name", role.getName()));
				roleElement.addAttribute(new Attribute("Transformable", transformables.get(role).getSimpleName()));
				children.add(roleElement);
				rootElement.appendChild(roleElement);
			} else {
				throw new IllegalArgumentException("Type not expected: " + object);
			}
		}
		Map<Role, Map<Class<? extends ITransformable>, Integer>> outerMap = new HashMap<>();
		Map<Object, Class<? extends ITransformable>> elementTransformables = AMLTransformationService.getTransformationProvider().getTransformationRepo().getAdapterTransformablesMapping();
		for (Object object : elementTransformables.keySet()) {
			if (object instanceof InternalElement) {
				InternalElement ie = (InternalElement) object;
				Role requiredRole = ie.getRequiredRole();
				Map<Class<? extends ITransformable>, Integer> innerMap = outerMap.get(requiredRole);
				if (innerMap != null) {
					Integer numberOfOccurrence = innerMap.get(elementTransformables.get(ie));
					if (numberOfOccurrence != null) {
						innerMap.put(elementTransformables.get(ie), numberOfOccurrence + 1);
					} else {
						innerMap.put(elementTransformables.get(ie), 1);
					}
				} else {
					innerMap = new HashMap<>();
					innerMap.put(elementTransformables.get(ie), 1);
					outerMap.put(requiredRole, innerMap);
				}
			} else {
				throw new IllegalArgumentException("Type not expected: " + object);
			}
		}
		
		for (Role role : outerMap.keySet()) {
			Element ele = searchForElement(children, role.getName());
			ele.addAttribute(new Attribute("DefaultClass", findMostOftenUsedTransformable(outerMap.get(role)).getSimpleName()));
			
		}
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		StreamSource source = new StreamSource(new ByteArrayInputStream(doc.toXML().getBytes(UTF8)));
		StreamResult result = new StreamResult(new File(filepath));
		// Output to console for testing
		transformer.transform(source, result);
	}
	

	
	private Class<?> findMostOftenUsedTransformable(Map<Class<? extends ITransformable>, Integer> map) {
		Class<?> result = null;
		Integer biggestInteger = 0;
		for (Class<?> cls : map.keySet()) {
			Integer candidate = map.get(cls);
			if (biggestInteger < candidate) {
				biggestInteger = candidate;
				result = cls;
				break;
			}
		}
		return result;
	}
	
	private Element searchForElement(List<Element> elements, String name) {
		for (Element ele : elements) {
			if (getNameFromElement(ele).equalsIgnoreCase(name)) {
				return ele;
			}
		}
		return null;
	}

	public static TransformationMappingExporter getInstance() {
		return INSTANCE;
	}
	
	private String getNameFromElement(Element element) {
		if (element.getAttribute("Name") != null) {
			return element.getAttribute("Name").getValue();
			
		}
		return null;
	}
}
