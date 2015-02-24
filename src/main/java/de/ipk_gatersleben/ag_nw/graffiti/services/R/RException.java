package de.ipk_gatersleben.ag_nw.graffiti.services.R;

public class RException extends Exception {
	private static final long serialVersionUID = 1L;
	public RException(String msg) 
		{ super("R error: \""+msg+"\""); } 
}