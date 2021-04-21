package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.apply_from_graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;

public class ShowLayoutTab extends AbstractEditorAlgorithm {
	
	public void execute() {
		getMainFrame().showAndHighlightSidePanelTab("Layout", false);
	}
	
	public String getName() {
		return "Layout Network";
	}
	
	@Override
	public String getCategory() {
		return "Network";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.UI));
	}
	
	public boolean activeForView(View v) {
		return v instanceof GraffitiView;
	}
}