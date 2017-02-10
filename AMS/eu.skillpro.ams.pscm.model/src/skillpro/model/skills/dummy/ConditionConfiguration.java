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

public enum ConditionConfiguration {
	
	NEUTRAL("neutral"), 
	
	POS_LEGO_BOTTOM("pos-lego-bottom"),
	POS_LEGO_MIDDLE("pos-lego-middle"),
	POS_LEGO_TOP("pos-lego-top"),
	
	POS_UR5("pos-ur5"),
	POS_KR6("pos-kr6"),
	POS_WP1("pos-wp1"),
	POS_WP2("pos-wp2"),
	POS_WP3("pos-wp3"),
	POS_PLATFORM("pos-platform"),
	POS_CHOCOLATE("pos-chocolate"),
	POS_BOX("pos-box"),
	

	UNCONFIGURED("unconfigured"),
	CONFIGURED("configured"),
	RUNNING("running"),
	POS_SMT1ASSEMBLY("pos_smt1assembly"),
	POS_THT1ASSEMBLY("pos_tht1assembly"), POS_INSPECTION("pos-inspection"),
	
	POS_INSPECTION_PREPARED("pos-inspection-prepared"),

	POS_THT2ASSEMBLY("pos_tht2assembly"), POS_THT2ASSEMBLY_PREPARED("pos_tht2assembly_prepared"),

	POS_AOI_INSPECTION("aoi-inspecton"),
	POS_WASHING("pos-washing"),
	POS_TEST("pos-test"), POS_TEST_PREPARED("pos-test-prepared"),
	POS_QUALITY("pos-quality"), POS_QUALITY_PREPARED("pos-quality-prepared"),
	
	POS_SMT2ASSEMBLY("pos_smt2assembly"), 
	
	
	
	
	
	// DDE v2
	
	POS_SMTASSEMBLYLINE("pos_SMTAssemblyLine"),
	IDLE("idle"),
	PRODA("prodA"),
	POS_MANUALINSPECTIONWORKPLACE("pos_ManualInspectionWorkplace"),
	POS_THTWAVESOLDERING("pos_THTWaveSoldering"),
	POS_THTASSEMBLYWORKPLACE1("pos_THTAssemblyWorkplace1"),
	POS_FUNCTIONALTESTINGWORKPLACE("pos_FunctionalTestingWorkplace"),
	POS_THTASSEMBLYWORKPLACE2("pos_THTAssemblyWorkplace2"),
	POS_QUALITYCONTROLWORKPLACE("pos_QualityControlWorkplace"),

	//b
	BOTTOM("Bottom,"),
	TOP("Top,"),
	PRODB("prodB,"),
	POS_OPTICALINSPECTIONWORKPLACE("pos_OpticalInspectionWorkplace"),
	POS_WASHINGWORKPLACE("pos_WashingWorkplace"),
	IDLE_PRODA("idle-prodA"),
	IDLE_PRODB("idle-prodB"),
	IDLE_PRODB_BOTTOM("idle-prodB-Bottom"),
	IDLE_PRODB_TOP("idle-prodB-Top"),
	
	ANY_OR_IDLE("AnyOrIdle");
	;
	
	
	private String name;
	
	private ConditionConfiguration(String name) {
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
