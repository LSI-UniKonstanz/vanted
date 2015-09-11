package org.vanted.animation.animators;
import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class ArrowSizeAnimation extends ContinuousAnimation {
	DoublePoint dataPoints[];
	public ArrowSizeAnimation(Edge edge,double duration,Interpolator interpolator,DoublePoint dataPoints[]) {
		super(edge,duration,interpolator);
		this.dataPoints = dataPoints;
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time,dataPoints);
		double newSize = interpolator.interpolate(time, duration, previousIndex, dataPoints);
		AttributeHelper.setArrowSize((Edge)attributable,newSize);
	}

}
