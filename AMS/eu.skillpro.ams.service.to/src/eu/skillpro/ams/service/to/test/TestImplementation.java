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

package eu.skillpro.ams.service.to.test;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.assets.FactoryNode;
import eu.skillpro.ams.service.to.assets.AssetTO;
import eu.skillpro.ams.service.to.assets.AttributeTO;
/**
 * @author caliqi
 * @date 11.03.2014
 *
 */
public class TestImplementation {
	public static List<FactoryNode> getFactories() {
		FactoryNode factoryNode1 = new FactoryNode("factoryNode1", true);
		FactoryNode factoryNode2 = new FactoryNode("factoryNode2", true);
		FactoryNode factoryNode3 = new FactoryNode("factoryNode3", true);
		FactoryNode factoryNode4 = new FactoryNode("factoryNode4", true);
		FactoryNode factoryNode5 = new FactoryNode("factoryNode5", true);
		List<FactoryNode> factories = new ArrayList<FactoryNode>();
		factories.add(factoryNode1);
		factories.add(factoryNode2);
		factories.add(factoryNode3);
		factories.add(factoryNode4);
		factories.add(factoryNode5);
		return factories;
	}

	/**
	 * @return a predefined list of assets
	 */
	public static List<AssetTO> getNewAssets() {
		AssetTO asset1 = new AssetTO();
		AssetTO asset2 = new AssetTO();
		AssetTO asset3 = new AssetTO();
		AssetTO asset4 = new AssetTO();

		asset1.setId("00001");
		asset2.setId("00002");
		asset3.setId("00003");
		asset4.setId("00004");
		
		asset1.getChildren().add(asset2);
		asset2.getChildren().add(asset3);
		asset2.getChildren().add(asset4);

		asset1.setName("Factory");
		asset2.setName("Hall2");
		asset3.setName("Machine1");
		asset4.setName("Robot1");
		
		AttributeTO attrX = new AttributeTO("1", "currentX", "10");
		AttributeTO attrY = new AttributeTO("2", "currentY", "10");
		AttributeTO attrZ = new AttributeTO("3", "currentZ", "0");
		AttributeTO attrLength = new AttributeTO("4", "length", "280");
		AttributeTO attrHeight = new AttributeTO("5", "height", "0");
		AttributeTO attrWidth = new AttributeTO("6", "width", "260");
		asset3.getAttributes().add(attrX);
		asset3.getAttributes().add(attrY);
		asset3.getAttributes().add(attrZ);
		asset3.getAttributes().add(attrLength);
		asset3.getAttributes().add(attrHeight);
		asset3.getAttributes().add(attrWidth);
		
		asset1.setType("room");
		asset2.setType("room");
		asset3.setType("asset");
		asset4.setType("asset");
		
		List<AssetTO> assets = new ArrayList<AssetTO>();
		assets.add(asset1);
		
		return assets;
	}
	
	/**
	 * @return a 
	 */
	public static List<AssetTO> getAssets() {
		//
		AssetTO asset1 = new AssetTO();
		AssetTO asset2 = new AssetTO();
		AssetTO asset3 = new AssetTO();
		AssetTO asset4 = new AssetTO();
		AssetTO asset5 = new AssetTO();
		AssetTO asset6 = new AssetTO();
		AssetTO asset7 = new AssetTO();
		AssetTO asset8 = new AssetTO();
		AssetTO asset9 = new AssetTO();
		AssetTO asset10 = new AssetTO();
		AssetTO asset11 = new AssetTO();
		AssetTO asset12 = new AssetTO();
		
		asset1.setId("00001");
		asset2.setId("00002");
		asset3.setId("00003");
		asset4.setId("00004");
		asset5.setId("00005");
		asset6.setId("00006");
		asset7.setId("00007");
		asset8.setId("00008");
		asset9.setId("00009");
		asset10.setId("00010");
		asset11.setId("00011");
		asset12.setId("00012");
		
		asset1.setName("Manipulator + Gripper");
		asset2.setName("Mobile Platform");
		asset3.setName("Soldering table");
		asset4.setName("Conveyor");
		asset5.setName("Testing Station");
		asset6.setName("PrinterA");
		asset7.setName("PrinterB");
		asset8.setName("Manipulator + Gripper");
		asset9.setName("Feeder");
		asset10.setName("Packaging station");
		asset11.setName("Packaging station");
		asset12.setName("3D Sensor");
		
		List<AssetTO> assets = new ArrayList<AssetTO>();
		assets.add(asset1);
		assets.add(asset2);
		assets.add(asset3);
		assets.add(asset4);
		assets.add(asset5);
		assets.add(asset6);
		assets.add(asset7);
		assets.add(asset8);
		assets.add(asset9);
		assets.add(asset10);
		assets.add(asset11);
		assets.add(asset12);
		
		return assets;
	}
	
	/**
	 * @return a predefined list of assets
	 */
	public static List<AssetTO> getAssets_Old() {
		//
		AssetTO asset1 = new AssetTO();
		asset1.setId("bf26f66e-9743-46d6-94fe-9358c3f02c8a");
		//
		AssetTO asset2 = new AssetTO();
		asset2.setId("7a8876a7-a487-48fc-ab61-a44083bdfd70");
		asset2.setName("Small-milling-robot (SMR)");
		
		AssetTO asset3 = new AssetTO();
		asset3.setId("6948b22f-1bf5-4f89-a491-4fdb138b84c9");
		asset3.setName("One-drill-robot (ODR)");
		
		List<AssetTO> assets = new ArrayList<AssetTO>();
		assets.add(asset1);
		assets.add(asset2);
		assets.add(asset3);
		
		return assets;
	}
}
