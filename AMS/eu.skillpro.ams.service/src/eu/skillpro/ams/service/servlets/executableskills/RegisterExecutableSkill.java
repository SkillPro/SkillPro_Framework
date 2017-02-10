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

@WebServlet(urlPatterns = { "/registerResourceExecutableSkill" })
public class RegisterExecutableSkill extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(RegisterExecutableSkill.class);

	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * Adds or updates executable skills.
	 * 
	 * @param request
	 *            (HttpServletRequest) containing following parameters:
	 *            <ul>
	 *            <li>resourceExecutableSkillID: the id of the executable skill</li>
	 *            <li>seeID: the ID of the skill's assigned SEE</li>
	 *            <li>amlDescription: the skill's AML description</li>
	 *            </ul>
	 * @param response
	 *            Returns a JSON-representation of a Report object containing
	 *            the new generated Skill ID or an error message if something went
	 *            wrong.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /registerExecutableSkill called.");
		ParameterMap p = new ParameterMap(request);
		try{
			String resourceExecutableSkillID = fixEncoding(p.getNonEmpty("id"));
			String seeID = fixEncoding(p.getNonEmpty("seeID"));
			String amlDescription = fixEncoding(p.getNonEmpty("amlDescription"));
			
			ExecutableSkillTO result = ServiceContext.getExecutableSkillById(resourceExecutableSkillID);
			if (result == null){
				result = new ExecutableSkillTO(resourceExecutableSkillID, seeID, amlDescription);
				ServiceContext.addExecutableSkill(result);
				logger.info("new ExecutableSkill registered successfully.");
			}else{
				result.setAmlDescription(amlDescription);
				result.setSeeID(seeID);
				logger.info("existing ExecutableSkill updated successfully.");
			}
			respondWithReport(response, Status.OK, result.getResourceExecutableSkillID());
			logger.info("call to /registerExecutableSkill finished successfully.");
		}catch (MissingParameterException e){
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}