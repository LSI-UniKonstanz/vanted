/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.services.network.BroadCastService;

/**
 * Represents the tab, which contains the functionality to edit the attributes
 * of the current graph object.
 * 
 * @version $Revision$
 * @vanted.revision 2.7.0
 */
public class TabAglet extends InspectorTab implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3946464315024239246L;
	
	public static String newline = System.getProperty("line.separator");
	
	private Timer networkBroadCast;
	private BroadCastService broadCastService = new BroadCastService(9900, 9910, 1000);
	private BroadCastTask broadCastTask;
	
	private JCheckBox runService = new JCheckBox("Allow Network Broadcast (udp-port " + broadCastService.getStartPort()
			+ "-" + broadCastService.getEndPort() + ")", false);
	private JLabel myStatusLabel = new JLabel();
	private JTextArea myDataIn = new JTextArea();
	private JTextField inputField = new JTextField();
	private JButton sendButton = new JButton("Send");
	
	private javax.swing.Timer updateNetStatus;
	
	private void initComponents() {
		double border = 2;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, 30,
						border } }; // Rows
		this.setLayout(new TableLayout(size));
		
		final TabAglet thisTabAglet = this;
		
		runService.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox src = (JCheckBox) e.getSource();
				if (src.isSelected()) {
					networkBroadCast = new Timer("Aglet Network Broadcast");
					broadCastTask = new BroadCastTask(broadCastService, thisTabAglet);
					networkBroadCast.schedule(broadCastTask, 0, BroadCastTask.timeDefaultBeatTime);
					sendButton.setEnabled(true);
				} else {
					broadCastTask.cancel();
					networkBroadCast.cancel();
					sendButton.setEnabled(false);
				}
				updateNetworkStatus();
			}
		});
		
		this.add(runService, "1,1");
		this.add(myStatusLabel, "1,2");
		this.add(myDataIn, "1,3");
		this.add(TableLayout.getSplit(inputField, sendButton, TableLayoutConstants.FILL, 80), "1,4");
		sendButton.setMnemonic('S');
		sendButton.setEnabled(false);
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = inputField.getText();
				inputField.setText("");
				inputField.requestFocusInWindow();
				broadCastTask.addMessageToBeSent(msg);
				updateNetworkStatus();
			}
		});
		
		myDataIn.setBackground(new Color(230, 230, 255));
		
		updateNetStatus = new javax.swing.Timer(3000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateNetworkStatus();
			}
		});
		updateNetStatus.start();
		
		this.revalidate();
	}
	
	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabAglet() {
		super();
		this.title = "Chat";
		initComponents();
	}
	
	public void run() {
		if (broadCastTask == null)
			return;
		ArrayList<?> inMessages = broadCastTask.getInMessages();
		for (Iterator<?> it = inMessages.iterator(); it.hasNext();) {
			String curText = myDataIn.getText();
			String msg;
			try {
				msg = new String((byte[]) it.next(), "UTF-8");
				myDataIn.setText(msg + newline + curText);
				MainFrame.showMessage("<html><b>Incoming Broadcast Chat Message:</b> " + msg,
						MessageType.PERMANENT_INFO);
			} catch (UnsupportedEncodingException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		updateNetworkStatus();
	}
	
	private void updateNetworkStatus() {
		if (broadCastTask == null) {
			myStatusLabel.setText("<html><small>Network functions are disabled");
		} else {
			List<?> hosts = broadCastTask.getActiveHosts();
			String hostList = "";
			for (Iterator<?> it = hosts.iterator(); it.hasNext();) {
				String name = it.next().toString();
				if (name.startsWith("/"))
					name = name.substring(1);
				hostList += ", " + name;
			}
			if (hostList.length() < 2)
				hostList = "no hosts found";
			else
				hostList = hostList.substring(2);
			String netW;
			if (runService.isSelected())
				netW = "Broadcast enabled";
			else
				netW = "Broadcast disabled";
			
			myStatusLabel.setText(
					"<html><small>" + netW + " (in/out/other in, listener-port): " + broadCastService.getInCount() + "/"
							+ broadCastService.getOutCount() + "/" + broadCastService.getOtherInCount() + ", "
							+ broadCastService.getBindPort() + "<br>Active Hosts (" + hosts.size() + "): " + hostList);
		}
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
}
