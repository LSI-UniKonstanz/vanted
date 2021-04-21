package org.vanted.animation.loopers;

import java.util.List;

import org.vanted.animation.data.TimePoint;

/**
 * Reverses the animation after it goes forward. (Reverses when the loop number
 * is an odd number)<br>
 * TODO: The SwingLooper only behaves properly for loop durations that are
 * longer than the largest time value in the animation's data point list.
 * 
 * @author - Patrick Shaw
 */
public class SwingLooper extends Looper {
	
	public SwingLooper() {
	}
	
	@Override
	protected int getIndexBeforePreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter) {
		return kthIndex < 0 ? dataPointsSize + (kthIndex) : kthIndex;
		/*
		 * int theIndex = pointsBefore; boolean goingBackward = true; for(int j =
		 * pointsBefore; j>kthIndex; j--) { if(theIndex <= 0) { goingBackward = false; }
		 * else { if(theIndex >= dataPointsSize - 1) { goingBackward = true; } }
		 * if(goingBackward) { theIndex--; } else { theIndex++; } } return theIndex;
		 */
	}
	
	@Override
	protected int getIndexAfterPreviousPoint(int kthIndex, int dataPointsSize, int pointsBefore, int pointsAfter) {
		return kthIndex >= dataPointsSize ? dataPointsSize - 1 : kthIndex;
		/*
		 * int theIndex = pointsBefore; boolean goingBackward = false; for(int j =
		 * pointsBefore; j<kthIndex; j++) { if(theIndex <= 0) { goingBackward = false; }
		 * else { if(theIndex >= dataPointsSize - 1) { goingBackward = true; } }
		 * if(goingBackward) { theIndex--; } else { theIndex++; } } return theIndex;
		 */
	}
	
	@Override
	public <V, T extends TimePoint<V>> int findPreviousIndex(List<T> dataPoints, int oldPreviousIndex,
			int currentLoopNumber, double time) {
		int newPreviousIndex = oldPreviousIndex;
		if (currentLoopNumber % 2 == 0) {
			newPreviousIndex = super.findPreviousIndex(dataPoints, oldPreviousIndex, currentLoopNumber, time);
		} else {
			for (int nextIndex = oldPreviousIndex - 1; nextIndex >= 0; nextIndex--) {
				if (dataPoints.get(nextIndex).getTime() > time) {
					newPreviousIndex = nextIndex;
				} else {
					break;
				}
			}
		}
		// System.out.println(newPreviousIndex);
		return newPreviousIndex;
	}
	
	@Override
	public double getTimeSinceStartOfLoop(int currentLoopNumber, double startTime, double loopDuration, double time) {
		// System.out.println(currentLoopNumber);
		if (currentLoopNumber % 2 == 0) {
			return super.getTimeSinceStartOfLoop(currentLoopNumber, startTime, loopDuration, time);
		} else {
			return loopDuration - super.getTimeSinceStartOfLoop(currentLoopNumber, startTime, loopDuration, time);
		}
	}
	
	@Override
	public <V, T extends TimePoint<V>> int getNextLoopPreviousIndex(List<T> dataPoints, int newLoopNumber) {
		return newLoopNumber % 2 == 0 ? 0 : dataPoints.size() - 1;
	}
}
