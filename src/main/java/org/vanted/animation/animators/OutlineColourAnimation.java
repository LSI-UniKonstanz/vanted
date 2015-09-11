package org.vanted.animation.animators;

import java.awt.Color;
import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.interpolators.Interpolator;

public class OutlineColourAnimation extends ContinuousAnimation {
	ColourPoint dataPoints[];
	public OutlineColourAnimation(Attributable attributable,double duration,Interpolator interpolator, ColourPoint dataPoints[]) {
		super(attributable,duration,interpolator);
		this.dataPoints = dataPoints;
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time,dataPoints);
		Color newColour = interpolator.interpolate(time,duration,previousIndex, dataPoints);
		AttributeHelper.setOutlineColor(attributable, newColour);
	}
}
