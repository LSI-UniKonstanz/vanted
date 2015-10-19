/**
 * 
 */
package org.vanted.animation.animations;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

/**
 * @author matthiak
 */
public class HideAnimation extends OpacityAnimation {
	
	public HideAnimation(Attributable attributable, double startTime, double duration,
			Interpolator interpolator, List<DoubleTimePoint> dataPoints, int noLoops, Looper looper)
	{
		super(attributable, startTime, duration, interpolator, dataPoints, noLoops, looper);
	}
	
	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		super.animate(time, interpolatedValue);
		if (AttributeHelper.isHiddenGraphElement((GraphElement) attributable)) {
			if ((double) interpolatedValue > 0.0)
				AttributeHelper.setHidden(false, (GraphElement) attributable);
		} else {
			if ((double) interpolatedValue <= 0.0)
				AttributeHelper.setHidden(true, (GraphElement) attributable);
		}
	}
}
