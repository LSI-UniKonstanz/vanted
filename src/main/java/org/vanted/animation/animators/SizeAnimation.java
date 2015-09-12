package org.vanted.animation.animators;

import java.awt.geom.Point2D;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class SizeAnimation extends ContinuousAnimation<Point2DPoint> {
	public SizeAnimation(Node node,double duration,Interpolator interpolator, List<Point2DPoint> dataPoints) {
		super(node,duration,interpolator,dataPoints);
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time);
		Point2D size = interpolator.interpolate(time,duration,previousIndex, dataPoints);
		AttributeHelper.setSize((Node)attributable, size.getX(), size.getY());
	}
}
