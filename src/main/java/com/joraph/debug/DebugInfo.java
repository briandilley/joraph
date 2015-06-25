package com.joraph.debug;

import java.util.LinkedHashSet;
import java.util.Set;

import com.joraph.ObjectGraph;
import com.joraph.plan.ExecutionPlan;

public class DebugInfo {

	private Set<ObjectGraph> objectGraphs = new LinkedHashSet<>();
	private Set<ExecutionPlan> executionPlans = new LinkedHashSet<>();

	public void addObjectGraph(ObjectGraph objectGraph) {
		this.objectGraphs.add(objectGraph);
	}

	public void addExecutionPlan(ExecutionPlan executionPlan) {
		this.executionPlans.add(executionPlan);
	}

	public Set<ObjectGraph> getObjectGraphs() {
		return objectGraphs;
	}

	public Set<ExecutionPlan> getExecutionPlans() {
		return executionPlans;
	}

}
