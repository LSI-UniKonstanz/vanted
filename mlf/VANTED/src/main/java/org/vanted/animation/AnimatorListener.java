package org.vanted.animation;

import org.vanted.animation.data.TimePoint;

/**
 * 
 * @author - Patrick Shaw
 *
 */
public interface AnimatorListener {
	/**
	 * Called when an animation is finished.
	 */
	public void onAnimationFinished(AnimatorData data, Animation<TimePoint> anim);

	public void onNewAnimatorLoop(AnimatorData data);

	public void onAnimatorStart(AnimatorData data);

	public void onAnimatorStop(AnimatorData data);

	public void onAnimatorReset(AnimatorData data);

	public void onAnimatorRestart(AnimatorData data);

	/**
	 * Called when the has totally finished running (if it is finished, it will also
	 * call 'onAnimationStop'
	 */
	public void onAnimatorFinished(AnimatorData data);
}