package org.vanted.animation.animations;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation; 
import org.vanted.animation.data.Point2DTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class Position2DAnimation extends ContinuousAnimation<Point2DTimePoint>
{
	public Position2DAnimation(Node node,double startTime,double duration,Interpolator interpolator, List<Point2DTimePoint> dataPoints,
			int noLoops,Looper looper)
	{
		super(node,startTime,duration,interpolator,dataPoints,noLoops,looper);
	}
	@Override
	protected <T>void animate(double time,T interpolatedValue)
	{
		AttributeHelper.setPosition((Node)attributable, (Point2D)interpolatedValue);
	}
}
