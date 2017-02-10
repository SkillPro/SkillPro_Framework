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

import eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.AMLProject;


/**
 * @author Kiril Aleksandrov
 * @author Abteilung ISPE/PDE, FZI Forschungszentrum Informatik 
 *
 * 18 Mar 2014
 *
 * last change: 18 Mar 2014
 */
public interface AMLClientInterface {

	/**
	 * Returns a list of all AML files
	 * @return list of string names for files
	 * @throws RemoteException 
	 */
	public Long[] getAllAMLFiles() throws RemoteException;

	/**
	 * returns a list of the IDs of the files that match the given name
	 * 
	 * @param fileName
	 *            the name of the searched file. Can contain wildcards % (for
	 *            multiple symbols if any) or _ for a single symbol.
	 * @return List of Long ids for the files matching the given string
	 */
	public Long[] getAllAMLFilesByName(String fileName) throws RemoteException;
	
	/**
	 * returns a list of AMLProjects, that contain file id and file name
	 * 
	 * @param fileName - the name to search for (can contain wildcards % )
	 * @return the array of matched AMLProjects
	 * @throws RemoteException
	 */
	public AMLProject[] getAMLFilesByName(String fileName) throws RemoteException;
	/**
	 * @param fileID
	 * @return the AML-File content (XML-Format) as a String
	 */
	public String getAMLFileInput(Long fileID) throws RemoteException;
	
	/**	
	 * Initially saves a file with with the given name and provided content
	 * @param fileName name of the new file
	 * @param fileContent
	 * @return the fileID
	 */
	public Long saveAMLFile(String fileName, String fileContent) throws RemoteException;
	/**
	 * Updates an AML file by the given name/id with the provided content
	 * @param fileName the name of the file to be updated
	 * @param fileContent the changed file content. <b>complete file content!</b>
	 * @return true if update successful, false otherwise
	 */
	public boolean updateAMLFile(Long fileID, String fileContent) throws RemoteException;
	
	/**
	 * @param fileID
	 * @param fileContent
	 * @return
	 * @throws RemoteException
	 */
	public boolean deleteAMLFile(Long fileID) throws RemoteException;
	
	/**
	 * checks if the server is online and reachable
	 * @return return true if service alive, false otherwise
	 */
	public boolean isAlive();
}
