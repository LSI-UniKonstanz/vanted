package org.vanted.animation.animators;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.vanted.animation.Animation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.StringPoint;

public class LabelTextAnimation extends Animation<StringPoint> {
	public LabelTextAnimation(Attributable attributable,double startTime, double duration, List<StringPoint> dataPoints,
			int noLoops,LoopType loopType) {
		super(attributable,startTime, duration,dataPoints,noLoops,loopType);
	}

	@Override
	protected void animate(double time) {
			AttributeHelper.setLabel((Edge)attributable, dataPoints.get(previousIndex).getValue());
	}
}
