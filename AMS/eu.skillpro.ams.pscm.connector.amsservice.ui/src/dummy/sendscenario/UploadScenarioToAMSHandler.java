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

package dummy.sendscenario;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.transform.TransformerException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import aml.amlparser.AMLExporter;
import aml.amlparser.AMLParser;
import aml.transformation.service.AMLTransformationService;
import dummy.generate.executableskill.DDEScenarioA;
import dummy.generate.executableskill.DDEScenarioB;
import dummy.generate.executableskill.DummyScenario;
import dummy.generate.executableskill.FullOrderScenario;
import dummy.generate.executableskill.KR6OnlyOrderScenario;
import dummy.generate.executableskill.OnlyChocolateOrderScenario;
import dummy.generate.executableskill.UR5OnlyOrderScenario;
import eu.skillpro.ams.pscm.connector.amsservice.ui.SendExecutableSkillToServer;

public class UploadScenarioToAMSHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell = Display.getCurrent().getActiveShell();
		ScenarioSelectionDialog dialog = new ScenarioSelectionDialog(activeShell);
		if (dialog.open() == WizardDialog.OK) {
			sendToAMSServer(dialog.getChosenExSkillScenario());
		}
		AMLParser.getInstance().wipeData();
		AMLTransformationService.getTransformationProvider().wipeAllData();
		return null;
	}
	
	private void sendToAMSServer(DummyScenario chosenExSkillScenario) {
		String defaultSnippet;
		String name = chosenExSkillScenario.getName();
		if (name.equals(FullOrderScenario.getInstance().getName())) {
			defaultSnippet = loadFile("fullorderscenario.aml");
		} else if (name.equals(OnlyChocolateOrderScenario.getInstance().getName())) {
			defaultSnippet = loadFile("onlychocolate.aml");
		} else if (name.equals(KR6OnlyOrderScenario.getInstance().getName())) {
			defaultSnippet = loadFile("kr6only.aml");
		} else if (name.equals(UR5OnlyOrderScenario.getInstance().getName())) {
			defaultSnippet = loadFile("ur5only.aml");
		} else if (name.equals(DDEScenarioA.getInstance().getName())) {
			defaultSnippet = loadFile("DDEa.aml");
		} else if (name.equals(DDEScenarioB.getInstance().getName())) {
			defaultSnippet = loadFile("DDEb.aml");
		} else {
			throw new IllegalArgumentException("Unknown scenario: " + chosenExSkillScenario.getName());
		}
		
		Document snippetDoc = null;
		try {
			snippetDoc = AMLParser.getInstance().getDocumentFromString(defaultSnippet);
		} catch (ParsingException | IOException e) {
			e.printStackTrace();
		}
		
		Map<String, Document> childrenOfDocAsDocs = AMLExporter.getSecondChildrenOfDocAsDocs(snippetDoc);
		for (Entry<String, Document> entry : childrenOfDocAsDocs.entrySet()) {
			try {
				String rexSnippet = AMLExporter.getExportedAsString(entry.getValue());
				rexSnippet = rexSnippet.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
				String seeID = getSEEID(entry.getValue());
				if (seeID != null) {
					seeID = seeID.trim();
				}
				SendExecutableSkillToServer.push(entry.getKey(), rexSnippet, seeID);
			} catch (TransformerException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String loadFile(String filename) {
		InputStream inputStream = this.getClass().getResourceAsStream("/resources/" + filename);
		if (inputStream != null) {
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} else {
			return "";
		}
	}
	
	private String getSEEID(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("Attribute")) {
				if (childOfRoot.getAttribute("Name") != null && childOfRoot
						.getAttribute("Name").getValue().equals("ResponsibleSEE")) {
					return childOfRoot.getValue();
				}
			}
		}
		return null;
	}
}
