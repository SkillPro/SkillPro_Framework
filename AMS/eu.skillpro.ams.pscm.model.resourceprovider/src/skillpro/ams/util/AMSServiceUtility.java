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

package skillpro.ams.util;

public class AMSServiceUtility {
	public static final String SERVICE_ADDRESS_EDO_FZI = "http://141.21.13.59:8080/amsservice/";
	public static final String SERVICE_ADDRESS_FUKUSHIMA = "http://172.22.151.106:8080/amsservice/";
	public static final String SERVICE_ADDRESS_LOCALHOST = "http://localhost:8080/amsservice/";
	public static final String SERVICE_ADDRESS_OSAKA05 = "http://osaka05.fzi.de:8080/amsservice/";
	public static final String SERVICE_ADDRESS_OTHER_LOCALHOST = "http://localhost:8081/eu.skillpro.ams.service/";
	public static final String SERVICE_ADDRESS_ENDDEMO = "http://141.3.82.79:8080/amsservice/";
	
	public static String serviceAddress = SERVICE_ADDRESS_EDO_FZI;
}
