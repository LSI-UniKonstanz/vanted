package org.vanted.animation.animators;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ArrowSizeAnimation extends ContinuousAnimation<DoublePoint> {
	public ArrowSizeAnimation(Edge edge,double startTime,double duration,Interpolator interpolator,List<DoublePoint> dataPoints,
			int noLoops,LoopType loopType) {
		super(edge,startTime,duration,interpolator,dataPoints,noLoops,loopType);
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setArrowSize((Edge)attributable,(double)interpolatedValue);
	}

}
