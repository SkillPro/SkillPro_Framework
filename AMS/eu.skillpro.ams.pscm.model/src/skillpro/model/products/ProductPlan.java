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

package skillpro.model.products;

import java.util.Objects;

import skillpro.model.assets.FactoryNode;
import skillpro.model.utils.ShiftSchedule;

public class ProductPlan {
	private FactoryNode factoryNode;
	private ShiftSchedule shiftPlan;
	private final double[] volume;
	
	private final boolean isValid;
	
	/**
	 * Creates a product plan.
	 * @param volume An array of piece numbers. The size of the array must
	 *        match the number of Produces objects in the factory node, so
	 *        <code>
	 * array.length == {@link FactoryNode#countProduces() 
	 * factoryNode.countProduces()}</code>.
	 * @param factoryNode
	 * @param shiftplan
	 */
	public ProductPlan(double[] volume, FactoryNode factoryNode, ShiftSchedule shiftplan) {
		this.volume = volume;
		this.factoryNode = factoryNode;
		this.shiftPlan = shiftplan;
		this.isValid = (volume != null);
	}
	
	public ProductPlan(FactoryNode factoryNode, ShiftSchedule shiftPlan) {
		this(null, factoryNode, shiftPlan);
	}
	
	public FactoryNode getFactoryNode() {
		return factoryNode;
	}
	
	public ShiftSchedule getShiftPlan() {
		return shiftPlan;
	}

	@Override
	public int hashCode() {
		return Objects.hash(factoryNode, shiftPlan);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductPlan other = (ProductPlan) obj;
		return Objects.equals(factoryNode, other.factoryNode)
				&& Objects.equals(shiftPlan, other.shiftPlan);
	}
	
	/**
	 * The product plan's piece number array.
	 * @return An array of piece numbers.
	 */
	public double[] getVolume() {
		return volume;
	}
	
	/**
	 * Returns <code>true</code> if the production plan is valid (contains
	 * piece numbers). If this returns [code]false[/code], the calculation that
	 * created this product plan's data could not be completed.
	 * @return <code>true</code> if the production plan is valid,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return isValid;
	}
}
