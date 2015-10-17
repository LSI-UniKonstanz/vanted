package org.vanted.animation.animations;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ArrowSizeAnimation extends ContinuousAnimation<DoubleTimePoint> {
	public ArrowSizeAnimation(Edge edge,double startTime,double duration,Interpolator interpolator,List<DoubleTimePoint> dataPoints,
			int noLoops,Looper looper) {
		super(edge,startTime,duration,interpolator,dataPoints,noLoops,looper);
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setArrowSize((Edge)attributable,(double)interpolatedValue);
	}

}
