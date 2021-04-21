/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectEdgesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectNodesWithExperimentalDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SelectClusterAlgorithm;

public class InvertSelectionPlugin extends IPK_EditorPluginAdapter {
	
	public InvertSelectionPlugin() {
		algorithms = new Algorithm[] { new ExportDataTableAlgorithm(),
				// new ChangeNodeStyle(),
				// new ChangeEdgeStyle(),
				// new ChangeElementStyle(),
				// new SelectAlgorithm(),
				
				new ExpandSelectionAlgorithm(true, false), new ExpandSelectionAlgorithm(false, false),
				new ExpandSelectionAlgorithm(true, true),
				
				new InvertSelectionAlgorithm(), new ClearSelection(), new SelectClusterAlgorithm(),
				new SelectNodesWithExperimentalDataAlgorithm(), new SelectEdgesAlgorithm(),
				
				new SetToolTipAlgorithm(), new SearchAndSelecAlgorithm(), new FindReplaceDialog() };
		//
		// guiComponents = new GraffitiComponent[] {
		// new SelectNodesComponent("defaultToolbar"), // defaultToolbar // toolbarPanel
		// };
	}
}
