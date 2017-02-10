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

package skillpro.providers.order;

import java.util.Calendar;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import skillpro.model.products.Order;
import skillpro.model.products.Product;

public class OrderTreeLabelProvider extends LabelProvider implements ITableLabelProvider {
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Order) {
			Order order = (Order) element;
			switch (columnIndex) {
			case 0:
				return order.getOrderID();
			case 1:
				return ""+order.getProductQuantity().getQuantity();
			case 2:
					Product product = order.getProductQuantity().getProduct();
					if (product == null) return "";
					return product.getName();
			case 3:
				Calendar start = order.getEarliestStartDate();
				if (start != null) {
					String date = ""+start.get(Calendar.DAY_OF_MONTH) +". " + start.get(Calendar.MONTH) + ". " + start.get(Calendar.YEAR);
					return date;
				}
				break;
			case 4:
				Calendar end = order.getDeadline();
				if (end != null) {
					String date = ""+end.get(Calendar.DAY_OF_MONTH) +". " + end.get(Calendar.MONTH) + ". " + end.get(Calendar.YEAR);
					return date;
				}
			default:
				return null;
			}
		}
		return null;
	}
}
