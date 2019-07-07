package org.vanted.addons.MultilevelFramework.BackgroundExecution;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider;

/**
 * Helper class that contains the progress and the status of a background
 * algorithm
 *
 */
public class BackgroundProgress implements BackgroundTaskStatusProvider, PropertyChangeListener {

	private BackgroundAlgorithm algorithm;
	private BackgroundStatus status;
	private String description; // status description of the algorithm
	private int progress;

	public BackgroundProgress(BackgroundAlgorithm algorithm) {
		this.algorithm = algorithm;
		progress = -1;
		status = BackgroundStatus.FINISHED;

		// add BackgroundExecution to listener list of algorithm
		algorithm.addPropertyChangeListener(this);
	}

	@Override
	public int getCurrentStatusValue() {
		return progress;
	}

	@Override
	public void setCurrentStatusValue(int value) {
		if (value < 0 || value > 100) {
			progress = -1;
		} else {
			progress = value;
		}
	}

	@Override
	public double getCurrentStatusValueFine() {
		return progress;
	}

	@Override
	public String getCurrentStatusMessage1() {
		return status.toString();
	}

	@Override
	public String getCurrentStatusMessage2() {
		return description;
	}

	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}

	@Override
	public void pleaseStop() {
		algorithm.stop();
	}

	@Override
	public void pleaseContinueRun() {
		algorithm.resume();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		switch (evt.getPropertyName()) {

		case "setStatus":
			status = (BackgroundStatus) evt.getNewValue();
			break;
		case "setProgress":
			this.setCurrentStatusValue((int) ((double) evt.getNewValue() * 100));
			break;
		case "setProgressDescription":
			System.out.println((String) evt.getNewValue());
			description = (String) evt.getNewValue();
			break;
		default:
			break;
		}

	}

}
