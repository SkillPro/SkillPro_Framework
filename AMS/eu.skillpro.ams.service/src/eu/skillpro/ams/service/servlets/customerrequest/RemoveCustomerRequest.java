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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.servlets.MissingParameterException;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.assets.CustomerRequestTO;
import eu.skillpro.ams.service.to.utility.ParameterMap;

@WebServlet(urlPatterns = { "/removeCustomerRequest" })
public class RemoveCustomerRequest extends BaseServlet {
	private static final Logger logger = LoggerFactory
			.getLogger(RemoveCustomerRequest.class);

	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * 
	 * A service to remove customer requests from the list.
	 * 
	 * @param request (HttpServletRequest) id - the id of the request to be
	 *            removed
	 * @param response a result text message
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /removeCustomerRequest called.");
		ParameterMap p = new ParameterMap(request);
		try {
			String id = p.get("id");

			List<CustomerRequestTO> customerRequests = ServiceContext.getAllCustomerRequests();
			CustomerRequestTO toRemove = ServiceContext.getCustomerRequestsByOrderId(id);
			if (customerRequests.remove(toRemove)){
				respondWithReport(response, Status.OK, "Request \"" + id + "\" has been marked as started.");
				logger.info("call to /removeCustomerRequest finished successfully.");
			}else{
				respondWithReport(response, Status.ERROR, "Unknown customer request: \"" + id + "\".");
				logger.info("call to /removeCustomerRequest couldn't find the requested id \"" + id + "\".");
			}
		} catch (MissingParameterException e) {
			respondWithReport(response, Status.ERROR, "Missing parameter: id");
		}
	}

	public class CustomerRequestIDAndName {
		public String name;
		public String id;

		public CustomerRequestIDAndName(CustomerRequestTO req) {
			this.name = req.getCustomerName();
			this.id = req.getOrderID();
		}
	}
}