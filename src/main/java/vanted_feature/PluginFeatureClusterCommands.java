/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import java.util.prefs.Preferences;

import org.SettingsHelperDefaultIsTrue;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.NoOverlappOfClustersAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.AddRandomClusterInformationAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.ClusterIndividualLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.ClusterOverviewNetworkLaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.PajekClusterColor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SelectClusterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterLaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SortIntoCluster;

/**
 * @author Christian Klukas
 */
public class PluginFeatureClusterCommands extends IPK_PluginAdapter {

	public static SelectClusterAlgorithm alg;

	public PluginFeatureClusterCommands() {
		if (new SettingsHelperDefaultIsTrue().isEnabled("Cluster commands")) {
			this.algorithms = new Algorithm[] { new AddRandomClusterInformationAlgorithm(), new PajekClusterColor(),
					new ClusterOverviewNetworkLaunchGui(), new ClusterIndividualLayout(), new SetClusterLaunchGui(),
					new NoOverlappOfClustersAlgorithm(), new SortIntoCluster() };
			alg = new SelectClusterAlgorithm();
		}
	}

	@Override
	public void configure(Preferences p) {
		super.configure(p);
	}

	public static Algorithm getSelectClusterAlgorithm() {
		return alg;
	}
}
