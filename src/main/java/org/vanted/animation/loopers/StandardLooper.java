package org.vanted.animation.loopers;
/**
 * 
 * @author - Patrick Shaw
 *
 */
public class StandardLooper extends Looper{
	public StandardLooper() { 
	} 
	@Override
	protected int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter)
	{
		return kthIndex < 0 ? 0 : kthIndex;
	}
	@Override
	protected int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter)
	{
		return (kthIndex >= dataPointsSize) ? dataPointsSize - 1 : kthIndex;
	}
}
