/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.expand_no_overlapp.NoOverlappLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim.NoOverlappLayoutAlgorithmAS;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.MyClusterGraphBasedReLayoutService;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class NoOverlappOfClustersAlgorithm
					extends AbstractAlgorithm {
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Remove Cluster Overlaps";
		else
			return "Separate Pathway-Subgraphs";
	}
	
	@Override
	public String getCategory() {
		return "Network.Cluster";
	}

	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.GRAPH,
				Category.LAYOUT,
				Category.CLUSTER
				));
	}
	
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	public void execute() {
		MyClusterGraphBasedReLayoutService mcs = new MyClusterGraphBasedReLayoutService(false, graph);
		mcs.setAlgorithm(new NoOverlappLayoutAlgorithmAS(), new NoOverlappLayoutAlgorithm());
		String name = "Separate Clusters";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			name = "Separate Pathways";
		BackgroundTaskHelper bth = new BackgroundTaskHelper(mcs, mcs, name, name, true, false);
		bth.startWork(this);
	}
}
