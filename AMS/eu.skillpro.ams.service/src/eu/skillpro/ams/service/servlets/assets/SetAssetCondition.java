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

package eu.skillpro.ams.service.servlets.assets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skillpro.model.assets.State;
import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.servlets.MissingParameterException;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * @author caliqi
 * @date 02.12.2013
 *
 */
@WebServlet(urlPatterns = { "/setAssetCondition" })
public class SetAssetCondition extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(SetAssetCondition.class);
	
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * 
	 * A service to set the asset condition.
	 * @param 	request (HttpServletRequest) containing following parameters:<ul>
	 * 				<li>assetId: the id of the asset whose condition should be set.</li>
	 * 				<li>conditionName: the condition to set.</li>
	 * 				</ul>
	 * @param	response a json representation of a Report object containing the message 
	 * 				that should be shown if any.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /setAssetCondition called.");
		ParameterMap p = new ParameterMap(request);
		try {		
			String assetId = p.getNonEmpty("assetId");
			State newCondition = State.valueOfIgnoreCase(p.get("conditionName"));
			
			PSCConfiguration pscConfiguration = ServiceContext.getPSCConfiguration();
			if (pscConfiguration == null) {
				String message = "ERROR: could not read configuration.";
				logger.info(message);
				respondWithReport(response,Status.ERROR, message);
			} else {
				AssetTO assetToChangeCondition = null;
				for (AssetTO asset : pscConfiguration.getPSCConfiguration()) {
					if (pscConfiguration.getId().equalsIgnoreCase(assetId)) {
						assetToChangeCondition = asset;
						break;
					}
				}
				
				if (assetToChangeCondition == null) {
					String message = "ERROR: asset for id " + assetId + " does not exist.";
					logger.info(message);
					respondWithReport(response,Status.ERROR, message);
				} else {
					assetToChangeCondition.setCondition(newCondition);
					
					String message = String.format("Condition for asset %s updated with value %s(%s)!",
							assetToChangeCondition.getId(),
							newCondition.name(),
							newCondition.getIdentifier()
							);
					respondWithReport(response,Status.OK, message);		
					
					logger.info("call to /setAssetCondition finished successfully.");
				}
			}
		} catch (MissingParameterException e) {
			logger.info(ERROR_INPUT_PARAMETERS);
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS + e.missingParameter);
		} catch (IllegalArgumentException e) {
			logger.info(e.getMessage());
			respondWithReport(response, Status.ERROR, e.getMessage());
		}
	}
}
