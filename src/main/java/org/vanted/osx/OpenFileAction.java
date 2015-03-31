package org.vanted.osx;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;

public abstract class OpenFileAction extends AbstractAction {
	public abstract void openFiles(List<File> listFiles);

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}