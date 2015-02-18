package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;

public class ChangeElementStyle extends AbstractEditorAlgorithm {
	
	public void execute() {
		getMainFrame().showAndHighlightSidePanelTab("Network", true);
	}
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Change Graphelement Style";
	}
	
	@Override
	public String getCategory() {
		return null;// "Elements";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(
				Category.UI
				));
	}
	public boolean activeForView(View v) {
		return v instanceof GraffitiView;
	}
}
