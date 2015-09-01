package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkTab;

public class TabExampleFiles extends PathwayWebLinkTab {
	
	public TabExampleFiles() {
		super("Examples", "https://immersive-analytics.infotech.monash.edu/vanted/examplefiles/",
							"example files", "example file", "https://immersive-analytics.infotech.monash.edu/vanted/", true);
	}
	
	private static final long serialVersionUID = 1L;
	
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
