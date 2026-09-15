/**
 * Adaptagrams Edge Routing Plugin class.
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams.edgerouting;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Tobias Czauderna
 */
public class EdgeRoutingPlugin extends GenericPluginAdapter {
	
	/**
	 * Initializes the edge routing plugin. Registers the edge routing algorithm.
	 * The native Adaptagrams library is unpacked and loaded on first use, see
	 * {@link org.vanted.plugins.layout.adaptagrams.AdaptagramsLibrary}.
	 */
	public EdgeRoutingPlugin() {
		
		this.algorithms = new Algorithm[] { new EdgeRoutingAlgorithm() };
		
	}
	
}
