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

package amltransformation.providers.transformable;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import eu.skillpro.ams.pscm.icons.IconActivator;

public class TransformableLabelProvider extends LabelProvider {
	private Image transformableIcon ;
	
	@Override
	public String getText(Object element) {
		Class<?> node = (Class<?>) element;
		return node.getSimpleName().replaceFirst("Adapter", "");
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Class<?>) {
			return getTransformableIcon();
		}
		return null;
	}
	
	protected Image getTransformableIcon() {
		if (transformableIcon == null) {
			transformableIcon = IconActivator.getImageDescriptor("icons/aml/transformable.png").createImage();
		}

		return transformableIcon;
	}
}
