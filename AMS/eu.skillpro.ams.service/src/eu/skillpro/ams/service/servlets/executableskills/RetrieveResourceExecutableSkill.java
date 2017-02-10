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

package eu.skillpro.ams.service.servlets.executableskills;

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
import eu.skillpro.ams.service.to.assets.ExecutableSkillTO;
import eu.skillpro.ams.service.to.utility.ParameterMap;

@WebServlet(urlPatterns = { "/retrieveResourceExecutableSkill" })
public class RetrieveResourceExecutableSkill extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(RetrieveResourceExecutableSkill.class);

	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * A service to retrieve the new registered SEEs.
	 * 
	 * @param request
	 *            (HttpServletRequest) contains no parameters
	 * @param response
	 *            a json representation of a List<SEETO>
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /retrieveResourceExecutableSkill called.");
		try {
			ParameterMap p = new ParameterMap(request);
			String resourceExecutableSkillID = fixEncoding(p.getNonEmpty("id"));
			ExecutableSkillTO result;
			if (resourceExecutableSkillID.equals("dummy")) {
				result = new ExecutableSkillTO(resourceExecutableSkillID, null, loadFile("dummyExecutableSkill.aml"));
			} else {
				result = ServiceContext.getExecutableSkillById(resourceExecutableSkillID);
			}
			
			if (result != null) {
				respondWithJSON(response, result);
				logger.info("call to /retrieveExecutableSkill finished successfully.");
			} else {
				String unknownID = "Cannot find the given ID: " + resourceExecutableSkillID;
				logger.info(unknownID);
				respondWithReport(response, Status.ERROR, unknownID);
			}
		} catch (MissingParameterException e) {
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}