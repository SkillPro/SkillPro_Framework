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
import eu.skillpro.ams.service.to.utility.NodeID;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * @author caliqi
 * @date 02.12.2013
 *
 */
@WebServlet(urlPatterns = { "/updateSEE" })
public class UpdateSEE extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(UpdateSEE.class);
	
	private static final long serialVersionUID = -864752506880834573L;
	
	private static final String UPDATED_SUCCESSFULLY = "updated successfully";
	private static final String NO_SEE_FOUND = "No SEE found for provided seeId ";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /updateSEE called [GET].");
		updateSEE(new ParameterMap(request), response);
	}
	
	//seeId=test&nodeId=%3D2%3B%3D3239&assetTypeNames=Human2&opcuaAddress=opc.tcp%3A%2F%2F141.21.13.22%3A51200&simulation=false&amlFile=%253CtestAMLAMLAMLAML
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /updateSEE called [POST].");
		updateSEE(new ParameterMap(request).usePost(), response);
	}
	
	private void updateSEE(ParameterMap p, HttpServletResponse response) throws IOException{
		try{
			String seeID = fixEncoding(p.get("seeId"));
			String nodeIdString = fixEncoding(p.get("nodeId").replace("%3B", ";"));
			String opcuaAddress = fixEncoding(p.get("opcuaAddress"));
			String assetTypeNames = fixEncoding(p.get("assetTypeNames"));
			boolean simulation = p.getOptionalBoolean("simulation", false);
			String amlFile = fixEncoding(p.get("amlFile"));

			SEETO seeto = ServiceContext.getSEEById(seeID);
			if (seeto != null){
				NodeID nodeId = NodeID.valueOf(nodeIdString);
				seeto.setNameSpace(nodeId.nameSpace);
				seeto.setIdentifier(nodeId.identifier);
				seeto.setAssetTypeNames(Arrays.asList(assetTypeNames.split("\\-")));
				seeto.setOpcuaAddress(opcuaAddress);
				seeto.setSimulation(simulation);
				seeto.setAmlDescription(amlFile);
				respondWithReport(response, Status.OK, UPDATED_SUCCESSFULLY);
				logger.info("The call to /updateSEE finished successfully.");
			}else{
				logger.info(NO_SEE_FOUND);
				respondWithReport(response, Status.ERROR, NO_SEE_FOUND + seeID);
				logger.info("The call to /updateSEE was not successful.");
			}
		} catch (MissingParameterException e){
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		}
	}
}