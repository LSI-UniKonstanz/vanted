/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.selection.Selection;

/**
 * @author matthiak
 *
 */
public class ClearSelection extends AbstractAlgorithm {
	@Override
	public String getName() {
		return "Clear Selection";
	}

	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.SELECTION));
	}

	@Override
	public void execute() {
		Selection selection = new Selection("cleared selection");
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(selection);
	}

}
