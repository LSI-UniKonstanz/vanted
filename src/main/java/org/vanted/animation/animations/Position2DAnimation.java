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
 * Animates the position of a Node object.
 * 
 * @author - Patrick Shaw
 */
public class Position2DAnimation extends ContinuousAnimation<Point2DTimePoint>
{
	public Position2DAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime, int noLoops, Looper looper,
			Interpolator interpolator) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		// TODO Auto-generated constructor stub
	}
	
	public Position2DAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime, int noLoops,
			Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		// TODO Auto-generated constructor stub
	}
	
	public Position2DAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		// TODO Auto-generated constructor stub
	}
	
	public Position2DAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration, double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		// TODO Auto-generated constructor stub
	}
	
	public Position2DAnimation(Attributable attributable, List<Point2DTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected <T> void animate(double time, T interpolatedValue)
	{
		
		AttributeHelper.setPosition((Node) attributable, (Point2D) interpolatedValue);
	}
}
