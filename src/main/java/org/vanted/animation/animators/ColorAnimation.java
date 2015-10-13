package org.vanted.animation.animators;

import java.awt.Color;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorPoint;
import org.vanted.animation.interpolators.Interpolator;

public abstract class ColorAnimation extends ContinuousAnimation<ColorPoint> {
	ColorMode colorMode;
	public ColorAnimation(Attributable attributable,double startTime, double duration, Interpolator interpolator, List<ColorPoint> dataPoints,
			int noLoops, LoopType loopType,ColorMode colorMode) {
		super(attributable,startTime, duration, interpolator, dataPoints,noLoops,loopType);
		this.colorMode = colorMode;
	}
	@Override
	protected Color getInterpolatedValue(double time)
	{
		return interpolator.interpolateColor(time, loopDuration, previousIndex, dataPoints, loopType, colorMode);
	}
}
