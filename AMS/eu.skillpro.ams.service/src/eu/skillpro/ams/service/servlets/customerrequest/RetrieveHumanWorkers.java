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
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.to.assets.HumanWorkerTO;

@WebServlet("/retrieveHumanWorkers")
public class RetrieveHumanWorkers extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(RetrieveHumanWorkers.class);

	private static final long serialVersionUID = -8067514174106454030L;

	/**
	 * A service to retrieve human workers that are available.
	 * 
	 * @param request
	 *            Parameter seeid: only return the customer requests for the given SEE
	 * @param response
	 *            a json representation of a List<HumanWorkerTO>
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /retrieveHumanWorkers called.");
		String seeID = request.getParameter("seeid");
		
		List<HumanWorkerTO> workers = Arrays.asList(
				new HumanWorkerTO("SMTLineOperatorSEE", "SMTLineOperator"),
				new HumanWorkerTO("THTOperator1SEE", "THTOperator1"),
				new HumanWorkerTO("THTOperator2SEE", "THTOperator2"),
				new HumanWorkerTO("FunctionalTestingOperatorSEE", "FunctionalTestingOperator"),
				new HumanWorkerTO("QualityControlOperatorSEE", "QualityControlOperator"),
				new HumanWorkerTO("OpticalInspectionOperatorSEE", "OpticalInspectionOperator"),
				new HumanWorkerTO("WashingOperatorSEE", "WashingOperator")
				);
		List<HumanWorkerTO> resultList;
		if (seeID == null){
			resultList = workers;
		}else{
			resultList = new ArrayList<>();
			for (HumanWorkerTO w : workers){
				if (seeID.equals(w.seeID)){
					resultList.add(w);
				}
			}
		}
		respondWithJSON(response, resultList);
		logger.info("call to /retrieveHumanWorkers finished successfully and returned " + resultList.size() + " records.");
	}
}