package org.vanted.addons.MultilevelFramework;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;

public class MultilevelFrameworkAddon extends AddonAdapter{

	@Override
	protected void initializeAddon() {
		this.algorithms = new Algorithm[] {new MultilevelLayoutAlgorithm(), 
				new RandomCoarseningAlgorithm(), 
				new RandomPlacementAlgorithm()};
		
	}

}
