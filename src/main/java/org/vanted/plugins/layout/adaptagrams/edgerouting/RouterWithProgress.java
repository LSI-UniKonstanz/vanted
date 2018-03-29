/**
 * This class extends the Router class from adaptagrams.jar (Java Wrapper for Adaptagrams).
 * Implements a callback for the native Adaptagrams library to provide routing progress information.
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams.edgerouting;

import java.util.Locale;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.adaptagrams.Router;
import org.apache.log4j.Logger;

/**
 * @author Tobias Czauderna
 */
public class RouterWithProgress extends Router {

	BackgroundTaskStatusProviderSupportingExternalCall backgroundTaskStatusProvider = null;

	private static final Logger logger = Logger.getLogger(RouterWithProgress.class);

	/**
	 * @param arg0
	 */
	public RouterWithProgress(long arg0) {

		super(arg0);

	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RouterWithProgress(long arg0, boolean arg1) {

		super(arg0, arg1);

	}

	/**
	 * @param arg0
	 * @param status
	 *            BackgroundTaskStatusProviderSupportingExternalCall
	 */
	public RouterWithProgress(long arg0, BackgroundTaskStatusProviderSupportingExternalCall status) {

		super(arg0);
		this.backgroundTaskStatusProvider = status;

	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param status
	 *            BackgroundTaskStatusProviderSupportingExternalCall
	 */
	public RouterWithProgress(long arg0, boolean arg1, BackgroundTaskStatusProviderSupportingExternalCall status) {

		super(arg0, arg1);
		this.backgroundTaskStatusProvider = status;

	}

	/**
	 * This method gets called from the native Adaptagrams library to provide
	 * routing progress information. Sets the status text and the progress bar of
	 * the BackgroundTaskStatusProviderSupportingExternalCall components. Sets
	 * logger information.
	 * 
	 * @param elapsedTime
	 *            time elapsed since start of routing
	 * @param phaseNumber
	 *            phase number
	 * @param totalPhases
	 *            total number of phases
	 * @param proportion
	 *            routing progress (0...1)
	 * @return true (routing should continue) or false (routing should stop)
	 */
	@Override
	public boolean shouldContinueTransactionWithProgress(long elapsedTime, long phaseNumber, long totalPhases,
			double proportion) {

		if (this.backgroundTaskStatusProvider != null) {
			this.backgroundTaskStatusProvider
					.setCurrentStatusText1(EdgeRoutingAlgorithm.getStatusTextPhaseNumber((int) phaseNumber));
			this.backgroundTaskStatusProvider
					.setCurrentStatusText2(EdgeRoutingAlgorithm.getStatusTextPhaseDescription((int) phaseNumber));
			if (proportion < 0.01)
				this.backgroundTaskStatusProvider.setCurrentStatusValue(-1);
			else
				this.backgroundTaskStatusProvider.setCurrentStatusValueFine(proportion * 100.0);
		}

		int hours = (int) Math.ceil(elapsedTime / 3600000.0);
		int minutes = (int) Math.ceil(elapsedTime / 60000.0) - hours * 60;
		int seconds = (int) Math.ceil(elapsedTime / 1000.0) - minutes * 60 - hours * 3600;
		long milliseconds = elapsedTime - seconds * 1000 - minutes * 60000 - hours * 3600000;
		String strElapsedTime = String.format("%02d:%02d:%02d.%03d", Integer.valueOf(hours), Integer.valueOf(minutes),
				Integer.valueOf(seconds), Long.valueOf(milliseconds));
		logger.info("Adaptagrams Edge Routing: running for " + strElapsedTime + ", phase " + phaseNumber + " of "
				+ totalPhases + " ... " + String.format(Locale.ENGLISH, "%1$,.2f", Double.valueOf(proportion * 100.0))
				+ "%");
		return true;

	}

}
