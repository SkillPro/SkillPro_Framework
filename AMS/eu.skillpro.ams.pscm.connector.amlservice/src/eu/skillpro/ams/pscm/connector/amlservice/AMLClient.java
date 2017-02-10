/**
 * de.fzi.skillpro.connector.amlservice: 18 Mar 2014
 */
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

package eu.skillpro.ams.pscm.connector.amlservice;

import java.rmi.RemoteException;

import eu.skillpro.ams.pscm.connector.amlservice.gen.v2.AMLServerServiceProxy;
import eu.skillpro.ams.pscm.connector.amlservice.gen.v2.AMLServerService_ServiceLocator;
import eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.AMLProject;


/**
 * @author Kiril Aleksandrov
 * @author Abteilung ISPE/PDE, FZI Forschungszentrum Informatik 
 *
 * 18 Mar 2014
 *
 */
public class AMLClient implements AMLClientInterface {
	
	/**
	 * default credentials.
	 */
	private static final String SKILLPRO = "Skillpro";
	
	/**
	 * the singleton instance
	 */
	private static AMLClient instance;
	
	private String username;
	private String password;

	private String address = new AMLServerService_ServiceLocator().getAMLServerServicePortAddress();
	
	/**
	 * Private constructor because this class is a singleton
	 * @param username
	 * @param password
	 */
	private AMLClient(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Create and get an {@link AMLClient}-instance for the given credentials.
	 * <br>
	 * <b>The previous existing instance is overwritten!</b> 
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	static public AMLClient createAMLClient(String username, String password) {
		instance = new AMLClient(username, password);
		return instance;
	}

	/**
	 * This method returns the {@link AMLClient}-instance. If not created, it
	 * creates an {@link AMLClient}-instance with default credentials.
	 * 
	 * @return the {@link AMLClient}-instance
	 */
	static public AMLClient getInstance() {
		if (instance == null)
			instance = new AMLClient(SKILLPRO, SKILLPRO);
		return instance;
	}

	@Override
	public Long[] getAllAMLFiles() throws RemoteException  {
		return getAllAMLFilesByName("%");
	}

	@Override
	public Long[] getAllAMLFilesByName(String fileName) throws RemoteException {
		Long[] result = new Long[0];
		result = new AMLServerServiceProxy().searchforAMLFilesByName(username, password, fileName); 
		return result;
	}

	@Override
	public String getAMLFileInput(Long fileID) throws RemoteException {
		return new AMLServerServiceProxy().getAMLfileStr(username, password, fileID);
	}

	@Override
	public Long saveAMLFile(String fileName, String fileContent) throws RemoteException {
		return new AMLServerServiceProxy().saveAMLfileStr(fileContent, username, password, fileName);
	}

	@Override
	public boolean updateAMLFile(Long fileID, String fileContent) throws RemoteException {
		return new AMLServerServiceProxy().updateAMLfileStr(fileContent, username, password, fileID);
	}
	
	@Override
	public boolean isAlive() {
		String response;
		try {
			response = new AMLServerServiceProxy().hello("FZI");
			return response.equalsIgnoreCase("Hello FZI !");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean deleteAMLFile(Long fileID)
			throws RemoteException {
		return new AMLServerServiceProxy().deleteAMLfile(fileID, username, password);
	}

	@Override
	public AMLProject[] getAMLFilesByName(String fileName)
			throws RemoteException {
		return new AMLServerServiceProxy(address ).searchforAMLFilesByNameXML(username, password, fileName);
	}
	
}
