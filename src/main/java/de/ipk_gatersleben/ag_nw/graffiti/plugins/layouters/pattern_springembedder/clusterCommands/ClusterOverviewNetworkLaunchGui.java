package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class ClusterOverviewNetworkLaunchGui extends LaunchGui {

	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new CreateClusterGraphAlgorithm());
		res.add(new ClusterGraphLayout());
		// res.add(new ClusterIndividualLayout());
		res.add(null);
		res.add(new ShowClusterGraphAlgorithm());
		return res;
	}

	@Override
	public boolean closeDialogBeforeExecution(Algorithm algorithm) {
		return algorithm instanceof ShowClusterGraphAlgorithm;
	}

	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Process Cluster Overview Graph";
	}
	
	@Override
	public String getCategory() {
		return "Network.Cluster";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.CLUSTER, Category.GRAPH));
	}

	@Override
	public boolean isModal() {
		return false;
	}

	
}
