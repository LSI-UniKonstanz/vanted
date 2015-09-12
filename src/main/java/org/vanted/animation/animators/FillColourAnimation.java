package org.vanted.animation.animators;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.data.TimePoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class FillColourAnimation extends ContinuousAnimation<ColourPoint> {
	public FillColourAnimation(Attributable attributable,double duration,Interpolator interpolator,List<ColourPoint> dataPoints)
	{
		super(attributable,duration,interpolator,(dataPoints));
	}
	@Override
	public void animate(double time)
	{
		recalcPreviousIndex(time);
		Color newColour = interpolator.interpolate(time,duration, previousIndex, dataPoints);
		AttributeHelper.setFillColor(attributable, newColour);
	}
}
