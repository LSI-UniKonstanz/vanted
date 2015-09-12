package org.vanted.animation.animators;

import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.interpolators.Interpolator;

public class OutlineColourAnimation extends ContinuousAnimation<ColourPoint> {
	public OutlineColourAnimation(Attributable attributable,double duration,Interpolator interpolator, List<ColourPoint> dataPoints) {
		super(attributable,duration,interpolator,dataPoints);
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time);
		Color newColour = interpolator.interpolate(time,duration,previousIndex, dataPoints);
		AttributeHelper.setOutlineColor(attributable, newColour);
	}
}
