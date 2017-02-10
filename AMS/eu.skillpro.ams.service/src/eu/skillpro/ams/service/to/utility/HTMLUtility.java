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

package eu.skillpro.ams.service.to.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dies helpful conversions to create html strings.
 * 
 * @author siebel
 * @date 2016-04-01
 */
public class HTMLUtility{

	private static final String ATTRIBUTE_CLASS = "xmlattribute";
	private static final String VALUE_CLASS = "xmlvalue";
	private static final String CONTENT_CLASS = "xmlcontent";
	
	private static final Pattern tagPattern =  Pattern.compile("(<(\\S*?)(\\s.*?)?\\s*/>)   |   (<(\\S*?)(\\s.*?)?>)(([^<]*)(</\\5>))?", Pattern.COMMENTS);
	
	/**
	 * Converts an xml string to html code with syntax highlighting for the input.
	 * @param xml an xml string
	 * @return a html representation of the given xml string
	 */
	public static String xmlToHtml(String xml){
		StringBuilder sb = new StringBuilder();
		sb.append("<p class=\"menu\">(<a onclick=\"expandAll(this)\">expand</a>) (<a onclick=\"collapseAll(this)\">collapse</a>)</p>");
		Matcher matcher = tagPattern.matcher(xml);
		while (matcher.find()){
			String s = matcher.group();
			if (matcher.group(1) != null || s.charAt(1) == '?' || s.charAt(1) == '!'){
				// <tag />
				sb.append("<div>");
				formatTag(sb, s);
				sb.append("</div>");
			}else if (matcher.group(7) != null){
				// <tag>content</tag>
				sb.append("<div>");
				sb.append(htmlEntities(matcher.group(4)) + "<span class=\""+CONTENT_CLASS+"\">" + htmlEntities(matcher.group(8)) + "</span>" + htmlEntities(matcher.group(9)));
				sb.append("</div>");
			}else if (s.charAt(1) == '/'){
				// </tag>
				formatTag(sb, s);
				sb.append("</div>");
			}else{
				// <tag>
				sb.append("<div><button onclick=\"toggle(this)\"></button>");
				formatTag(sb, s);
			}
		}
		sb.append("<p class=\"menu\">(<a onclick=\"expandAll(this)\">expand</a>) (<a onclick=\"collapseAll(this)\">collapse</a>)</p>");
		return sb.toString();
	}

	private static Pattern attributePattern = Pattern.compile("(\\S+?)\\s*=\\s*\"([^\"]*?)\"");
	private static void formatTag(StringBuilder sb, String tag){
		Matcher matcher = attributePattern.matcher(tag);
		int previousEnd = 0;
		while (matcher.find()){
			sb.append(htmlEntities(tag.substring(previousEnd, matcher.start())));

			sb.append("<span class=\""+ATTRIBUTE_CLASS+"\">" + matcher.group(1) + "</span>");
			sb.append("=");
			sb.append("<span class=\""+VALUE_CLASS+"\">\"" + htmlEntities(matcher.group(2)) + "\"</span>");
			
			previousEnd = matcher.end();
		}
		if (previousEnd == 0){
			sb.append(htmlEntities(tag));
		}else{
			sb.append(htmlEntities(tag.substring(previousEnd)));
		}
	}
	
	/**
	 * Escapes special html characters in a string, namely &amp;, &lt;, &gt; and &quot;. 
	 * @param s a string
	 * @return the escaped string
	 */
	public static String htmlEntities(String s){
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"", "&quot;");
		return s;
	}
}
