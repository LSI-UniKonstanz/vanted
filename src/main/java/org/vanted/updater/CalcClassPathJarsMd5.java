package org.vanted.updater;

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

import org.ErrorMsg;
import org.apache.commons.codec.digest.DigestUtils;
import org.graffiti.util.Pair;

/**
 * Calculates all MD5 sums for all the jars found on the classpath of the
 * ClassLoader for VANTED, as provided by the bootstrap.
 * This is used for the update mechanism to compare the MD5s of remote libraries
 * and copy them if they differ in their MD5s.
 * 
 * @author matthiak, Dimitar Garkov
 * @vanted.revision 2.7.1
 */
public class CalcClassPathJarsMd5 {
	
	List<Pair<String, String>> listJarMd5Pairs;
	
	/**
	 * @return a list with pairs of jar paths and MD5s.
	 * @vanted.revision 2.7.1 Fix regression w.r.t. ClassLoder <s>2.7.0 Update File
	 *                  API</s>
	 */
	public static List<Pair<String, String>> getJarMd5Pairs() {
		List<Pair<String, String>> listJarMd5Pairs = new ArrayList<Pair<String, String>>();
		try {
			MessageDigest.getInstance("MD5");
			URLClassLoader bootstrapLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
			for (URL jarurl : bootstrapLoader.getURLs()) {
				String md5 = DigestUtils.md5Hex(Files.readAllBytes(Paths.get(jarurl.toURI())));
				listJarMd5Pairs.add(new Pair<String, String>(jarurl.toURI().toString(), md5));
			}
		} catch (NoSuchAlgorithmException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			// During development there is no bootstrap, hence no libraries, core can be
			// loaded, so do not check for updates
			listJarMd5Pairs = null;
		}
		
		return listJarMd5Pairs;
	}
}
