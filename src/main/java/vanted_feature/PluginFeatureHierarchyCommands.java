/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import java.util.prefs.Preferences;

import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsTrue;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go.CreateGOtreeAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go.InterpreteGOtermsAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.CreateDirectChildrenClustersHistogramAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.ProcessHierarchynodesDepOnLeafNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.PruneTreeAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.hierarchies.CreateKEGGOrthologyGraphAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.kegg_reaction.CreateKeggReactionNetworkAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy.HideOrShowChildNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy.HierarchyAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy.SelectLeafNodesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.ExpandSelectionAlgorithm;

/**
 * @author Christian Klukas
 */
public class PluginFeatureHierarchyCommands extends IPK_PluginAdapter {

	private static Algorithm[] otherAlgorithms = null;

	public PluginFeatureHierarchyCommands() {
		if (new SettingsHelperDefaultIsTrue().isEnabled("Hierarchy commands")) {
			if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
				this.algorithms = new Algorithm[] {
						// new HierarchyAlgorithms(), //Old housing of the algorithms

						/*
						 * some of the following algorithms will be removed from the final version of
						 * vanted since they seem to be very implemented for very special use
						 * 
						 * The latest vanted version will focus on GO/KEGG hierarchy including cluster
						 * histograms
						 * 
						 * All disabled algorithms will be in a special category (See category list
						 * //TODO: to be implemented). If the user enables a global setting e.g. enable
						 * special/extra algorithms. they will reappear
						 * 
						 * So.. the algorithms will be listed in the global algorithm list but will not
						 * be added to the menu but will be available in the vanted classpath, to be
						 * called by other algorithms if needed
						 */
						new CreateDirectChildrenClustersHistogramAlgorithm(), // Disable for normal use
						new PruneTreeAlgorithm(), // Disable for normal use
						// new HierarchyWizard(), // Disable for normal use, since its functionality
						// doesn't match what we want
						new HideOrShowChildNodes(), // Disable for normal use. We don't need that now
						// new CreateHierarchyTree(), //Another Super-Dialog containing hierarchy
						// algorithms
						/*
						 * including: CreateGOtreeAlgorithm(), //Disable for normal use
						 * InterpreteGOtermsAlgorithm(), //combine the two into one
						 * CreateFuncatGraphAlgorithm()
						 * 
						 * The two latter algos will be combined and the resulting algorithm will
						 * support GO and KEGG and EC and maybe later other hierarchy searching and
						 * creation algorithms think of a new interface with easy extension
						 */
						new ProcessHierarchynodesDepOnLeafNodes(), // Super-dialog that contains the actutal algorithms
																	// to create the hierarchies
						/*
						 * including CreateGOchildrenAverageDataDiagramAlgorithm()
						 * CreateGOchildrenClustersHistogramAlgorithm() ClusterHistogramFisherTest()
						 * CreateGOchildrenTtestHistogramAlgorithm() AlternativeIDannotationStatistics()
						 */

						new CreateKeggReactionNetworkAlgorithm(), // Disable. This would create the complete KEGG
																	// reaction network, which we cannot create anymore
						new HierarchyAlgorithm(), new SelectLeafNodesAlgorithm(),

						new CreateKEGGOrthologyGraphAlgorithm(), new InterpreteGOtermsAlgorithm(),
						new CreateGOtreeAlgorithm()

				};
				otherAlgorithms = new Algorithm[] { new ExpandSelectionAlgorithm(true, false),
						new ExpandSelectionAlgorithm(false, false), new ExpandSelectionAlgorithm(true, true) };
			}
		}
	}

	@Override
	public void configure(Preferences p) {
		super.configure(p);
	}

	public static Algorithm[] getSelectionAlgorithms() {
		Algorithm[] res = new Algorithm[] {};
		if (otherAlgorithms != null)
			res = otherAlgorithms;
		return res;
	}
}
