/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: AMS Server
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

package eu.skillpro.ams.service.servlets.geodata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.assets.GeoDataTO;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * @author caliqi
 *
 */
@WebServlet(urlPatterns = { "/readGeoData" })
public class ReadGeoData extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(ReadGeoData.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * 
	 * Retrieves GeoData.
	 * 
	 * @param request (HttpServletRequest) containing following parameters:
	 *            assetId (optional)
	 * @param response delivers the geodata of the current configuration returns
	 *            a json representation of a Map<String, List<GeoDataTO>>
	 *            representing the geodata of 1 - If assetId parameter not
	 *            present returns the geodata of all assets present in the
	 *            configuration 2 - If assetId parameter is present returns the
	 *            geo data of the asset with the given id
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /readGeoData called.");
		
		ParameterMap p = new ParameterMap(request);
		PSCConfiguration configuration = ServiceContext.getPSCConfiguration();
		if (configuration == null){
			String message = "ERROR: could not read configuration!";
			logger.info(message);
			respondWithReport(response, Status.ERROR, message);
		}else{
			Map<String, List<GeoDataTO>> result = new HashMap<String, List<GeoDataTO>>();
			String assetId = p.getOptional("assetId");
			if (isEmpty(assetId)){
				logger.info("service /readGeoData called without assetid parameter. Geo data of whole configuration will be delivered.");
				List<GeoDataTO> geoData = new ArrayList<GeoDataTO>();
				for (AssetTO asset : configuration.getPSCConfiguration()){
					retrieveAllGeoData(geoData, asset, "");
				}
				result.put(configuration.getId(), geoData);
				respondWithJSON(response, result);
			}else{
				logger.info("service /readGeoData called with assetid parameter. Geo data for asset with id: " + assetId + " will be delivered.");
				AssetTO asset = getAssetById(configuration, assetId);
				if (asset == null){
					String message = "ERROR: The Asset with id: " + assetId + " does not exist!";
					logger.info(message);
					respondWithReport(response, Status.ERROR, message);
				}else{
					List<GeoDataTO> geoData = new ArrayList<GeoDataTO>();
					geoData.add(new GeoDataTO(asset, "")); // TODO: parent id is
															// here not given
					result.put(configuration.getId(), geoData);
					respondWithJSON(response, result);
				}
			}
			logger.info("call to /readGeoData finished successfully.");
		}
	}
}
