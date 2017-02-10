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

package dummy.generate.executableskill;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import skillpro.model.skills.dummy.Condition;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;

/**
 * A report that shows scenarios and their ExecutableSkills, including pre- and
 * post-conditions
 * 
 * @author siebel
 * 
 */
public class DummyScenarioHtmlReport{
	
	private List<DummyScenario> scenarios;

	/**
	 * Creates a report for the given scenarios.
	 * @param scenarios an array of scenarios
	 */
	public DummyScenarioHtmlReport(List<DummyScenario> scenarios) {
		this.scenarios = scenarios;
	}
	
	/**
	 * Saves the report to the file "skills.html" on the pc's desktop.
	 * @throws FileNotFoundException if the file couldn't be created
	 */
	public void save() throws FileNotFoundException {
		save(System.getProperty("user.home") + "\\Desktop\\skills.html");
	}
	
	/**
	 * Saves the report to the file at the given path.
	 * @param path the file's path
	 * @throws FileNotFoundException if the file couldn't be created
	 */
	public void save(String path) throws FileNotFoundException {
		File file = new File(path);
		try (PrintWriter printWriter = new PrintWriter(file)) {
			save(printWriter);
		} catch (FileNotFoundException e) {
			throw e;
		}
	}

	private void save(PrintWriter printWriter) {
		createContent(printWriter);
	}

	private void createContent(PrintWriter p) {
		p.println("<!DOCTYPE HTML>");
		p.println("<html>");
		p.println("\t<head>");
		p.println("\t<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />");
		p.println("\t<title>Scenarios</title>");
		p.println("\t<style type=\"text/css\">body{font-family:sans-serif} td,th{border:solid 1px #888888;padding:0.1em 0.4em; text-align:left; vertical-align:top}</style>");
		p.println("</head>");
		p.println("<body>");
		p.println("<ul>");
		for (DummyScenario d : scenarios) {
			p.println("\t<li><a href=\"#"+escape(d.getSuffix())+"\">"+escape(d.getName())+"</a></li>");
		}
		p.println("</ul>");
		for (DummyScenario d : scenarios) {
			p.println("\t<h1 id=\"" + escape(d.getSuffix()) + "\">" + escape(d.getName()) + "</h1>");
			p.println("\t<table style=\"border-collapse:collapse\">");
			p.println("\t\t<tr><th>#</th><th>Skill</th><th>Resource</th><th>Condition</th><th>Product</th><th>ResourceSkill</th></tr>");
			int n = 0;
			for (ExecutableSkillDummy e : d.getScenarioSkills()) {
				if (!e.getName().toLowerCase().contains("wp2") && !e.getName().toLowerCase().contains("wp3")) {
					p.println("\t\t<tr>");
					p.println("\t\t\t<th rowspan=\"" + 2*e.getDummies().size() + "\">" + ++n + "</th>");
					p.println("\t\t\t<th rowspan=\"" + 2*e.getDummies().size() + "\">" + escape(e.getName()) + "</th>");
					boolean first = true;
					for (ResourceExecutableSkillDummy r : e.getDummies()) {
						if (!first) {
							p.println("\t\t<tr>");
						} else {
							first = false;
						}
						p.println("\t\t\t<td style=\"background:" + getBackgroundColor(r.getResource().getResourceId()) + "\" rowspan=\"2\">" + r.getResource().getResourceId() + "</td>");
						
						Condition pre = r.getPreCondition();
						p.println("\t\t\t" + printColored("td", pre.getFirstElement().toString()));
						p.println("\t\t\t" + printColored("td", pre.getSecondElement()));
						
						p.println("\t\t\t<td rowspan=\"2\">" + escape(r.getName()) + "</td>");
						p.println("\t\t</tr>");
						p.println("\t\t<tr>");
						
						Condition post = r.getPostCondition();
						p.println("\t\t\t" + printColored("td", post.getFirstElement().toString()));
						p.println("\t\t\t" + printColored("td", post.getSecondElement()));
						
						p.println("\t\t</tr>");
					}
				}
				p.flush();
			}
			p.println("\t</table>");
		}
		p.println("</body>");
		p.println("</html>");
	}
	
	private static String escape(Object o) {
		String s = Objects.toString(o);
		s = s.replace("&", "&amp;");
		s = s.replace("\"", "&quot;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		return s;
	}
	
	private static String printColored(String tag, String text) {
		return String.format("<%s style=\"background:%s\">%s</%s>", tag, getBackgroundColor(text), escape(text), tag);
	}

	/**
	 * A color for the given string. Equal strings return the same color of each
	 * call, different string usually return different colors.
	 * 
	 * @param s a string
	 * @return a color in HTML format, <code>#rrggbb</code>
	 */
	private static String getBackgroundColor(String s) {
		long hash = Objects.hashCode(s);
		int color = (int) (hash & 0xffffff | 0x808080);
		return String.format("#%06X", color);
	}
}
