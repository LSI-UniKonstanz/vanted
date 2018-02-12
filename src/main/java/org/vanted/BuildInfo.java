package org.vanted;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A utility helper for accessing build information.
 * 
 * @author Dimitar Garkov
 * @since 2.6.5
 */
public class BuildInfo {

	private BuildInfo() {}
	
	/**
	 * Gets the current Vanted version, as specified in the
	 * build file.
	 * 
	 * @return the version as String or just an empty String
	 */
	public static String getCurrentVersion() {
		try (InputStream stream = BuildInfo.class.getClassLoader()
				.getResourceAsStream("build.number")) {
			Properties built_props = new Properties();
			built_props.load(stream);
			return built_props.getProperty("vanted.version.number");				
		} catch (IOException e) {
			e.printStackTrace();
			
			return "";
		}
	}
}
