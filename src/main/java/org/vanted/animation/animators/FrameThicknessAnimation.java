package org.vanted.animation.animators;
import java.util.List;

import org.AttributeHelper; 
import org.graffiti.graph.GraphElement;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class FrameThicknessAnimation extends ContinuousAnimation<DoublePoint> {
	public FrameThicknessAnimation(GraphElement attributable,double duration,Interpolator interpolator, List<DoublePoint> dataPoints) {
		super(attributable,duration,interpolator,dataPoints);
	}
	
	@Override
	public void animate(double time) {
		recalcPreviousIndex(time);
		double newThickness = interpolator.interpolate(time,duration,previousIndex,dataPoints);
		AttributeHelper.setFrameThickNess((GraphElement)attributable, newThickness);
	}

}
