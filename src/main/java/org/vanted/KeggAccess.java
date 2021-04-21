package org.vanted;

import java.io.File;

import javax.swing.JOptionPane;

import org.ErrorMsg;
import org.ReleaseInfo;

/**
 * Handle KEGG Access with regards to the user.
 * 
 * @author Dímitar Garkov
 * @since 2.8.0
 * @version 3.0
 */
public final class KeggAccess {
	
	private static final String TITLE = "VANTED Features Initialisation";
	
	/**
	 * Static use only.
	 */
	private KeggAccess() {
	}
	
	/**
	 * Show the message dialog for enabling KEGG access by accepting the license.
	 * 
	 * @return true, if KEGG access should be enabled.
	 */
	public static boolean doEnableKEGGaskUser() {
		return askForEnablingKEGG() == JOptionPane.YES_OPTION;
	}
	
	/**
	 * Build 3 message dialogs to explain and enable/disable KEGG access.
	 * 
	 * @return See {@link JOptionPane#YES_OPTION}, {@link JOptionPane#NO_OPTION}
	 */
	private static int askForEnablingKEGG() {
		JOptionPane.showMessageDialog(null, "<html><h3>KEGG License Status Evaluation</h3>"
				+ "While VANTED is available as an academic research tool at no cost for commercial and non-commercial use,&emsp;&emsp;<br>"
				+ "for using KEGG-related functions, it is necessary all users to adhere to the KEGG license. To use VANTED, you&emsp;&emsp;<br>"
				+ "need to also be aware of the remaining license information, listed at VANTED's About (F1) and on the&emsp;&emsp;<br>"
				+ "VANTED website (www.vanted.org).&emsp;&emsp;<br><br>"
				+ "VANTED does not distribute information from KEGG, however, it contains functionality for the online-access&emsp;&emsp;<br>"
				+ "of information from the KEGG website.&emsp;&emsp;<br><br>"
				+ "<b>Before these functions can be enabled, you should carefully read the following license information and&emsp;&emsp;<br>"
				+ "decide for you on using the KEGG-related functions in VANTED. If you choose not to, all other features&emsp;&emsp;<br>"
				+ "of VANTED are still fully available to you.&emsp;&emsp;<br><br>", TITLE,
				JOptionPane.INFORMATION_MESSAGE);
		
		JOptionPane.showMessageDialog(null, "<html><h3>KEGG License Status Evaluation</h3>" + getKEGGLicense(), TITLE,
				JOptionPane.INFORMATION_MESSAGE);
		
		int result = JOptionPane.showConfirmDialog(null, "<html><h3>Enable KEGG functions?", TITLE,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return result;
	}
	
	/**
	 * Whether the KEGG License agreement has been accepted by the user.
	 * 
	 * @return true, if the correct file exists.
	 */
	public static boolean isKEGGAccepted() {
		boolean isAccepted = false;
		try {
			if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
				isAccepted = true;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		return isAccepted;
	}
	
	/**
	 * Create a new empty file, indicating the state of the KEGG license agreement.
	 * 
	 * @param state
	 *           either "accepted" or "rejected"
	 */
	public static void createLicenseFile(String state) {
		try {
			new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_" + state).createNewFile();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/**
	 * Deletes any existing file, indicating the state of the KEGG license
	 * agreement.
	 * 
	 * @param state
	 *           either "accepted" or "rejected"
	 */
	public static void deleteLicenseFile(String state) {
		try {
			if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_" + state).exists())
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_" + state).delete();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/**
	 * Full license available at http://www.genome.jp/kegg/legal.html
	 * 
	 * @return the HTML-adjusted text of the license
	 */
	public static String getKEGGLicense() {
		return "<html>KEGG: Kyoto Encyclopedia of Genes and Genomes (Kanehisa Laboratory of Kyoto University Bioinformatics Center)<br>"
				+ "<small>(Changes to the license can occur, those are considered via http://www.genome.jp/kegg/legal.html)</small><br>"
				+ "<hr>" + "<h4>Copyright and Disclaimer</h4>"
				+ "<b>KEGG</b> is an original database product, copyright Kanehisa Laboratories.<br>"
				+ "Although best efforts are always applied when developing KEGG products, Kanehisa Laboratories makes no warrant<br>"
				+ "nor assumes any legal responsibility for the accuracy or usefulness of KEGG or any information contained therein.<br>"
				+ "<br>" + "<h4>Academic use of KEGG</h4>"
				+ "Academic users may freely use the KEGG website at https://www.kegg.jp or its mirror site at GenomeNet https://www.genome.jp/kegg.<br>"
				+ "Academic users who utilize KEGG for providing academic services are requested to obtain an academic service provider license,<br>"
				+ "which is included in the KEGG FTP academic subscription (https://www.pathway.jp/en/academic.html) for organizational use.<br>"
				+ "The KEGG FTP academic subscription, which is a paid service (see https://www.genome.jp/kegg/docs/plea.html), may also be<br>"
				+ "obtained to conveniently download the entire KEGG database.<br>" + "<br>"
				+ "<h4>Non-academic use of KEGG</h4>"
				+ "Non-academic users must understand that KEGG is not a public database and non-academic use of KEGG generally requires a<br>"
				+ "commercial license. There are two types of commercial licenses available: end user and business. The end user license includes<br>"
				+ "access rights to the FTP site and the website, while the business license includes access rights to the FTP site only. Please contact<br>"
				+ "Pathway Solutions (https://www.pathway.jp) for more details." + "<br><br>"
				+ "<h4>Reference website: http://www.genome.jp/kegg/legal.html</h4>" + "<br>";
	}
	
	/**
	 * For compatibility reasons, please do not use!
	 * 
	 * @return See {@linkplain askForEnablingKEGG}
	 */
	@Deprecated
	public static int _askForEnablingKEGG() {
		return askForEnablingKEGG();
	}
}
