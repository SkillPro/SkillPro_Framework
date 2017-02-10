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
import java.util.Arrays;
import java.util.List;

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
@WebServlet(urlPatterns = { "/registerSEE" })
public class RegisterSEE extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(RegisterSEE.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * A service to register new SEEs. If the SEE already exists, it is updated.
	 * @param 	request (HttpServletRequest) containing following parameters:<u
	 * 				<li>nameSpace (optional): the SEE's namespace</li>
	 * 				<li>simulation (optional): the SEE's simulation flag</li>
	 * 				<li>amlFile: the SEE's aml description</li>
	 * </ul>
	 * @param	response Returns a JSON-representation of a Report object 
	 * 						containing the new generated GUID or an error 
	 * 						message if something went wrong.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /registerSEE called [GET].");
		ParameterMap p = new ParameterMap(request);
		registerSEE(p, response);
	}

	/**
	 * A service to register new SEEs. If the SEE already exists, it is updated.
	 * @param 	request (HttpServletRequest) containing following parameters:<ul>
	 * 				<li>assetTypeNames: a list of names for the asset types, separated with "-".</li>
	 * 				<li>nameSpace (optional): the SEE's namespace</li>
	 * 				<li>simulation (optional): the SEE's simulation flag</li>
	 * 				<li>amlFile: the SEE's aml description</li>
	 * </ul>
	 * @param	response Returns a JSON-representation of a Report object 
	 * 						containing the new generated GUID or an error 
	 * 						message if something went wrong.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /registerSEE called [POST].");
		ParameterMap p = new ParameterMap(request).usePost();
		registerSEE(p, response);
	}
	
	private void registerSEE(ParameterMap p, HttpServletResponse response) throws IOException{
		try {
			String seeId = fixEncoding(p.get("seeId"));
			SEETO result = ServiceContext.getSEEById(seeId);
			String amlFile = fixEncoding(p.get("amlFile"));
			if (result == null){
				String opcuaAddress = fixEncoding(p.get("opcuaAddress"));
				String assetTypeNames = fixEncoding(p.get("assetTypeNames"));
				boolean isSimulation = p.getOptionalBoolean("simulationString", false);
				List<String> assetNamesList = Arrays.asList(assetTypeNames.split("\\-"));
				result = new SEETO(seeId, assetNamesList, "", "", opcuaAddress, amlFile, true, isSimulation);
				ServiceContext.addSEE(result);
			}else{
				// TODO: update the amlFile description: use what came as
				// parameter, and add the existing communication data (NodeID,
				// server addresses)
				// result.setAmlDescription(amlFile);
			}
		} catch (MissingParameterException e) {
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}
