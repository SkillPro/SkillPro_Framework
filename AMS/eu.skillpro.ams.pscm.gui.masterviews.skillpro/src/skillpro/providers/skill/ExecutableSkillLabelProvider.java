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

import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class ExecutableSkillLabelProvider extends LabelProvider {
	private Image exSkillIcon;
	private Image rexSkillIcon;
	
	@Override
	public String getText(Object element) {
		if (element instanceof ExecutableSkill) {
			ExecutableSkill node = (ExecutableSkill) element;
			return node.getName();
		} else if (element instanceof ResourceExecutableSkill) {
			ResourceExecutableSkill node = (ResourceExecutableSkill) element;
			return node.getName() + "[" + node.getResource().getName() + "]";
		}
		
		return null;
		
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ExecutableSkill) {
			return getExecutableSkillIcon();
		} else if (element instanceof ResourceExecutableSkill) {
			return getResourceExecutableSkillIcon();
		}
		return null;
	}
	
	private Image getExecutableSkillIcon() {
		if (exSkillIcon == null) {
			exSkillIcon = IconActivator.getImageDescriptor("icons/skill/exSkill.png").createImage();
		}
		return exSkillIcon;
	}

	private Image getResourceExecutableSkillIcon() {
		if (rexSkillIcon == null) {
			rexSkillIcon = IconActivator.getImageDescriptor("icons/skill/rexSkill.png").createImage();
		}

		return rexSkillIcon;
	}
}
