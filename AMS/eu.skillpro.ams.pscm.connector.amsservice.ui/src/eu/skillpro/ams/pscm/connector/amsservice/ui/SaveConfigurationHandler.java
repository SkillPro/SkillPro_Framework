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
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.FactoryNode;
import skillpro.model.service.SkillproService;
import eu.skillpro.ams.service.to.Report;
import eu.skillpro.ams.service.to.assets.AssetTO;

public class SaveConfigurationHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			saveConfigurations(SkillproService.getSkillproProvider().getAssetRepo().getRootAssets());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void saveConfigurations(List<FactoryNode> assets) throws ClientProtocolException, IOException {
		List<AssetTO> assetTOs = new ArrayList<>();
		
		for (FactoryNode asset : assets) {
			if(asset.isLayoutable()) {
				assetTOs.add(new AssetTO(asset));
			} else {
				List<FactoryNode> subNodes = asset.getSubNodes();
				if(subNodes != null) {
					for(FactoryNode sn : subNodes) {
						assetTOs.addAll(AssetTO.retrieveNotVirtualChildren(sn));
					}
				}
			}
		}
		
		String conf = JSONUtility.convertToJSON(assetTOs);
		saveConfiguration(conf);
	}
	
	private Report saveConfiguration(String conf) throws ClientProtocolException, IOException {
		String serviceName = "saveConfiguration";
		HttpPost post = new HttpPost(AMSServiceUtility.serviceAddress + serviceName);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("configuration", conf));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		post.setHeader("Content-type", "application/x-www-form-urlencoded");

		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(post);
		
		System.out.println("Response status: " + response.getStatusLine().getStatusCode());
		// Get the response
		BufferedReader rd = new BufferedReader
				(new InputStreamReader(response.getEntity().getContent()));
		
		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
			break;
		} 
		Report result = JSONUtility.convertToObject(line, Report.class);
		return result;
	}
}
