package org.vanted.scaling;

import java.awt.Container;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;

import javax.swing.UIManager;

/**
 * A '<i>static</i>' Loader for scaling effects to take place. Use this to 
 * perform/confirm the scaling operations. Although, the implemented slider
 * interface uses live update, it is needed to perform the scaling explicitly
 * on some occasions, such as - on start-up or on foreign components, not part
 * of the main container. The most convenient way is simply calling {@link 
 * ScalerLoader#init(Container, Class)}. As of the current implementation, this
 * relies on a static <code>getInstance</code> method in your main container.
 * For alternatives - read below.<p>
 * 
 * Additional usage:<p>
 * 
 * <b>Important</b>: Call<p> 
 * 
 * <code>{@link ScalerLoader#signal()}</code> <p>
 * 
 * in your main frame, after it has been initialized, to allow live startup 
 * scaling of its components. Then you could use the synchronized initial 
 * scaling {@link ScalerLoader#doSyncInitialScaling()}.
 * 
 * @author dim8
 */
public final class ScalerLoader {

	/** Semaphore to synchronize main frame's initialization. */
	private static Semaphore initialized = new Semaphore(0);	
	
	private static boolean START_UP = true;
	
	private static boolean SYNC_UP = true;
	
	/* Not publicly initialize-able!*/
	private ScalerLoader() {};
	
	/**
	 * Global <code>init</code> for LAF and External scaling operations.
	 * <p>
	 * 
	 * You should specify what listeners should be notified, given the 
	 * respective components have been updated (<i>see also</i>).
	 * 
	 * @param main the application's main container, frame, etc. Could be 
	 * still uninitialized, but has to be in the process of being, or just 
	 * before doing so! Its resizable components will then be resized.
	 * 
	 * @param clazz the Class of this <b>main</b> Container, use:
	 * <code>Classname.class</code>.
	 * 
	 * @see XScaler#notifyListeners()
	 * 
	 */
	public static void init(Container main, final Class<? extends Container> clazz) {
		if (START_UP) {
			
			/**
			 * Only for initial usage. */
			START_UP = false;
			
			/**
			 * Check, if scaling is necessary at all. */
			if (DPIManager.isAvoidable())
				return;
			
			/**Dispatch DPIManager. */
			DPIManager manager = new DPIManager();
			
			/** This is a safeguard against incautiously set unsafe values.
			 * It avoids need of hacky preferences resetting.*/
			manager.displayResetter();
			
			/**
			 * Straightaway try to scale all - LAF & non-LAF components,
			 * and most probably succeed only in LAF-scaling, if main is
			 * still uninitialized, together with its children.
			 */
			doScaling(main);
			
			/**
			 * We use another Thread to avoid blocking EDT/Main Thread and then
			 * wait on it. Would be a whale of time..
			 */
			new Thread(new Runnable() {
				
				@Override
				public void run() {
			
					/**
					 * Block here, until main container shows. 
					 * iContainer ---> the initialized Container, which is 
					 * being shown.
					 */
					Container iContainer = awaitMainContainer(clazz);
					
					/**
					 * Perform exclusively external scaling now, because 
					 * initially only LAF-Defaults have been scaled before
					 * the main container has been initialized. In worst case,
					 * this would scale the user-defined & -maintained components
					 * twice. in this case one should simply call the <code>
					 * doScaling()</code> method instead. So to take effect, we
					 * first wait (normally < 5000ms) until initialized, then we
					 * call only the ExternalScaler. The ExternalScaler from 
					 * <code>doScaling()</code>, simply returns, because the 
					 * main container is null.
					 */
					doSyncExternalScaling(iContainer);
				}
			})
			.start();
		}
	}

	/**
	 * It does exactly what its name suggests - it ventures preferences and
	 * scales according to the value. Particularly on start up.
	 */
	public static void doScaling(Container c) {
		//GTK-related L&Fs not supported, because by default non-re-scalable
		if (UIManager.getLookAndFeel().getClass().getCanonicalName().contains("GTK"))
			return;
				
		int sValue = DPIManager.managePreferences(DPIManager.VALUE_DEFAULT,
				DPIManager.PREFERENCES_GET);
				
		new ScalingCoordinator(DPIManager.processDPI(sValue), //factor
							 c);
	}
	
	/**
	 * Scale the DBE Splash Screen to reflect the DPI factor.
	 * As a side effect, it fixes an issue with Metal L&F.
	 * Metal does not call updatePreferences on startup and
	 * therefore does not scale properly.
	 */
	public static void doInitialScaling(Container c) {
		
		/* Ensures only once re-scaled, since there are 
		 * multiple VantedPreferences instances during a lifetime.
		 */
		if (START_UP) {
			doScaling(c);
			START_UP = false;
		}
	}
	
	/**
	 * This waits for the application's main frame to have been initialized,
	 * in order to scale its components on initial basis. Should be signaled.<p>
	 * 
	 * <b>Synchronized with Main Frame/Container.</b>
	 */
	public static void doSyncInitialScaling(final Container c) {
		if (SYNC_UP) {
			Thread runner = new Thread(new Runnable() {
			
				@Override
				public void run() {
					try {
						initialized.acquire();
						doScaling(c);
						SYNC_UP = false;
					} catch (InterruptedException e) {
						System.err.println("Interrupted during DOWN on semaphore!");
						e.printStackTrace();
					}
				}
			});
		
			runner.start();
		}
	}
	
	/**
	 * This waits for the application's main frame to have been initialized,
	 * in order to scale its user-set components.<p>
	 * 
	 * <b>Synchronized with Main Frame.</b>
	 * 
	 */
	public static void doSyncExternalScaling(final Container c) {
		
		if (UIManager.getLookAndFeel().getClass().getCanonicalName().contains("GTK"))
			return;
		
		try {
			initialized.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int value = DPIManager.managePreferences(DPIManager.VALUE_DEFAULT,
				DPIManager.PREFERENCES_GET);
		float scaleFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 
				DPIManager.processDPI(value);
		
		ScalingCoordinator plainCoordinator = new ScalingCoordinator();
		//perform external scaling
		plainCoordinator.adjustUserComponents(scaleFactor, c);
		
		//update GUI
		ScalingCoordinator.refreshGUI(c);
	}
	
	/**
	 * Signal that the main frame has loaded to scale its components.
	 */
	public static void signal() {
		initialized.release();
	}
	
	/**
	 * An utility method for waiting on the main container visibility.
	 * 
	 * @param the class to use Type Introspection on.
	 */
	public static Container awaitMainContainer(final Class<? extends Container> clazz) {
		
		/**
		 * To access the container.
		 * 
		 * @author dim8
		 *
		 */
		class Holder {
			private Container c = null;
			
			public Container getContainer() {
				return c;
			}
			
			public void setContainer(Container c) {
				this.c = c;
			}
		}
		
		
		final Holder holder = new Holder();
		
		Thread waiter = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Container c = null; 
				
				//wait till initialized
				while (c == null) {
					try {
						//give it some loading time
						Thread.sleep(200);
						
						c = (Container) clazz.getMethod("getInstance")
								.invoke(null);
					} catch (IllegalAccessException | IllegalArgumentException 
							| InvocationTargetException | NoSuchMethodException
							| SecurityException | InterruptedException e) {
						System.err.println("Not able to load initial external Scaler!");
						System.err.println("EXCEPTION: " + e.getCause());
						e.printStackTrace();
						
						//kill the thread
						if (!Thread.currentThread().isInterrupted())
							Thread.currentThread().interrupt();
					}
				}
				
				//wait till its components are surely initialized
				while(!c.isShowing())
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				
				//& lastly signal
				signal();
		
				//the initialized container
				holder.setContainer(c);
			}
		});
		
		waiter.start();
		
		/**
		 * Do not return before the waiter has finished waiting! */
		try {
			waiter.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return holder.getContainer();
	}
}