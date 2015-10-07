package org.vanted.garuda;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

public class GarudaPlugin extends GenericPluginAdapter {

	public GarudaPlugin() {
	
		this.algorithms = new Algorithm[]{
				new VantedGarudaExtension()
		};
		
	}
}
