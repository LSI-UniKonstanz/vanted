package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.Category;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

@Deprecated
public class ChangeLabelsAlgorithm extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new ReplaceLabelAlgorithm());
		res.add(new RemoveParenthesisLabels());
		res.add(new RestoreLabelAlgorithm());
		res.add(null);
		res.add(new EnrichHiddenLabelsAlgorithm());
		res.add(new RemoveHiddenLabelsAlgorithm());
		return res;
	}
	
	@Override
	public String getName() {
		return "Change Labels";
	}
	
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Nodes";
		else
			return "menu.edit";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return null;
	}
	
	@Override
	public ButtonSize getButtonSize() {
		return ButtonSize.SMALL;
	}
}
