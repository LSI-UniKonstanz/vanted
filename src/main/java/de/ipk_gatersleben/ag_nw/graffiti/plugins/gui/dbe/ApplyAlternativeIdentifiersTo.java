package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids.ReplaceLabelFromAlternativeSubstanceNames;

public class ApplyAlternativeIdentifiersTo extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new ReplaceDiagramTitleFromAlternativeSubstanceNames());
		res.add(new ReplaceLabelFromAlternativeSubstanceNames());
		return res;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Use Alternative Identifiers to...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.MAPPING, Category.CHART));
	}
}
