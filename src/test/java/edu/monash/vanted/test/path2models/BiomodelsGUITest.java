/**
 * 
 */
package edu.monash.vanted.test.path2models;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.biomodels.BiomodelsPanel;

/**
 * @author matthiak
 */
public class BiomodelsGUITest extends JFrame {
	
	BiomodelsPanel panel;
	
	/**
	 * 
	 */
	public BiomodelsGUITest() {
		
		panel = new BiomodelsPanel();
		
		setSize(300, 400);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new BiomodelsGUITest();
	}
	
}
