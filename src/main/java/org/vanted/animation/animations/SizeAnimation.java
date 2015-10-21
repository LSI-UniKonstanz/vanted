package org.vanted.animation.animations;
import java.awt.geom.Point2D;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation; 
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * Animates the size of a Node object.
 * @author - Patrick Shaw
 * 
 */
public class SizeAnimation extends ContinuousAnimation<Point2DTimePoint> {
	public SizeAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime, int noLoops, Looper looper,
			Interpolator interpolator) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		// TODO Auto-generated constructor stub
	}

	public SizeAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime, int noLoops, Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		// TODO Auto-generated constructor stub
	}

	public SizeAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		// TODO Auto-generated constructor stub
	}

	public SizeAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		// TODO Auto-generated constructor stub
	}

	public SizeAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <T> void animate(double time,T interpolatedValue) {
		Point2D newPoint = (Point2D) interpolatedValue;
		AttributeHelper.setSize((Node)attributable, newPoint.getX(), newPoint.getY());
	}
}
