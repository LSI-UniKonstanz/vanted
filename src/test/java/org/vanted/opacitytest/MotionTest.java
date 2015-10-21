/**
 * 
 */
package org.vanted.opacitytest;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.vanted.animation.Animator;
import org.vanted.animation.animations.Position2DAnimation;
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.interpolators.CosineInterpolator;
import org.vanted.animation.loopers.StandardLooper;

/**
 * @author matthiak
 */
public class MotionTest extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Test Movement Animation";
	}
	
	@Override
	public String getMenuCategory() {
		return "Network";
	}
	
	@Override
	public void execute() {
		int duration = 1000;
		Animator animator = new Animator(graph, 2);
		for (GraphElement ge : getSelectedOrAllNodes()) {
			List<Point2DTimePoint> listP2dTP = new ArrayList<>();
			Point2D position = AttributeHelper.getPosition((Node) ge);
			listP2dTP.add(new Point2DTimePoint(0, position));
			Point2D newPosition = new Point2D.Double(position.getX() + 100, position.getY() + 100);
			listP2dTP.add(new Point2DTimePoint(1000, newPosition));
			Position2DAnimation posAnim = new Position2DAnimation((Node) ge, listP2dTP, duration);
			
			animator.addAnimation(posAnim);
		}

		animator.start();
	}
	
}
