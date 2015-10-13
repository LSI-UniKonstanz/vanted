package org.vanted.animation.animators;

import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorPoint;
import org.vanted.animation.interpolators.Interpolator;

public class OutlineColorAnimation extends ColorAnimation {
	public OutlineColorAnimation(Attributable attributable,double startTime,double duration,Interpolator interpolator, List<ColorPoint> dataPoints,
			int noLoops,LoopType loopType,ColorMode colourMode) {
		super(attributable,startTime,duration,interpolator,dataPoints,noLoops,loopType,colourMode);
	}

	@Override
	protected <T> void animate(double time,T interpolatedValue) {
		AttributeHelper.setOutlineColor(attributable, (Color)interpolatedValue);
	}
}
