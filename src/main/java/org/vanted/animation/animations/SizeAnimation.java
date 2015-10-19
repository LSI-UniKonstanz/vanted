package org.vanted.animation.animations;

import java.awt.geom.Point2D;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation; 
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class SizeAnimation extends ContinuousAnimation<Point2DTimePoint> {
	public SizeAnimation(Node node,double startTime,double duration,Interpolator interpolator, List<Point2DTimePoint> dataPoints,
			int noLoops,Looper looper) {
		super(node,startTime,duration,interpolator,dataPoints,noLoops,looper);
	}

	@Override
	public <T> void animate(double time,T interpolatedValue) {
		Point2D newPoint = (Point2D) interpolatedValue;
		AttributeHelper.setSize((Node)attributable, newPoint.getX(), newPoint.getY());
	}
}
