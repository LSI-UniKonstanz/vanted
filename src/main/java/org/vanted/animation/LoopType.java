package org.vanted.animation;
/**
 * 
 * @author - Patrick Shaw
 *
 */
public enum LoopType {
	/**
	 * Abrupt stop. If the last data point ends before the end of the animation,
	 * the animation will just stop.
	 */
	none,
	/**
	 * Interpolation between the last and the first data point. Creates a smooth
	 * transition from the end of the animation back to the start.
	 */
	forward,
	/**
	 * Interpolates between the last data points and the points before it.
	 * Creates a reverse-like a effect, the animation will begin to go backward.
	 */
	reverse 
}
