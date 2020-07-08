package org.vanted.updater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.ErrorMsg;
import org.apache.commons.codec.digest.DigestUtils;
import org.graffiti.util.Pair;

/**
 * Calculates all MD5 sums for all the jars found on the classpath of the
 * URLClassLoader for VANTED.
 * 
 * This is used for the update mechanism to compare the MD5s of remote libraries
 * and copy them, if they differ in their MD5s.
 * 
 * @author matthiak
 * @vanted.revision 2.7.0
 *
 */
public class CalcClassPathJarsMd5 {

	List<Pair<String, String>> listJarMd5Pairs;

	/**
	 * 
	 * @return a list with pairs of jar paths and MD5s.
	 * 
	 * @vanted.revision 2.7.0 Update File API
	 */
	public static List<Pair<String, String>> getJarMd5Pairs() {
		List<Pair<String, String>> listJarMd5Pairs = new ArrayList<Pair<String, String>>();
		try {
			MessageDigest.getInstance("MD5");
			Path jarpath;
			for (String jarString : getClasspathEntries()) {
				
				try (InputStream is = Files.newInputStream(jarpath = Paths.get(jarString),
						StandardOpenOption.READ)) {
					String md5 = DigestUtils.md5Hex(Files.readAllBytes(jarpath));
					listJarMd5Pairs.add(new Pair<String, String>(jarString, md5));
				} catch (SecurityException | IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			ErrorMsg.addErrorMessage(e);
		}

		return listJarMd5Pairs;
	}

	/**
	 * 
	 * @return the paths of the JARs in the classpath as strings
	 * @since 2.7.0
	 */
	public static String[] getClasspathEntries() {
		return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
	}
}
