package org.vanted.animation.animations;

/**
 * 
 * Animates the text of a label in an Edge object.
 * @author - Patrick Shaw
 * 
 */
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.vanted.animation.Animation;
import org.vanted.animation.data.StringTimePoint;
import org.vanted.animation.loopers.Looper;

public class LabelTextAnimation extends Animation<StringTimePoint> {

	public LabelTextAnimation(Edge attributable, List<StringTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		// TODO Auto-generated constructor stub
	}

	public LabelTextAnimation(Edge attributable, List<StringTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		// TODO Auto-generated constructor stub
	}

	public LabelTextAnimation(Edge attributable, List<StringTimePoint> dataPoints, double loopDuration,
			double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		// TODO Auto-generated constructor stub
	}

	public LabelTextAnimation(Edge attributable, List<StringTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void animate(double time) {
		AttributeHelper.setLabel((Edge) attributable, dataPoints.get(previousIndex).getDataValue());
	}
}
