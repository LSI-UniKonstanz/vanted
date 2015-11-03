/**
 * 
 */
package org.vanted.animation.tests;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Edge; 
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animation;
import org.vanted.animation.Animator;
import org.vanted.animation.NumberOfLoops;
import org.vanted.animation.animations.FillColorAnimation;
import org.vanted.animation.animations.FrameThicknessAnimation;
import org.vanted.animation.animations.LabelColorAnimation;
import org.vanted.animation.animations.OutlineColorAnimation;
import org.vanted.animation.animations.Position2DAnimation;
import org.vanted.animation.animations.SizeAnimation;
import org.vanted.animation.data.BooleanTimePoint;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.BezierInterpolator;
import org.vanted.animation.interpolators.CosineInterpolator;
import org.vanted.animation.interpolators.CubicInterpolator;
import org.vanted.animation.interpolators.LinearInterpolator;
import org.vanted.animation.loopers.ForwardLooper;
import org.vanted.animation.loopers.StandardLooper; 

/**
 * 
 * @author - Patrick Shaw
 * 
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
		for(Node node : getSelectedOrAllNodes())
			nodes.add(node);
		List<Animation> animations = ParagraphSentencer.sentencesToNodeInfo(
				graph, nodes,Arrays.asList(
						new String[]
								{
										".","hi everyone my name is patrick.", "and this is vanted.",
										"my project was...", "animated visualisation of dynamic graphs...",
										"for life science applications.","one semester.", "this is what i ended up with."
								}
						)
				);
		for(int i =0; i < animations.size(); i++)
		{
			animator.addAnimation(animations.get(i));
		}
		animator.start();
	}
	
}
