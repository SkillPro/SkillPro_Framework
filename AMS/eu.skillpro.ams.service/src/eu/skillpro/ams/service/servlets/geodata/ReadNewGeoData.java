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
import java.util.Collections;
import java.util.List;

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
import eu.skillpro.ams.service.to.utility.PSCConfiguration.PSCConfigurationState;

/**
 * @author caliqi
 * 
 */
@WebServlet(urlPatterns = { "/readNewGeoData" })
public class ReadNewGeoData extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(ReadNewGeoData.class);
	private static final long serialVersionUID = -864752506880834573L;
	
	/**
	 * 
	 * Retrieves GeoData.
	 * 
	 * @param request (HttpServletRequest) containing no parameters
	 * @param response delivers the geodata of the actual configuration returns
	 *            a json representation of a Map<String, List<GeoDataTO>>
	 *            representing the geodata of all assets present in the
	 *            configuration
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /readNewGeoData called.");
		PSCConfiguration configuration = ServiceContext.getPSCConfiguration();
		if (configuration == null){
			String message = "ERROR: could not read configuration!";
			logger.info(message);
			respondWithReport(response, Status.ERROR, message);
		}else{
			if (configuration.getConfigurationState() == PSCConfigurationState.APPROVED && !configuration.isGeoDataHasBeenRead()){
				List<GeoDataTO> geoData = new ArrayList<GeoDataTO>();
				for (AssetTO asset : configuration.getPSCConfiguration()){
					retrieveAllGeoData(geoData, asset, "");
				}
				respondWithJSON(response, Collections.singletonMap(configuration.getId(), geoData));
			}else{
				respondWithJSON(response, Collections.emptyMap());
			}
			logger.info("call to /readNewGeoData finished successfully.");
		}
	}
}
