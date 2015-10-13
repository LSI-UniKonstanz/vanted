package org.vanted.animation.animators;
import java.util.List;

import org.AttributeHelper; 
import org.graffiti.graph.GraphElement;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class FrameThicknessAnimation extends ContinuousAnimation<DoublePoint> {
	public FrameThicknessAnimation(GraphElement attributable,double startTime,double duration,Interpolator interpolator, List<DoublePoint> dataPoints,
			int noLoops,LoopType loopType) {
		super(attributable,startTime,duration,interpolator,dataPoints,noLoops,loopType);
	}
	
	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setFrameThickNess((GraphElement)attributable, (double)interpolatedValue);
	}

}
