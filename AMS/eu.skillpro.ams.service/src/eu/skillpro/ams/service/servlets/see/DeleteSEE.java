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
 * A servlet that removes an SEE from the server.
 * @author caliqi
 * @date 02.12.2013
 *
 */
@WebServlet(urlPatterns = { "/deleteSEE" })
public class DeleteSEE extends BaseServlet {
	private static final long serialVersionUID = -8064606657198402047L;

	private static final Logger logger = LoggerFactory.getLogger(DeleteSEE.class);
	
	private static final String DELETE_SUCCESSFULLY = "delete successfully";
	private static final String NO_SEE_FOUND = "No SEE found for provided seeId ";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /deleteSEE called.");
		ParameterMap p = new ParameterMap(request);
		try{
			String seeId = fixEncoding(p.getNonEmpty("seeId"));
			SEETO seeToDelete = ServiceContext.getSEEById(seeId);
			if (seeToDelete != null){
				ServiceContext.getAllSEEs().remove(seeToDelete);
				respondWithReport(response, Status.OK, DELETE_SUCCESSFULLY);
				logger.info("call to /deleteSEE finished successfully.");
			}else{
				respondWithReport(response, Status.ERROR, NO_SEE_FOUND + seeId);
				logger.info("call to /deleteSEE not successful.");
			}
		}catch (MissingParameterException e){
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}
