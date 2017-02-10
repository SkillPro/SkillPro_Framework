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

package calculation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.products.Order;
import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.update.UpdateType;
import skillpro.model.utils.Pair;
import calculation.model.Condition;
import calculation.model.state.MiniState;
import calculation.repo.MiniStateRepo;

public class GenerateExSkills extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("Starting execution");
		MiniStateRepo.getInstance().wipeAllData();
		MiniState miniState = new MiniState();
		Map<Resource, Condition> currentStateMap = miniState.getCurrentStateMap();
		for (FactoryNode fn : SkillproService.getSkillproProvider().getAssetRepo()) {
			if (fn instanceof Resource) {
				Resource resource = (Resource) fn;
				currentStateMap.put(resource, new Condition(resource
						.getCurrentResourceConfiguration(), resource.getCurrentProductConfiguration()));
				if (resource.getCurrentResourceConfiguration() == null) {
					throw new IllegalArgumentException("Current ResourceConfiguration is null! For Resource: " + resource.getName());
				}
			}
		}
		//adds a default order for "1 PlateInBoxPackaged",
		//Do this if you feel that adding the order manually every time is too much work.
		if (SkillproService.getSkillproProvider().getOrderRepo().isEmpty()) {
			for (Product product : SkillproService.getSkillproProvider().getProductRepo()) {
				if (product.getName().equalsIgnoreCase("PlateInBoxPackaged")) {
					System.out.println("Added Default Order.");
					Order order = new Order(UUID.randomUUID().toString(), "Order", new ProductQuantity(product, 1), null, null);
					SkillproService.getSkillproProvider().getOrderRepo().add(order);
				}
			}
			
		}

		List<ExecutableSkill> generatePossibleExecutableSkills = new ArrayList<>();
		Pair<List<ExecutableSkill>, Map<Order, Set<List<ExecutableSkill>>>> generatePossibleExSkillsPair = OrderPlanner
				.generatePossibleExecutableSkills(SkillproService
				.getSkillproProvider().getOrderRepo().getEntities(), miniState);
		long before = System.currentTimeMillis();
		generatePossibleExecutableSkills.addAll(generatePossibleExSkillsPair.getFirstElement());

		//generate random ID
		generateRandomIDForAllRExs(generatePossibleExecutableSkills);
		
		SkillproService.getSkillproProvider().getSkillRepo().getExecutableSkills().clear();
		SkillproService.getSkillproProvider().getSkillRepo().getExecutableSkills().addAll(generatePossibleExecutableSkills);
		//CLEAR alternatives
		SkillproService.getSkillproProvider().getSkillRepo().getPossibleOrderedExSkillsBasedOnOrder().clear();
		SkillproService.getSkillproProvider().getSkillRepo().getPossibleOrderedExSkillsBasedOnOrder()
				.putAll(generatePossibleExSkillsPair.getSecondElement());
		SkillproService.getUpdateManager().notify(UpdateType.EXECUTABLE_SKILLS_GENERATED, null);
		System.err.println("Everything finished: " + (System.currentTimeMillis() - before));
		return null;
	}
	
	private void generateRandomIDForAllRExs(List<ExecutableSkill> exSkills) {
		for (ExecutableSkill exSkill : exSkills) {
			for (ResourceExecutableSkill rexSkill : exSkill.getResourceExecutableSkills()) {
				rexSkill.setId(UUID.randomUUID().toString());
			}
		}
	}
}