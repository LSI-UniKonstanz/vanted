/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.exampleaddon;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;

/**
 * A small algorithm for manually opening a view (for detailed explanation of
 * algorithms see {@link DoNothingAlgorithm}.
 * 
 * @author Hendrik Rohn
 */
public class OpenViewAlgorithm extends AbstractEditorAlgorithm {

	// creates a menuitem
	public String getName() {
		return "Open My View";
	}

	// in which JMenu do you want it, if it is not there it will be
	// automatically constructed
	@Override
	public String getCategory() {
		return "Views";
	}

	/**
	 * Actually an algorithm is not the best way to create a view, instead check
	 * File -> new View to open a new view of the same graph. But, however...
	 */
	public void execute() {

		MainFrame.showMessageDialog("<html>"
							+ "This view can be opened using the menu:<br>"
							+ "File -> new View (graph needs to be open)<br>"
							+ "To open an internal frame manually see the code.",
							"Open my view");

	}

	public boolean activeForView(View v) {
		return v != null;
	}
}
