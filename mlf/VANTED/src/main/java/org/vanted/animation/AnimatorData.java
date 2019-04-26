package org.vanted.animation;

/**
 * 
 * @author - Patrick Shaw
 *
 */
public class AnimatorData {
	private final double timeElapsed;
	private final int currentLoopNumber;
	private final double loopDuration;
	private final int noLoops;
	private final boolean isAutoLoopDuration;

	public AnimatorData(double timeElapsed, int currentLoopNumber, double loopDuration, int noLoops,
			boolean isAutoLoopDuration) {
		this.timeElapsed = timeElapsed;
		this.currentLoopNumber = currentLoopNumber;
		this.loopDuration = loopDuration;
		this.noLoops = noLoops;
		this.isAutoLoopDuration = isAutoLoopDuration;
	}

	public double getTimeElapsed() {
		return timeElapsed;
	}

	public int getCurrentLoopNumber() {
		return currentLoopNumber;
	}

	public double getLoopDuration() {
		return loopDuration;
	}

	public int getNoLoops() {
		return noLoops;
	}

	public boolean getIsAutoLoopDuration() {
		return isAutoLoopDuration;
	}
}
