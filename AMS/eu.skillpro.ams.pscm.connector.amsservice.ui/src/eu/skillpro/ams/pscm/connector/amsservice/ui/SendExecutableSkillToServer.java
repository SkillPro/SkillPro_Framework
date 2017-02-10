/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: Production System Configuration Manager (PSCM)
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

package eu.skillpro.ams.pscm.connector.amsservice.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import nu.xom.Document;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import aml.amlparser.AMLExporter;
import aml.skillpro.transformer.ReverseTransformer;
import aml.transformation.service.AMLTransformationService;
import eu.skillpro.ams.service.to.Report;

public class SendExecutableSkillToServer {
	public static Report push(String id, String snippet, String seeID) throws IOException {
		String serviceName = "registerResourceExecutableSkill";
		
		StringBuilder parameters = new StringBuilder();
		
		if (id != null && !id.trim().isEmpty()) {
			parameters.append("?id=" + URLEncoder.encode(id, "UTF-8"));
		}
		
		if (seeID != null && !seeID.trim().isEmpty()) {
			parameters.append("&seeID=" + URLEncoder.encode(seeID, "UTF-8"));
		}
		
		if (snippet != null && !snippet.trim().isEmpty()) {
			parameters.append("&amlDescription=" + URLEncoder.encode(snippet, "UTF-8"));
		}
		
		HttpGet request = new HttpGet(AMSServiceUtility.serviceAddress + serviceName + parameters.toString());
		System.out.println(request.getRequestLine() + " =====================================");
		request.setHeader("Content-type", "application/json");
		
		HttpClient client = HttpClientBuilder.create().build();;
		HttpResponse response = client.execute(request);
		
		System.out.println("Response status: " + response.getStatusLine().getStatusCode());
		
		String resp = EntityUtils.toString(response.getEntity());
		Report result = JSONUtility.convertToObject(resp, Report.class);
		return result;
	}
	
	protected static String fixEncoding(String latin1) {
		try {
			String url = URLEncoder.encode(latin1, "UTF-8");
			byte[] bytes = url.getBytes("ISO-8859-1");
			if (!validUTF8(bytes))
				return url;   
			return new String(bytes, "UTF-8");  
		} catch (UnsupportedEncodingException e) {
			// Impossible, throw unchecked
			throw new IllegalStateException("No Latin1 or UTF-8: " + e.getMessage());
		}
	}

	protected static boolean validUTF8(byte[] input) {
		int i = 0;
		// Check for BOM
		if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
				&& (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
			i = 3;
		}

		int end;
		for (int j = input.length; i < j; ++i) {
			int octet = input[i];
			if ((octet & 0x80) == 0) {
				continue; // ASCII
			}
			// Check for UTF-8 leading byte
			if ((octet & 0xE0) == 0xC0) {
				end = i + 1;
			} else if ((octet & 0xF0) == 0xE0) {
				end = i + 2;
			} else if ((octet & 0xF8) == 0xF0) {
				end = i + 3;
			} else {
				// Java only supports BMP so 3 is max
				return false;
			}

			while (i < end) {
				i++;
				octet = input[i];
				if ((octet & 0xC0) != 0x80) {
					// Not a valid trailing byte
					return false;
				}
			}
		}
		return true;
	}
	
	public static String convertExecutableSkillToAMLWithoutXMLVersion(ExecutableSkill skill) throws TransformerException, IOException {
		String s = convertExecutableSkillToAML(skill).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		return s;
	}

	public static String convertResourceExecutableSkillToAMLWithoutXMLVersion(ResourceExecutableSkill skill) throws TransformerException, IOException {
		String s = convertResourceExecutableSkillToAML(skill).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		return s;
	}
	
	public static String convertExecutableSkillToAML(ExecutableSkill skill) throws TransformerException, IOException {
		ReverseTransformer.getInstance().reverseTransformExecutableSkill(skill);
		Collection<Object> values = AMLTransformationService.getTransformationProvider().getTransformationRepo().getReverseTransformedObjectsMap().values();
		Set<Object> objects = new HashSet<>();
		objects.addAll(values);
		Document exportedAsDoc = AMLExporter.getExportedAsDoc(objects, true);
		exportedAsDoc = AMLExporter.getInternalElementsOnly(exportedAsDoc);
		return AMLExporter.getExportedAsString(exportedAsDoc);
	}
	
	public static String convertResourceExecutableSkillToAML(ResourceExecutableSkill skill) throws TransformerException, IOException {
		ReverseTransformer.getInstance().reverseTransformResourceExecutableSkill(skill);
		Collection<Object> values = AMLTransformationService.getTransformationProvider().getTransformationRepo().getReverseTransformedObjectsMap().values();
		Set<Object> objects = new HashSet<>();
		objects.addAll(values);
		Document exportedAsDoc = AMLExporter.getExportedAsDoc(objects, true);
		exportedAsDoc = AMLExporter.getInternalElementsOnly(exportedAsDoc);
		return AMLExporter.getExportedAsString(exportedAsDoc);
	}
}
