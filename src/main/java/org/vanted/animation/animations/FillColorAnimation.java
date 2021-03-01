package org.vanted.animation.animations;

import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

/**
 * Animates the fill color of an attributable object.
 * 
 * @author - Patrick Shaw
 */
public class FillColorAnimation extends ColorAnimation {
	public FillColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator, ColorMode colorMode) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator, colorMode);
		// TODO Auto-generated constructor stub
	}
	
	public FillColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		// TODO Auto-generated constructor stub
	}
	
	public FillColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		// TODO Auto-generated constructor stub
	}
	
	public FillColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		// TODO Auto-generated constructor stub
	}
	
	public FillColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		// TODO Auto-generated constructor stub
	}
	
	public FillColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setFillColor(attributable, (Color) interpolatedValue);
	}
}
