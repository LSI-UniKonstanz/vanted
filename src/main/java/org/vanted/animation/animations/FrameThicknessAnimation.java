package org.vanted.animation.animations;
import java.util.List;

import org.AttributeHelper; 
import org.graffiti.graph.GraphElement;
import org.vanted.animation.ContinuousAnimation; 
import org.vanted.animation.data.DoubleTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class FrameThicknessAnimation extends ContinuousAnimation<DoubleTimePoint> {
	public FrameThicknessAnimation(GraphElement attributable,double startTime,double duration,Interpolator interpolator, List<DoubleTimePoint> dataPoints,
			int noLoops,Looper looper) {
		super(attributable,startTime,duration,interpolator,dataPoints,noLoops,looper);
	}
	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setFrameThickNess((GraphElement)attributable, (double)interpolatedValue);
	}

}
