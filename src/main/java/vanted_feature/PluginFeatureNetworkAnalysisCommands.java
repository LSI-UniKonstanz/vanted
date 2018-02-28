/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import java.util.prefs.Preferences;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsTrue;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.circle_search.CircleSearchAndLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.AllPathsSelectionAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.ShortestPathSelectionAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.RemoveSelectedNodesPreserveEdgesAlgorithm;

/**
 * @author Christian Klukas
 */
public class PluginFeatureNetworkAnalysisCommands extends IPK_PluginAdapter {

	public PluginFeatureNetworkAnalysisCommands() {

		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			if (new SettingsHelperDefaultIsTrue().isEnabled("Network analysis commands"))
				ReleaseInfo.enableFeature(FeatureSet.TAB_PATTERNSEARCH);

			this.algorithms = new Algorithm[] { new ShortestPathSelectionAlgorithm(),
					// new WeightedShortestPathSelectionAlgorithm(),
					new AllPathsSelectionAlgorithm(), new CircleSearchAndLayoutAlgorithm(),
					new RemoveSelectedNodesPreserveEdgesAlgorithm() };
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(Preferences p) {
		super.configure(p);
	}
}
