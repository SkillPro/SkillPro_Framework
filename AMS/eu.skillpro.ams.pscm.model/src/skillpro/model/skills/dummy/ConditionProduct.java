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

package skillpro.model.skills.dummy;

public enum ConditionProduct {

	BOX_UNFOLDED("box-unfolded"), BOX_EMPTY("box-empty"), BOX_WITH_CHOCOLATE("box-with-chocolate"),
	FLAG_AND_BOX_WITH_CHOCOLATE("flag-and-box-with-chocolate"), BOX_FINISHED("box-finished"),
	EMPTY("empty"),
	
	CHOCOLATE("chocolate"),
	
	LEGO_TOP("lego-top"), LEGO_BOTTOM("lego-bottom"), LEGO_MIDDLE("lego-middle"),
	ONE_LEGO("one-lego"), TWO_LEGOS("two-legos"), THREE_LEGOS("three-legos"), FLAG("flag"), TRANSFORMEDCHOCOLATE("transformed-chocolate"), TRANSFORMED_LEGO("transformed-lego"),
	
	
	
	
	
	PCB_EMPTY("pcb-empty"),
	PCB_SMT1_FINISHED("pcb-smt1-finished"),
	PCB_SMT2_FINISHED("pcb-smt2-finished"),
	PCB_THT1_FINISHED("pcb-tht1-finished"),
	PCB_AOI_FINISHED("pcb-aoi-finished"),
	PCB_THT2_FINISHED("pcb-tht1-finished"),
	PCB_INSPECTED("pcb-inspected"),
	PCB_TESTED("pcb-tested"),
	PCB_QUALITIED("pcb-qualitied"),
	PCB_WASHED("pcb-washed"), 
	
	
	
	
	;
	
	private String name;
	
	private ConditionProduct(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
