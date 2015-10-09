package org.vanted.animation.animators;

import java.awt.geom.Point2D;
import java.util.List;
import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.DoublePoint;
import org.vanted.animation.interpolators.Interpolator;
public class PositionXAnimation extends ContinuousAnimation<DoublePoint> {

	public PositionXAnimation(Node attributable, double duration,Interpolator interpolator, List<DoublePoint> dataPoints,
			int noLoops,LoopType loopType) {
		super(attributable, duration,interpolator, dataPoints,noLoops,loopType);
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setPosition((Node)attributable, new Point2D.Double((double)interpolatedValue,AttributeHelper.getPositionY((Node)attributable)));
	}

}
