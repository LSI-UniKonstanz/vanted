package org.graffiti.plugin.io;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

/***
 * After an InputSerializer reads a graph, this postprocessor determines whether
 * default labels should be the empty string or the node ID. Specifically, <br>
 * <br>
 * 
 * "", for graphs with labels;<br>
 * "", for KEGG, SBML, SBGN graphs;<br>
 * ID, otherwise
 * 
 * @author Dimitar Garkov
 * @since 2.8.7
 *
 */
public class DefaultLabelPostProcessor implements GraphPostProcessor {

	@Override
	public String[] getExtensions() {
		return null;
	}

	@Override
	public String[] getFileTypeDescriptions() {
		return null;
	}

	@Override
	public void processNewGraph(Graph g) {
		for (Node n : g.getNodes())
			if (!AttributeHelper.getLabel(n, "").isEmpty())
				return;

		boolean isKegg = AttributeHelper.hasAttribute(g, "", "kegg_link");
		boolean isSBML = AttributeHelper.hasAttribute(g, "sbml");
		boolean isSBGN = AttributeHelper.hasAttribute(g, "sbgn");

		if (isKegg || isSBML || isSBGN)
			return;

		for (Node n : g.getNodes())
			if (AttributeHelper.hasAttribute(n, "graphml_id"))
				AttributeHelper.setLabel(n, (String) 
						AttributeHelper.getAttribute(n, "graphml_id").getValue());
			else
				AttributeHelper.setLabel(n, String.valueOf(n.getID()));
	}

}
