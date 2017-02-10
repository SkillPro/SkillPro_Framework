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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.servlets.MissingParameterException;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;
import eu.skillpro.ams.service.to.utility.ParameterMap;
import eu.skillpro.ams.service.to.utility.PSCConfiguration.PSCConfigurationState;

/**
 * @author caliqi
 *
 */
@WebServlet(urlPatterns = { "/geoDataAcquired" })
public class GeoDataAcquired extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(GeoDataAcquired.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * Acknowledges that geo data was received.
	 * @param 	request (HttpServletRequest) containing following parameters: configurationId
	 * @param	response json representation of a Report object with a message
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /geoDataAcquired called.");
		ParameterMap p = new ParameterMap(request);
		try{
			String configurationId = p.get("configurationId");
			PSCConfiguration configurationTest = ServiceContext.getPSCConfiguration();
			if (configurationTest == null){
				String message = "ERROR: could not read configuration for id " + configurationId;
				logger.info(message);
				respondWithReport(response, Status.ERROR, message);
			}else if (!configurationTest.getId().equalsIgnoreCase(configurationId)){
				String message = "ERROR: configuration for id " + configurationId + " does not exist.";
				logger.info(message);
				respondWithReport(response, Status.ERROR, message);
			}else{
				if (configurationTest.isGeoDataHasBeenRead()){
					respondWithReport(response, Status.OK, "Geo data was already acknowlegded");
				}else if (configurationTest.getConfigurationState() == PSCConfigurationState.APPROVED){
					configurationTest.setGeoDataHasBeenRead(true);
					respondWithReport(response, Status.OK, "Geo data has been acknowlegded");
				}
				logger.info("call to /geoDataAcquired finished successfully.");
			}
		}catch (MissingParameterException e){
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}
