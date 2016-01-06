/**
 * 
 */
package org.vanted.updater;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.graffiti.editor.MainFrame;

/**
 * @author matthiak
 */
public class UpdateMessageDialog extends JDialog {
	
	int width = 200;
	int height = 300;
	
	String headerText;
	String messageText;
	String messageText2;
	String footerText;
	
	/**
	 * Creates and shows the Update message dialog
	 * with a given message formatted in HTML
	 */
	private UpdateMessageDialog(String header, String htmlMessage, String htmlMessage2, String footer) {
		
		super(MainFrame.getInstance(), true);
		
		this.headerText = header;
		this.messageText = htmlMessage;
		this.messageText2 = htmlMessage2;
		this.footerText = footer;
		
		createUI();
		
		addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				dispose();
			}
		});
		
		pack();
		setLocationRelativeTo(MainFrame.getInstance());
		setResizable(false);
		setVisible(true);
	}
	
	public static void showUpdateMessageDialog(String header, String htmlMessage, String htmlMessage2, String footer) {
		new UpdateMessageDialog(header, htmlMessage, htmlMessage2, footer);
	}
	
	/**
	 * 
	 */
	private void createUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new TableLayout(new double[][] {
				{
						TableLayoutConstants.FILL,
						TableLayoutConstants.PREFERRED,
						TableLayoutConstants.FILL,
		},
				{
						TableLayoutConstants.PREFERRED, //header
						5,
						TableLayoutConstants.PREFERRED, //html message
						5,
						TableLayoutConstants.PREFERRED, //html message 2
						5,
						TableLayoutConstants.PREFERRED, //footer
						5,
						TableLayoutConstants.MINIMUM //Button
		}
		}));
		
		JLabel header = new JLabel();
		header.setText("<html><strong>" + headerText);
//		header.setSize(width, 50);
		
		/*
		JTextPane message = new JTextPane();
		message.setContentType("text/html");
		message.setEditable(false);
		message.setText(messageText);
		JScrollPane spane = new JScrollPane(message, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spane.setPreferredSize(new Dimension(300, 100));
		*/
		JLabel msg = new JLabel();
		msg.setText("<html>" + messageText);
		
		JTextPane message2 = new JTextPane();
		message2.setContentType("text/html");
		message2.setEditable(false);
		message2.setText(messageText2);
		JScrollPane spane2 = new JScrollPane(message2, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spane2.setPreferredSize(new Dimension(300, 200));
		
		JLabel footer = new JLabel();
		footer.setText("<html>" + footerText);
		
		JButton button = new JButton("OK");
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		panel.add(header, new TableLayoutConstraints(0, 0, 2, 0, TableLayoutConstraints.MAX_ALIGN, TableLayoutConstraints.MAX_ALIGN));
		panel.add(msg, new TableLayoutConstraints(0, 2, 2, 2, TableLayoutConstraints.MAX_ALIGN, TableLayoutConstraints.MAX_ALIGN));
		if(messageText2 != null && !messageText2.isEmpty())
			panel.add(spane2, new TableLayoutConstraints(0, 4, 2, 4, TableLayoutConstraints.MAX_ALIGN, TableLayoutConstraints.MAX_ALIGN));
		panel.add(footer, new TableLayoutConstraints(0, 6, 2, 6, TableLayoutConstraints.MAX_ALIGN, TableLayoutConstraints.MAX_ALIGN));
		panel.add(button, new TableLayoutConstraints(1, 8, 1, 8, TableLayoutConstraints.CENTER, TableLayoutConstraints.CENTER));
		
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		getContentPane().add(panel);
	}
	
	/**
	 * test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		showUpdateMessageDialog(
				"<html>My Header with a long thing and <br/>more messages",
				"<html>my<strong>html</strong><br/>message",
				"<html>Changelog. my<strong>html</strong><br/>message",
				"my footer");
	}
}
