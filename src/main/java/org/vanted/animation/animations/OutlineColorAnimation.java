package org.vanted.animation.animations;

import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation; 
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorTimePoint;
import org.vanted.animation.interpolators.Interpolator;
import org.vanted.animation.loopers.Looper;

public class OutlineColorAnimation extends ColorAnimation {
	public OutlineColorAnimation(Attributable attributable,double startTime,double duration,Interpolator interpolator, List<ColorTimePoint> dataPoints,
			int noLoops,Looper looper,ColorMode colourMode) {
		super(attributable,startTime,duration,interpolator,dataPoints,noLoops,looper,colourMode);
	}

	@Override
	protected <T> void animate(double time,T interpolatedValue) {
		AttributeHelper.setOutlineColor(attributable, (Color)interpolatedValue);
	}
}
