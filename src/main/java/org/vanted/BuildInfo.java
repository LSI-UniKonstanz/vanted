package org.vanted;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.HelperClass;

/**
 * A utility helper for accessing build information. High-level interface for
 * accessing the build information file.
 * 
 * @author Dimitar Garkov
 * @since 2.6.5
 */
public class BuildInfo implements HelperClass {

	private BuildInfo() {
	}

	/**
	 * Gets the current Vanted version, as specified in the build file.
	 * 
	 * @return the version as String or just empty String
	 */
	public static String getCurrentVersion() {
		return BuildInfo.getBuildProperty("vanted.version.number");
	}

	/**
	 * Gets the minimal Vanted compatibility, as specified in the build file.
	 * 
	 * @return the compatibility as version-String or just empty String
	 */
	public static String getMinimalCompatibility() {
		return BuildInfo.getBuildProperty("vanted.min.compatibility");
	}

	/**
	 * Gets the last Vanted build number, as specified in the build file.
	 * 
	 * @return the build number as String or just empty String
	 * @vanted.todo Attach build number update after deployment build
	 */
	public static String getLastBuildNumber() {
		return BuildInfo.getBuildProperty("build.number");
	}

	/**
	 * Gets the last Vanted build number, as specified in the build file.
	 * 
	 * @return the build number as String or just empty String
	 * @vanted.todo Attach build number update after deployment build
	 */
	public static String getLastBuildDate() {
		return BuildInfo.getBuildProperty("build.date");
	}

	/**
	 * Worker method for retrieving properties.
	 * 
	 * @param property
	 *            build file variable key
	 * @return the retrieved property or empty String
	 */
	private static String getBuildProperty(String property) {
		try (InputStream stream = BuildInfo.class.getClassLoader().getResourceAsStream("build.number")) {
			Properties built_props = new Properties();
			built_props.load(stream);
			return built_props.getProperty(property);
		} catch (IOException e) {
			e.printStackTrace();

			return "";
		}
	}
}
