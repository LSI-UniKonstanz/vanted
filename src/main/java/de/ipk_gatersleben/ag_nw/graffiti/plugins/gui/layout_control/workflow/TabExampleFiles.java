package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkTab;

public class TabExampleFiles extends PathwayWebLinkTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7073237240383682448L;

	public TabExampleFiles() {
		super("Examples", "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/examplefiles/", "example files",
				"example file", "www.vanted.org", true);
	}

	@Override
	protected void addAnnotationsToGraphElements(Graph graph) {
		// empty
	}

	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}

	@Override
	protected String[] getValidExtensions() {
		return new String[] { ".gml", ".graphml" };
	}

}
