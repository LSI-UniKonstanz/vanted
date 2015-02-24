package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.graffiti.editor.MainFrame;


/**
 * Dialog zur Eingabe der Array Dimensionen 
 * @author Torsten
 * Wird beim Druck auf den Button 'set dimensions' im VariableSelectionDialog erzeugt
 */
public class RArrayDimensionDialog implements ActionListener{

	static private JDialog dimDia;
	static private boolean ok = false;
	
	static ArrayList<JTextField> dimensionTF;
	static JPanel textfields;
	static ArrayList<Integer> ret;
	
	public RArrayDimensionDialog(){}
	
	static ArrayList<Integer> showRArrayDimensionDialog()
	{
		dimDia = new JDialog(MainFrame.getInstance(), "new R variable", true);
	
		ActionListener al = new RArrayDimensionDialog();
		
		dimDia.setLayout(new BorderLayout());
		dimDia.setLocationRelativeTo(MainFrame.getInstance());

		JLabel topText= new JLabel("<html>Please set the new dimensions of the array."
				+"<br>(older data will be lost)");
		
		JButton lessButton = new JButton("<<");
		lessButton.setActionCommand("less");
		lessButton.addActionListener(al);
		
		JButton moreButton = new JButton(">>");
		moreButton.setActionCommand("more");
		moreButton.addActionListener(al);
		
		dimensionTF = new ArrayList<JTextField>();
		
		JTextField tmpTF = new JTextField();
		tmpTF.setText("1");
		tmpTF.setPreferredSize(new Dimension(30,25));
		
		dimensionTF.add(tmpTF);
		
		textfields = new JPanel(new FlowLayout());
		adjustTFNumber();
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(al);
		
		JButton cancButton = new JButton("Cancel");
		cancButton.setActionCommand("cancel");
		cancButton.addActionListener(al);
		
		JPanel cen = new JPanel(new GridLayout(2,1));
		JPanel cenTop = new JPanel(new FlowLayout());
		cenTop.add(lessButton);
		cenTop.add(moreButton);
		
		cen.add(cenTop);
		cen.add(textfields);
		
		JPanel bot = new JPanel(new FlowLayout());
		bot.add(okButton);
		bot.add(cancButton);
		
		dimDia.add(topText, BorderLayout.NORTH);
		dimDia.add(cen, BorderLayout.CENTER);
		dimDia.add(bot, BorderLayout.SOUTH);
		dimDia.pack();
		dimDia.setResizable(false);
		dimDia.setVisible(true);
		
		if(ok)
			return ret;
		else
			return null;
	}
	
	static private void adjustTFNumber()
	{
		textfields.removeAll();
		textfields.add(dimensionTF.get(0));
		for(int i = 1; i < dimensionTF.size(); i++)
		{
			textfields.add(new JLabel("x"));
			textfields.add(dimensionTF.get(i));
		}
		textfields.revalidate();
		dimDia.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals("ok"))
		{
			long number = 0;
			try
			{
				ret = new ArrayList<Integer>();
				for(JTextField tf : dimensionTF)
				{
					ret.add(Integer.parseInt(tf.getText()));
				}
				number = 1;
				for(Integer i : ret)
				{
					
					number *= i;
					if(i > 1000 || i < 1)
					{
						throw new IllegalArgumentException();
					}
				}
				if(number > 1000000)
					throw new OutOfMemoryError();
				System.out.println(number);
				ok = true;
				dimDia.dispose();
			}
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "All textfields have to contain integers!", "Warning", JOptionPane.WARNING_MESSAGE);
			}
			catch(IllegalArgumentException ex)
			{
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Please choose values between 1 and 1000!", "Warning", JOptionPane.WARNING_MESSAGE);
			}
			catch(OutOfMemoryError ex)
			{
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Multiplied the dimensions must be under 1,000,000!<br>You're trying to apply an array with "+number+" elements.", "Warning", JOptionPane.WARNING_MESSAGE);				
			}
		}
		else if(e.getActionCommand().equals("cancel"))
		{
			dimDia.dispose();
		}
		else if(e.getActionCommand().equals("less") && dimensionTF.size() > 1)
		{
			dimensionTF.remove(dimensionTF.size()-1);
			adjustTFNumber();
		}
		else if(e.getActionCommand().equals("more") && dimensionTF.size() < 5)
		{
			JTextField tmpTF = new JTextField();
			tmpTF.setText("1");
			tmpTF.setPreferredSize(new Dimension(30, 25));
			dimensionTF.add(tmpTF);
			adjustTFNumber();
		}
		
		
	}
}
