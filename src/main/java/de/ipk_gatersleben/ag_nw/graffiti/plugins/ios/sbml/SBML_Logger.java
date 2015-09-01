/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;


/**
 * Special logging wrapper or SBML errors
 * 
 * currently mutes the errors thrown by the SBML Reader
 * Later it'll be integrated in the new notification-framework
 * @author matthiak
 *
 */
public class SBML_Logger{

	/**
	 * dummy method, catching errors
	 * @param errorMsg
	 */
	public synchronized static void addErrorMessage(String errorMsg) {
		
	}

	/**
	 * dummy method, catching errors
	 * @param errorMsg
	 */
	public static void addErrorMessage(Exception e) {
		
	}
}
