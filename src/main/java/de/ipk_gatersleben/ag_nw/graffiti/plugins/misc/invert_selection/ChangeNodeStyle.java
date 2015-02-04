package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;

public class ChangeNodeStyle extends AbstractEditorAlgorithm {
	
	public void execute() {
		getMainFrame().showAndHighlightSidePanelTab("Node", false);
	}
	
	public String getName() {
		return "Node Attributes";
	}
	
	@Override
	public String getCategory() {
		return "Network.Nodes";
	}
	
	public boolean activeForView(View v) {
		return v instanceof GraffitiView;
	}
}
