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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.ams.util.AMSServiceUtility;

public class CheckConnectionHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String message = "";
		try {
			message = checkConnection();
		} catch (ClientProtocolException e) {
			message += e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			message += e.getMessage();
			e.printStackTrace();
		}
		MessageDialog.open(MessageDialog.INFORMATION, HandlerUtil.getActiveShell(event), "Connection check result", message, SWT.SHEET);
		return null;
	}
	
	public String checkConnection() throws ClientProtocolException, IOException {
		String serviceName = "checkConnection";
		HttpGet request = new HttpGet(AMSServiceUtility.serviceAddress + serviceName);
		request.setHeader("Content-type", "application/json");

		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(request);
		String result = "Response status: " + response.getStatusLine().getStatusCode();
		// Get the response
		BufferedReader rd = new BufferedReader
				(new InputStreamReader(response.getEntity().getContent()));

		String line = "";
		result = result += "\n" + line;
		while ((line = rd.readLine()) != null) {
			if (result.trim().isEmpty()) {
				result += line;
			} else {
				result += "\n" + line;
			}
			break;
		} 
		return result;
	}
}
