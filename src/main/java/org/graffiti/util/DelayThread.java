/**
 * 
 */
package org.graffiti.util;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.event.AttributeEvent;

/**
 * @author matthiak
 *
 */
/**
 * This delay thread will help prevent too many calls in short time
 * by catching the calls and only giving the last stored event after
 * a short period of time to the actual event handler using a
 * callback mechanism
 * 
 * If it has delivered the event it will trigger "wait" and halt the thread
 * A new event will then wake up the thread.
 * @author matthiak
 *
 */
public class DelayThread extends Thread {
	private Logger logger = Logger.getLogger(DelayThread.class);

	static final int MAX_COUNT = 10;
	int counter;
	DelayedCallback callback;
	AttributeEvent e;
	/**
	 * 
	 */
	public DelayThread(DelayedCallback callback) {
		this.callback = callback;
		logger.setLevel(Level.INFO);
	}
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			increment();
			if(counter > MAX_COUNT){
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						logger.debug("invoking callback");
						callback.call(e);
					}
				});

				hibernate();
			}
		}
	}

	public synchronized void setAttributeEvent(AttributeEvent e) {
		logger.debug("setting attribute");
		notify();
		this.e = e;
		reset();
	}		

	private void reset() {
		logger.debug("resetting counter");
		counter = 0;
	}

	private void increment() {
		logger.debug("incrementing");
		counter++;
	}

	private synchronized void hibernate() {
		logger.debug("going to hibernate");
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.debug("got wakeup call");
	}


	/**
	 * This callback interface is used by the DelayThread
	 * Implementing classes can set the method to be called
	 * @author matthiak
	 *
	 */
	public interface DelayedCallback {
		public void call(AttributeEvent e);
	}
}
