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
import eu.skillpro.ams.service.servlets.MissingParameterException;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.utility.ParameterMap;

/**
 * Manages the no-fly zones stored on the server. Zones can be added or deleted.
 * @author siebel
 * @date 2015-09-14
 * 
 */
@WebServlet(urlPatterns = { "/zone" })
public class NoFlyZones extends BaseServlet {
	private static final Logger logger = LoggerFactory.getLogger(NoFlyZones.class);
	private static final long serialVersionUID = -864752506880834573L;

	/**
	 * Deletes a no-fly zone, specified by its ID.
	 * @param request a {@link HttpServletRequest} containing following parameter:<ul>
	 *            <li>id: the id of the zone which will be deleted</li>
	 *            </ul>
	 * @param response a json string that shows if the deletion has been successful
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("service /zone (DELETE) called.");
		ParameterMap p = new ParameterMap(request);
		String id = p.getOptional("id");
		List<AssetTO> noFlyZones = ServiceContext.getNoFlyZones();
		AssetTO toRemove = null;
		for (AssetTO noFlyZone : noFlyZones) {
			if (noFlyZone.getId().equals(id)) {
				toRemove = noFlyZone;
				break;
			}
		}
		Map<String, String> result = new HashMap<>();
		boolean success = noFlyZones.remove(toRemove);
		result.put("action", "delete");
		result.put("status", success ? "ok" : "error");
		result.put("id", id);
		respondWithJSON(response, result);
		logger.info("service /zone (DELETE) " + (success ? "finished successfully:" : "failed"));
	}

	/**
	 * Adds a no-fly zone.
	 * @param request a {@link HttpServletRequest} containing following parameters:<ul>
	 *            <li>currentX: the x position of the zone</li>
	 *            <li>currentY: the y position of the zone</li>
	 *            <li>width: the width of the zone</li>
	 *            <li>length: the length of the zone</li>
	 *            <li>name (optional): the name of the zone</li>
	 *            </ul>
	 * @param response a json representation of the no-fly zone
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	/**
	 * Adds a no-fly zone.
	 * 
	 * @param request a {@link HttpServletRequest} containing following parameters:<ul>
	 *            <li>currentX: the x position of the zone</li>
	 *            <li>currentY: the y position of the zone</li>
	 *            <li>width: the width of the zone</li>
	 *            <li>length: the length of the zone</li>
	 *            <li>name (optional): the name of the zone</li>
	 *            </ul>
	 * @param response a json representation of the no-fly zone
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /zone called.");
		try {
			ParameterMap p = new ParameterMap(request);
			double currentX = p.getDouble("currentX");
			double currentY = p.getDouble("currentY");
			double width = p.getDouble("width");
			double length = p.getDouble("length");
			String name = p.getOptional("name", "No-fly zone");
			
			AssetTO noflyZone = AssetTO.createNoFlyZone(name, currentX, currentY, width, length);
			ServiceContext.getNoFlyZones().add(noflyZone);

			respondWithJSON(response, noflyZone);
			logger.info("call to /zone finished successfully.");
		} catch (NumberFormatException | MissingParameterException e) {
			respondWithReport(response, Status.ERROR, e.getMessage());
			logger.info("call to /zone failed.");
		}
	}
}
