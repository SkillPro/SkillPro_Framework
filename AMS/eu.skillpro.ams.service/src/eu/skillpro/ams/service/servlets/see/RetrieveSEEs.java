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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.to.assets.SEETO;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * @author caliqi
 * @date 02.12.2013
 * 
 */
@WebServlet(urlPatterns = { "/retrieveSEEs" })
public class RetrieveSEEs extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(RetrieveSEEs.class);
	private static final long serialVersionUID = -864752506880834573L;
	
	/**
	 * A service to retrieve the new registered SEEs.
	 * 
	 * @param request (HttpServletRequest) contains no parameters
	 * @param response a json representation of a List of {@link SEETO}s.
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /retrieveSEEs called.");
		ParameterMap p = new ParameterMap(request);
		boolean newOnly = p.getOptionalBoolean("newOnly", false);
		logger.info("only new SEEs requested: " + newOnly);
		List<SEETO> result = new ArrayList<SEETO>();
		for (SEETO see : ServiceContext.getAllSEEs()){
			if (see.isNewAsset() || !newOnly){
				result.add(see);
				see.setNewAsset(false);
			}
		}
		respondWithJSON(response, result);
		logger.info("call to /retrieveSEEs finished successfully.");
	}
}