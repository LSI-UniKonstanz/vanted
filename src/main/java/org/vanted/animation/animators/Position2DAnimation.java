package org.vanted.animation.animators;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.Point2DPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class Position2DAnimation extends ContinuousAnimation<Point2DPoint>
{
	public Position2DAnimation(Node node,double startTime,double duration,Interpolator interpolator, List<Point2DPoint> dataPoints,
			int noLoops,LoopType loopType)
	{
		super(node,startTime,duration,interpolator,dataPoints,noLoops,loopType);
	}
	@Override
	protected <T>void animate(double time,T interpolatedValue)
	{
		AttributeHelper.setPosition((Node)attributable, (Point2D)interpolatedValue);
	}
}
