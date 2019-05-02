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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JMenuItem;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.view.View;

/**
 * If you need a right-click context menu entry (more than the already
 * provided). This kind of contextmenu is an evil hack and will be improved
 * later, hopefully.
 * <p>
 * The sense of this menu is also quite ... small, don't wonder!
 * 
 * @author Hendrik Rohn
 */
public class RightClickContextMenuAlgorithm extends AbstractEditorAlgorithm
					implements
					ProvidesNodeContextMenu {

	// with returning null you suppress an entry in the top-menu
	public String getName() {
		return null;
	}

	private JMenuItem[] getMenuItem(final Collection<Node> selectedNodes) {
		JMenuItem[] result = new JMenuItem[2];
		result[0] = new JMenuItem("Show Dialog with number of selected Nodes");
		result[1] = new JMenuItem(
							"Show status-panel-message with number of Edges in Graph");

		if (selectedNodes != null && selectedNodes.size() > 0)
			graph = selectedNodes.iterator().next().getGraph();

		result[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("is " + selectedNodes.size(),
									"Number of selected Nodes");
			}
		});
		result[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (graph != null)
					MainFrame.showMessage("is " + graph.getEdges().size(),
										MessageType.INFO);
				else
					ErrorMsg.addErrorMessage("Graph is null!");
			}
		});
		return result;
	}

	public JMenuItem[] getCurrentNodeContextMenuItem(
						Collection<Node> selectedNodes) {
		try {
			if (selectedNodes != null && selectedNodes.size() >= 0)
				return getMenuItem(selectedNodes);
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void execute() {
		// 

	}

	public boolean activeForView(View v) {
		return v != null;
	}

}
