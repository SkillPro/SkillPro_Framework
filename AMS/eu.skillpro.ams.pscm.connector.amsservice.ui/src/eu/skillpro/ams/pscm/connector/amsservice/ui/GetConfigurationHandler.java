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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.service.SkillproService;

import com.google.gson.reflect.TypeToken;

import eu.skillpro.ams.service.to.assets.AssetTO;

public class GetConfigurationHandler extends AbstractHandler {
	private Map<AssetTO, FactoryNode> knownFactoryNodes;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		knownFactoryNodes = new HashMap<AssetTO, FactoryNode>();
		try {
			for (FactoryNode node : getConfigurations()) {
				SkillproService.getSkillproProvider().updateFactoryNode(node);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private List<FactoryNode> getConfigurations() throws ClientProtocolException, IOException {
		List<AssetTO> assetTOs = new ArrayList<>();
		Map<String, List<AssetTO>> configuration = getConfiguration(false);
		for (String key : configuration.keySet()) {
			assetTOs.addAll(configuration.get(key));
		}
		
		List<FactoryNode> assets = new ArrayList<>();
		
		for (AssetTO to : assetTOs) {
			assets.add(convertToFactoryNode(to));
		}
		
		for (AssetTO to : assetTOs) {
			if (to.getChildren() != null) {
				for (AssetTO child : to.getChildren()) {
					knownFactoryNodes.get(to).addSubNode(knownFactoryNodes.get(child));
				}
			}
		}
		return assets;
	}
	
	private FactoryNode convertToFactoryNode(AssetTO to) {
		FactoryNode asset = null;
		
		if (to.getType().equalsIgnoreCase("factorynode")) {
			asset = new FactoryNode(to.getName());
		} else if (to.getType().equalsIgnoreCase("workplace")) {
			asset = new Resource(to.getName(), new ArrayList<Setup>(), null);
		} else if (to.getType().equalsIgnoreCase("factory")) {
			asset = new Factory(to.getName());
		} else {
			throw new IllegalArgumentException("Unknown AssetTO type: " + to.getType());
		}
		//getAttributes
		double currentX = Double.parseDouble(to.getAttribute("currentX").getValue());
		double currentY = Double.parseDouble(to.getAttribute("currentY").getValue());
		double currentZ = Double.parseDouble(to.getAttribute("currentZ").getValue());
		asset.setCurrentCoordinates(currentX, currentY, currentZ);
		asset.setLength(Double.parseDouble(to.getAttribute("length").getValue()));
		asset.setWidth(Double.parseDouble(to.getAttribute("width").getValue()));
		asset.setHeight(Double.parseDouble(to.getAttribute("height").getValue()));
		
		if (to.getType().equalsIgnoreCase("factorynode") && (asset.getLength() == -1
				|| asset.getWidth()==-1||asset.getHeight()==-1)) {
			asset.setLayoutable(false);
		}
		knownFactoryNodes.put(to, asset);
		
		return asset;
	}

	/**
	 * @param approved if only approved configuration should be called or not
	 * @return a map ... 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Map<String, List<AssetTO>> getConfiguration(boolean approved) throws ClientProtocolException, IOException {
		String serviceName = "getNewConfiguration";
		if (approved) {
			serviceName = "getApprovedConfiguration";
		}
		HttpGet request = new HttpGet(AMSServiceUtility.serviceAddress + serviceName);
		System.out.println(request.getRequestLine() + " =====================================");
		request.setHeader("Content-type", "application/json");

		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(request);

		System.out.println("Response status: " + response.getStatusLine().getStatusCode());
		// Get the response
		BufferedReader rd = new BufferedReader
				(new InputStreamReader(response.getEntity().getContent()));

		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
			break;
		} 
		Map<String, List<AssetTO>> result = JSONUtility.convertToMap(line,
				new TypeToken<Map<String, List<AssetTO>>>(){ }.getType());
		return result;
	}
}
