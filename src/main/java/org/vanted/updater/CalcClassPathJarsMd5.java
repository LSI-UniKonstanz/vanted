package org.vanted.updater;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ErrorMsg;
import org.apache.commons.codec.digest.DigestUtils;
import org.graffiti.util.Pair;

/**
 * Calculates all MD5 sums for all the jars found on the classpath of the
 * ClassLoader for VANTED, as provided by the bootstrap.
 * 
 * This is used for the update mechanism to compare the MD5s of remote libraries
 * and copy them if they differ in their MD5s.
 * 
 * @author matthiak, Dimitar Garkov
 * @vanted.revision 2.7.1
 *
 */
public class CalcClassPathJarsMd5 {

	List<Pair<String, String>> listJarMd5Pairs;

	/**
	 * 
	 * @return a list with pairs of jar paths and MD5s.
	 * 
	 * @vanted.revision 2.7.1 Fix regression w.r.t. ClassLoder
	 * <s>2.7.0 Update File API</s>
	 */
	public static List<Pair<String, String>> getJarMd5Pairs() {
		List<Pair<String, String>> listJarMd5Pairs = new ArrayList<Pair<String, String>>();
		try {
			MessageDigest.getInstance("MD5");
//			File dirLibs = new File("C:" + File.separator + "Program Files" + File.separator + "Vanted" + File.separator
//					+ "core-libs" + File.separator);
//			File coreLibs = new File("C:" + File.separator + "Program Files" + File.separator + "Vanted"
//					+ File.separator + "vanted-core" + File.separator);
//			for (Object obj : ArrayUtils.addAll(dirLibs.listFiles(), coreLibs.listFiles())) {
//				File jar = (File) obj;
			for (URL jar : Collections.list(Thread.currentThread().getContextClassLoader().getResources(""))) {
				String md5 = DigestUtils.md5Hex(Files.readAllBytes(Paths.get(jar.toURI())));
				listJarMd5Pairs.add(new Pair<String, String>(jar.toURI().toString(), md5));
			}
		} catch (NoSuchAlgorithmException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
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
