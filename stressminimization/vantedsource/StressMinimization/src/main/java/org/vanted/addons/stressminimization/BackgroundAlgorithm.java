package org.vanted.addons.stressminimization;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.function.Supplier;

import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;

/**
 * Algorithm which runs in the background and
 * able to notify other classes about state
 * of the algorithm or the layout change of the graph.
 */
public abstract class BackgroundAlgorithm extends AbstractEditorAlgorithm {

	/**
	 * change support to notify other classes
	 */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * old status of the algorithm
	 */
	private volatile BackgroundStatus status;
	/**
	 * some progress measure. Value between 0.0 and 1.0.
	 */
	private double progress;

	/**
	 * A textual description of the current status.
	 * Used as a replacement for console logging.
	 */
	private String statusDescription;

	/**
	 * A supplier that gives the current layout.
	 * Instead of a simple Map, a supplier is used here,
	 * because some conversion maybe necessary from the format of the running algorithm
	 * to this format and unnecessary conversion may be avoided,
	 * in case the algorithm is not showing intermediate results.
	 */
	private Supplier<HashMap<Node, Vector2d>> layout;

	/**
	 * Whether this algorithm was told to pause by the executing frontend algorithm.
	 */
	private volatile boolean paused = false;

	/**
	 * Whether this algorithm was told to stop by the executing frontend algorithm.
	 */
	private volatile boolean stopped = false;

	/**
	 * returns an array of PropertyChangeListener that were added to the BackgroundAlgorithm
	 * @return all of the PropertyChangeListeners added or an empty array if no listeners have been added
	 */
	public PropertyChangeListener[] getPropertyChangeListener() {
		return pcs.getPropertyChangeListeners();
	}

	/**
	 * add new PropertyChangeListener to the listener list
	 * @param pcl PropertyChangeListner to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}
	/**
	 * remove PropertyChangeListener from the listener list
	 * @param pcl PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}

	/**
	 * add elements of an array to the property change list
	 * @param pcl Array of PropertyChangeListener which should be added to the listener list
	 */
	public void addPropertyChangeListeners(PropertyChangeListener[] pcl) {
		for(PropertyChangeListener listener:pcl) {
			pcs.addPropertyChangeListener(listener);
		}
	}

	/**
	 * notify listener classes a new graph layout is available and
	 * send the old and the new layout
	 * @param layoutSupplier new  Supplier<HashMap<Node, Vector2d>> layout
	 */
	protected void setLayout(Supplier<HashMap<Node, Vector2d>> layoutSupplier) {
		pcs.firePropertyChange("setLayout",this.layout, layoutSupplier);
		this.layout=layoutSupplier;
	}

	/**
	 * notify listener classes a new status of the algorithm is available.
	 * @param status new BackgroundStatus value
	 */
	protected void setStatus(BackgroundStatus status) {
		pcs.firePropertyChange("setStatus", this.status, status);
		this.status=status;
	}

	/**
	 * notify listener classes a new progress value is available
	 * @param newProgress new double progress value
	 */
	protected void setProgress(double newProgress) {
		double oldProgress = this.progress;
		this.progress = newProgress;
		pcs.firePropertyChange("setProgress", oldProgress, newProgress);
	}

	/**
	 * notify listener classes a new status description is available
	 * @param newStatusDescription new status description String
	 */
	protected void setStatusDescription(String newStatusDescription) {
		String oldStatusDescription = this.statusDescription;
		this.statusDescription = newStatusDescription;
		pcs.firePropertyChange("setStatusDescription", oldStatusDescription, newStatusDescription);
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
	 * Stops the execution of the algorithm. In contrast to pause the algorithm will terminate.
	 * reset has to be called before the next execution of the algorithm.
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
		this.layout = null;
	}

	public BackgroundStatus getStatus() {
		return status;
	}

	public double getProgress() {
		return progress;
	}

	public String getStatusDescription() {
		return statusDescription;
	}

	public Supplier<HashMap<Node, Vector2d>> getLayout() {
		return layout;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isStopped() {
		return stopped;
	}

}
