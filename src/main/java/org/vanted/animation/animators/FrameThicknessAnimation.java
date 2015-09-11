package org.vanted.animation.animators;
import org.AttributeHelper; 
import org.graffiti.graph.GraphElement;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class FrameThicknessAnimation extends ContinuousAnimation {
	DoublePoint dataPoints[];
	public FrameThicknessAnimation(GraphElement attributable,double duration,Interpolator interpolator, DoublePoint dataPoints[]) {
		super(attributable,duration,interpolator);
		this.dataPoints = dataPoints;
	}
	
	@Override
	public void animate(double time) {
		recalcPreviousIndex(time,dataPoints);
		double newThickness = interpolator.interpolate(time,duration,previousIndex,dataPoints);
		AttributeHelper.setFrameThickNess((GraphElement)attributable, newThickness);
	}

}
