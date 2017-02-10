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

package eu.skillpro.ams.service.servlets.see;

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
import eu.skillpro.ams.service.to.assets.SEETO;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * @author caliqi
 * @date 02.12.2013
 *
 */
@WebServlet(urlPatterns = { "/getRegisteredSEE" })
public class GetRegisteredSEE extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(GetRegisteredSEE.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * A service to retrieve the SEE for a given configurationId.
	 * @param 	request (HttpServletRequest) containing following parameters:
	 * 				configurationId - the configurationId of the SEE that should be required
	 * @param	response a json representation of a Report object containing as message 
	 * 			 	the see with the given configuration id or an error 
	 * 				message if nothing was found.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /getRegisteredSEE called.");
		ParameterMap p = new ParameterMap(request);
		try{
			String seeId = p.getNonEmpty("seeId");
			logger.info("Requested information for SEE with ID: " + seeId);
			SEETO result = ServiceContext.getSEEById(seeId);
			if (result == null){
				respondWithReport(response, Status.ERROR, "The SEE with the ID \"" + seeId + "\" has not been registered.");
			}else if (isEmpty(result.getIdentifier()) || isEmpty(result.getNameSpace())){
				respondWithReport(response, Status.ERROR, "The SEE with the ID \"" + seeId + "\" has no node id or namespace");
			}else{
				respondWithJSON(response, result);
				logger.info("call to /getRegisteredSEE finished successfully.");
			}
		}catch (MissingParameterException e){
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}