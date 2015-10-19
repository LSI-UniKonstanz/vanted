/**
 * 
 */
package org.vanted.opacitytest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animator;
import org.vanted.animation.animations.HideAnimation;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.interpolators.CosineInterpolator;
import org.vanted.animation.loopers.StandardLooper;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

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
		Animator animator = new Animator(graph, 1);
		animator.setLoopDuration(1000, TimeUnit.MILLISECONDS);
		Set<GraphElement> nodesAndEdges = new HashSet<GraphElement>();
		if (selection.isEmpty())
			nodesAndEdges.addAll(graph.getNodes());
		else
			nodesAndEdges.addAll(getSelectedOrAllGraphElements());
		
		Set<Edge> edges = new HashSet<>();
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			if (ge instanceof Node) {
				edges.clear();
				edges.addAll(((Node) ge).getAllInEdges());
				edges.addAll(((Node) ge).getAllOutEdges());
				for (Edge curEdge : edges) {
					Node source = curEdge.getSource();
					Node target = curEdge.getTarget();
					boolean sourcehidden = AttributeHelper.isHiddenGraphElement(source);
					boolean targethidden = AttributeHelper.isHiddenGraphElement(target);
					System.out.println("source: " + source + " " + sourcehidden);
					System.out.println("target: " + target + " " + targethidden);
					
					if ((sourcehidden && targethidden)
							|| (!sourcehidden && !targethidden)) {
						nodesAndEdges.add(curEdge);
						System.out.println("adding");
					} else
						System.out.println("not adding");
				}
			}
		}
		for (GraphElement ge : nodesAndEdges) {
			/*
			 * handle faded elements (faded element to hidden state)
			 */
			double opacity = AttributeHelper.getOpacity(ge);
			if (AttributeHelper.isHiddenGraphElement(ge)) {
				
//				if (opacity <= 0.0) {
				List<DoubleTimePoint> listTP = new ArrayList<DoubleTimePoint>();
				listTP.add(new DoubleTimePoint(0, opacity));
				listTP.add(new DoubleTimePoint(950, 1.1));
				HideAnimation opacAnim = new HideAnimation(ge, 0, 1000, new CosineInterpolator(), listTP, 1, new StandardLooper());
				
				animator.addAnimation(opacAnim);
				
//				}
			} else {
				List<DoubleTimePoint> listTP = new ArrayList<DoubleTimePoint>();
				listTP.add(new DoubleTimePoint(0, opacity));
				listTP.add(new DoubleTimePoint(950, -0.1));
				HideAnimation opacAnim = new HideAnimation(ge, 0, 1000, new CosineInterpolator(), listTP, 1, new StandardLooper());
				animator.addAnimation(opacAnim);
				
			}
			
		}
		GraphHelper.clearSelection();
		animator.start();
	}
}
