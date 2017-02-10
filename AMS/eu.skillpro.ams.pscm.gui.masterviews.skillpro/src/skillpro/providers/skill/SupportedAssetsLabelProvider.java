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

package skillpro.providers.skill;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import skillpro.model.skills.Requirement;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.SkillSynchronizationType;
import skillpro.model.utils.Pair;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class SupportedAssetsLabelProvider extends LabelProvider {
	private Image skillIcon;
	
	@Override
	public String getText(Object element) {
		if (element instanceof ResourceSkill) {
			ResourceSkill node = (ResourceSkill) element;
			return node.getName() + ": " + node.getTemplateSkill();
		} else if (element instanceof Pair<?, ?>) {
			Pair<?, ?> pair = (Pair<?, ?>) element;
			if (pair.getFirstElement() instanceof Requirement) {
				Requirement preRequirement = (Requirement) pair.getFirstElement();
				String syncType = "(" + preRequirement.getSyncType() + ")";
				if (preRequirement.getSyncType() == SkillSynchronizationType.NONE) {
					syncType = "";
				}
				return preRequirement.getRequiredResourceSkill() + syncType;
			}
		}
		return null;
		
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ResourceSkill) {
			return getResourceSkillIcon();
		}
		return null;
	}
	
	private Image getResourceSkillIcon() {
		if (skillIcon == null) {
			skillIcon = IconActivator.getImageDescriptor("icons/skill/rs.png").createImage();
		}

		return skillIcon;
	}
}
