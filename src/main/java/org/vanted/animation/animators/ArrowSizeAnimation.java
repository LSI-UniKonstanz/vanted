package org.vanted.animation.animators;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ArrowSizeAnimation extends ContinuousAnimation<DoublePoint> {
	public ArrowSizeAnimation(Edge edge,double duration,Interpolator interpolator,List<DoublePoint> dataPoints) {
		super(edge,duration,interpolator,dataPoints);
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time);
		double newSize = interpolator.interpolate(time, duration, previousIndex, dataPoints);
		AttributeHelper.setArrowSize((Edge)attributable,newSize);
	}

}
