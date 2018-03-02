package org.vanted.osx;

import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.swing.Action;

/**
 * Handles Mac OS X specific stuff.
 *
 * @author <a href="mailto:johan@kaving.se">Johan Kaving</a>
 */
public class OSXSupport {

	public static final String OPEN_FILES = "openFiles";

	public static final String HANDLE_PREFERENCES = "handlePreferences";

	public static final String HANDLE_QUIT_REQUEST_WITH = "handleQuitRequestWith";

	public static final String HANDLE_ABOUT = "handleAbout";

	//private static final Logger logger = Logger.getLogger(OSXSupport.class.getName());

	private static Object application = null;

	/**
	 * Initializes various Mac OS X adaptations, using reflection and proxies in
	 * order to be compilable on other platforms.
	 *
	 * @param aboutAction
	 *            the action to use for the About menu item
	 * @param quitAction
	 *            the action to use for the Quit menu item
	 * @param iconImage
	 *            the icon image to use for the dock
	 * @param fullScreenableWindow
	 *            the window that should be full screen enabled
	 */
	public static void initializeMacOSX(Action aboutAction, Action quitAction, Action preferencesAction,
			OpenFileAction openFileAction, Image iconImage, Window fullScreenableWindow) {
		try {
			if (!isOSX()) {
				return;
			}

			// Handle the system About menu item
			if (aboutAction != null) {
				addOSXHandler("com.apple.eawt.AboutHandler", HANDLE_ABOUT, "setAboutHandler", aboutAction);
			}

			// Handle the system Quit menu item
			if (quitAction != null) {
				addOSXHandler("com.apple.eawt.QuitHandler", HANDLE_QUIT_REQUEST_WITH, "setQuitHandler", quitAction);
			}

			if (preferencesAction != null) {
				// Handle the system Preferences menu item
				addOSXHandler("com.apple.eawt.PreferencesHandler", HANDLE_PREFERENCES, "setPreferencesHandler",
						preferencesAction);
			}

			if (openFileAction != null) {
				addOSXHandler("com.apple.eawt.OpenFilesHandler", OPEN_FILES, "setOpenFileHandler", openFileAction);
			}

			if (hasOSXFullScreenSupport()) {
				// Enable Mac OS X Lion full screen support
				Class.forName("com.apple.eawt.FullScreenUtilities")
						.getDeclaredMethod("setWindowCanFullScreen", Window.class, Boolean.TYPE)
						.invoke(null, fullScreenableWindow, true);
			}

			// Use the screen menu bar instead of the window menu bar
			System.setProperty("apple.laf.useScreenMenuBar", "true");

			// TODO Check it!
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VANTED");
			System.setProperty("apple.awt.textantialiasing", "true");

			// Set the dock icon image
			// TODO Deprecated/Not working
			if (iconImage != null) {
				Object application = getOSXApplication();
				Method setDockIconImageMethod = application.getClass().getMethod("setDockIconImage", Image.class);
				setDockIconImageMethod.invoke(application, iconImage);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Adds an OS X handler to the application
	 * 
	 * @param handlerClassName
	 *            the class name of the handler to add
	 * @param handlerMethodName
	 *            the name of the method that is called on the handler when invoked
	 * @param handlerSetterMethodName
	 *            the name of the method used to set the handler
	 * @param action
	 *            the action to perform when the handler is invoked
	 */
	public static void addOSXHandler(String handlerClassName, final String handlerMethodName,
			String handlerSetterMethodName, final Action action) {
		try {
			Object application = getOSXApplication();

			Class<?> handlerClass = Class.forName(handlerClassName);
			if (action != null) {
				Object aboutHandlerProxy = Proxy.newProxyInstance(OSXSupport.class.getClassLoader(),
						new Class[] { handlerClass }, new InvocationHandler() {
							@SuppressWarnings("unchecked")
							@Override
							public Object invoke(Object o, Method method, Object[] args) throws Throwable {

								switch (handlerMethodName) {
								case HANDLE_ABOUT:
								case HANDLE_PREFERENCES:
								case HANDLE_QUIT_REQUEST_WITH:
									if (method.getName().equals(handlerMethodName)) {

										ActionEvent ae = new ActionEvent((Object) args, 1,
												new String(handlerMethodName));
										action.actionPerformed(ae);
									}
									break;
								case OPEN_FILES:
									if (method.getName().equals(handlerMethodName)) {
										Object obj = args[0].getClass().getMethod("getFiles").invoke(args[0]);
										if (obj instanceof List<?> && action instanceof OpenFileAction)
											((OpenFileAction) action).openFiles((List<File>) obj);

									}
									break;
								default:
								}
								return null;
							}
						});
				application.getClass().getMethod(handlerSetterMethodName, handlerClass).invoke(application,
						aboutHandlerProxy);
			} else {
				application.getClass().getMethod(handlerSetterMethodName, handlerClass).invoke(application,
						(Object) null);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Get an Apple eAWT Application object
	 * 
	 * @return an Application object
	 */
	public static Object getOSXApplication() {
		if (application == null) {
			try {
				Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
				Method method = applicationClass.getMethod("getApplication");
				application = method.invoke(applicationClass);
			} catch (Exception e) {
			}
		}

		return application;
	}

	/**
	 * Determine whether we are running on MAC OS X or not
	 * 
	 * @return <code>true</code> if is running on MAC OS X
	 */
	public static boolean isOSX() {
		String osName = System.getProperty("os.name");

		if (!osName.startsWith("Mac OS X")) {
			return false;
		}

		return true;
	}

	/**
	 * Mac OS X Lion full screen support was added in Java for Mac OS X 10.7 Update
	 * 1 and 10.6 Update 6. This determines whether we are running on a version with
	 * Lion full screen support.
	 *
	 * Which Java version corresponds to which Java for Mac OS X update can be found
	 * in Apple's: "Technical Note TN2110 - Identifying Java on Mac OS X"
	 * 
	 * @see <a href=
	 *      "https://developer.apple.com/library/mac/#technotes/tn2002/tn2110.html">Technical
	 *      Note TN2110</a>
	 *
	 * @return true if we are running on OS X with the new eAWT
	 */
	public static boolean hasOSXFullScreenSupport() {

		if (!isOSX()) {
			return false;
		}

		String javaVersion = System.getProperty("java.version");
		String[] fragments = javaVersion.split("\\.");

		// sanity check the "1." part of the version
		if (!fragments[0].equals("1"))
			return false;
		if (fragments.length < 2)
			return false;

		try {
			int minorVers = Integer.parseInt(fragments[1]);
			String[] updateFragments = fragments[2].split("_");
			if (minorVers == 6) {
				if (Integer.parseInt(updateFragments[1]) >= 29) {
					return true;
				}
			}
			if (minorVers >= 7) {
				return true;
			}
		} catch (NumberFormatException e) {
			// was not an integer
		}

		return false;
	}
}