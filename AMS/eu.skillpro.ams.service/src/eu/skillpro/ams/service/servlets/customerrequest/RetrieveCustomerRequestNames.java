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

@WebServlet(urlPatterns = { "/retrieveCustomerRequestNames" })
public class RetrieveCustomerRequestNames extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(RetrieveCustomerRequestNames.class);
	
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * A service to retrieve the names and ids of all CustomerRequests in queue.
	 * 
	 * @param request (HttpServletRequest) optional parameter seeid, to limit
	 *            the result set to requests on that see
	 * @param response a list of names and ids.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /retrieveCustomerRequestNames called.");
		ParameterMap p = new ParameterMap(request);
		String seeId = p.getOptional("seeid");
		List<CustomerRequestTO> customerRequests = ServiceContext.getAllCustomerRequests();
		ArrayList<CustomerRequestIDAndName> result = new ArrayList<>();
		for (CustomerRequestTO req : customerRequests){
			if (seeId == null || seeId.equals(req.getSeeID())){
				result.add(new CustomerRequestIDAndName(req));
			}
		}
		respondWithJSON(response, result);
		logger.info("call to /retrieveCustomerRequestNames finished successfully, returned " + result.size() + " entries.");
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