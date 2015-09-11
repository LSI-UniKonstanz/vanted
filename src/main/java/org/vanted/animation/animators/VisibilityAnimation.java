package org.vanted.animation.animators;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.Animation;
import org.vanted.animation.data.BooleanPoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class VisibilityAnimation extends Animation{
	BooleanPoint dataPoints[];
	/**
	 * 
	 * @param attributable
	 * @param duration
	 * @param dataPoints
	 * True = Hidden
	 * False = Visible
	 */
	public VisibilityAnimation(Attributable attributable, double duration, BooleanPoint dataPoints[]) {
		super(attributable, duration);
		this.dataPoints = dataPoints;
	}

	@Override
	public void animate(double time) {
		int oldIndex = previousIndex;
		recalcPreviousIndex(time, dataPoints);
		if(oldIndex != previousIndex)
		{
			AttributeHelper.setHidden(dataPoints[previousIndex].getValue(), (GraphElement)attributable);
		}
	}
	
}
