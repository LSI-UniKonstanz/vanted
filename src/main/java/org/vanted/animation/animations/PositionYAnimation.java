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

public class PositionYAnimation extends ContinuousAnimation<DoubleTimePoint> {

	public PositionYAnimation(Attributable attributable,double startTime, double duration, Interpolator interpolator, List<DoubleTimePoint> dataPoints,
			int noLoops,Looper looper) {
		super(attributable,startTime, duration, interpolator, dataPoints,noLoops,looper);
	}

	@Override
	protected <T> void animate(double time,T interpolatedValue) {
		AttributeHelper.setPosition((Node)attributable, new Point2D.Double(AttributeHelper.getPositionX((Node)attributable),(double)interpolatedValue));
	}

}
