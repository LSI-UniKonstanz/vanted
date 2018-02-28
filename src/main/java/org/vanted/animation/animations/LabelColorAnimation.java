package org.vanted.animation.animations;

import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

/**
 * 
 * Animates a label's color in a GraphElement object.
 * 
 * @author - Patrick Shaw
 * 
 */
public class LabelColorAnimation extends ColorAnimation {
	private int labelIndex;

	public LabelColorAnimation(GraphElement attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator, ColorMode colorMode,
			int labelIndex) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator, colorMode);
		this.labelIndex = labelIndex;
	}

	public LabelColorAnimation(GraphElement attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, Interpolator interpolator, int labelIndex) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper, interpolator);
		this.labelIndex = labelIndex;
	}

	public LabelColorAnimation(GraphElement attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper, int labelIndex) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		this.labelIndex = labelIndex;
	}

	public LabelColorAnimation(GraphElement attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, int labelIndex) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		this.labelIndex = labelIndex;
	}

	public LabelColorAnimation(GraphElement attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			double startTime, int labelIndex) {
		super(attributable, dataPoints, loopDuration, startTime);
		this.labelIndex = labelIndex;
	}

	public LabelColorAnimation(GraphElement attributable, List<ColorTimePoint> dataPoints, double loopDuration,
			int labelIndex) {
		super(attributable, dataPoints, loopDuration);
		this.labelIndex = labelIndex;
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setLabelColor(labelIndex, (GraphElement) attributable, (Color) interpolatedValue);
	}

}
