package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

@Deprecated
public class BendsLaunchGUI extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new RemoveBendsAlgorithm());
		res.add(new IntroduceSelfEdgeBends());
		res.add(new IntroduceBendsAlgorithm());
		
		return res;
	}
	
	@Override
	public String getName() {
		return "Bends";
	}
	
	@Override
	public String getCategory() {
		return "Edges";
	}

	@Override
	public Set<Category> getSetCategory() {
		return null;
	}
	

}
