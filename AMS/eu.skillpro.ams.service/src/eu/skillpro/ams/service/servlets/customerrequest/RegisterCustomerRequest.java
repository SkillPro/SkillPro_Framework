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
import eu.skillpro.ams.service.servlets.customerrequest.forms.RegisterForm;
import eu.skillpro.ams.service.to.assets.CustomerRequestTO;

@WebServlet("/registerCustomerRequest")
public class RegisterCustomerRequest extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(RegisterCustomerRequest.class);
	
	private static final long serialVersionUID = -864752506880834573L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		respondHTML(response, loadFile("registerCustomerRequest.html"));
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /registerCustomerRequest called.");
		
		RegisterForm form = new RegisterForm();
        CustomerRequestTO customerRequestTO = form.registerCustomerRequest(request);
        
        request.setAttribute("form", form);
		request.setAttribute("seeID", customerRequestTO.getSeeID());
		request.setAttribute("customerRequest", customerRequestTO);

        if (form.getErrors().isEmpty()) {
        	ServiceContext.addCustomerRequest(customerRequestTO);
    		respondHTML(response, loadFile("registerCustomerRequestDone.html"));
			logger.info("new CustomerRequest registered successfully.");  
        } else {
    		respondHTML(response, loadFile("registerCustomerRequest.html"));
            logger.info("call to /registerCustomerRequest failed " + form.getErrors());
        }
	}
}