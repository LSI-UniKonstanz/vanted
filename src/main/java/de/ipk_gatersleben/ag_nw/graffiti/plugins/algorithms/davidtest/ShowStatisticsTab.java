package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

public class ShowStatisticsTab extends AbstractAlgorithm implements Algorithm {
	
	public void execute() {
		MainFrame.getInstance().showAndHighlightSidePanelTab("Statistics", true);
	}
	
	public String getName() {
		return "Correlation analysis, compare sample averages (e.g. t-Test)";
	}

	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.UI
				));
	}
}
