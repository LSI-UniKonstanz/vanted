package org.vanted.animation.animators;
import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.ColorMode;
import org.vanted.animation.data.ColorPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class LabelColorAnimation extends ColorAnimation {
	private int labelIndex;
	public LabelColorAnimation(GraphElement attributable,double startTime,double duration,Interpolator interpolator,List<ColorPoint> dataPoints, int labelIndex,
			int noLoops,LoopType loopType,ColorMode colourMode) {
		super(attributable,startTime,duration,interpolator,dataPoints,noLoops,loopType,colourMode);
		this.labelIndex = labelIndex;
	}

	@Override
	protected <T> void animate(double time, T interpolatedValue) {
		AttributeHelper.setLabelColor(labelIndex, (GraphElement)attributable, (Color)interpolatedValue);
	}

}
