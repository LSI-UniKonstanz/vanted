package org.vanted.animation.animators;

import java.awt.Color;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.ColourMode;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.interpolators.Interpolator;

public abstract class ColorAnimation extends ContinuousAnimation<ColourPoint> {
	ColourMode colourMode;
	public ColorAnimation(Attributable attributable, double duration, Interpolator interpolator, List<ColourPoint> dataPoints,
			int noLoops, LoopType loopType,ColourMode colourMode) {
		super(attributable, duration, interpolator, dataPoints,noLoops,loopType);
		this.colourMode = colourMode;
	}
	@Override
	protected Color getInterpolatedValue(double time)
	{
		return interpolator.interpolateColour(time, duration, previousIndex, dataPoints, loopType, colourMode);
	}
}
