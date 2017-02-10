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

package eu.skillpro.ams.service.servlets.customerrequest;

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
import eu.skillpro.ams.service.to.assets.CustomerRequestTO;
import eu.skillpro.ams.service.to.utility.ParameterMap;

@WebServlet("/retrieveCustomerRequests")
public class RetrieveCustomerRequests extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(RetrieveCustomerRequests.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * A service to retrieve the CustomerRequests that are currently in queue.
	 * 
	 * @param request
	 *            Parameter newOnly: only return the new customer requests
	 *            Parameter seeid: only return the customer requests for the given SEE
	 * @param response
	 *            a json representation of a List<SEETO>
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /retrieveCustomerRequests called.");
		ParameterMap p = new ParameterMap(request);
		String seeId = p.getOptional("seeid");
		boolean newOnly = p.getOptionalBoolean("newOnly", false);
		logger.info("only new CustomerRequests requested: " + newOnly);
		List<CustomerRequestTO> result = new ArrayList<CustomerRequestTO>();
		for (CustomerRequestTO req : ServiceContext.getAllCustomerRequests()) {
			if (req.isNewRequest() || !newOnly) {
				if (seeId == null || seeId.equals(req.getSeeID())) {
					result.add(req);
				}
			}
		}
		for (CustomerRequestTO see : result) {
			see.setNewRequest(false);
		}
		respondWithJSON(response, result);
		logger.info("call to /retrieveCustomerRequests finished successfully.");
	}
}