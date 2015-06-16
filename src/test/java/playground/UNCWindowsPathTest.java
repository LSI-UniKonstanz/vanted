/**
 * 
 */
package playground;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

/**
 * @author matthiak
 *
 */
public class UNCWindowsPathTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {


		byte[] filecache = null;
		for(String key : System.getenv().keySet())
			System.out.println(key + " : " + System.getenv(key));
		System.out.println("=========================");
		for(Object key : System.getProperties().keySet()) {
			System.out.println(key + " : " + System.getProperty(key.toString()));
		}
		JFileChooser chooser = new JFileChooser();
		int showOpenDialog = chooser.showOpenDialog(null);
		
		if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			System.out.println("selected file path: (toString)        " + selectedFile);
			System.out.println("selected file path: (getPath)         " + selectedFile.getPath());
			System.out.println("selected file path: (getAbsolutePath) " + selectedFile.getAbsolutePath());
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			byte[] buf = new byte[1000];
			
			FileInputStream fileInputStream = new FileInputStream(selectedFile);
			int length;
			while((length = fileInputStream.read(buf)) > 0) {
				bos.write(buf, 0, length);
			}
			fileInputStream.close();
			filecache = bos.toByteArray();
			System.out.println("filecache holds file with size: " + filecache.length);
		}
		
		if(filecache != null) {
			 chooser = new JFileChooser();
			 chooser.setSelectedFile(new File("lala"));
			 chooser.showSaveDialog(null);
			
			if(showOpenDialog == JFileChooser.APPROVE_OPTION) {
				
				File selectedFile = chooser.getSelectedFile();
				
				System.out.println("current directory: " + chooser.getCurrentDirectory());
				System.out.println("getName:  " + chooser.getName(selectedFile));
				System.out.println("selected save file path: " + selectedFile);
//				System.out.println("selected save file path: " + selectedFile.getCanonicalPath());
				FileOutputStream fos = new FileOutputStream(selectedFile);
				fos.write(filecache);
				fos.close();
				
				System.out.println("saved file to " + selectedFile);
			}
		}
	}

}
