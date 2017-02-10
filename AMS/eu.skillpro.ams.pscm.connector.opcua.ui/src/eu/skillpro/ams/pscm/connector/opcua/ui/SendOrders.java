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

package eu.skillpro.ams.pscm.connector.opcua.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import javax.xml.transform.TransformerException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.model.products.Order;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.update.UpdateType;
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
import eu.skillpro.ams.pscm.connector.amsservice.ui.SendExecutableSkillToServer;
import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.connector.opcua.SkillProOPCUAException;
import eu.skillpro.ams.pscm.connector.opcua.ui.dialogs.SendOrdersDialog;

public class SendOrders extends AbstractHandler implements IHandler {
	private Order order;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SendOrdersDialog dialog = new SendOrdersDialog(HandlerUtil.getActiveShell(event));
		if (dialog.open() == Dialog.OK) {
			order = dialog.getOrder();
			Set<ExecutableSkill> executableSkills = new HashSet<>();
			for (Entry<ExecutableSkill, Boolean> entry : SkillproService.getSkillproProvider()
					.getSkillRepo().getExSkillsSelectionMap().entrySet()) {
				if (entry.getValue()) {
					executableSkills.add(entry.getKey());
				}
			}
			List<String> goalSkills = new ArrayList<>();
			for (ExecutableSkill executableSkill : executableSkills) {
				for (ResourceExecutableSkill rexSkill : executableSkill.getResourceExecutableSkills()) {
					if (rexSkill.getPostProductConfiguration() != null && !rexSkill.getPostProductConfiguration()
							.equals(rexSkill.getPreProductConfiguration())) {
						for (ProductQuantity pq : rexSkill.getPostProductConfiguration().getProductQuantities()) {
							if (pq.getProduct().equals(order.getProductQuantity().getProduct())) {
								goalSkills.add(executableSkill.getId());
								break;
							}
						}
					}
				}
			}
			String snippet = doExport(executableSkills);
			saveToDesktop(snippet, goalSkills);

			try {
				OPCUAServerRepository.sendOrders(order, dialog.getPriority(), snippet.toString(),goalSkills, Activator.getDefault().getCurrentUAaddress());
				SkillproService.getSkillproProvider().getOrderRepo().remove(order);
				SkillproService.getUpdateManager().notify(UpdateType.ORDER_DELETED, null);
				sendSkillsToAMS(snippet);
			} catch (SkillProOPCUAException e) {
				MessageDialog.openError(HandlerUtil.getActiveShell(event), "OPCUA error", e.getMessage());
				e.printStackTrace();
			} catch (ParsingException | IOException | TransformerException e) {
				e.printStackTrace();
			}

			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.OK);
			messageBox.setText("ExSkillSnippet");
			messageBox.setMessage(snippet.toString().isEmpty() ? "Snippet creation failed" : "Snippet successfully created");
			messageBox.open();
		}
		return null;
	}

	private void sendSkillsToAMS(String snippet) throws ValidityException, ParsingException, IOException, TransformerException {
		Document snippetDoc = AMLParser.getInstance().getDocumentFromString(snippet);
		Map<String, Document> childrenOfDocAsDocs = AMLExporter.getSecondChildrenOfDocAsDocs(snippetDoc);
		for (Entry<String, Document> entry : childrenOfDocAsDocs.entrySet()) {
			String rexSnippet = AMLExporter.getExportedAsString(entry.getValue());
			rexSnippet = rexSnippet.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
			String seeID = getSEEID(entry.getValue());
			if (seeID != null) {
				seeID = seeID.trim();
			}
			SendExecutableSkillToServer.push(entry.getKey(), rexSnippet, seeID);
		}
	}
	
	private String getSEEID(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("Attribute")) {
				Attribute name = childOfRoot.getAttribute("Name");
				if (name != null && name.getValue().equals("ResourceId")) {
					return childOfRoot.getValue();
				}
			}
		}
		return null;
	}
	
	protected String loadFile(String filename) {
		InputStream inputStream = this.getClass().getResourceAsStream("/resources/" + filename);
		if (inputStream != null) {
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} else {
			return "";
		}
	}

	/**
	 * Saves the most recent order to the desktop.
	 * @param executableSkills the ExecutableSkills
	 * @param goalskills the Goalskills
	 */
	private void saveToDesktop(String executableSkills, List<String> goalskills) {
		String path1 = System.getProperty("user.home") + "\\Desktop\\recentExecutableSkills.aml";
		File file1 = new File(path1);
		try (PrintWriter printWriter = new PrintWriter(file1)) {
			printWriter.print(executableSkills);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String path2 = System.getProperty("user.home") + "\\Desktop\\recentGoalskills.txt";
		File file2 = new File(path2);
		try (PrintWriter printWriter = new PrintWriter(file2)) {
			boolean first = true;
			for (String goalskill : goalskills) {
				if (!first) {
					printWriter.print(",");
				}
				first = false;
				printWriter.print(goalskill);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public String doExport(Collection<ExecutableSkill> toExport) {
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
		reverseTransform(new ArrayList<>(toExport), roleHierarchies, interfaceHierarchies);
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
		Document amlDoc = AMLExporter.getExportedAsDoc(parsedObjects, true);
		try {
			return AMLExporter.getExportedAsString(amlDoc).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void parseTransformationMapping(String transformationMappingPath, Set<Role> roles, Set<Hierarchy<InternalElement>> hierarchies) {
		try {
			TransformationMappingParser.loadTransformationMapping(transformationMappingPath, roles, hierarchies);
		} catch (ParsingException | IOException e) {
			e.printStackTrace();
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			throw new IllegalArgumentException("Transformation didn't function correctly");
		}
	}
	
	private static void reverseTransform(List<ExecutableSkill> executableSkills, List<Hierarchy<Role>> roleHierarchies, List<Hierarchy<Interface>> interfaceHierarchies) {
		// Set role and interface hierarchies before performing reverse
		// transformation
		TransformableAdapterTemplate.setCurrentRoleHierarchies(roleHierarchies);
		TransformableAdapterTemplate.setCurrentInterfaceHierarchies(interfaceHierarchies);
		// reverse transform SEE
		ReverseTransformer.getInstance().reverseTransformExecutableSkills(executableSkills);
		// REVERT
		TransformableAdapterTemplate.revertEverything();
	}
}