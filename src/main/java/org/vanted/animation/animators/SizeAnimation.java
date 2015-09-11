package org.vanted.animation.animators;

import java.awt.geom.Point2D;
import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class SizeAnimation extends ContinuousAnimation {
	Point2DPoint dataPoints[];
	public SizeAnimation(Node node,double duration,Interpolator interpolator, Point2DPoint dataPoints[]) {
		super(node,duration,interpolator);
		this.dataPoints = dataPoints;
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time,dataPoints);
		Point2D size = interpolator.interpolate(time,duration,previousIndex, dataPoints);
		AttributeHelper.setSize((Node)attributable, size.getX(), size.getY());
	}
}
