package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid_placement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ErrorMsg;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

public class IterateGridPlacementLayout extends AbstractAlgorithm implements Algorithm {
	
	public void execute() {
		Collection<Node> nodes = getSelectedOrAllNodes();
		if (nodes.size() < 1)
			return;
		
		new Grid(nodes.size(), nodes.size());
		ExecutorService run = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs());
		
		run.shutdown();
		try {
			run.awaitTermination(60 * 60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public String getName() {
		return null;
		// return "Grid Placement Iteration Test";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.LAYOUT));
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
}
