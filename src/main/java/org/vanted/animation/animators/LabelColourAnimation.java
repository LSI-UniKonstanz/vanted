package org.vanted.animation.animators;
import java.awt.Color;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.ContinuousAnimation;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class LabelColourAnimation extends ContinuousAnimation<ColourPoint> {
	private int labelIndex;
	public LabelColourAnimation(GraphElement attributable,double duration,Interpolator interpolator,List<ColourPoint> dataPoints, int labelIndex) {
		super(attributable,duration,interpolator,dataPoints);
		this.labelIndex = labelIndex;
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time);
		Color newColour = interpolator.interpolate(time, duration, previousIndex, dataPoints);
		AttributeHelper.setLabelColor(labelIndex, (GraphElement)attributable, newColour);
	}

}
