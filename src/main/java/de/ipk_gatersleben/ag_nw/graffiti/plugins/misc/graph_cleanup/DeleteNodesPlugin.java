/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id$
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.graph_cleanup;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * Provides a spring embedder algorithm a la KK.
 * 
 * @version $Revision$
 */
public class DeleteNodesPlugin extends IPK_PluginAdapter {
	
	public DeleteNodesPlugin() {
		this.algorithms = new Algorithm[2];
		this.algorithms[0] = new DeleteNodesAlgorithm();
		this.algorithms[1] = new NumberNodesAndEdgesAlgorithm();
	}
	
}
