package edu.monash.vanted.test.preferences;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance.HammingDistanceAlgorithm;

public class TestPlugin extends IPK_PluginAdapter{

	
	
	
	
	public TestPlugin() {
		// TODO Auto-generated constructor stub
		this.algorithms = new Algorithm[]{
			new HammingDistanceAlgorithm(),
			new TestAlgorithm(),
			new TestAlgorithm2()
		};
		
		this.views = new String[]{
				TestViewWithPreferences.class.getName()
		};
	}
	
	
}
