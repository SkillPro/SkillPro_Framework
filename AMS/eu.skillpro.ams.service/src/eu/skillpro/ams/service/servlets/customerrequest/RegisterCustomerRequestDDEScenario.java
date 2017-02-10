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
import eu.skillpro.ams.service.to.assets.CustomerRequestTO;
import eu.skillpro.ams.service.to.utility.ParameterMap;
import eu.skillpro.ams.service.utils.ScenarioType;

@WebServlet("/startDDEScenario")
public class RegisterCustomerRequestDDEScenario extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(RegisterCustomerRequestDDEScenario.class);
	
	private static final long serialVersionUID = -864752506880834573L;

	public static final String ATT_CUSTOMER = "customerRequest";
	public static final String ATT_FORM = "form";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /registerCustomerRequestManualScenario called.");
		ParameterMap p = new ParameterMap(request);

		respondHTML(response, loadFile("startDDEScenario.html"));

		try {
			ScenarioType scenarioType;
			int count;
			if (request.getParameter("ddeA") != null) {
				scenarioType = ScenarioType.DDE_A;
				count = p.getInt("ddeACount");
			} else if (request.getParameter("ddeB") != null) {
				scenarioType = ScenarioType.DDE_B;
				count = p.getInt("ddeBCount");
			} else {
				throw new MissingParameterException("Missing parameter: scenario", "scenario");
			}

			CustomerRequestTO reqTO = new CustomerRequestTO();
			reqTO.setScenarioType(scenarioType);
			reqTO.setCount(count);
			ServiceContext.addCustomerRequest(reqTO);
		} catch (MissingParameterException | NumberFormatException e) {
			respondWithReport(response, Status.ERROR, ERROR_INPUT_PARAMETERS);
		}
	}
}