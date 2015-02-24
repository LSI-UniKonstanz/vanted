package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.graffiti.editor.MainFrame;

/**
 * Dialog zur Erstellung neuer Variablen
 * @author Torsten
 * wird im RTab auf den Button add Variable geclickt, �ffnet sich dieser Dialog
 */
public class newRVariableDialog implements ActionListener{

	static JDialog newVarDialog;
	static JTextField nameField;
	static boolean ok = false;
	
	newRVariableDialog(){}
	
	static RVariable showNewVariableDialog()
//    TODO: Output-Variablen
	//	static RVariable showNewVariableDialog(String inout)
	{
		newVarDialog = new JDialog(MainFrame.getInstance(), "new R variable", true);
		newVarDialog.setLayout(new BorderLayout());
		newVarDialog.setLocationRelativeTo(MainFrame.getInstance());
		JPanel cp = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel l = new JLabel("Name");
		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
		cp.add(l,gbc);
		
		l = new JLabel("Type");
		gbc.gridy = 1;
		cp.add(l,gbc);
		
		l = new JLabel("Description");
		gbc.gridy = 2;
		cp.add(l,gbc);
		
		nameField = new JTextField();
		nameField.setPreferredSize(new Dimension(300,25));
		gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
		cp.add(nameField,gbc);
		
		JComboBox typeField = new JComboBox(new String[] {"vector", "list", "factor", "matrix", "array", "data.frame"});
		gbc.gridy = 1;
		cp.add(typeField,gbc);
		
		JTextField descriptionField = new JTextField();
		descriptionField.setPreferredSize(new Dimension(300,25));
		gbc.gridy = 2;
		cp.add(descriptionField,gbc);
		
		JButton okButton = new JButton("OK");
		ActionListener al = new newRVariableDialog();
		okButton.setActionCommand("ok");
		okButton.addActionListener(al);
		
		JButton cancButton = new JButton("Cancel");
		cancButton.setActionCommand("cancel");
		cancButton.addActionListener(al);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(okButton);
		buttonPanel.add(cancButton);
		gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; 
		gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
		cp.add(buttonPanel,gbc);
		
		newVarDialog.add(cp, BorderLayout.CENTER);
		newVarDialog.pack();
		newVarDialog.setResizable(false);
		newVarDialog.setVisible(true);

		if(ok)
		{
			
			ok = false;
			return new RVariable(nameField.getText(),(String)typeField.getSelectedItem(), descriptionField.getText());
//            TODO: Output-Variablen
//			return new RVariable(nameField.getText(),(String)typeField.getSelectedItem(),inout, descriptionField.getText());	
		}
		return null;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("ok"))
		{
			if(nameField.getText().length() > 0)
			{
				ok = true;
				newVarDialog.dispose();
			}
			else
				JOptionPane.showMessageDialog(newVarDialog, "The name field has to be filled!", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		if(e.getActionCommand().equals("cancel"))
			newVarDialog.dispose();
	}

}
