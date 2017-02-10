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

package eu.skillpro.ams.pscm.ontology.ui;

import javax.xml.transform.TransformerException;

import masterviews.dialogs.MasterFileDialog;
import masterviews.util.SupportedFileType;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import eu.skillpro.ams.ontology.OntologyExporter;
import eu.skillpro.ams.pscm.ontology.transformer.SkillproToOntologyTransformer;

public class ExportOntologyHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			exportOWLData(event);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void exportOWLData(ExecutionEvent event) throws TransformerException {
		String filename = MasterFileDialog.saveFile(SupportedFileType.OWL);
		if (filename != null && !filename.equals("")) {
			//transform assets, skills etc to ontology model
			//save them in repo
			SkillproToOntologyTransformer.getInstance().transform();
			//export repo to .owl file
			OntologyExporter.getInstance().export(filename);
		}
	}
}
