package org.vanted.addons.MultilevelFramework.BackgroundExecution;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;

/**
 * Algorithm which runs in the background and able to notify other classes about
 * state of the algorithm or the layout change of the graph.
 */
public abstract class BackgroundAlgorithm extends AbstractEditorAlgorithm {
	/**
	 * change support to notify other classes
	 */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * old status of the algorithm
	 */
	private BackgroundStatus status;
	/**
	 * old progress value of the algorithm between 0.0 and 1.0
	 */
	private double progress;
	/**
	 * old graph layout
	 */
	private Graph graph;
	/**
	 * old progress description
	 */
	private String progressDescription;
	/**
	 * Whether this algorithm was told to pause by the executing frontend algorithm.
	 */
	private volatile boolean paused = false;
	/**
	 * Whether this algorithm was told to stop by the executing frontend algorithm.
	 */
	private volatile boolean stopped = false;

	/**
	 * returns an array of PropertyChangeListener that were added to the
	 * BackgroundAlgorithm
	 * 
	 * @return all of the PropertyChangeListeners added or an empty array if no
	 *         listeners have been added
	 */
	public PropertyChangeListener[] getPropertyChangeListener() {
		return pcs.getPropertyChangeListeners();
	}

	/**
	 * add new PropertyChangeListener to the listener list
	 * 
	 * @param pcl PropertyChangeListner to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}

	/**
	 * remove PropertyChangeListener from the listener list
	 * 
	 * @param pcl PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}

	/**
	 * add elements of an array to the property change list
	 * 
	 * @param pcl Array of PropertyChangeListener which should be added to the
	 *            listener list
	 */
	public void addPropertyChangeListeners(PropertyChangeListener[] pcl) {
		for (PropertyChangeListener listener : pcl) {
			pcs.addPropertyChangeListener(listener);
		}
	}

	/**
	 * notify listener classes a new graph layout is available and send the old and
	 * the new layout
	 * 
	 * @param graph new Graph
	 */
	public void showLevel(Graph graph) {
		pcs.firePropertyChange("showLevel", this.graph, graph);
		this.graph = graph;
	}

	/**
	 * notify listener classes a new status of the algorithm is available
	 * 
	 * @param status new BackgroundStatus value
	 */
	public void setStatus(BackgroundStatus status) {
		pcs.firePropertyChange("setStatus", this.status, status);
		this.status = status;
	}

	/**
	 * notify listener classes a new progress value is available
	 * 
	 * @param newProgressValue value between 0.0 and 1.0
	 */
	public void setProgress(double newProgressValue) {
		pcs.firePropertyChange("setProgress", this.progress, newProgressValue);
		progress = newProgressValue;
	}

	/**
	 * notify listener classes a new progress description is available
	 * 
	 * @param progressDescription new description of the progress
	 */
	public void setProgressDescription(String progressDescription) {
		pcs.firePropertyChange("setProgressDescription", this.progressDescription, progressDescription);
		this.progressDescription = progressDescription;
	}

	/**
	 * Pauses the execution of this algorithm, if the algorithm is running.
	 */
	public void pause() {
		this.paused = true;
	}

	/**
	 * Resumes execution of this algorithm, if the algorithm has been paused.
	 */
	public void resume() {
		this.paused = false;
	}

	/**
	 * Stops the execution of the algorithm. In contrast to pause the algorithm will
	 * terminate. reset has to be called before the next execution of the algorithm.
	 */
	public void stop() {
		this.stopped = true;
	}

	@Override
	public void reset() {
		super.reset();
		this.paused = false;
		this.stopped = false;
		this.status = null;
		this.progress = 0.0;
	}

	public BackgroundStatus getStatus() {
		return status;
	}

	public double getProgress() {
		return progress;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isStopped() {
		return stopped;
	}

}
