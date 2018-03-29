package org.vanted.animation.animations;

import java.awt.geom.Point2D;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

/**
 * 
 * Animates the y coordinate of a Node object.
 * 
 * @author - Patrick Shaw
 * 
 */
public class PositionYAnimation extends ContinuousAnimation<DoubleTimePoint> {
	public PositionYAnimation(Attributable attributable, List<DoubleTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		// TODO Auto-generated constructor stub
	}

	public PositionYAnimation(Attributable attributable, List<DoubleTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		// TODO Auto-generated constructor stub
	}

	public PositionYAnimation(Attributable attributable, List<DoubleTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		// TODO Auto-generated constructor stub
	}

	public PositionYAnimation(Attributable attributable, List<DoubleTimePoint> dataPoints, double loopDuration,
			double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		// TODO Auto-generated constructor stub
	}

	public PositionYAnimation(Attributable attributable, List<DoubleTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setPosition((Node) attributable,
				new Point2D.Double(AttributeHelper.getPositionX((Node) attributable), (Double) interpolatedValue));
	}

}
