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

package skillpro.asset.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import skillpro.model.assets.SEE;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import aml.amlparser.AMLExporter;
import aml.amlparser.AMLParser;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.skillpro.mapping.TransformationMappingParser;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformer.ReverseTransformer;
import aml.transformation.service.AMLTransformationService;

public class UpdateSEEsAMLHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		updateSEEs();
		return null;
	}

	private void updateSEEs() {
		for (SEE see : SkillproService.getSkillproProvider().getSEERepo()) {
			if (see.getAmlDescription() == null || see.getAmlDescription().isEmpty()) {
				throw new IllegalArgumentException(
						"AML Description of this SEE: " + see + ", is empty.");
			}
			List<ResourceExecutableSkill> relevantResourceExecutableSkills = new ArrayList<>();
			for (ExecutableSkill ex : SkillproService.getSkillproProvider()
					.getSkillRepo().getExecutableSkills()) {
				for (ResourceExecutableSkill rex : ex
						.getResourceExecutableSkills()) {
					if (rex.getResource().equals(see.getResource())) {
						relevantResourceExecutableSkills.add(rex);
					}
				}
			}

			String amlDescription = see.getAmlDescription();
			try {
				amlDescription = excludeRExsFromAMLDescription(amlDescription);
				amlDescription = appendRExsToAMLDescription(relevantResourceExecutableSkills, amlDescription);
				see.setAmlDescription(amlDescription);
			} catch (ParsingException | IOException
					| TransformerException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("Updating SEE has encountered an error.");
			}
			
		}
	}
	
	private String excludeRExsFromAMLDescription(String amlDescription)
			throws ParsingException, IOException, TransformerException {
		Document doc = AMLParser.getInstance().getDocumentFromString(amlDescription);
		Document copyDoc = (Document) doc.copy();
		Element root = copyDoc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootLibs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			rootLibs.add(childrenOfRoot.get(i));
		}
		List<Element> seeElements = new ArrayList<>();
		for (Element rootLib : rootLibs) {
			Elements childElements = rootLib.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				Element element = childElements.get(i);
				boolean isSEE = false;
				for (int j = 0; j < element.getChildElements().size(); j++) {
					Element childElement = element.getChildElements().get(j);
					if (childElement.getLocalName().equalsIgnoreCase("RoleRequirements")) {
						if (childElement.getAttribute("RefBaseRoleClassPath").getValue().contains("SkillExecutionEngine")) {
							isSEE = true;
							break;
						}
					}
				}
				if (isSEE) {
					seeElements.add(element);
				}
			}
		}
		
		for (Element seeElement : seeElements) {
			boolean shouldBeExcluded = false;
			for (int j = 0; j < seeElement.getChildElements().size(); j++) {
				Element childElement = seeElement.getChildElements().get(j);
				for (int k = 0; k < childElement.getChildElements().size(); k++) {
					Element childOfChildElement = childElement.getChildElements().get(k);
					if (childOfChildElement.getLocalName().equalsIgnoreCase("RoleRequirements")) {
						if (childOfChildElement.getAttribute("RefBaseRoleClassPath").getValue().contains("ResourceExecutableSkill")) {
							shouldBeExcluded = true;
							break;
						}
					}
				}
				
				if (shouldBeExcluded) {
					seeElement.removeChild(childElement);
					childElement.detach();
				}
			}
			
			
		}
		return AMLExporter.getExportedAsString(copyDoc);
	}
	
	private String appendRExsToAMLDescription(
			List<ResourceExecutableSkill> rexSkills, String amlDescription)
			throws ParsingException, IOException, TransformerException {
		Document doc = AMLParser.getInstance().getDocumentFromString(amlDescription);
		Document copyDoc = (Document) doc.copy();

		List<Element> rexElements = extractResourceExecutableSkillElementsFromDoc(reverseTransformRExsToAML(rexSkills));
		
		Element root = copyDoc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootLibs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			rootLibs.add(childrenOfRoot.get(i));
		}
		List<Element> seeElements = new ArrayList<>();
		for (Element rootLib : rootLibs) {
			Elements childElements = rootLib.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				Element element = childElements.get(i);
				boolean isSEE = false;
				for (int j = 0; j < element.getChildElements().size(); j++) {
					Element childElement = element.getChildElements().get(j);
					if (childElement.getLocalName().equalsIgnoreCase("RoleRequirements")) {
						if (childElement.getAttribute("RefBaseRoleClassPath").getValue().contains("SkillExecutionEngine")) {
							isSEE = true;
							break;
						}
					}
				}
				if (isSEE) {
					seeElements.add(element);
				}
			}
		}
		
		for (Element seeElement : seeElements) {
			for (Element rexElement : rexElements) {
				rexElement.detach();
				seeElement.appendChild(rexElement);
			}
		}
		return AMLExporter.getExportedAsString(copyDoc);
	}
	
	//this only works for this specific type 
	private List<Element> extractResourceExecutableSkillElementsFromDoc(Document doc) {
		Document copyDoc = (Document) doc.copy();
		List<Element> rexElements = new ArrayList<>();

		Element root = copyDoc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		List<Element> rootLibs = new ArrayList<>();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			rootLibs.add(childrenOfRoot.get(i));
		}
		for (Element rootLib : rootLibs) {
			Elements childElements = rootLib.getChildElements();
			for (int i = 0; i < childElements.size(); i++) {
				Element element = childElements.get(i);
				boolean isREX = false;
				for (int j = 0; j < element.getChildElements().size(); j++) {
					Element childElement = element.getChildElements().get(j);
					if (childElement.getLocalName().equalsIgnoreCase("RoleRequirements")) {
						if (childElement.getAttribute("RefBaseRoleClassPath").getValue().contains("ResourceExecutableSkill")) {
							isREX = true;
							break;
						}
					}
				}
				if (isREX) {
					rexElements.add(element);
				}
			}
		}
		return rexElements;
	}
	
	@SuppressWarnings("unchecked")
	private Document reverseTransformRExsToAML(List<ResourceExecutableSkill> rexSkills) {
		Set<Role> roles = new HashSet<>(AMLTransformationService.getAMLProvider().getAMLRoleRepo().getEntities());
		Set<Hierarchy<InternalElement>> hierarchies = new HashSet<>();
		List<Hierarchy<Role>> roleHierarchies = AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(Role.class).getFlattenedHierarchies();
		List<Hierarchy<Interface>> interfaceHierarchies = AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(Interface.class).getFlattenedHierarchies();
		Set<Object> parsedObjects = new HashSet<>();
		parsedObjects.addAll(AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(Role.class).getEntities());
		parsedObjects.addAll(AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(Interface.class).getEntities());

		parseTransformationMapping("DefaultMapping.xml", roles, hierarchies);

		AMLTransformationService.getTransformationProvider().getTransformationRepo().getReverseTransformedObjectsMap().clear();
		reverseTransform(new ArrayList<>(rexSkills), roleHierarchies, interfaceHierarchies);
		// add reverse transformed object
		Root<InternalElement> defaultRoot = new Root<InternalElement>("Configuration", InternalElement.class);
		parsedObjects.add(defaultRoot);
		Collection<Object> objects = AMLTransformationService.getTransformationProvider().getTransformationRepo().getReverseTransformedObjectsMap().values();
		for (Object obj : objects) {
			if (obj instanceof Interface) {
				parsedObjects.add((Interface) obj);
			} else if (obj instanceof Role) {
				parsedObjects.add((Role) obj);
			} else if (obj instanceof InternalElement) {
				parsedObjects.add((InternalElement) obj);
			} else if (obj instanceof SystemUnit) {
				parsedObjects.add((SystemUnit) obj);
			} else if (obj instanceof Root<?>) {
				parsedObjects.add((Root<?>) obj);
			} else if (obj instanceof Hierarchy<?>) {
				Hierarchy<?> hie = (Hierarchy<?>) obj;
				if (hie.getElement() instanceof InternalElement && hie.getParent() == null) {
					defaultRoot.addChild((Hierarchy<InternalElement>) hie);
				}
				parsedObjects.add(hie);
			} else {
				// do nothing
			}
		}
		//FIXME delete later
//		try {
//			System.out.println(AMLExporter.getExportedAsString(AMLExporter.getExportedAsDoc(parsedObjects, true)));
//		} catch (TransformerException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return AMLExporter.getExportedAsDoc(parsedObjects, true);
	}
	
	private void parseTransformationMapping(String transformationMappingPath, Set<Role> roles, Set<Hierarchy<InternalElement>> hierarchies) {
		try {
			TransformationMappingParser.loadTransformationMapping(transformationMappingPath, roles, hierarchies);
		} catch (ParsingException | IOException e) {
			e.printStackTrace();
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			throw new IllegalArgumentException("Transformation didn't function correctly");
		}
	}
	
	private void reverseTransform(List<ResourceExecutableSkill> rexSkills, List<Hierarchy<Role>> roleHierarchies, List<Hierarchy<Interface>> interfaceHierarchies) {
		// Set role and interface hierarchies before performing reverse
		// transformation
		TransformableAdapterTemplate.setCurrentRoleHierarchies(roleHierarchies);
		TransformableAdapterTemplate.setCurrentInterfaceHierarchies(interfaceHierarchies);
		// reverse transform SEE
		System.out.println("REX SKILLS: " + rexSkills.size());
		ReverseTransformer.getInstance().reverseTransformResourceExecutableSkills(rexSkills);
		// REVERT
		TransformableAdapterTemplate.revertEverything();
	}
}
