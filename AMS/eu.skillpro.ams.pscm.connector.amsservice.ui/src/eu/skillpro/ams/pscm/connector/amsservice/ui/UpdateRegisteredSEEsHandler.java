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
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.SEE;
import skillpro.model.service.SkillproService;
import eu.skillpro.ams.service.to.Report;

public class UpdateRegisteredSEEsHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			List<SEE> registeredSEEs = new ArrayList<>();
			for (SEE see : SkillproService.getSkillproProvider().getSEERepo()) {
				if (see.getMESNodeID() != null || !see.getMESNodeID().isEmpty()) {
					registeredSEEs.add(see);
				}
			}
			//can actually be done in the first for-loop, but let's just leave it like this for now
			for (SEE see : registeredSEEs) {
				updateSEE(see);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			MessageDialog.open(MessageDialog.ERROR, HandlerUtil.getActiveShell(event), "Protocol exception", e.getMessage(), SWT.SHEET);
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog.open(MessageDialog.ERROR, HandlerUtil.getActiveShell(event), "IOException exception", e.getMessage(), SWT.SHEET);
		}
		return null;
	}
	
	private Report updateSEE(SEE see) throws ClientProtocolException, IOException {
		String serviceName = "updateSEE";
		
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		if (see.getSeeID() != null && !see.getSeeID().trim().isEmpty()) {
			jsonBuilder.add("seeId", see.getSeeID());
		}
		
		if (see.getMESNodeID() != null && !see.getMESNodeID().trim().isEmpty()) {
			jsonBuilder.add("nodeId", see.getMESNodeID().replaceAll("ns", "").replaceAll("i", "")
						.replace("=", ""));
		}
		
		if (see.getResource() != null) {
			jsonBuilder.add("assetTypeNames", see.getResource().getName());
		}
		
		if (see.getOpcUAAddress() != null && !see.getOpcUAAddress().trim().isEmpty()) {
			jsonBuilder.add("opcuaAddress", see.getOpcUAAddress());
		}
	
		jsonBuilder.add("simulation", see.isSimulation() + "");
		
		if (see.getAmlDescription() != null && !see.getAmlDescription().trim().isEmpty()) {
			jsonBuilder.add("amlFile", see.getAmlDescription());
		}
		
		HttpPost request = new HttpPost(AMSServiceUtility.serviceAddress + serviceName);
		
		request.setEntity(new StringEntity(jsonBuilder.build().toString(), "UTF-8"));
		System.out.println(request.getRequestLine() + " =====================================");
		request.setHeader("Content-type", "application/json");
		
		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(request);
		
		String resp = EntityUtils.toString(response.getEntity());
		return JSONUtility.convertToObject(resp, Report.class);
	}
}