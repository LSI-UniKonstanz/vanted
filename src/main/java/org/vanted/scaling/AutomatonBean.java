package org.vanted.scaling;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * This introduces states in terms of the scaling work flow. It is useful for
 * synchronizing scaling events on special components, which haven't been scaled
 * globally. For instance, components that are initialized after the scaling is
 * done or any that change their layout continuously. Then their respective
 * scaler could be dispatched. One should listen for property change on the one
 * of the three active states: <i>onStart</i>, <i>onSlider</i> and
 * <i>rescaled</i>.
 * <p>
 * 
 * Possible values are:<br>
 * <b>null</b>: not set, but called upon.<br>
 * <b>unscaled</b>: initial property value; could go to <i>onSlider</i>,
 * <i>onStart</i>.<br>
 * <b>onStart</b>: scaled on application start-up; could go to <i>idle</i>.<br>
 * <b>onSlider</b>: scaled with the slider while application running; could go
 * to <i>unscaled</i>, <i>rescaled</i>, <i>idle</i>.<br>
 * <b>rescaled</b>: re-scaling with the same slider instance; could go to
 * <i>onSlider</i>, <i>idle</i>.<br>
 * <b>idle</b>: after scaling is done and custom DPI is established; could go to
 * <i>onSlider</i>.<br>
 * 
 * @author dim8
 *
 */
class AutomatonBean implements Serializable {

	private static final long serialVersionUID = 1L;

	static final String NAME = "scaling state";

	static enum State {
		UNSCALED, ON_SLIDER, ON_START, RESCALED, IDLE
	}

	private static PropertyChangeSupport support;

	private static String state;

	static AutomatonBean instance;

	AutomatonBean() {
		state = State.UNSCALED.toString();
		support = new PropertyChangeSupport(this);
		support.firePropertyChange(NAME, null, State.UNSCALED);
		instance = this;
	}

	static String getState() {
		return state;
	}

	static boolean setState(State newState) {
		if (isStateTransitionCorrect(newState)) {
			support.firePropertyChange(NAME, state, newState.toString());
			state = newState.toString();
			return true;
		} else
			return false;
	}

	void addPropertyChangeListener(PropertyChangeListener listener) {
		for (PropertyChangeListener l : support.getPropertyChangeListeners(NAME))
			if (l.equals(listener))
				return;

		support.addPropertyChangeListener(NAME, listener);
	}

	void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	static AutomatonBean getInstance() {
		return instance;
	}

	private static boolean isStateTransitionCorrect(State toCheck) {
		switch (state) {
		case "UNSCALED":
			if (toCheck == State.ON_SLIDER || toCheck == State.ON_START)
				return true;
			break;
		case "ON_SLIDER":
			if (!(toCheck == State.ON_START || toCheck == State.ON_SLIDER))
				return true;
			break;
		case "ON_START":
			if (toCheck == State.IDLE)
				return true;
			break;
		case "RESCALED":
			if (toCheck == State.ON_SLIDER || toCheck == State.IDLE)
				return true;
			break;
		case "IDLE":
			if (toCheck == State.ON_SLIDER)
				return true;
			break;
		}

		return false;
	}
}
