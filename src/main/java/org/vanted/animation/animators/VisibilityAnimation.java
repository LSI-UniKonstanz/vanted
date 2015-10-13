package org.vanted.animation.animators;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.Animation;
import org.vanted.animation.LoopType;
import org.vanted.animation.data.BooleanPoint;
/**
 * 
 * @author - Patrick Shaw
 * 
 */
public class VisibilityAnimation extends Animation<BooleanPoint>{
	/**
	 * 
	 * @param attributable
	 * @param duration
	 * @param dataPoints
	 * True = Hidden
	 * False = Visible
	 */
	public VisibilityAnimation(Attributable attributable,double startTime, double duration, List<BooleanPoint> dataPoints,
			int noLoops,LoopType loopType) {
		super(attributable,startTime, duration,dataPoints,noLoops,loopType);
	}

	@Override
	public void animate(double time) {
			AttributeHelper.setHidden(dataPoints.get(previousIndex).getDataValue(), (GraphElement)attributable);
	}
	
}
