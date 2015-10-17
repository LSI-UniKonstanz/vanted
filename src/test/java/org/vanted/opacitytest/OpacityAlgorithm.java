/**
 * 
 */
package org.vanted.opacitytest;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;

/**
 * @author matthiak
 */
public class OpacityAlgorithm extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Animate Opacity";
	}
	
	@Override
	public String getMenuCategory() {
		return "Network";
	}
	
	@Override
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			double opac = AttributeHelper.getOpacity(ge);
			if (opac < 1.0)
				AttributeHelper.setOpacity(ge, 1.0);
			else
				AttributeHelper.setOpacity(ge, 0.4);
		}
		graph.getListenerManager().transactionFinished(this);
	}
}
