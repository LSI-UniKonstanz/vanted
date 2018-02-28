package org.vanted.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.graffiti.util.Pair;

/**
 * Calculates all MD5 sums for all the jars found on the classpath of the
 * URLClassLoader for VANTED
 * 
 * This is used for the update mechanism to compare md5 of remote libraries and
 * copy them if they differ in their MD5
 * 
 * @author matthiak
 *
 */
public class CalcClassPathJarsMd5 {

	List<Pair<String, String>> listJarMd5Pairs;

	public static List<Pair<String, String>> getJarMd5Pairs() {
		List<Pair<String, String>> listJarMd5Pairs = new ArrayList<Pair<String, String>>();

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			if (CalcClassPathJarsMd5.class.getClassLoader() instanceof URLClassLoader) {
				URLClassLoader cl = (URLClassLoader) CalcClassPathJarsMd5.class.getClassLoader();
				for (URL jarurl : cl.getURLs()) {
					try {
						FileInputStream fis = new FileInputStream(new File(jarurl.toURI()));
						fis.available();
						byte[] readAllBytes = Files.readAllBytes(Paths.get(jarurl.toURI()));
						String md5 = DigestUtils.md5Hex(readAllBytes);

						listJarMd5Pairs.add(new Pair<String, String>(jarurl.toURI().getPath(), md5));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listJarMd5Pairs;
	}

}
