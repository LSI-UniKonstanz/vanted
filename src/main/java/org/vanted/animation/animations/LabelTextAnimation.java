package org.vanted.animation.animations;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.Edge;
import org.vanted.animation.Animation; 
import org.vanted.animation.data.StringTimePoint;
import org.vanted.animation.loopers.Looper;

public class LabelTextAnimation extends Animation<StringTimePoint> {
	public LabelTextAnimation(Attributable attributable,double startTime, double duration, List<StringTimePoint> dataPoints,
			int noLoops,Looper looper) {
		super(attributable,startTime, duration,dataPoints,noLoops,looper);
	}

	@Override
	protected void animate(double time) {
			AttributeHelper.setLabel((Edge)attributable, dataPoints.get(previousIndex).getValue());
	}
}
