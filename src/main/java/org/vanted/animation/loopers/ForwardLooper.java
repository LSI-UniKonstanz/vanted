package org.vanted.animation.loopers;
public class ForwardLooper extends Looper {
	public ForwardLooper() { 
	}
	@Override
	protected int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter)
	{
		return kthIndex < 0 ? dataPointsSize + (kthIndex) : kthIndex;
	}
	@Override
	protected int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter)
	{
		return kthIndex % dataPointsSize;
	} 
}
