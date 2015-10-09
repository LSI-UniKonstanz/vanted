package org.vanted.animation.animators;

import java.awt.geom.Point2D;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class SizeAnimation extends ContinuousAnimation<Point2DPoint> {
	public SizeAnimation(Node node,double duration,Interpolator interpolator, List<Point2DPoint> dataPoints,
			int noLoops,LoopType loopType) {
		super(node,duration,interpolator,dataPoints,noLoops,loopType);
	}

	@Override
	public <T> void animate(double time,T interpolatedValue) {
		Point2D newPoint = (Point2D) interpolatedValue;
		AttributeHelper.setSize((Node)attributable, newPoint.getX(), newPoint.getY());
	}
}
