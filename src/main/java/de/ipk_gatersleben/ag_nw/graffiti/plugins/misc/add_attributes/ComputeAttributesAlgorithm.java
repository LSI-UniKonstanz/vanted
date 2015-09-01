package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.add_attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class ComputeAttributesAlgorithm extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			res.add(new AddInterestingAttributes());
		res.add(new CalculateAttribute());
		return res;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Compute Attributes...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.GRAPH,
				Category.COMPUTATION,
				Category.MAPPING
				));
	}
	
	
}
