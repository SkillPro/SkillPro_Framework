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

package eu.skillpro.ams.pscm.connector.amsservice.ui;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.ams.util.AMSServiceUtility;
import eu.skillpro.ams.service.to.Report;

public class WipeResourceExecutableSkillsHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Report report = wipeWipeWipe();
			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.OK);
			messageBox.setText("Wiping Resource Executable Skills");
			messageBox.setMessage(report.message);
			messageBox.open();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			MessageDialog.open(MessageDialog.ERROR, HandlerUtil.getActiveShell(event), "Protocol exception", e.getMessage(), SWT.SHEET);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.open(MessageDialog.ERROR, HandlerUtil.getActiveShell(event), "IOException exception", e.getMessage(), SWT.SHEET);
		}
		return null;
	}
	
	private Report wipeWipeWipe() throws ClientProtocolException, IOException {
		String serviceName = "wipeResourceExecutableSkills";
		HttpGet request = new HttpGet(AMSServiceUtility.serviceAddress + serviceName);
		request.setHeader("Content-type", "application/json");
		
		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(request);
		
		String resp = EntityUtils.toString(response.getEntity());
		Report result = JSONUtility.convertToObject(resp, Report.class);
		return result;
	}
}
