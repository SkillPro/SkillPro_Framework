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

package eu.skillpro.ams.service.servlets.customerrequest.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import eu.skillpro.ams.service.to.assets.CustomerRequestTO;
import eu.skillpro.ams.service.utils.ScenarioType;

public class RegisterForm {
	private static final String CUSTOMER_NAME_FIELD = "name";
	private static final String SEE_ID_FIELD = "seeID";
	private static final String CHOCOLATE = "chocolate";
	private static final String COLOR1 = "color1";
	private static final String COLOR2 = "color2";
	private static final String COLOR3 = "color3";
	private static final String HUMAN_WORKER = "humanWorker";

	private String result;
	private Map<String, String> errors = new HashMap<String, String>();

	public String getResult() {
		return result;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public CustomerRequestTO registerCustomerRequest(HttpServletRequest request) {
		String customerName = getFieldValue(request, CUSTOMER_NAME_FIELD);
		String seeID = getFieldValue(request, SEE_ID_FIELD) != null ? getFieldValue(
				request, SEE_ID_FIELD) : "";
		String color1 = getFieldValue(request, COLOR1);
		String color2 = getFieldValue(request, COLOR2);
		String color3 = getFieldValue(request, COLOR3);
		String chocolate = getFieldValue(request, CHOCOLATE);
		boolean humanWorker = getFieldValue(request, HUMAN_WORKER) != null ? true
				: false;
		CustomerRequestTO customerRequestTO = new CustomerRequestTO();

		customerRequestTO.setHumanSEE(humanWorker);
		if (humanWorker) {
			customerRequestTO.setScenarioType(ScenarioType.FULL_ORDER);
		} else {
			customerRequestTO.setScenarioType(ScenarioType.CHOCOLATE_ONLY);
		}
		customerRequestTO.setSeeID(seeID);

		try {
			validateName(customerName);
		} catch (IllegalArgumentException e) {
			setError(CUSTOMER_NAME_FIELD, e.getMessage());
		}
		customerRequestTO.setCustomerName(customerName);

		if (humanWorker) {
			try {
				validateColor(color1);
			} catch (IllegalArgumentException e) {
				setError(COLOR1, e.getMessage());
			}

			try {
				validateColor(color2);
			} catch (IllegalArgumentException e) {
				setError(COLOR2, e.getMessage());
			}

			try {
				validateColor(color3);
			} catch (IllegalArgumentException e) {
				setError(COLOR3, e.getMessage());
			}
		} else {
			color1 = "none";
			color2 = "none";
			color3 = "none";
		}
		List<String> products = new ArrayList<String>();
		products.add(color1);
		products.add(color2);
		products.add(color3);

		try {
			validateChocolate(chocolate);
		} catch (IllegalArgumentException e) {
			setError(CHOCOLATE, e.getMessage());
		}
		products.add(chocolate + " chocolate");

		customerRequestTO.setProducts(products);

		if (errors.isEmpty()) {
			result = "Customer successfully registered";
		} else {
			result = "Registration failed";
		}
		customerRequestTO.setNewRequest(true);

		return customerRequestTO;
	}

	private void validateName(String name) throws IllegalArgumentException {
		if ((name == null || name.isEmpty())) {
			throw new IllegalArgumentException(
					"The customer name may not be empty");
		}
	}

	private void validateColor(String color) throws IllegalArgumentException {
		if (color == null || color.isEmpty()) {
			throw new IllegalArgumentException("The color may not be empty");
		}
	}

	private void validateChocolate(String color)
			throws IllegalArgumentException {
		if (color == null || color.isEmpty()) {
			throw new IllegalArgumentException(
					"The chocolate field may not be empty");
		}
	}

	private void setError(String field, String message) {
		errors.put(field, message);
	}

	private static String getFieldValue(HttpServletRequest request,
			String fieldName) {
		String value = request.getParameter(fieldName);
		if (value == null || value.trim().length() == 0) {
			return null;
		} else {
			return value.trim();
		}
	}
}
