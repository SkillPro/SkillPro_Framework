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

package eu.skillpro.ams.service.servlets;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

import eu.skillpro.ams.service.contextinitialiser.ServiceContext;
import eu.skillpro.ams.service.to.assets.ExecutableSkillTO;
import eu.skillpro.ams.service.to.assets.SEETO;
import eu.skillpro.ams.service.to.utility.HTMLUtility;
import eu.skillpro.ams.service.to.utility.JSONUtility;
import eu.skillpro.ams.service.to.utility.ServerStateContents;

/**
 * Imports and exports of the complete server state, or views server contents
 * like SEEs or resource executable skills.
 * 
 * @author siebel
 * @date 2016-02-18
 * 
 */
@WebServlet(urlPatterns = { "/serverState" })
public class ServerState extends BaseServlet{
	private static final Logger logger = LoggerFactory.getLogger(ServerState.class);
	
	private static final long serialVersionUID = 4815620504998983492L;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		logger.info("service /serverState called (Method = POST).");
		
		Map<String, String> parameters = new HashMap<>();
		for (Entry<String, String[]> e :  request.getParameterMap().entrySet()){
			parameters.put(e.getKey(), e.getValue().length == 0 ? "" : e.getValue()[0]);
		}
		parameters.putAll(getMultipartParameters(request));
		
		if (parameters.containsKey("export")){
			ServerStateContents ssc = getServerStateContents();
			String filename = "AMS server state " + new Timestamp(new Date().getTime()).toString().replace(":", "") + ".json";
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			respondWithJSON(response, ssc); 
		}else if (parameters.containsKey("import") && parameters.containsKey("importfile")){
			String jsonString = parameters.get("importfile");
			ServerStateContents ssc = null;
			try{
				ssc = JSONUtility.convertToObject(jsonString, ServerStateContents.class);
			}catch (JsonParseException e){
			}
			if (ssc != null){
				setServerStateContents(ssc);
				String message = "<p class=\"success\">JSON has been imported successfully. The server now has ";
				message += ssc.seetos.size() + " SEEs, ";
				message += ssc.noflyZones.size() + " no-fly zones, ";
				message += ssc.executableSkills.size() + " resource executable skills, ";
				message += ssc.customerRequests.size() + " customer requests, ";
				message += ssc.callService.size() + " call services.";
				message += "</p>";
				doGet(request, response, message, "");
			}else{
				doGet(request, response, "<p class=\"warning\">Import failed: Unreadable JSON!</p>", "");
			}
		}else if (parameters.containsKey("show")){
			String data = null;
			switch (parameters.get("show")){
			case "SEE":
				data = getSEEData();
				break;
			case "resourceExecutableSkills":
				data = getResourceExecutableSkillData();
				break;
			}
			doGet(request, response, "", data);
		}else{
			doGet(request, response, "", "");
		}
		logger.info("call to /serverState finished successfully.");
	}
	
	private String getResourceExecutableSkillData(){
		StringBuilder sb = new StringBuilder();
		List<ExecutableSkillTO> executableSkills = ServiceContext.getAllExecutableSkills();
		if (executableSkills.isEmpty()){
			sb.append("<p>There are no resource executable skills in the server.</p>");
		}else{
			sb.append("<p>Showing " +  executableSkills.size() + " resource executable skills:</p>");
			sb.append("<table>");
			sb.append("<thead><tr><th>Name</th><th>Resources</th><th>ID</th></tr></thead>");
			for (ExecutableSkillTO rexs : executableSkills){
				sb.append("<tr>");
				sb.append("<td><a href=\"#" + rexs.getResourceExecutableSkillID() + "\">" + extractResourceExecutableSkillName(rexs) + "</a></td>");
				SEETO see = ServiceContext.getSEEById(rexs.getSeeID());
				sb.append("<td>" + listToString(see.getAssetTypeNames()) + "</td>");
				sb.append("<td>" + rexs.getResourceExecutableSkillID() + "</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
	
			for (ExecutableSkillTO rexs : executableSkills){
				SEETO see = ServiceContext.getSEEById(rexs.getSeeID());
				sb.append("<h3 id=\"" + rexs.getResourceExecutableSkillID() + "\">" + extractResourceExecutableSkillName(rexs) + " (" + listToString(see.getAssetTypeNames()) + ")</h3>");
				sb.append("<p>ID: " + rexs.getResourceExecutableSkillID() + "</a></p>");
				sb.append("<p>SEE: <a href=\"?show=SEEs#"+rexs.getSeeID()+"\">" + rexs.getSeeID() + "</a></p>");
				sb.append("<div class=\"xml\">" + HTMLUtility.xmlToHtml(rexs.getAmlDescription()) + "</div>");
				sb.append("<p><a href=\"#top\">(back to top)</a></p>");
			}
		}
		return sb.toString();
	}
	
	private String extractResourceExecutableSkillName(ExecutableSkillTO rexs){
		String aml = rexs.getAmlDescription();
		String find = "Name=\"";
		int pos1 = aml.indexOf(find);
		if (pos1 >= 0){
			pos1 += find.length();
			int pos2 = aml.indexOf("\"", pos1);
			if (pos2 > 0){
				return aml.substring(pos1, pos2);
			}
		}
		return "";
	}
	
	private String getSEEData(){
		StringBuilder sb = new StringBuilder();
		List<SEETO> sees = new ArrayList<>(ServiceContext.getAllSEEs());
		Collections.sort(sees, new Comparator<SEETO>(){
			@Override
			public int compare(SEETO o1, SEETO o2){
				return o1.getIdentifier().compareTo(o2.getIdentifier());
			}
		});
		
		if (sees.isEmpty()){
			sb.append("<p>There are no SEEs in the server.</p>");
		}else{
			sb.append("<p>Showing " +  sees.size() + " SEEs:</p>");
			sb.append("<table>");
			sb.append("<thead><tr><th>Resources</th><th>Namespace/Id</th><th>SEE ID</th></tr></thead>");
			for (SEETO see : sees){
				sb.append("<tr>");
				sb.append("<td><a href=\"#" + see.getSeeID() + "\">" + listToString(see.getAssetTypeNames()) + "</a></td>");
				sb.append("<td>ns=" + see.getNameSpace() + ";i=" + see.getIdentifier() + "</td>");
				sb.append("<td>" + see.getSeeID() + "</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			for (SEETO see : sees){
				sb.append("<h3 id=\"" + see.getSeeID() + "\">" + listToString(see.getAssetTypeNames()) + " (ns=" + see.getNameSpace() + ";i=" + see.getIdentifier() + ")</h3>");
				sb.append("<p>ID: " + see.getSeeID() + "</a></p>");
				sb.append("<div class=\"xml\">" + HTMLUtility.xmlToHtml(see.getAmlDescription()) + "</div>");
				sb.append("<p><a href=\"#top\">(back to top)</a></p>");
			}
		}
		return sb.toString();
	}

	private String listToString(Collection<?> list){
		String result = "";
		boolean first = true;
		for (Object element : list){
			if (!first){
				result += ", ";
			}
			result += element;
			first = false;
		}
		return result;
	}
	
	private synchronized ServerStateContents getServerStateContents(){
		ServerStateContents ssc = new ServerStateContents();
		ssc.pscConfiguration = ServiceContext.getPSCConfiguration();
		ssc.seetos = new ArrayList<>(ServiceContext.getAllSEEs());
		ssc.customerRequests = new ArrayList<>(ServiceContext.getAllCustomerRequests());
		ssc.executableSkills = new ArrayList<>(ServiceContext.getAllExecutableSkills());
		ssc.callService = new ArrayList<>(ServiceContext.getAllCallServices());
		ssc.noflyZones = new ArrayList<>(ServiceContext.getNoFlyZones());
		return ssc;
	}
	
	private synchronized void setServerStateContents(ServerStateContents ssc){
		ServiceContext.savePSCConfiguration(ssc.pscConfiguration);
		setList(ServiceContext.getAllSEEs(), ssc.seetos);
		setList(ServiceContext.getAllCustomerRequests(), ssc.customerRequests);
		setList(ServiceContext.getAllExecutableSkills(), ssc.executableSkills);
		setList(ServiceContext.getAllCallServices(), ssc.callService);
		setList(ServiceContext.getNoFlyZones(), ssc.noflyZones);
	}
	
	private <T> void setList(List<T> list, List<T> newValues){
		list.clear();
		list.addAll(newValues);
	}

	protected Map<String, String> getMultipartParameters(HttpServletRequest request){
		if (ServletFileUpload.isMultipartContent(request)) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				ServletRequestContext requestContext = new ServletRequestContext(request);
				
				Map<String, String> multipartParameters = new HashMap<String, String>();
				for (FileItem fileItem : upload.parseRequest(requestContext)){
					multipartParameters.put(fileItem.getFieldName(), fileItem.getString());
				}
				return multipartParameters;
			} catch (FileUploadException e) {
				e.printStackTrace();
				return Collections.emptyMap();
			}
		}else{
			return Collections.emptyMap();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("service /serverState called.");

		Map<String, String> parameters = new HashMap<>();
		for (Entry<String, String[]> e :  request.getParameterMap().entrySet()){
			parameters.put(e.getKey(), e.getValue().length == 0 ? "" : e.getValue()[0]);
		}
		if (parameters.containsKey("show")){
			String data = null;
			switch (parameters.get("show")){
			case "SEEs":
				data = getSEEData();
				break;
			case "resourceExecutableSkills":
				data = getResourceExecutableSkillData();
				break;
			}
			doGet(request, response, "", data);
		}else{
			doGet(request, response, "", "");
		}
		logger.info("call to /serverState finished successfully.");
	}
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response, String message, String data) throws ServletException, IOException {
		String file = loadFile("serverState.html");
		if (message != null && !message.isEmpty()){
			file = file.replace("<!--%message%-->", message);
		}
		if (data != null && !data.isEmpty()){
			file = file.replace("<!--%showdata%-->", data);
		}
		respondHTML(response, file);
	}
}
