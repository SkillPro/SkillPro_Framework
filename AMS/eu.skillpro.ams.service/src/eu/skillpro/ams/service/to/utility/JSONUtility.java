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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Anteo
 *
 */
public class JSONUtility {
	//https://code.google.com/p/google-gson/issues/detail?id=203
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	/**
	 * @param object
	 * @return conversion of object to json
	 */
	public static String convertToJSON(Object object) {
		String json = gson.toJson(object);
		return json;
	}
	
	/**
	 * @param jsonString
	 * @param classOfT
	 * @return conversion of json to Object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertToObject(String jsonString, Class<T> classOfT) {
		Object object = gson.fromJson(jsonString, classOfT);
		return (T) object;
	}

	/**
	 * Call like:
	 * <pre> 
	 * ArrayList< String > result = JSONUtility.convertToList(jsonLine, new TypeToken< List< String > >() {}.getType());
	 * </pre>
	 * or for primitive types
	 * <pre> 
	 * ArrayList< Object > c = JSONUtility.convertToList(jsonLine, ArrayList.class);
	 * </pre>
	 * @param jsonString
	 * @param type
	 * @return conversion of 
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> convertToList(String jsonString, Type type) {
		Object object = gson.fromJson(jsonString, type);
		return (ArrayList<T>) object;
	}
	
	/**
	 * Call like:
	 * <pre> 
	 * Map< String, FBOBaseTO > result = JSONUtility.convertToMap(line, new TypeToken< Map< String, FBOBaseTO > >() {}.getType());
	 * </pre>
	 * @param jsonString
	 * @param type
	 * @return conversion of json to Map 
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> convertToMap(String jsonString, Type type) {
		Object object = gson.fromJson(jsonString, type);
		return (Map<K, V>) object;
	}
}
