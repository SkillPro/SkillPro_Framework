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

package eu.skillpro.ams.service.servlets.pscconfiguration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.servlets.MissingParameterException;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.utility.JSONUtility;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;
import eu.skillpro.ams.service.to.utility.ParameterMap;
import eu.skillpro.ams.service.to.utility.PSCConfiguration.PSCConfigurationState;

/**
 * @author caliqi
 * @date 02.12.2013
 *
 */
@WebServlet(urlPatterns = { "/saveConfiguration" })
public class SaveConfiguration extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(SaveConfiguration.class);
	private static final long serialVersionUID = -864752506880834573L;
	private static SimpleDateFormat sdf = new SimpleDateFormat("YYYMMdd-HHmmssSSS");
	
	/**
	 * 
	 * A service to put the configuration done in pscm.
	 * @param 	request (HttpServletRequest) containing following parameter:
	 * 				configuration - the json representation of an List<AssetTO> containing the 
	 * 								elements configured.
	 * @param	response a json object with status information
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /saveConfiguration called.");
		ParameterMap p = new ParameterMap(request);
		try {
			String configuration = fixEncoding(p.getNonEmpty("configuration"));
			List<AssetTO> configurationList = JSONUtility.convertToList(configuration, new TypeToken<List<AssetTO>>(){ }.getType());
			if (configurationList == null || configurationList.isEmpty()) {
				logger.info(ERROR_EMPTY_INPUT);
				respondWithReport(response, Status.ERROR, ERROR_EMPTY_INPUT);
			}else{
				logger.info("service /saveConfiguration saving the new configuration.");
				PSCConfiguration pSCConfiguration = new PSCConfiguration();
				pSCConfiguration.setId(sdf.format(new Date()));
				pSCConfiguration.setPSCConfigurationState(PSCConfigurationState.DIRTY);
				pSCConfiguration.setPSCConfiguration(configurationList);
				pSCConfiguration.setGeoDataHasBeenRead(false);
				ServiceContext.savePSCConfiguration(pSCConfiguration);
				
				respondWithReport(response, Status.OK, pSCConfiguration.getId());		
				
				logger.info("call to /saveConfiguration finished successfully.");
			}
		} catch (MissingParameterException e) {
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}
