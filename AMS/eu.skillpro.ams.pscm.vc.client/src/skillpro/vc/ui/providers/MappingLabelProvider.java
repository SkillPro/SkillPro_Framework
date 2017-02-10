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

package skillpro.vc.ui.providers;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.utils.Pair;
import skillpro.vc.client.gen.datacontract.Asset;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class MappingLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider, IColorProvider{
	
	private Image factoryIcon ;
	private Image factoryNodeIcon;
	private Image factoryNodeLogicalIcon;
	private Image workplaceIcon;
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<FactoryNode, Asset> pair = (Pair<FactoryNode, Asset>) element;
			FactoryNode firstElement = pair.getFirstElement();
			if (firstElement == null) {
				return null;
			} else {
				if (firstElement instanceof Factory) {
					return getFactoryIcon();
				} else if (firstElement instanceof Resource) {
					return getResourceIcon();
				} else if (((FactoryNode)firstElement).isLayoutable()) {
					return getFactoryNodeIcon();
				} else {
					return getFactoryNodeLogicalIcon();
				}
			}
		}
		
		return null;
	}
	

	@Override
	public String getText(Object element) {
		if (element == null) {
			return "";
		}
		if (element instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<FactoryNode, Asset> pair = (Pair<FactoryNode, Asset>) element;
			FactoryNode firstElement = pair.getFirstElement();
			if (firstElement == null) {
				return "";
			} else {
				return firstElement.getName();
			}
		}
		return super.getText(element);
	}

	@Override
	public Font getFont(Object element) {
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getImage(element);
		default:
			break;
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getText(element);
		case 1:
			return "";
		default:
			break;
		}
		return null;
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


	@Override
	public Color getForeground(Object element) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	@SuppressWarnings("unchecked")
	public Color getBackground(Object element) {
		Pair<FactoryNode, Asset> pair = (Pair<FactoryNode, Asset>) element;
		if (pair.getFirstElement() == null || pair.getSecondElement() == null) {
			return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
		}
		return null;
	}

}
