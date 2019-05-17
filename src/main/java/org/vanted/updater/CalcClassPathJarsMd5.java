package org.vanted.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.AccessDeniedException;
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

			if (!(CalcClassPathJarsMd5.class.getClassLoader() instanceof URLClassLoader)) {
				ErrorMsg.addErrorMessage("Class loader is not an URLClassLoader. No update scan!");
				return listJarMd5Pairs;
			}

			URLClassLoader cl = (URLClassLoader) CalcClassPathJarsMd5.class.getClassLoader();
			for (URL jarurl : cl.getURLs()) {
				Path jarpath;
				try (InputStream is = Files.newInputStream(jarpath = Paths.get(jarurl.toURI()),
						StandardOpenOption.READ)) {
					String md5 = DigestUtils.md5Hex(Files.readAllBytes(jarpath));
					listJarMd5Pairs.add(new Pair<String, String>(jarpath.toString(), md5));
				} catch (AccessDeniedException ade) {
					// This exception happens only on Windows from Java 8 and not in production
					// (on /target/classes). Therefore, report only in console.
					ade.printStackTrace();
				} catch (SecurityException | URISyntaxException e) {
					ErrorMsg.addErrorMessage(e);
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			ErrorMsg.addErrorMessage(e);
		}

		return listJarMd5Pairs;
	}

}
