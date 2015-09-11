package org.vanted.animation.animators;
import java.awt.Color;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.data.ColourPoint;
import org.vanted.animation.interpolators.Interpolator;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class LabelColourAnimation extends ContinuousAnimation {
	ColourPoint dataPoints[];
	int labelIndex;
	public LabelColourAnimation(GraphElement attributable,double duration,Interpolator interpolator,ColourPoint dataPoints[], int labelIndex) {
		super(attributable,duration,interpolator);
		this.dataPoints = dataPoints;
		this.labelIndex = labelIndex;
	}

	@Override
	public void animate(double time) {
		recalcPreviousIndex(time,dataPoints);
		Color newColour = interpolator.interpolate(time, duration, previousIndex, dataPoints);
		AttributeHelper.setLabelColor(labelIndex, (GraphElement)attributable, newColour);
	}

}
