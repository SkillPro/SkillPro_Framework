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
import java.util.ArrayList;
import java.util.List;

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
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.SEE;
import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;

import com.google.gson.reflect.TypeToken;

import eu.skillpro.ams.service.to.assets.SEETO;


/**
 * 
 * The menu handler that is used to retrieve all the SEEs stored in the AMS-Server
 *
 */
public class RetrieveSEEHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if (SkillproService.getSkillproProvider().getSEERepo().isEmpty()) {
				getAllSEE();
			} else {
				getNewSEE();
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
	
	private void getAllSEE() throws ClientProtocolException, IOException {
		List<SEE> sees = convert(getSEETOs(false));
		SkillproService.getSkillproProvider().getSEERepo().setEntities(sees);
		SkillproService.getUpdateManager().notify(UpdateType.SEE_IMPORTED, null);
	}
	
	private void getNewSEE() throws ClientProtocolException, IOException {
		List<SEE> sees = convert(getSEETOs(true));
		SkillproService.getSkillproProvider().getSEERepo().getEntities().addAll(sees);
		SkillproService.getUpdateManager().notify(UpdateType.SEE_ADDED, null);
	}

	private List<SEE> convert(List<SEETO> seetos) {
		List<SEE> sees = new ArrayList<SEE>();
		for (SEETO seeto : seetos) {
			SEE see = new SEE(seeto.getSeeID());
			for (String name : seeto.getAssetTypeNames()) {
				for (FactoryNode fn : SkillproService.getSkillproProvider().getAssetRepo()) {
					if (fn instanceof Resource && fn.getName().equals(name)) {
						if (see.getMESNodeID() != null && see.getMESNodeID().split("\\;").length == 2) {
							see.addRegisteredResource((Resource) fn);
						} else {
							see.addNotRegisteredResource((Resource) fn);
						}
						((Resource) fn).setResponsibleSEE(see);
					}
				}
			}
			see.setMESNodeID("ns=" + seeto.getNameSpace() + ";i=" + seeto.getIdentifier());
			see.setAmlDescription(seeto.getAmlDescription());
			see.setOpcUAAddress(seeto.getOpcuaAddress());
			see.setSimulation(seeto.isSimulation());
			sees.add(see);
		}
		return sees;
	}

	/**
	 * Returns a list of SEEs, retrieved by the server's retrieveSEEs method.
	 * @param newOnly <code>true</code> to get only the newly registered SEEs, <code>false</code> to get all SEEs
	 * @return a list of SEE transfer objects
	 * @throws IOException if there were any problems with the sonnection to the server
	 */
	public static List<SEETO> getSEETOs(boolean newOnly) throws IOException {
		String serviceName = "retrieveSEEs";
		String parameters = "?newOnly=" + newOnly;
		HttpGet request = new HttpGet(AMSServiceUtility.serviceAddress + serviceName + parameters);
		request.setHeader("Content-type", "application/json");

		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(request);
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		if (sb.length() == 0) {
			return new ArrayList<SEETO>();
		} else {
			List<SEETO> result = JSONUtility.convertToList(sb.toString(), new TypeToken<List<SEETO>>() { }.getType());
			System.out.println("Number of retrieved SEEs: " + result.size());
			return result;
		}
	}
}
