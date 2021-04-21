package org.vanted.animation.animations;

import java.util.List;

import org.AttributeHelper;
import org.graffiti.attributes.Attributable;
import org.graffiti.graph.GraphElement;
import org.vanted.animation.Animation;
import org.vanted.animation.data.BooleanTimePoint;
import org.vanted.animation.loopers.Looper;

/**
 * Animates the visibility of an Attributable object.<br>
 * BooleanTimePoint data values: <br>
 * True = Hidden <br>
 * False = Visible <br>
 * 
 * @author - Patrick Shaw
 */
public class VisibilityAnimation extends Animation<BooleanTimePoint> {
	
	/**
	 * @param attributable
	 * @param dataPoints
	 * @param loopDuration
	 * @param startTime
	 * @param noLoops
	 * @param looper
	 */
	public VisibilityAnimation(Attributable attributable, List<BooleanTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops, Looper looper) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops, looper);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param attributable
	 * @param dataPoints
	 * @param loopDuration
	 * @param startTime
	 * @param noLoops
	 */
	public VisibilityAnimation(Attributable attributable, List<BooleanTimePoint> dataPoints, double loopDuration,
			double startTime, int noLoops) {
		super(attributable, dataPoints, loopDuration, startTime, noLoops);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param attributable
	 * @param dataPoints
	 * @param loopDuration
	 * @param startTime
	 */
	public VisibilityAnimation(Attributable attributable, List<BooleanTimePoint> dataPoints, double loopDuration,
			double startTime) {
		super(attributable, dataPoints, loopDuration, startTime);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param attributable
	 * @param dataPoints
	 * @param loopDuration
	 */
	public VisibilityAnimation(Attributable attributable, List<BooleanTimePoint> dataPoints, double loopDuration) {
		super(attributable, dataPoints, loopDuration);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void animate(double time) {
		AttributeHelper.setHidden(dataPoints.get(previousIndex).getDataValue(), (GraphElement) attributable);
	}
	
}
