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
import eu.skillpro.ams.service.to.utility.PSCConfiguration.PSCConfigurationState;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * @author caliqi
 * @date 02.12.2013
 * 
 */
@WebServlet(urlPatterns = { "/approveConfiguration" })
public class ApproveConfiguration extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(ApproveConfiguration.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * 
	 * A service to put the configuration done in pscm.
	 * 
	 * @param request
	 *            (HttpServletRequest) containing following parameters:
	 *            configurationId - the id of the configuration that should be
	 *            approved or not. approved - a string representation of a
	 *            boolean that denotes if the configuration should be approved
	 *            or not.
	 * @param response
	 *            a json representation of a Report object containing the
	 *            message that should be shown if any.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /approveConfiguration called.");
		ParameterMap p = new ParameterMap(request);
		try {
			String configurationId = p.getNonEmpty("configurationId");
			String approved = p.getNonEmpty("approved");
			PSCConfiguration configurationTest = ServiceContext.getPSCConfiguration();
			if (configurationTest == null) {
				String message = "ERROR: could not read configuration for id " + configurationId;
				logger.info(message);
				respondWithReport(response, Status.ERROR, message);
			} else if (!configurationTest.getId().equalsIgnoreCase(configurationId)) {
				String message = "ERROR: configuration for id " + configurationId + " does not exist.";
				logger.info(message);
				respondWithReport(response, Status.ERROR, message);
			} else {
				PSCConfigurationState pSCConfigurationState = approved
						.matches("true|True|TRUE|yes|Yes|YES|wahr|Wahr|WAHR|ja|Ja|JA|1") ? PSCConfigurationState.APPROVED
								: PSCConfigurationState.NOT_APPROVED;
				configurationTest.setPSCConfigurationState(pSCConfigurationState);
				respondWithReport(response, Status.OK, "Configuration updated with approved information "+pSCConfigurationState.name() + "!");
				logger.info("call to /approveConfiguration finished successfully.");
			}
		} catch (MissingParameterException e) {
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}
