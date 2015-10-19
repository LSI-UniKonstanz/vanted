/**
 * 
 */
package org.vanted.opacitytest;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animator;

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
		Animator animator = new Animator(graph, 1000);
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			/*
			 * handle faded elements (faded element to hidden state)
			 */
			if (AttributeHelper.isHiddenGraphElement(ge)) {
				
				if (AttributeHelper.getOpacity(ge) <= 0.0) {
					
				}
			} else {
				
			}
			
		}
	}
}
