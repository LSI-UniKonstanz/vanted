package org.vanted.animation.animations;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.Animation; 
import org.vanted.animation.data.BooleanTimePoint;
import org.vanted.animation.loopers.Looper;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class VisibilityAnimation extends Animation<BooleanTimePoint>{
	/**
	 * 
	 * @param attributable
	 * @param duration
	 * @param dataPoints
	 * True = Hidden
	 * False = Visible
	 */
	public VisibilityAnimation(Attributable attributable,double startTime, double duration, List<BooleanTimePoint> dataPoints,
			int noLoops,Looper looper) {
		super(attributable,startTime, duration,dataPoints,noLoops,looper);
	}

	@Override
	public void animate(double time) {
			AttributeHelper.setHidden(dataPoints.get(previousIndex).getDataValue(), (GraphElement)attributable);
	}
	
}
