package org.vanted.animation.animators;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.vanted.animation.Animation;
import org.vanted.animation.data.StringPoint;

public class LabelTextAnimation extends Animation {
	StringPoint dataPoints[];
	public LabelTextAnimation(Attributable attributable, double duration, StringPoint dataPoints[]) {
		super(attributable, duration);
		this.dataPoints = dataPoints;
	}

	@Override
	public void animate(double time) {
		int oldIndex = previousIndex;
		recalcPreviousIndex(time,dataPoints);
		if(oldIndex == previousIndex)
		{
			AttributeHelper.setLabel((Edge)attributable, dataPoints[previousIndex].getValue());
		}
	}

}
