/**
 * 
 */
package org.vanted.animation.animations;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

/**
 * @author matthiak
 */
public class OpacityAnimation extends ContinuousAnimation<DoubleTimePoint> {

	public OpacityAnimation(Attributable attributable, double startTime, double duration, Interpolator interpolator,
			List<DoubleTimePoint> dataPoints, int noLoops, Looper looper) {
		super(attributable, dataPoints, duration, startTime, noLoops, looper, interpolator);
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setOpacity((GraphElement) attributable, (Double) interpolatedValue);
	}

}
