package org.vanted.animation.animators;

import java.awt.geom.Point2D;
import java.util.List;
import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
public class PositionXAnimation extends ContinuousAnimation<DoublePoint> {

	public PositionXAnimation(Node attributable, double duration,Interpolator interpolator, List<DoublePoint> dataPoints) {
		super(attributable, duration,interpolator, dataPoints);
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time);
		double newX = interpolator.interpolate(time, duration, previousIndex, dataPoints);
		AttributeHelper.setPosition((Node)attributable, new Point2D.Double(newX,AttributeHelper.getPositionY((Node)attributable)));
	}

}
