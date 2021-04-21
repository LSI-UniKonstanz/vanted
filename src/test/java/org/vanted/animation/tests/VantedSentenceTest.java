/**
 * 
 */
package org.vanted.animation.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animation;
import org.vanted.animation.Animator;

/**
 * @author - Patrick Shaw
 */
public class VantedSentenceTest extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Vanted Presentation";
	}
	
	@Override
	public String getMenuCategory() {
		return "Network";
	}
	
	@Override
	public void execute() {
		Animator animator = new Animator(graph);
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node node : getSelectedOrAllNodes())
			nodes.add(node);
		List<Animation> animations = ParagraphSentencer.sentencesToNodeInfo(graph, nodes,
				Arrays.asList(new String[] { ".", "hi everyone my name is patrick.", "and this is vanted.",
						"my project was...", "animated visualisation of dynamic graphs...",
						"for life science applications.", "one semester.", "this is what i ended up with." }));
		for (int i = 0; i < animations.size(); i++) {
			animator.addAnimation(animations.get(i));
		}
		animator.start();
	}
	
}
