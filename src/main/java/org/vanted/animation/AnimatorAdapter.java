// ==============================================================================
//
// AnimatorAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.vanted.animation;

import org.vanted.animation.data.TimePoint;

/**
 * Adapter for the {@linkplain AnimatorListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.7
 *
 */
public class AnimatorAdapter<T> implements AnimatorListener<T> {

	@Override
	public void onAnimationFinished(AnimatorData data, Animation<TimePoint<T>> anim) {
	}

	@Override
	public void onNewAnimatorLoop(AnimatorData data) {
	}

	@Override
	public void onAnimatorStart(AnimatorData data) {
	}

	@Override
	public void onAnimatorStop(AnimatorData data) {
	}

	@Override
	public void onAnimatorReset(AnimatorData data) {
	}

	@Override
	public void onAnimatorRestart(AnimatorData data) {
	}

	@Override
	public void onAnimatorFinished(AnimatorData data) {
	}

}
