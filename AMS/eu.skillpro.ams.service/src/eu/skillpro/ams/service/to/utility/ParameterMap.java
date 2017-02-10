package eu.skillpro.ams.service.to.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;

import eu.skillpro.ams.service.servlets.MissingParameterException;

/**
 * Manages the parameters in a request object and provides methods to retrieve them.
 * @author siebel
 * @date 2016-12-15
 *
 */
public class ParameterMap{
	
	private final HttpServletRequest request;
	private HashMap<String, String> post = null;
	
	public ParameterMap(HttpServletRequest request){
		this.request = request;
	}
	
	/**
	 * Calling this makes this ParameterMap use the Parameters from the http header. This is necessary for submitted multipart form data.
	 * @return a reference to this ParameterMap.
	 * @throws IOException if the header could not be read
	 */
	public ParameterMap usePost() throws IOException{
		HttpEntity entity = new InputStreamEntity(request.getInputStream(), request.getContentLength());
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
		String line, lastLine = "";
		while ((line = reader.readLine()) != null) {
			lastLine = line;
		}
		Map<String, String> post = new HashMap<>();
		try (JsonReader jsonReader = Json.createReader(new StringReader(lastLine))){
			for (Entry<String, JsonValue> entry : jsonReader.readObject().entrySet()){
				post.put(entry.getKey(), entry.getValue().toString());
			}
		}catch (JsonParsingException e){
			System.out.println("ParameterMap: No valid parameter data in header!");
		}
		return this;
	}
	
	private String getFromSource(String name){
		if (post == null){
			return request.getParameter(name);
		}else{
			return post.get(name);
		}
	}
	
	public String get(String name) throws MissingParameterException{
		String result = getFromSource(name);
		if (result == null){
			throw new MissingParameterException("Missing parameter: " + name, name);
		}else{
			return result;
		}
	}
	
	public String getNonEmpty(String name) throws MissingParameterException{
		String result = get(name);
		if (result.trim().isEmpty()){
			throw new MissingParameterException("Missing parameter: " + name, name);
		}else{
			return result;
		}
	}
	
	public String getOptional(String name){
		return getOptional(name, null);
	}
	
	public String getOptional(String name, String defaultValue){
		String result = getFromSource(name);
		if (result == null){
			return defaultValue;
		}else{
			return result;
		}
	}
	
	public double getDouble(String name) throws MissingParameterException, NumberFormatException{
		return Double.valueOf(get(name));
	}
	
	public int getInt(String name) throws MissingParameterException, NumberFormatException{
		return Integer.valueOf(get(name));
	}
	
	public int getOptionalInt(String name, int defaultValue) throws NumberFormatException{
		String optionalParameter = getOptional(name);
		if (optionalParameter == null){
			return defaultValue;
		}else{
			return Integer.valueOf(optionalParameter);
		}
	}
	
	public boolean getOptionalBoolean(String name, boolean defaultValue){
		String parameter = getFromSource(name);
		if (parameter != null){
			return Boolean.parseBoolean(parameter);
		}else{
			return defaultValue;
		}
	}
}
