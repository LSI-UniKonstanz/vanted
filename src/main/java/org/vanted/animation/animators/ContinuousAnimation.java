package org.vanted.animation.animators;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Node;
import org.vanted.animation.Animation;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public abstract class ContinuousAnimation extends Animation
{
	protected Interpolator interpolator;
	/**
	 * @param interpolator
	 * Determines how values will be interpolated between data points.
	 * @param duration
	 * The total duration of the animation.
	 */
	public ContinuousAnimation(Attributable attributable,double duration,Interpolator interpolator)
	{
		super(attributable,duration);
		this.interpolator = interpolator;
	}
	/**
	 * 
	 */
	public void setIsCircularData(boolean isCircularData)
	{
		interpolator.setIsCircularData(isCircularData);
	}
	public boolean getIsCircularData()
	{
		return interpolator.getIsCircularData();
	}
}