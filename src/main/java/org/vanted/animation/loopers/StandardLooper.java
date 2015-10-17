package org.vanted.animation.loopers;
public class StandardLooper extends Looper{
	public StandardLooper() { 
	} 
	@Override
	protected int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter)
	{
		return kthIndex < 0 ? dataPointsSize + (kthIndex) : kthIndex;
	}
	@Override
	protected int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter)
	{
		return kthIndex =  kthIndex >= dataPointsSize ? dataPointsSize - 1 : kthIndex;
	}
}
