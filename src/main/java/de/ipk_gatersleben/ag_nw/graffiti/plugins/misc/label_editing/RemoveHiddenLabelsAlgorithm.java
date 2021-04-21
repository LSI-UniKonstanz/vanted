package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;

/**
 * @author rohn
 */
public class RemoveHiddenLabelsAlgorithm extends AbstractAlgorithm {
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANNOTATION));
	}
	
	@Override
	public String getMenuCategory() {
		return "edit.Change Label";
	}
	
	public String getName() {
		return "Remove hidden labels";
	}
	
	public void execute() {
		int deletedCnt = 0, cntges = 0;
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			int markdelete = deletedCnt;
			for (int k = 1; k < 100; k++)
				if (AttributeHelper.hasAttribute(ge, GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k))) {
					ge.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k));
					deletedCnt++;
				}
			if (markdelete != deletedCnt)
				cntges++;
		}
		if (deletedCnt == 0)
			MainFrame.showMessage("<html>No hidden labels removed", MessageType.PERMANENT_INFO);
		else
			MainFrame.showMessageDialog("<html>" + deletedCnt + " hidden labels of " + cntges
					+ " graphelements have been deleted from graph<p><i>" + graph.getName(), "Information");
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}
