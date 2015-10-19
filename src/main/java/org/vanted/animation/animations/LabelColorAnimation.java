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
 * @author - Patrick Shaw
 * 
 */
public class LabelColorAnimation extends ColorAnimation {
	private int labelIndex;
	public LabelColorAnimation(GraphElement attributable,double startTime,double duration,Interpolator interpolator,List<ColorTimePoint> dataPoints, int labelIndex,
			int noLoops,Looper looper,ColorMode colourMode) {
		super(attributable,startTime,duration,interpolator,dataPoints,noLoops,looper,colourMode);
		this.labelIndex = labelIndex;
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setLabelColor(labelIndex, (GraphElement)attributable, (Color)interpolatedValue);
	}

}
