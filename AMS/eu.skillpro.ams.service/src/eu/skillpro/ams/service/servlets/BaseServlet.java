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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import eu.skillpro.ams.service.to.Report;
import eu.skillpro.ams.service.to.Status;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.assets.GeoDataTO;
import eu.skillpro.ams.service.to.utility.JSONUtility;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;

/**
 * 
 * @author caliqi
 *
 */
public abstract class BaseServlet extends HttpServlet {
	private static final long serialVersionUID = 8550843219904178410L;
	public static final String PSC_CONFIGURATION = "psc_configuration";
	public static final String SEEs = "sees";
	public static final String CUSTOMER_REQUEST = "customer_request";
	public static final String ROS_CALL_SERVICE = "ros_call_service";
	public static final String EXECUTABLE_SKILL = "executable_skill";
	public static final String NO_FLY_ZONE = "no_fly_zone";
	
	public static final String ERROR_INPUT_PARAMETERS = "Some input parameters have not been supplied: ";
	public static final String ERROR_EMPTY_INPUT = "The given input is empty!";
	
	protected void respondWithReport(HttpServletResponse response, Status status, String message) throws IOException {
		respondWithJSON(response, new Report(status, message));
	}
	
	protected void respondWithJSON(HttpServletResponse response, Object object) throws IOException {
		respond(response, JSONUtility.convertToJSON(object), "application/json");
	}
	
	protected void respondHTML(HttpServletResponse response, String htmlString) throws IOException {
		respond(response, htmlString, "text/html");
	}
	
	private void respond(HttpServletResponse response, String message, String contentType) throws IOException {
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter writer = response.getWriter();
		writer.write(message);
		writer.close();
		response.flushBuffer();
	}
	
	private final static String UTF8_BOM = "\uFEFF";
	
	/**
	 * Tries to fix a string which has been interpreted using the wrong
	 * encoding. Also removes the Byte Order Mark in the beginning, if it
	 * exists.
	 * 
	 * @param latin1 the string to be fixed
	 * @return the fixed string
	 * @see http://stackoverflow.com/questions/887148/how-to-determine-if-a-string-contains-invalid-encoded-characters
	 */
	protected static String fixEncoding(String latin1) {
		if (isEmpty(latin1)) {
			return latin1;
		}
		if (latin1.startsWith(UTF8_BOM)){
			latin1 = latin1.substring(1);
		}
		try {
			byte[] bytes = latin1.getBytes("ISO-8859-1");
			if (validUTF8(bytes)){
				return new String(bytes, "UTF-8");
			}else{
				return latin1;
			}
		} catch (UnsupportedEncodingException e) {
			// Impossible, throw unchecked
			throw new IllegalStateException("No Latin1 or UTF-8: " + e.getMessage());
		}
	}
	
	protected static boolean validUTF8(byte[] input){
		int i = 0;
		// Check for BOM
		if (input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF){
			i = 3;
		}
		
		int end;
		for (int j = input.length; i < j; ++i){
			int octet = input[i];
			if ((octet & 0x80) == 0){
				continue; // ASCII
			}
			// Check for UTF-8 leading byte
			if ((octet & 0xE0) == 0xC0){
				end = i + 1;
			}else if ((octet & 0xF0) == 0xE0){
				end = i + 2;
			}else if ((octet & 0xF8) == 0xF0){
				end = i + 3;
			}else{
				// Java only supports BMP so 3 is max
				return false;
			}
			
			while (i < end){
				i++;
				octet = input[i];
				if ((octet & 0xC0) != 0x80){
					// Not a valid trailing byte
					return false;
				}
			}
		}
		return true;
	}

	protected static AssetTO getAssetById(PSCConfiguration root, String assetId) {
		for (AssetTO asset : root.getPSCConfiguration()) {
			AssetTO result = getAssetById(asset, assetId);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * @param root
	 * @param assetId
	 * @return
	 */
	protected static AssetTO getAssetById(AssetTO root, String assetId) {
		if (root.getId().equalsIgnoreCase(assetId)) {
			return root;
		} else {
			for (AssetTO asset : root.getChildren()) {
				AssetTO result = getAssetById(asset, assetId);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * @param geoData
	 * @param root
	 */
	protected static void retrieveAllGeoData(List<GeoDataTO> geoData, AssetTO root, String parentId) {
		geoData.add(new GeoDataTO(root, parentId));
		for (AssetTO asset : root.getChildren()) {
			retrieveAllGeoData(geoData, asset, root.getId());
		}
	}
	
	/**
	 * Loads a file from the WEB-INF folder in this project.
	 * @param filename the file name in the folder, without the "/WEB-INF/" path in the beginning
	 * @return the file as a string, or an empty string if the file couldn't be read
	 */
	protected String loadFile(String filename) {
		InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/" + filename);
		if (inputStream != null) {
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} else {
			return "";
		}
	}
	
	/**
	 * Checks if a string is <code>null</code> or empty.
	 * @param s a string
	 * @return <code>true</code> if the string is <code>null</code> or empty
	 */
	protected static boolean isEmpty(String s){
		return s == null || s.isEmpty();
	}
}
