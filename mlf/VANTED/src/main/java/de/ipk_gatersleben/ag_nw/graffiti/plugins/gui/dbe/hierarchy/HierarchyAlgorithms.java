package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.CreateDirectChildrenClustersHistogramAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.ProcessHierarchynodesDepOnLeafNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.PruneTreeAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.CreateHierarchyTree;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy.HideOrShowChildNodes;

@Deprecated
public class HierarchyAlgorithms extends LaunchGui {

	public HierarchyAlgorithms() {
		super();
	}

	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new CreateDirectChildrenClustersHistogramAlgorithm());
		res.add(new PruneTreeAlgorithm());
		res.add(new HierarchyWizard());
		res.add(new HideOrShowChildNodes());
		res.add(new CreateHierarchyTree());
		res.add(new ProcessHierarchynodesDepOnLeafNodes());
		return res;
	}

	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Process Hierarchy...";// "Create Hierarchy";
		else
			return null;
	}

	@Override
	public String getCategory() {
		return "Analysis";
	}

	@Override
	public Set<Category> getSetCategory() {
		return null;
	}

}
