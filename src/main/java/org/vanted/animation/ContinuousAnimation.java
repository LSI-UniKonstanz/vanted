package org.vanted.animation;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class ContinuousAnimation<DataPointClass extends TimePoint> extends Animation<DataPointClass>
{
	protected Interpolator interpolator;
	/**
	 * @param interpolator
	 * Determines how values will be interpolated between data points.
	 * @param duration
	 * The total duration of the animation.
	 */
	public ContinuousAnimation(Attributable attributable,double duration,Interpolator interpolator, List<DataPointClass> dataPoints)
	{
		super(attributable,duration,dataPoints);
		this.interpolator = interpolator;
	}
	/**
	 * 
	 */
	public void setIsCircularData(LoopType loopType)
	{
		interpolator.setLoopType(loopType);
	}
	public LoopType getIsCircularData()
	{
		return interpolator.getLoopType();
	}
}