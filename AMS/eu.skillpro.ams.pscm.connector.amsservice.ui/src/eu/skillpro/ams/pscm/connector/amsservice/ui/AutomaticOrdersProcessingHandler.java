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

package eu.skillpro.ams.pscm.connector.amsservice.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.Factory;
import skillpro.model.products.CustomerRequest;
import skillpro.model.products.Order;
import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import aml.amlparser.AMLExporter;
import aml.amlparser.AMLParser;

import com.google.gson.reflect.TypeToken;

import dummy.generate.executableskill.DDEScenarioA;
import dummy.generate.executableskill.DDEScenarioB;
import dummy.generate.executableskill.FullOrderScenario;
import dummy.generate.executableskill.KR6OnlyOrderScenario;
import dummy.generate.executableskill.OnlyChocolateOrderScenario;
import dummy.generate.executableskill.UR5OnlyOrderScenario;
import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.connector.opcua.SkillProOPCUAException;
import eu.skillpro.ams.service.to.assets.CustomerRequestTO;

public class AutomaticOrdersProcessingHandler extends AbstractHandler implements IHandler {
	private Job synchronizationJob;
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (synchronizationJob == null) {
			synchronizationJob = new Job("Retrieving and sending orders automatically...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					while (!monitor.isCanceled()) {
						try {
							for (CustomerRequestTO reqTO : getNewCustomerRequestTOs()) {
								processCustomerRequestTO(reqTO);
							}
						} catch (ClientProtocolException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (SkillProOPCUAException e) {
							MessageDialog.openError(Display.getCurrent().getActiveShell(), "OPCUA error", e.getMessage());
							System.err.println("OPCUA error: " + e.getMessage());
							e.printStackTrace();
						} catch (ParsingException e) {
							e.printStackTrace();
						} catch (TransformerException e) {
							e.printStackTrace();
						}

						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					monitor.done();
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};
			synchronizationJob.setUser(false);
			synchronizationJob.schedule();
		} else {
			if (synchronizationJob.getState() == Job.RUNNING) {
				synchronizationJob.cancel();
			} else {
				synchronizationJob.schedule();
			}
		}
		return null;
	}

	private void processCustomerRequestTO(CustomerRequestTO reqTO) throws IOException, SkillProOPCUAException, ValidityException, ParsingException, TransformerException {
		CustomerRequest req = new CustomerRequest(reqTO.getCustomerName(), 
				reqTO.getOrderID(), reqTO.getProducts(), reqTO.isHumanSEE(), reqTO.getSeeID());
		String snippet;
		List<String> goalSkillsList;
		String productName;
		switch (reqTO.getScenarioType()) {
		case FULL_ORDER:
			snippet = loadFile("fullorderscenario.aml");
			snippet = replaceCustomerSnippet(snippet, req);
			goalSkillsList = FullOrderScenario.getInstance().getGoalSkills();
			productName = "Box with chocolate and flag";
			break;
		case CHOCOLATE_ONLY:
			snippet = loadFile("onlychocolate.aml");
			snippet = replaceCustomerSnippet(snippet, req);
			goalSkillsList = OnlyChocolateOrderScenario.getInstance().getGoalSkills();
			productName = "Box with chocolate and flag";
			break;
		case KR6_ONLY:
			snippet = loadFile("kr6only.aml");
			snippet = snippet.replace("$top-color$", toHex("black"));
			goalSkillsList = KR6OnlyOrderScenario.getInstance().getGoalSkills();
			productName = "Box with chocolate and flag";
			break;
		case UR5_ONLY:
			snippet = loadFile("ur5only.aml");
			snippet = snippet.replace("$chocolate-color$", "blue");
			goalSkillsList = UR5OnlyOrderScenario.getInstance().getGoalSkills();
			productName = "Box with chocolate and flag";
			break;
		case DDE_A:
			snippet = loadFile("DDEa.aml");
			goalSkillsList = DDEScenarioA.getInstance().getGoalSkills();
			productName = "Product A";
			break;
		case DDE_B:
			snippet = loadFile("DDEb.aml");
			goalSkillsList = DDEScenarioB.getInstance().getGoalSkills();
			productName = "Product B";
			break;
		default:
			throw new IllegalArgumentException("Unidentifiable scenario type: " + reqTO.getScenarioType());
		}
		Order order = transformToOrder(req, productName, reqTO.getCount());
		
		SkillproService.getSkillproProvider().getOrderRepo().add(order);
		
		saveToDesktop(snippet, goalSkillsList);
		OPCUAServerRepository.sendOrders(order, reqTO.getScenarioType().defaultPriority, snippet.toString(), goalSkillsList, Activator.getDefault().getCurrentUAaddress());
		sendSkillsToAMS(req, snippet);
		SkillproService.getSkillproProvider().getOrderRepo().getEntities().remove(order);
	}
	
	private void sendSkillsToAMS(CustomerRequest req, String snippet) throws ValidityException, ParsingException, IOException, TransformerException {
		List<String> excludedStrings = getExcludedStrings(req.getSeeID());
		Document snippetDoc = AMLParser.getInstance().getDocumentFromString(snippet);
		snippetDoc = AMLExporter.excludeChildrenFromDocuments(snippetDoc, excludedStrings);
		Map<String, Document> childrenOfDocAsDocs = AMLExporter.getSecondChildrenOfDocAsDocs(snippetDoc);
		for (Entry<String, Document> entry : childrenOfDocAsDocs.entrySet()) {
			String rexSnippet = AMLExporter.getExportedAsString(entry.getValue());
			rexSnippet = rexSnippet.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
			String seeID = getSEEID(entry.getValue());
			if (seeID != null) {
				seeID = seeID.trim();
			}
			SendExecutableSkillToServer.push(entry.getKey(), rexSnippet, seeID);
		}
	}

	private String replaceCustomerSnippet(String snippet, CustomerRequest req) {
		List<String> products = new ArrayList<>(req.getProducts());
		while (products.size() < 4) {
			products.add("");
		}
		String productTop = products.get(0);
		String productMiddle = products.get(1);
		String productBottom = products.get(2);
		String chocolate = products.get(3);
		
		return snippet
				.replace("$top-product$", productTop)
				.replace("$top-color$", toHex(productTop))
				.replace("$top-color-name$", productTop)
				.replace("$middle-product$", productMiddle)
				.replace("$middle-color$", toHex(productMiddle))
				.replace("$middle-color-name$", productMiddle)
				.replace("$bottom-product$", productBottom)
				.replace("$bottom-color$", toHex(productBottom))
				.replace("$bottom-color-name$", productBottom)
				.replace("$chocolate$", " " + chocolate)
				.replace("$chocolate-color$", chocolate.replace(" chocolate", ""));
	}
	
	private List<String> getExcludedStrings(String seeID) {
		List<String> result = Arrays.asList("WP1", "WP2", "WP3");
		switch (seeID) {
		case "217137e4-d23e-11e4-b49d-88b718c45141":
			result.remove("WP1");
			break;
		case "2c8db814-d23e-11e4-92d1-88b718c45141":
			result.remove("WP2");
			break;
		case "352354b6-d23e-11e4-aba8-88b718c45141":
			result.remove("WP3");
			break;
		}
		return result;
	}
	
	private String getSEEID(Document doc) {
		Element root = doc.getRootElement();
		Elements childrenOfRoot = root.getChildElements();
		for (int i = 0; i < childrenOfRoot.size(); i++) {
			Element childOfRoot = childrenOfRoot.get(i);
			if (childOfRoot.getLocalName().equalsIgnoreCase("Attribute")) {
				Attribute name = childOfRoot.getAttribute("Name");
				if (name != null && name.getValue().equals("ResponsibleSEE")) {
					return childOfRoot.getValue();
				}
			}
		}
		return null;
	}
	
	private String toHex(String product) {
		switch (product.toLowerCase()) {
		case "black":
			return "#000000";
		case "white":
			return "#FFFFFF";
		case "yellow":
			return "#FFFF00";
		case "blue":
			return "#0000FF";
		case "red":
			return "#FF0000";
		default :
			return "";
		}
	}
	
	protected String loadFile(String filename) {
		InputStream inputStream = this.getClass().getResourceAsStream("/resources/" + filename);
		if (inputStream != null) {
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} else {
			return "";
		}
	}

	private Order transformToOrder(CustomerRequest req, String productName, int count) {
		String uuid = UUID.randomUUID().toString();
		Product product = new Product(productName, null, new Factory());
		ProductQuantity productQuantity = new ProductQuantity(product, count);
		Calendar today = Calendar.getInstance();
		today.set(Calendar.MONTH, today.get(Calendar.MONTH) + 1);
		String customerName = req.getCustomerName();
		return new Order(uuid, customerName, productQuantity, today, today);
	}
	
	private List<CustomerRequestTO> getNewCustomerRequestTOs() throws ClientProtocolException, IOException {
		String serviceName = "retrieveCustomerRequests";
		String parameters = "?newOnly=" + true;
		HttpGet request = new HttpGet(AMSServiceUtility.serviceAddress + serviceName + parameters);
		request.setHeader("Content-type", "application/json");
		
		HttpClient client = HttpClientBuilder.create().build();;
		System.out.println("Retrieving customer requests from AMS ...");
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (SocketException e) {
			response = null;
		}
		List<CustomerRequestTO> result = null;
		if (response == null) {
			System.err.println("Retrieving customer requests from AMS: connection failed.");
		} else if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Retrieving customer requests from AMS: Status code " + response.getStatusLine().getStatusCode());
		} else {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String text = "";
			String line;
			while ((line = rd.readLine()) != null) {
				text += line;
			} 
			result = JSONUtility.convertToList(text, new TypeToken<List<CustomerRequestTO>>() { }.getType());
			if (result.size() > 0) {
				System.out.println("Retrieved " + result.size()  + " new CustomerRequests.");
			}
		}
		return result == null ? new ArrayList<CustomerRequestTO>() : result;
	}

	/**
	 * Saves the most recent order to the desktop.
	 * @param executableSkills the ExecutableSkills
	 * @param goalskills the Goalskills
	 */
	private void saveToDesktop(String executableSkills, List<String> goalskills) {
		String path1 = System.getProperty("user.home") + "\\Desktop\\recentExecutableSkills.aml";
		File file1 = new File(path1);
		try (PrintWriter printWriter = new PrintWriter(file1)) {
			printWriter.print(executableSkills);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String path2 = System.getProperty("user.home") + "\\Desktop\\recentGoalskills.txt";
		File file2 = new File(path2);
		try (PrintWriter printWriter = new PrintWriter(file2)) {
			boolean first = true;
			for (String goalskill : goalskills) {
				if (!first) {
					printWriter.print(",");
				}
				first = false;
				printWriter.print(goalskill);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}