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

package skillpro.vc.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.model.assets.FactoryNode;
import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import skillpro.model.utils.Pair;
import skillpro.model.utils.Vector3d;
import skillpro.vc.client.gen.datacontract.Asset;
import skillpro.vc.csclient.VCClient;

public class VCSynchronizeHandler extends AbstractHandler implements IHandler {
	private static List<Pair<FactoryNode, Asset>> input; 

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//inputs are initialized in the dialog
		VISMappingDialog dialog = new VISMappingDialog(HandlerUtil.getActiveShellChecked(event)) {
			@Override
			protected List<?> refreshInput() {
				input = initInput();
				return input;
			}
		};
		if (dialog.open() == Window.OK) {
			
			for (Pair<FactoryNode, Asset> pair : input) {
				FactoryNode firstElement = pair.getFirstElement();
				Asset secondElement = pair.getSecondElement();

				if (firstElement != null && secondElement != null) {
					double[] boundingBox = secondElement.getBoundingBox();
					/* 
					 * In compare with Properties 
					 * setLength sets width
					 * setWidth sets height
					 * width and height are exchanged to look good in Top view!!
					 */
					firstElement.setLength(boundingBox[1]);
					firstElement.setWidth(boundingBox[0]);
					Vector3d position = new Vector3d();
					do {
						position.x += firstElement.getCurrentCoordinates().x;
						position.y += firstElement.getCurrentCoordinates().y;
						position.z += firstElement.getCurrentCoordinates().z;
						firstElement = firstElement.getParent();
					} while (firstElement != null);
					System.out.println(position);
					// x, y are exchanged!!
					secondElement.setWorldPositionMatrixSkillPro(new double[]{position.y, position.x, position.z});				


				}
			}
			//update assets
			SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
		} 
		return null;
	}

	private List<Pair<FactoryNode, Asset>> initInput() {
		List<Pair<FactoryNode, Asset>> input = new ArrayList<>();

		List<Asset> assets = new ArrayList<>();
		assets.addAll(VCClient.getInstance().getAssets());
		for (FactoryNode node : SkillproService.getSkillproProvider().getAssetRepo()) {
			input.add(new Pair<FactoryNode, Asset>(node, findAndRemoveAssetByName(node.getName(), assets)));
		}

		for (Asset asset : assets) {
			input.add(new Pair<FactoryNode, Asset>(null, asset));
		}

		return input;
	}

	private Asset findAndRemoveAssetByName(String name, List<Asset> assets) {

		Iterator<Asset> iter = assets.iterator();
		while (iter.hasNext()) {
			Asset next = iter.next();
			if (next != null && next.getName().equals(name)) {
				iter.remove();
				return next;
			}
		}


		return null;
	} 
	
	public static List<Pair<FactoryNode, Asset>> getInput() {
		return input;
	}

}
