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

package skillpro.providers.catalog;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.skills.ResourceSkill;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class CatalogTreeLabelProvider extends LabelProvider {
	private Image factoryIcon ;
	private Image factoryNodeIcon;
	private Image factoryNodeLogicalIcon;
	private Image workplaceIcon;
	private Image skillIcon;
	private Image configurationIcon;
	
	@Override
	public String getText(Object element) {
		if (element instanceof ResourceSkill) {
			ResourceSkill node = (ResourceSkill) element;
			return node.getName() + ": " + node.getTemplateSkill();
		} else if (element instanceof Setup) {
			Setup config = (Setup) element;
			if (config.equals(config.getResource().getCurrentSetup())) {
				return config.getName() + " - current configuration";
			} else {
				return config.getName();
			}
		} else {
			FactoryNode node = (FactoryNode) element;
			if (node instanceof Resource) {
				return node.getName();
			} else {
				return node.getName();
			}
		}
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Factory) {
			return getFactoryIcon();
		} else if (element instanceof Resource) {
			return getResourceIcon();
		} else if (element instanceof ResourceSkill) {
			return getResourceSkillIcon();
		} else if (element instanceof Setup) {
			return getSetupIcon();
		} if (((FactoryNode)element).isLayoutable()) {
			return getFactoryNodeIcon();
		}
		return getFactoryNodeLogicalIcon(); 
	}
	
	private Image getSetupIcon() {
		if (configurationIcon == null) {
			configurationIcon = IconActivator.getImageDescriptor("icons/asset/conf.png").createImage(); 
		}
		return configurationIcon;
	}

	private Image getResourceSkillIcon() {
		if (skillIcon == null) {
			//FIXME make resourceSkill.png
			skillIcon = IconActivator.getImageDescriptor("icons/skill/rs.png").createImage();
		}
		return skillIcon;
	}
	protected Image getFactoryIcon() {
		if (factoryIcon == null) {
			factoryIcon = IconActivator.getImageDescriptor("icons/asset/fact.png").createImage();
		}
		return factoryIcon;
	}
	

	protected Image getFactoryNodeIcon() {
		if (factoryNodeIcon == null) {
			factoryNodeIcon = IconActivator.getImageDescriptor("icons/asset/fn.png").createImage();
		}
		return factoryNodeIcon;
	}

	protected Image getFactoryNodeLogicalIcon() {
		if (factoryNodeLogicalIcon == null) {
			factoryNodeLogicalIcon = IconActivator.getImageDescriptor("icons/asset/fnl.png").createImage();
		}
		return factoryNodeLogicalIcon;
	}

	protected Image getResourceIcon() {
		if (workplaceIcon == null) {
			workplaceIcon = IconActivator.getImageDescriptor("icons/asset/wp.png").createImage();
		}
		return workplaceIcon;
	}
}
