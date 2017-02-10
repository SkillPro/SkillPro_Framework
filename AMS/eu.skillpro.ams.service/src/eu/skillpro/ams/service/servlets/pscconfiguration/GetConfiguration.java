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

package eu.skillpro.ams.service.servlets.pscconfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;

/**
 * @author caliqi
 * @date 02.12.2013
 * 
 */
@WebServlet(urlPatterns = { "/getConfiguration" })
public class GetConfiguration extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(GetConfiguration.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * A service to get the configuration created in pscm.
	 * 
	 * @param request
	 *            (HttpServletRequest) contains no parameter
	 * @param response
	 *            a json representation of a Map<String, List<AssetTO>> where
	 *            the key is the configuration id and the value the
	 *            configuration done in pscm.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /getConfiguration called.");
		Map<String, List<AssetTO>> result = new HashMap<String, List<AssetTO>>();
		PSCConfiguration configuration = ServiceContext.getPSCConfiguration();
		if (configuration != null) {
			List<AssetTO> pscConfiguration = configuration.getPSCConfiguration();
			List<AssetTO> noFlyZones = ServiceContext.getNoFlyZones();
			if (pscConfiguration.size() > 0 && noFlyZones.size() > 0) {
				pscConfiguration = new ArrayList<>(pscConfiguration);
				AssetTO copyOfFirst = new AssetTO(pscConfiguration.get(0));
				copyOfFirst.setChildren(new ArrayList<>(copyOfFirst.getChildren()));
				copyOfFirst.getChildren().addAll(noFlyZones);
				pscConfiguration.set(0, copyOfFirst);
			}
			result.put(configuration.getId(), pscConfiguration);
		}
		respondWithJSON(response, result);
		logger.info("call to /getConfiguration finished successfully.");
	}
}
