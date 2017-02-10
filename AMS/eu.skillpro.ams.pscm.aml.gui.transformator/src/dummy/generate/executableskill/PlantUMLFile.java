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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import skillpro.model.skills.dummy.ConditionConfiguration;
import skillpro.model.skills.dummy.ConfigurationSet;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.PropertyDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;

public class PlantUMLFile{
	
	public static void main(String [] args) {
//		new PlantUMLFile("DDEa.plantuml", "DDEa properties.csv").getExecutableSkills();
		new PlantUMLFile("DDEb.plantuml", "DDEb properties.csv").getExecutableSkills();
	}

	public final String filename;
	public final String text;
	public final String filenameProperties;
	public final String textProperties;
	
	private Map<String, Map<String, List<PropertyDummy>>> properties = null;
	
	public PlantUMLFile(String filename, String filenameProperties) {
		this.filename = filename;
		this.text = loadFile(filename);
		this.filenameProperties = filenameProperties;
		this.textProperties = loadFile(filenameProperties);
	}
	
	public List<ExecutableSkillDummy> getExecutableSkills() {
		List<ExecutableSkillDummy> result = new ArrayList<>();
		Pattern pattern = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(\\w+)\\s*\\((.*)\\)\\s*$");
		Pattern patternCondition = Pattern.compile("^\\s*(\\{[\\w\\-,]*\\}|[\\w\\-]*)\\s*,\\s*(\\{.*\\})\\s*$");
		Set<String> templateSkills = new HashSet<>();
		ExecutableSkillDummy current = null;
		for (String line : text.split("[\r\n]+")) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				MatchResult matchResult = matcher.toMatchResult();
				String executableSkillName = matchResult.group(1);
				System.out.println("ExName: " + executableSkillName);
				if (current == null || !current.getName().equals(executableSkillName)) {
					current = new ExecutableSkillDummy(executableSkillName);
					result.add(current);
				}
				
				String resourceName = matchResult.group(2);
				ResourceDummy resource = new ResourceDummy(resourceName, resourceName);
				String properties[] = matchResult.group(3).split(";");
				if (properties.length != 3) {
					throw new IllegalArgumentException("Parse error in file " + filename + " on line " + line);
				}
				String templateSkill = properties[0];
				System.out.println("TEmplateSill: " + templateSkill);
				boolean addMainDummy = true;
				boolean addProducer = false;
				if (templateSkill.endsWith("-Master")) {
					templateSkill = templateSkill.substring(0, templateSkill.length() - 7);
				} else if (templateSkill.endsWith("-Slave")) {
					addMainDummy = false;
					templateSkill = templateSkill.substring(0, templateSkill.length() - 6);
				} else if (templateSkill.endsWith("-Main")) {
					if (current.getMainDummy() != null) {
						addMainDummy = false;
					}
					addProducer = true;
					templateSkill = templateSkill.substring(0, templateSkill.length() - 5);
				}
				System.out.println("TEmplateSillAfter: " + templateSkill);
				templateSkills.add(templateSkill);
				String preCondition = properties[1];
				String postCondition = properties[2];
				
				Matcher matcherPreCondition = patternCondition.matcher(preCondition);
				Matcher matcherPostCondition = patternCondition.matcher(postCondition);
				if (!matcherPreCondition.find() || !matcherPostCondition.find()) {
					throw new IllegalArgumentException("Parse error in file " + filename + " on line " + line);
				} else {
					String preConfiguration = matcherPreCondition.group(1);
					String preProduct = matcherPreCondition.group(2);
					System.out.println("Pre Configuration: " + preConfiguration);
					System.out.println("Pre Product: " + preProduct);
					ConfigurationSet ccPre = getConfigurationSet(preConfiguration);
					if (ccPre.size() > 1) {
						System.out.println("Dear God, what have you done: " + Arrays.toString(ccPre.toArray()));
					}
					String postConfiguration = matcherPostCondition.group(1);
					String postProduct = matcherPostCondition.group(2);
					System.out.println("Post Product: " + postProduct);
					System.out.println("Post Configuration: " + postConfiguration);
					ConfigurationSet ccPost = getConfigurationSet(postConfiguration);
					
					if (ccPost.size() > 1) {
						System.out.println("Dear God, what have you done post: " + Arrays.toString(ccPost.toArray()));
					}
					
					ResourceExecutableSkillDummy res = current.add(resource+executableSkillName, resource, templateSkill);
					System.out.println("Res: " + res);
					System.out.println("========");
					res.setPreCondition(ccPre, preProduct);
					res.setPostCondition(ccPost, postProduct);
					
					for (PropertyDummy p : getProperties(executableSkillName, resourceName)) {
//						System.out.println(p);
						res.addProperty(p.getName(), p.getDataType(), p.getUnit(), p.getValue());
					}
					if (addMainDummy) {
						if (current.getMainDummy() != null) {
							throw new IllegalArgumentException("ExSkill already has a main dummy.");
						}
						current.setMainDummy(res);
					}
					
					if (addProducer) {
						System.out.println("PRODUCER ADDED: " + res);
						current.setProducer(res);
					}
				}
			}
		}
//		System.out.println("Template skills: (" + templateSkills.size() + ")");
//		System.out.println(templateSkills);
		return result;
	}
	
	private ConfigurationSet getConfigurationSet(String configuration) {
		if (configuration.equals("{}")) {
			return new ConfigurationSet();
		} else if (configuration.startsWith("{") && configuration.endsWith("}")) {
			String[] configurationList = configuration.substring(1, configuration.length()-1).split(",");
			ConfigurationSet cs = new ConfigurationSet();
			for (String s : configurationList) {
				cs.add(getConfiguration(s));
			}
			return cs;
		} else {
			return ConfigurationSet.of(getConfiguration(configuration));
		}
	}

	private static ConditionConfiguration getConfiguration(String name) {
		name = name.trim();
		name = name.toUpperCase();
		name = name.replace("-", "_");
		try {
			return ConditionConfiguration.valueOf(name);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown ConditionConfiguration: " + name);
		}
	}
	
	private List<PropertyDummy> getProperties(String skillName, String resourceName) {
		if (properties == null) {
			readProperties();
		}
		Map<String, List<PropertyDummy>> listForSkill = properties.get(skillName);
		if (listForSkill != null) {
			List<PropertyDummy> listForResource = listForSkill.get(resourceName);{
				if (listForResource != null) {
					return listForResource;
				}
			}
		}
		return new ArrayList<>();
	}
	
	private void readProperties() {
		Map<String, Map<String, List<PropertyDummy>>> result = new HashMap<>();
		for (String line : textProperties.split("[\r\n]+")) {
			List<String> data = split(line, ';', '"');
			if (!line.startsWith("//") && !line.trim().isEmpty() && data.size() != 0 && !data.get(0).isEmpty()) {
				// Skill;Resource;PropertyName;TemplateSkill;PropertyDataType;PropertyUnit;PropertyValue
				if (data.size() != 7) {
					throw new IllegalArgumentException("Cannot parse Property: " + line);
				}
				String skillName = data.get(0);
				Map<String, List<PropertyDummy>> listForSkill = result.get(skillName);
				if (listForSkill == null) {
					result.put(skillName, listForSkill = new HashMap<>());
				}
				String resourceName = data.get(1);
				List<PropertyDummy> listForResource = listForSkill.get(resourceName);
				if (listForResource == null) {
					listForSkill.put(resourceName, listForResource = new ArrayList<>());
				}
				
				listForResource.add(new PropertyDummy(data.get(3), data.get(4), data.get(5), data.get(6)));
			}
		}
		properties = result;
	}
	
	/**
	 * Splits a CSV line into cells.
	 * 
	 * @param s the string to be split
	 * @param separator the separator between the cells
	 * @param quote the quotation mark that encloses the text of cells that have
	 *            critical characters
	 * @return a list of Strings
	 */
	private static List<String> split(String s, char separator, char quote) {
		List<String> list = new ArrayList<>();
		int off = 0;
		int next = 0;
		while ((next = s.indexOf(separator, off)) != -1) {
			if (s.charAt(off) == quote) {
				off++;
				next = off-1;
				while ((next = s.indexOf(quote, next+1)) != -1) {
					if (next+1 >= s.length()) {
						next++;
						break;
					} else if (s.charAt(next+1) != quote) {
						next++;
						break;
					} else {
						next++;
					}
				}
				if (next != -1) {
					list.add(s.substring(off, next-1).replace(""+quote+quote, ""+quote));
					off = next + 1;
				}
			} else {
				list.add(s.substring(off, next));
				off = next + 1;
			}
		}
		if (off < s.length()) {
			String remainder = off == 0 ? s : s.substring(off, s.length());
			if (remainder.charAt(0) == quote) {
				remainder = remainder.substring(1, remainder.length()-1);
				remainder = remainder.replace(""+quote+quote, ""+quote);
			}
			list.add(remainder);
		}
		return list;
	}
	
	/**
	 * Loads a file from the resource folder in this project.
	 * @param filename the file name in the folder, without the "/resource/" path in the beginning
	 * @return the file as a string, or an empty string if the file couldn't be read
	 */
	protected String loadFile(String filename) {
		InputStream inputStream = getClass().getResourceAsStream("/" + filename);
		if (inputStream != null) {
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} else {
			return "";
		}
	}
}
