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

package eu.skillpro.ams.pscm.connector.opcua;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.ServerState;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.spongycastle.util.encoders.Base64;

import skillpro.model.products.Order;
import skillpro.model.utils.ExecutableSkillKPIs;
import skillpro.model.utils.Pair;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaAddress;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.nodes.MethodArgumentException;
import com.prosysopc.ua.nodes.UaMethod;

public class OPCUAServerRepository {
	private static final String APPLICATION_NAME = "PSCM";
	/*
	 * A place for all constants that could be made configurable in later
	 * versions
	 */
	public static final String EDO_FZI_SERVER = "opc.tcp://141.21.13.59:51200";
	public static final String IOSB_SERVER = "opc.tcp://192.44.1.52:51200";
	public static final String LOCALHOST_SERVER = "opc.tcp://localhost:51200";
	public static final String DEFAULT_EDO_SERVER = "opc.tcp://edo:51200";
	public static final String EDO_SERVER = "opc.tcp://172.22.151.105:51200";
	public static final String IOSB_SERVER_2 = "opc.tcp://SkillPro.iosb.fraunhofer.de:51200";
	public static final String HMI_SERVER = "opc.tcp://10.69.69.55:51200";
	public static final String IPR_SERVER = "opc.tcp://141.3.82.74:51200";

	private final static int uncompressedLengthLimit = (1 << 15) - 1024;
	
	private static Map<String, UaClient> clients = new HashMap<String, UaClient>();

	private static String selectedUAClient = EDO_FZI_SERVER;
	
	private static Map<Integer, List<DataPoint>> dataPoints = new HashMap<>();
	private static int modeActive = 210;
	
	/**
	 * @param urlAddress
	 * @throws SkillProOPCUAException
	 *             if address is already in use or address has an unsupported URI syntax.
	 */
	public static void createClientForURL(String urlAddress)
			throws SkillProOPCUAException {
		if (clients.containsKey(urlAddress)) {
			throw new SkillProOPCUAException("Address exists.");
		} else {
			try {
				clients.put(urlAddress, new UaClient(urlAddress));
				selectedUAClient = urlAddress;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new SkillProOPCUAException(e);
			}
		}
	}

	/**
	 * Connects the service instance of the provided urlAddress to the
	 * OPCUAServer
	 * 
	 * @param urlAddress
	 * @throws SkillProOPCUAException
	 */
	public static void connect(String urlAddress) throws SkillProOPCUAException {
		if (!clients.containsKey(urlAddress)) {
			throw new SkillProOPCUAException(
					"No server created for the provided urlAddress: "
							+ urlAddress);
		} else {
			if (clients.get(urlAddress).isConnected()) {
				throw new SkillProOPCUAException("Already connected");
			}
			try {
				clients.get(urlAddress).connect();
			} catch (ServiceException e) {
				e.printStackTrace();
				System.out.println("Removed Client: " + urlAddress);
				clients.remove(urlAddress);
				throw new SkillProOPCUAException(e);
			}
		}
	}

	/**
	 * @param urlAddress
	 * @throws SkillProOPCUAException
	 */
	public static void disconnect(String urlAddress)
			throws SkillProOPCUAException {
		if (!clients.containsKey(urlAddress)) {
			throw new SkillProOPCUAException(
					"No server created for the provided urlAddress: "
							+ urlAddress);
		} else {
			if (!clients.get(urlAddress).isConnected()) {
				throw new SkillProOPCUAException("Not connected");
			}
			clients.get(urlAddress).disconnect();
		}
	}
	
	public static void disconnectAll() throws SkillProOPCUAException {
		for (String serverAddress: clients.keySet()) {
			disconnect(serverAddress);
		}
	}

	/**
	 * This method is to be called on a urlAddress before the
	 * {@link #createClientForURL(String)} for the urlAddress is called
	 * 
	 * @param urlAddress
	 * @return null if urlAddress valid, the error message otherwise
	 */
	public static String verifyAddress(String urlAddress) {
		try {
			new UaAddress(urlAddress);
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public static String getSelectedServerUri() {
		return selectedUAClient;
	}

	public static void configureServer(String value) {
		UaClient client = clients.get(value);

		//application description and identity
		ApplicationDescription application = new ApplicationDescription();
		application.setApplicationName(new LocalizedText(APPLICATION_NAME,
				Locale.ENGLISH));
		application.setApplicationUri("urn:localhost:UA:AMS."
				+ APPLICATION_NAME);
		application.setProductUri("urn:fzi.de:UA:AMS." + APPLICATION_NAME);
		application.setApplicationType(ApplicationType.Client);

		ApplicationIdentity identity = new ApplicationIdentity();
		identity.setApplicationDescription(application);

		client.setApplicationIdentity(identity);

		//security mode
		client.setSecurityMode(SecurityMode.NONE);
	}

	public static String registerSEE(String configurationID, String amlSnippet,
			String serverURL) throws SkillProOPCUAException {
		UaClient uaClient = clients.get(serverURL);
		try {
			NodeId nodeId = getSeeNodeId();
			List<UaMethod> methods = uaClient.getAddressSpace().getMethods(
					nodeId);
			//the create method
			NodeId createId = methods.get(0).getNodeId();
			//create method call
			Variant nameArgument = new Variant(configurationID);
			Variant amlArgument = new Variant(amlSnippet);
			Variant[] output = uaClient.call(nodeId, createId, nameArgument,
					amlArgument);
			return output[0].toString();

		} catch (ServiceException | StatusException | AddressSpaceException e) {
			try {
				registerSEE2(configurationID, amlSnippet, configurationID, serverURL);
			} catch (SkillProOPCUAException e2) {
				throw e2;
			}
			e.printStackTrace();
			throw new SkillProOPCUAException(e);
		}

	}
	
	public static String registerSEE2(String seeName, String amlSnippet, String configurationID, 
			String serverURL) throws SkillProOPCUAException {
//		return createDummyMap().get(seeName);
		UaClient uaClient = clients.get(serverURL);
		try {
			//SEE node
			NodeId nodeId = new NodeId(2, "SEE");
			List<UaMethod> methods = uaClient.getAddressSpace().getMethods(
					nodeId);
			//the create method
			NodeId createId = methods.get(0).getNodeId();

			Variant nameArgument = new Variant(seeName);
			Variant amlArgument = new Variant(amlSnippet);
			Variant[] output = null;
			if (methods.get(0).getInputArguments().length == 3) {
				Variant seeArgument = new Variant(configurationID);
				output = uaClient.call(nodeId, createId, nameArgument,
						amlArgument, seeArgument);
			} else {
			//create method call
			output = uaClient.call(nodeId, createId, nameArgument,
					amlArgument);
			}
			return output[0].toString();

		} catch (ServiceException | StatusException | AddressSpaceException e) {
			e.printStackTrace();
			throw new SkillProOPCUAException(e);
		} catch (MethodArgumentException e) {
			e.printStackTrace();
			throw new SkillProOPCUAException(e);
		}

	}
	
//	private static Map<String, String> createDummyMap() {
//		Map<String, String> dummyMap = new HashMap<>();
//		
//		dummyMap.put("HumanWorker", "ns=2;i=3001");
//		dummyMap.put("KR5", "ns=2;i=3005");
//		dummyMap.put("SchunkArm","ns=2;i=3009");
//		dummyMap.put("BoxOutput","ns=2;i=3013");
//		dummyMap.put("MobilePlatform","ns=2;i=3017");
//		dummyMap.put("SolderingMachine","ns=2;i=3021");
//		dummyMap.put("QualityCheck","ns=2;i=3025");
//		dummyMap.put("KR5Output","ns=2;i=3029");
//		dummyMap.put("LineOutput","ns=2;i=3033");
//		dummyMap.put("PCBPrinter","ns=2;i=3037");
//		
//		return dummyMap;
//	}

	public static boolean testConnection(String currentUAaddress) {
		return clients.get(currentUAaddress) != null && clients.get(currentUAaddress).isConnected()
				&& clients.get(currentUAaddress).getServerState() != ServerState.CommunicationFault;
	}

	public static void deregisterSEE(String nodeID, String currentUAaddress) throws SkillProOPCUAException {
		UaClient uaClient = clients.get(currentUAaddress);
		try {
			//SEE node
			NodeId nodeId = new NodeId(2, "SEE");
			List<UaMethod> methods = uaClient.getAddressSpace().getMethods(
					nodeId);

			NodeId deleteId = methods.get(1).getNodeId(); //the remove method

			NodeId nodeIdtoDelete = parseNodeIdString(nodeID);

			//create method call
			Variant nodeToDeleteArgument = new Variant(nodeIdtoDelete);
			
			uaClient.call(nodeId, deleteId, nodeToDeleteArgument);

		} catch (ServiceException | StatusException | AddressSpaceException | SkillProOPCUAException e) {
			e.printStackTrace();
			throw new SkillProOPCUAException(e);
		}
		throw new SkillProOPCUAException("Method not implemented yet!");
	}

	private static NodeId parseNodeIdString(String completeNodeId) throws SkillProOPCUAException {
		String[] uastrings = completeNodeId.split(";");
		if (uastrings.length != 2) {
			throw new SkillProOPCUAException("Wrong id to delete: " + completeNodeId);
		}
		String addressSpace = uastrings[0].substring(uastrings[0].indexOf("=") + 1);
		String nodeID = uastrings[1].substring(uastrings[1].indexOf("=") + 1);
		try {
			return new NodeId(Integer.parseInt(addressSpace), nodeID);
		} catch (Exception e) {
			throw new SkillProOPCUAException(e);
		}		
	}
	
	public static String sendOrders(Order order, int priority, String amlExSkills,
			List<String> goalSkills, String serverURL) throws SkillProOPCUAException {
		UaClient uaClient = clients.get(serverURL);
		try {
			//SEE node
			NodeId nodeId = new NodeId(2, "Orders");
			List<UaMethod> methods = uaClient.getAddressSpace().getMethods(
					nodeId);

			if (methods.size()!= 2) {
				throw new SkillProOPCUAException("Number of methods for Orders not as expected! Check the version of the OPC-UA server!\n Methods number expected: 2, actual methods number: "+methods.size()); 
			}
			//the create method
			NodeId createId = methods.get(0).getNodeId();
			Variant productNameArgument = new Variant(order.getOrderID());
			Variant productid = new Variant(order.getProductQuantity().getProduct().getProductTypeID());
			Variant quantityArgument = new Variant(order.getProductQuantity().getQuantity());
			Calendar latestDelCalender = order.getDeadline();
			int month = latestDelCalender.get(Calendar.MONTH) - 1; //To be write in OPCUA
			latestDelCalender.set(Calendar.MONTH, month);
			DateTime time = new DateTime(latestDelCalender);
			Variant latestDeliveryDateArgument = new Variant(time);

			Variant priorityArgument = new Variant(priority);
			
			Variant[] output = null;
			
			//create method call
			output = uaClient.call(nodeId, createId, productNameArgument, productid,
					quantityArgument, latestDeliveryDateArgument,
					priorityArgument);
			
			NodeId nodeIdOrder = parseNodeIdString(output[0].toString() + ".ExecutableSkills");
			
			/* If the executable skills are too large, they will be compressed */
			String amlExSkillsCompressed = compressIfNecessary(amlExSkills);
			uaClient.writeAttribute(nodeIdOrder, Attributes.Value, new DataValue(new Variant(amlExSkillsCompressed)));
			
			nodeIdOrder = parseNodeIdString(output[0].toString() + ".GoalSkills");
			
			String goalSkillsInString = "";
			for (String goalSkill : goalSkills) {
				if (goalSkillsInString.isEmpty()) {
					goalSkillsInString += goalSkill;
				} else {
					goalSkillsInString += "," + goalSkill;
				}
			}
			
			uaClient.writeAttribute(nodeIdOrder, Attributes.Value, new DataValue(new Variant(goalSkillsInString)));
			nodeIdOrder = parseNodeIdString(output[0].toString() + ".Name");
			uaClient.writeAttribute(nodeIdOrder, Attributes.Value, new DataValue(new Variant(""+order.getOrderName())));
			nodeIdOrder = parseNodeIdString(output[0].toString() + ".Mode");
			uaClient.writeAttribute(nodeIdOrder, Attributes.Value, 110);
			
			return output[0].toString();

		} catch (ServiceException | StatusException | AddressSpaceException e) {
			e.printStackTrace();
			throw new SkillProOPCUAException(e);
		}
	}

	/**
	 * Shortens a string by compressing it, if necessary. If the string is too long, it is GZIPed and converted to BASE64.
	 * @param amlExSkills a string
	 * @return a possibly compressed string
	 */
	private static String compressIfNecessary(String amlExSkills) {
		if (amlExSkills.length() > uncompressedLengthLimit) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream zip = new GZIPOutputStream(out)) {
				zip.write(amlExSkills.getBytes());
				zip.close();
				byte[] compressedBytes = out.toByteArray();
				byte[] compressedBase64 = Base64.encode(compressedBytes);
				return new String(compressedBase64);
			} catch (IOException e) {
				e.printStackTrace();
				return amlExSkills;
			}
		} else {
			return amlExSkills;
		}
	}

	private static NodeId getSeeNodeId() {
		return new NodeId(2, "SEE");
	}

	public static Map<String, ExecutableSkillKPIs> getKPIData(String serverURL) {
		Map<String, ExecutableSkillKPIs> result = new HashMap<>();
		UaClient uaClient = clients.get(serverURL);
		NodeId seeNodeId = getSeeNodeId();
		try {
			List<ReferenceDescription> list = uaClient.getAddressSpace().browse(seeNodeId);
			for (ReferenceDescription rd : list) {
				result.putAll(getKPIData(((UnsignedInteger) rd.getNodeId().getValue()).toIntBits(), serverURL));
			}
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (StatusException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Map<String, ExecutableSkillKPIs> getKPIData(int seeNodeIdentifier, String serverURL) {
		List<DataPoint> dataPointList = dataPoints.get(seeNodeIdentifier);
		if (dataPointList == null) {
			dataPoints.put(seeNodeIdentifier, dataPointList = new ArrayList<>());
		}
		UaClient uaClient = clients.get(serverURL);
		NodeId nodeId = new NodeId(2, seeNodeIdentifier + ".KPIList");
		try {
			String s = uaClient.readValue(nodeId).getValue().toString();
			String[] entries = s.split(";");
			for (String entry : entries) {
				String[] cells = entry.split(";");
				for (String c : cells) {
					try {
						DataPoint dp = new DataPoint(c);
						if (!dataPointList.contains(dp)) {
							dataPointList.add(dp);
						}
					} catch (IllegalArgumentException e) {
					}
				}
			}
			Collections.sort(dataPointList);
		} catch (ServiceException | StatusException e1) {
			e1.printStackTrace();
		}
		Map<String, List<Pair<Double, Double>>> timeBySkill = getExecutionTimeBySkill(dataPointList);
		Map<String, ExecutableSkillKPIs> result = new HashMap<>();
		for (Entry<String, List<Pair<Double, Double>>> e : timeBySkill.entrySet()) {
			double sumProductive = 0;
			double sumLead = 0;
			List<Pair<Double, Double>> list = e.getValue();
			if (list.size() > 0) {
				for (Pair<Double, Double> d : list) {
					sumProductive += d.getFirstElement();
					sumLead += d.getFirstElement();
				}
				Pair<Double, Double> recent = list.get(list.size() - 1);

				double averageExecutionTime = (sumProductive+sumLead) / list.size();
				double recentExecutionTime = recent.getFirstElement() + recent.getSecondElement();
				double averageLeadTime = sumLead / list.size();
				double recentLeadTime = recent.getSecondElement();
				double averageMainProductiveTime = sumLead / list.size();
				double recentMainProductiveTime = recent.getSecondElement();
				ExecutableSkillKPIs esk = new ExecutableSkillKPIs(averageExecutionTime, recentExecutionTime, averageLeadTime,
						recentLeadTime, averageMainProductiveTime, recentMainProductiveTime);
				result.put(e.getKey(), esk);
			}
		}
		return result;
	}
	
	 
	private static Map<String, List<Pair<Double, Double>>> getExecutionTimeBySkill(List<DataPoint> dataPointList) {
		Map<String, List<Pair<Double, Double>>> timeBySkill = new HashMap<>();
		if (!dataPointList.isEmpty()) {
			Date previousStart = dataPointList.get(0).date;
			int previousMode = -1;
			double productiveTime = 0;
			double leadTime = 0;
			for (DataPoint dp : dataPointList) {
				if (dp.mode == modeActive) {
					double timeSincePrevious = (dp.date.getTime() - previousStart.getTime()) / 1000.0;
					if (dp.isProductive) {
						productiveTime += timeSincePrevious;
					} else {
						leadTime += timeSincePrevious;
					}
				} else if(previousMode == modeActive) {
					List<Pair<Double, Double>> list = timeBySkill.get(dp.rexSkillId);
					if (list == null) {
						timeBySkill.put(dp.rexSkillId, list = new ArrayList<>());
					}
					list.add(new Pair<Double, Double>(productiveTime, leadTime));
				}
				previousMode = dp.mode;
			}
		}
		return timeBySkill;
	}
	
	private static class DataPoint implements Comparable<DataPoint>{
		private final SimpleDateFormat sdf = new SimpleDateFormat("YY-mm-dd hh:mm:ss");

		private final Date date;
		private final int mode;
		private final String rexSkillId;
		private final boolean isProductive;
		
		private DataPoint(String dataPointString) throws IllegalArgumentException {
			String[] fields = dataPointString.split(",");
			try {
				this.date = sdf.parse(fields[0]);
				this.mode = Integer.valueOf(fields[1]);
				this.rexSkillId = fields[2];
				this.isProductive = Boolean.valueOf(fields[3]);
			} catch (ParseException e) {
				throw new IllegalArgumentException();
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(date, isProductive, mode, rexSkillId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataPoint other = (DataPoint) obj;

			return Objects.equals(date, other.date)
					&& isProductive == other.isProductive
					&& mode == other.mode
					&& Objects.equals(rexSkillId, other.rexSkillId);
		}

		@Override
		public int compareTo(DataPoint o) {
			return date.compareTo(o.date);
		}
		
		@Override
		public String toString() {
			return "Date: " + date + ", mode: " + mode + ", rexSkillID: " + rexSkillId 
					+ ", isProductive: " + isProductive;
		}
	}
}
