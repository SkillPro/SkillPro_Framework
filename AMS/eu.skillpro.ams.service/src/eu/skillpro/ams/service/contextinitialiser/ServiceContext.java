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

package eu.skillpro.ams.service.contextinitialiser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.skillpro.ams.service.servlets.BaseServlet;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.assets.CustomerRequestTO;
import eu.skillpro.ams.service.to.assets.ExecutableSkillTO;
import eu.skillpro.ams.service.to.assets.SEETO;
import eu.skillpro.ams.service.to.ros.CallServiceTO;
import eu.skillpro.ams.service.to.test.TestImplementation;
import eu.skillpro.ams.service.to.utility.PSCConfiguration;
import eu.skillpro.ams.service.to.utility.PSCConfiguration.PSCConfigurationState;

@WebListener
public class ServiceContext implements ServletContextListener, HttpSessionListener{
	private static final Logger logger = LoggerFactory.getLogger(ServiceContext.class);
	
	private static ServletContext cxt = null;
	
	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event){
		logger.info("initializing servlet context for ams_service...");
		logger.info("Servlet context for ams_service created with name: " + event.getServletContext().getServletContextName());
		
		cxt = event.getServletContext();
		cxt.setAttribute(BaseServlet.SEEs, new ArrayList<SEETO>());
		cxt.setAttribute(BaseServlet.CUSTOMER_REQUEST, new ArrayList<CustomerRequestTO>());
		cxt.setAttribute(BaseServlet.EXECUTABLE_SKILL, new ArrayList<ExecutableSkillTO>());
		cxt.setAttribute(BaseServlet.ROS_CALL_SERVICE, new ArrayList<CallServiceTO>());
		cxt.setAttribute(BaseServlet.NO_FLY_ZONE, new ArrayList<AssetTO>());
		
		// ----DUMMY DATA
		PSCConfiguration pSCConfiguration = new PSCConfiguration();
		pSCConfiguration.setId(new SimpleDateFormat("YYYMMdd-HHmmssSSS").format(new Date()));
		pSCConfiguration.setPSCConfigurationState(PSCConfigurationState.DIRTY);
		pSCConfiguration.setPSCConfiguration(TestImplementation.getNewAssets());
		savePSCConfiguration(pSCConfiguration);
		// ----DUMMY DATA
		
		System.out.println(pSCConfiguration.getId());
	}
	
	@Override
	public void sessionCreated(HttpSessionEvent event){
		HttpSession session = event.getSession();
		logger.info("Session created " + session.getId());

		Date dateObj = new Date();
		dateObj.setTime(session.getCreationTime());
		logger.info("Created: " + dateObj.toString());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event){
		Date dateObj = new Date();
		HttpSession session = event.getSession();
		
		logger.info("Session closed " + session.getId());
		
		dateObj.setTime(session.getCreationTime());
		logger.info("Created: " + dateObj.toString());
		
		dateObj.setTime(session.getLastAccessedTime());
		logger.info("Last accessed: " + dateObj.toString());
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		logger.info("Servlet context destroyed: " + event.getServletContext().getServletContextName());
	}
	
	public static synchronized void savePSCConfiguration(PSCConfiguration newConfiguration){
		cxt.setAttribute(BaseServlet.PSC_CONFIGURATION, newConfiguration);
	}
	
	public static synchronized PSCConfiguration getPSCConfiguration(){
		return (PSCConfiguration) cxt.getAttribute(BaseServlet.PSC_CONFIGURATION);
	}
	
	public static synchronized void addSEE(SEETO newSEE){
		getAllSEEs().add(newSEE);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<SEETO> getAllSEEs(){
		return (List<SEETO>) cxt.getAttribute(BaseServlet.SEEs);
	}
	
	public static synchronized SEETO getSEEByIdentifier(String identifier){
		if (!identifier.isEmpty()){
			for (SEETO see : getAllSEEs()){
				if (identifier.equals(see.getIdentifier())){
					return see;
				}
			}
		}
		return null;
	}
	
	public static synchronized SEETO getSEEById(String seeId){
		if (!seeId.isEmpty()){ // TODO unnecessary check? Can SEEs in the list ever have an empty id?
			for (SEETO see : getAllSEEs()){
				if (seeId.equals(see.getSeeID())){
					return see;
				}
			}
		}
		return null;
	}
	
	public static synchronized void addCustomerRequest(CustomerRequestTO newCustomerRequest){
		getAllCustomerRequests().add(newCustomerRequest);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<CustomerRequestTO> getAllCustomerRequests(){
		return (List<CustomerRequestTO>) cxt.getAttribute(BaseServlet.CUSTOMER_REQUEST);
	}
	
	public static synchronized CustomerRequestTO getCustomerRequestsByOrderId(String orderId){
		for (CustomerRequestTO cutsomerRequest : getAllCustomerRequests()){
			if (cutsomerRequest.getOrderID().equals(orderId)){
				return cutsomerRequest;
			}
		}
		return null;
	}
	
	public static synchronized void addExecutableSkill(ExecutableSkillTO newExecutableSkill){
		getAllExecutableSkills().add(newExecutableSkill);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<ExecutableSkillTO> getAllExecutableSkills(){
		return (List<ExecutableSkillTO>) cxt.getAttribute(BaseServlet.EXECUTABLE_SKILL);
	}
	
	/**
	 * Finds an executable skill by its ID
	 * 
	 * @param resourceExecutableSkillID the ID of the skill
	 * @return the skill as a transfer object, or <code>null</code> if no
	 *         matching skill was found
	 */
	public static ExecutableSkillTO getExecutableSkillById(String resourceExecutableSkillID){
		for (ExecutableSkillTO executableSkill : getAllExecutableSkills()){
			if (executableSkill.getResourceExecutableSkillID().equals(resourceExecutableSkillID)){
				return executableSkill;
			}
		}
		return null;
	}
	
	public static synchronized void addCallService(CallServiceTO callService){
		getAllCallServices().add(callService);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<CallServiceTO> getAllCallServices(){
		return (List<CallServiceTO>) cxt.getAttribute(BaseServlet.ROS_CALL_SERVICE);
	}
	
	@SuppressWarnings("unchecked")
	public static List<AssetTO> getNoFlyZones(){
		return (List<AssetTO>) cxt.getAttribute(BaseServlet.NO_FLY_ZONE);
	}
}
