package org.vanted.animation.animators;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.vanted.animation.Animation;
import org.vanted.animation.data.StringPoint;

public class LabelTextAnimation extends Animation<StringPoint> {
	public LabelTextAnimation(Attributable attributable, double duration, List<StringPoint> dataPoints) {
		super(attributable, duration,dataPoints);
	}

	@Override
	public void animate(double time) {
		int oldIndex = previousIndex;
		recalcPreviousIndex(time);
		if(oldIndex == previousIndex)
		{
			AttributeHelper.setLabel((Edge)attributable, dataPoints.get(previousIndex).getValue());
		}
	}

}
