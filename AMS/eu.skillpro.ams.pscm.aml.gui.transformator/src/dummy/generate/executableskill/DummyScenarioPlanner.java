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

package dummy.generate.executableskill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import skillpro.model.skills.dummy.Condition;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import skillpro.model.utils.Pair;
import skillpro.model.utils.Triple;

/**
 * A simple planner that tries to bring the skills of a scenario into an
 * executable order.
 * 
 * @author siebel
 * 
 */
public class DummyScenarioPlanner{

	private DummyScenario scenario;
	
	private List<Triple<Integer, ExecutableSkillDummy, Map<ResourceDummy, Condition>>> result = null;
	private Map<ResourceDummy, Condition> initialResourceStates = new HashMap<>();
	private boolean reachedGoalskill = false;
	private int totalSkillCount;
	
	public DummyScenarioPlanner(DummyScenario scenario) {
		this.scenario = scenario;
	}
	
	private void calculate() {
		if (result == null) {
			result = new ArrayList<>();
			Map<ResourceDummy, Condition> resourceStates = new HashMap<>();
			
			int n = 0;
			int nAll = 0;
			List<ExecutableSkillDummy> scenarioSkills = scenario.getScenarioSkills();
			totalSkillCount = scenarioSkills.size();
			
			for (ExecutableSkillDummy e : scenarioSkills) {
				nAll++;
				if (workplaceCheck(e)) {
					n++;
					boolean executable = true;
					Map<ResourceDummy, Condition> newResourcePostConditions = new HashMap<>();
					Map<ResourceDummy, Condition> newResourcePreConditions = new HashMap<>();
					Map<ResourceDummy, Pair<Condition,Condition>> differences = new HashMap<>();
					for (ResourceExecutableSkillDummy r : e.getDummies()) {
						ResourceDummy resource = r.getResource();
						Condition resourceState = resourceStates.get(resource);
						if (resourceState != null && !resourceState.equals(r.getPreCondition())) {
							executable = false;
							differences.put(resource, new Pair<>(resourceState, r.getPreCondition()));
						}
						newResourcePreConditions.put(r.getResource(), r.getPreCondition());
						newResourcePostConditions.put(r.getResource(), r.getPostCondition());
					}
					if (executable) {
						for (Entry<ResourceDummy, Condition> entry : newResourcePreConditions.entrySet()) {
							if (!resourceStates.containsKey(entry.getKey())) {
								initialResourceStates.put(entry.getKey(), entry.getValue());
							}
						}
						resourceStates.putAll(newResourcePostConditions);
						addToResult(n, e, resourceStates);
					} else {
						// try to fix the differences with a single other skill
						int n2 = n;
						for (ExecutableSkillDummy e2 : scenarioSkills.subList(nAll, scenarioSkills.size())) {
							if (workplaceCheck(e2)) {
								n2++;
								int fixedDifferences = 0;
								boolean possible = true;
								for (ResourceExecutableSkillDummy re2 : e2.getDummies()) {
									Pair<Condition, Condition> d = differences.get(re2.getResource());
									if (d == null) {
										possible = false;
									} else if (!d.getFirstElement().equals(re2.getPreCondition())) {
										possible = false;
									} else if (!d.getSecondElement().equals(re2.getPostCondition())) {
										possible = false;
									} else {
										fixedDifferences++;
									}
								}
								if (possible && fixedDifferences == differences.size()) {
									update(resourceStates, e2);
									addToResult(n2, e2, resourceStates);
									update(resourceStates, e);
									addToResult(n, e, resourceStates);
									break;
								}
							}
						}
					}
				}
				if (scenario.getGoalSkills().contains(e.getId()) || scenario.getGoalSkills().contains("ID_"+e.getId())) {
					reachedGoalskill = true;
					break;
				}
			}
		}
	}

	public void print() {
		calculate();
		System.out.println("Creating a possible skill sequence ...");
		for (Triple<Integer, ExecutableSkillDummy, Map<ResourceDummy, Condition>> t : result) {
			System.out.printf("(%d) %s\n", t.e1, t.e2.getName());
			printConfiguration(t.e3);
		}
		System.out.printf("\nExecuted %d of the %d ExecutableSkills\n", result.size(), totalSkillCount);
		if (result.size() > 0) {
			System.out.println("Initial state was:");
			for (Entry<ResourceDummy, Condition> entry : initialResourceStates.entrySet()) {
				System.out.printf("\t%s\t%s\n", entry.getKey(), entry.getValue());
			}
		}
		if (reachedGoalskill) {
			System.out.println("Finished with a GoalSkill");
		} else {
			System.out.println("Couldn't reach a GoalSkill");
		}
	}
	
	public boolean reachedGoalSkill() {
		calculate();
		return reachedGoalskill;
	}
	
	private boolean addToResult(int n, ExecutableSkillDummy e, Map<ResourceDummy, Condition> stateAfter) {
		return result.add(new Triple<Integer, ExecutableSkillDummy, Map<ResourceDummy, Condition>>(n, e, new HashMap<>(stateAfter)));
	}
	
	private void update(Map<ResourceDummy, Condition> resourceStates, ExecutableSkillDummy e) {
		for (ResourceExecutableSkillDummy re : e.getDummies()) {
			if (!re.getPreCondition().equals(resourceStates.get(re.getResource()))) {
				throw new IllegalArgumentException();
			} else {
				resourceStates.put(re.getResource(), re.getPostCondition());
			}
		}
	}
	
	private void printConfiguration(Map<ResourceDummy, Condition> resourceStates) {
		for (Entry<ResourceDummy, Condition> entry : resourceStates.entrySet()) {
			System.out.printf("\t%s\t%s\n", entry.getKey(), entry.getValue());
		}
	}
	
	private boolean workplaceCheck(ExecutableSkillDummy e) {
		return !e.getName().toLowerCase().contains("wp2") && !e.getName().toLowerCase().contains("wp3");
	}
}
