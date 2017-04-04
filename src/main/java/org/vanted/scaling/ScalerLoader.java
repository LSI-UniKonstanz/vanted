package org.vanted.scaling;

import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.graffiti.managers.PreferenceManager;

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
	
	private static final String RESOURCE_PKG = "org.vanted.scaling.resources";
	/**
	 * A ratio between display height and emulated DPI. All values
	 * below this threshold are safe in all cases, in sense of 
	 * user-friendly (not too big/small UIDefaults size). */
	private static final int USABILITY_THRESHOLD = 22;
	
	private static final String RESET_DIALOG_PREFS = "ResetDialogPreferences";
	
	private static Preferences resetDialogPreferences = null;
	
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
			if (isAvoidable())
				return;
			
			displayResetDialog();
			
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
				
		int sValue = ScalingSlider.managePreferences(ScalingSlider.VALUE_DEFAULT,
				ScalingSlider.PREFERENCES_GET);
				
		new ScalingCoordinator(ScalingSlider.processDPI(sValue), //factor
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
		
		int value = ScalingSlider.managePreferences(ScalingSlider.VALUE_DEFAULT,
				ScalingSlider.PREFERENCES_GET);
		float scaleFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 
				ScalingSlider.processDPI(value);
		
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
	
	/**
	 * Test whether scaling is currently necessary.
	 * 
	 * @return true if Scaling could be skipped.
	 */
	private static boolean isAvoidable() {
		int value = ScalingSlider.managePreferences(ScalingSlider.VALUE_UNSET,
				ScalingSlider.PREFERENCES_GET);
		float factor = Toolkit.getDefaultToolkit().getScreenResolution() / 
				ScalingSlider.processDPI(value);
		
		/**
		 * ScalingSlider.managePreferences() called with the above
		 * combination of parameters returns a flag value for checking,
		 * if actually a value has ever been stored under the specified
		 * preferences. If there is some writing error at the time, this 
		 * would also affect scaling. Additionally, if the factor is the
		 * identity element, just avoid defaults & components iteration.
		 */
		if (value == ScalingSlider.VALUE_UNSET || factor == 1.0)
			return true;
		
		return false;
	}
	
/**
 * Checks, if the DPI is in the usability boundaries 
 * and whether resetting conditions should apply.
 * 
 * @param value current or last set slider value to check against
 * 
 * @return true if safe
 */
	private static boolean isSafe(int value) {
		int dpi = Math.round(ScalingSlider.processDPI(value));
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		float height = (float) gd.getDisplayMode().getHeight();
		int usabilityRatio = Math.round(height/dpi);
		
		if (usabilityRatio < USABILITY_THRESHOLD && value < ScalingSlider.max)
			return true;
		
		return false;
	}
	
	private static void displayResetDialog() {
		int value = ScalingSlider.managePreferences(ScalingSlider.VALUE_DEFAULT, true);
		
		if (isSafe(value))
			return;
		
		if (hide())
			return;
		
		ImageIcon lifesaver = new ImageIcon(
				ScalerLoader.loadResource(ScalerLoader.class, "lifesaver.png"));
		String title = "Reset DPI";
		JFrame parent = new JFrame(title);
		parent.setIconImage(lifesaver.getImage());
	    parent.setUndecorated(true);
	    parent.pack();
		parent.setVisible(true);
	    parent.setLocationRelativeTo(null);
		int selection = JOptionPane.showConfirmDialog(parent, getContents(value),
				title, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, lifesaver);
		
		if (selection == JOptionPane.YES_OPTION) {
			//write
			ScalingSlider.managePreferences(50, false);
			//& flush
			try {
				ScalingSlider.flushPreferences();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		parent.dispose();
	}
	
	/**
	 * Get the contents of the reset dialog ready.
	 * 
	 * @param value the ScalingSlider value, saved in Preferences
	 *  
	 * @return a JPanel, filled with all needed contents
	 */
	private static JPanel getContents(int value) {
		int dpi = Math.round(ScalingSlider.processDPI(value));
		JPanel contents = new JPanel();
		contents.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		
		contents.add(new JLabel("<html>Emulated DPI is " + dpi + 
				". This could render interaction impossible.<br><br>"
				+ "Would you like to reset it?</html>"), c);
		
		JButton toDisable = new JButton("Disable");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.insets = new Insets(0, 0, 0, 10);
		c.gridx = 1; c.gridy = 1;
		toDisable.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				resetDialogPreferences.putBoolean(RESET_DIALOG_PREFS, true);
			}
		});
		contents.add(toDisable, c);
		
		return contents;
	}
	
	private static boolean hide() {
		resetDialogPreferences = PreferenceManager
								.getPreferenceForClass(ScalerLoader.class);
		boolean hide = resetDialogPreferences.getBoolean(RESET_DIALOG_PREFS, false);
		
		return hide;
	}
	
	/**
	 * A resource loading utility method for resources placed in the resource package.
	 * 
	 * @param clazz delegates to the respective class loader
	 * @param filename the resource name
	 * 
	 * @return the loaded resource
	 * 
	 * @see {@link ScalerLoader#RESOURCE_PKG} 
	 * 
	 */
	public static URL loadResource(Class<?> clazz, String filename) {
		ClassLoader cl = clazz.getClassLoader();
		String path = RESOURCE_PKG.replace('.', '/');
		
		return	cl.getResource(path + "/" + filename);
	}
}