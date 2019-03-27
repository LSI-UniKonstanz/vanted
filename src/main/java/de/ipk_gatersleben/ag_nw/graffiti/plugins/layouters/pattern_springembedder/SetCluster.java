package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.clustering.sorting.SortIntoCluster;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterInfoAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterInfoFromLabelAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterInfoFromSubgraphAlgorithm;

public class SetCluster extends LaunchGui {

	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new SetClusterInfoAlgorithm());
		res.add(new SetClusterInfoFromLabelAlgorithm());
		res.add(new SortIntoCluster());
		res.add(new SetClusterInfoFromSubgraphAlgorithm());
		return res;
	}

	@Override
	public boolean closeDialogBeforeExecution(Algorithm algorithm) {
		return !(algorithm instanceof SetClusterInfoAlgorithm);
	}

	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Set Cluster ID";
	}

	@Override
	public String getCategory() {
		return "Network.Cluster";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.CLUSTER, Category.ANNOTATION));
	}

	@Override
	public boolean isModal() {
		return false;
	}
	
}
