package org.vanted.animation;
/**
 * Represents the amount of time taken for a single loop of an animation.<br>
 * Primarily exists to prevent confusion about setting the duration to infinity (-1).<br>
 * @author - Patrick Shaw
 *
 */
public enum LoopDuration {
	INFINITY(-1),
	ONE_MILLISECOND(1),
	ONE_SECOND(1000),
	ONE_MINUTE(60000),
	ONE_HOUR(360000);
	double loopDuration; 
   LoopDuration(double loopDuration) { this.loopDuration = loopDuration; }
   public double getValue() { return loopDuration; }
}
