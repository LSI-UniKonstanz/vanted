package org.vanted.animation.animations;

import java.awt.Color;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

/**
 * 
 * Animates the color of an Attributable object.
 * 
 * @author - Patrick Shaw
 *
 */
public abstract class ColorAnimation extends ContinuousAnimation<ColorTimePoint> {
	ColorMode colorMode;

	/**
	 * @param colorMode
	 *            Specifies which colour-interpolation mode the interpolator should
	 *            use<br>
	 *            RGB: Maintains brightness but not saturation during
	 *            interpolation<br>
	 *            HSB: Maintains saturation but not perceived-brightness during
	 *            interpolation<br>
	 *            TODO CIE-Lch: Maintains both perceived-brightness and saturation.
	 *            Performance heavy.
	 */
	public ColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator, ColorMode colorMode) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		this.colorMode = colorMode;
	}

	/**
	 * Uses RGB interpolation
	 */
	public ColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		this.colorMode = ColorMode.RGB;
	}

	/**
	 * Uses RGB interpolation
	 */
	public ColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		this.colorMode = ColorMode.RGB;
	}

	/**
	 * Uses RGB interpolation
	 */
	public ColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		this.colorMode = ColorMode.RGB;
	}

	/**
	 * Uses RGB interpolation
	 */
	public ColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		this.colorMode = ColorMode.RGB;
	}

	/**
	 * Uses RGB interpolation
	 */
	public ColorAnimation(Attributable attributable, List<ColorTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		this.colorMode = ColorMode.RGB;
	}

	@Override
	protected Color getInterpolatedValue(double time) {
		return interpolator.interpolateColor(time, loopDuration, previousIndex, dataPoints, looper, colorMode);
	}
}
