package org.vanted.animation.animators;
import java.awt.Color;
import java.util.Arrays;
import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class FillColourAnimation extends ContinuousAnimation {
	private ColourPoint dataPoints[];
	public FillColourAnimation(Attributable attributable,double duration,Interpolator interpolator,ColourPoint dataPoints[])
	{
		super(attributable,duration,interpolator);
		this.dataPoints = dataPoints;
	}
	@Override
	public void animate(double time)
	{
		recalcPreviousIndex(time,dataPoints);
		Color newColour = interpolator.interpolate(time,duration, previousIndex, dataPoints);
		AttributeHelper.setFillColor(attributable, newColour);
	}
	@Override
	public String toString()
	{
		return interpolator.toString()+"\n"+Arrays.toString(dataPoints);
	}
}
