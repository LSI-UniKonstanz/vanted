package org.vanted.animation.animations;

import java.awt.Color;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation; 
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

public abstract class ColorAnimation extends ContinuousAnimation<ColorTimePoint> {
	ColorMode colorMode;
	public ColorAnimation(Attributable attributable,double startTime, double duration, Interpolator interpolator, List<ColorTimePoint> dataPoints,
			int noLoops, Looper looper,ColorMode colorMode) {
		super(attributable,startTime, duration, interpolator, dataPoints,noLoops,looper);
		this.colorMode = colorMode;
	}
	@Override
	protected Color getInterpolatedValue(double time)
	{
		return interpolator.interpolateColor(time, loopDuration, previousIndex, dataPoints, looper, colorMode);
	}
}
