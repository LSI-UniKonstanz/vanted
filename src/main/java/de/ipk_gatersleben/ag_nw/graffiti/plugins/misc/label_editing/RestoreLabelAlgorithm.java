/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;

/**
 * @author Christian Klukas (c) 2006, 2007 IPK Gatersleben, Group Network
 *         Analysis
 */
public class RestoreLabelAlgorithm extends AbstractAlgorithm {

	@Override
	public String getName() {
		return "Restore labels...";
	}

	@Override
	public String getDescription() {
		return "<html>" + "Some commands offer an option to save the current label before it has<br>"
				+ "been changed. If such has already been executed, this command will<br>"
				+ "restore it (via its 'oldlabel' attribute).<br>";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION));
	}

	@Override
	public String getMenuCategory() {
		return "edit.Change Label";
	}

	@Override
	public void execute() {
		int idCnt = 0;
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR && ge instanceof Edge)
				continue;
			String oldlabel = (String) AttributeHelper.getAttributeValue(ge, "", "oldlabel", null, "");
			if (oldlabel != null) {
				AttributeHelper.setLabel(ge, oldlabel);
				idCnt++;
			}
		}
		MainFrame.showMessageDialog(idCnt + " labels have been restored", "Information");
	}

	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
