package de.ipk_gatersleben.ag_nw.graffiti.services.R;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import org.ReleaseInfo;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;



//TODO: Support für andere Betriebssysteme als Windows
//Ich kenne mich abseits von Windows leider zu wenig aus, um die Pfade zu den Programmen zu wissen, bzw. was man beachten muss
//Zumindest der Process-Kill für Linux basierte Systeme sollte so klappen, zumindest laut Internet
//TODO: Abfragen, ob R bzw. RServe tatsächlich installiert sind (bzw. automatischer Download?)
public class RService {
	
	static private StartStopRserve ssrs;

	static private String rdir;
	static private boolean RserveReady = false;
	static private Object waitingObject;
	static private String osName;
	
	RConnection connection;
	
	public RService()
	{
		openRserve();

		try {
			connection = new RConnection();
		} catch (RserveException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Zuweisung. Funktioniert wie "name <- values"
	 * @param name: Variablenname
	 * @param values: Werte die der Variable zugeordnet werden
	 */
	public void assign(String name, String values)
	{
		try {
			connection.assign(name, values);
		} catch (RserveException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Zuweisung. Funktioniert wie "name <- values"
	 * @param name: Variablenname
	 * @param values: Werte die der Variable zugeordnet werden
	 */
	public void assign(String name, int[] values)
	{
		try {
			connection.assign(name, values);
		} catch (REngineException e) {
			e.printStackTrace();
		}				
	}
	
	/**
	 * Zuweisung. Funktioniert wie "name <- values"
	 * @param name: Variablenname
	 * @param values: Werte die der Variable zugeordnet werden
	 */
	public void assign(String name, double[] values)
	{
		try {
			connection.assign(name, values);
		} catch (REngineException e) {
			e.printStackTrace();
		}				
	}
	
	/**
	 * Zuweisung. Funktioniert wie "name <- values"
	 * @param name: Variablenname
	 * @param values: Werte die der Variable zugeordnet werden
	 */
	public void assign(String name, byte[] values)
	{
		try {
			connection.assign(name, values);
		} catch (REngineException e) {
			e.printStackTrace();
		}				
	}
	
	/**
	 * Zuweisung. Funktioniert wie "name <- values"
	 * @param name: Variablenname
	 * @param values: Werte die der Variable zugeordnet werden
	 */
	public void assign(String name, REXP values)
	{
		try {
			connection.assign(name, values);
		} catch (RserveException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Zuweisung. Funktioniert wie "name <- values"
	 * @param name: Variablenname
	 * @param values: Werte die der Variable zugeordnet werden
	 */
	public void assign(String name, String[] values)
	{
		try {
			connection.assign(name, values);
		} catch (REngineException e) {
			e.printStackTrace();
		}		
	}
	
	public void assign(String name, REXP arg1, REXP arg2)
	{
		try {
			connection.assign(name, arg1, arg2);
		} catch (REngineException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Ausführung des R-Ausdrucks expression
	 * @param expression: R-Ausdruck
	 * @return
	 */
	public REXP eval(String expression)
	{
		try {
			return connection.eval(expression);
		} catch (RserveException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Ausführung des R-Ausdrucks expression und bei Fehler Rückgabe desselben
	 * @param expression: R-Ausdruck
	 * @return
	 */
   public REXP safeEval( String expression) throws RserveException,
    RException, REXPMismatchException {
    	if(expression.contains("#"))
    		expression = expression.substring(0,expression.indexOf("#"));
	    REXP r = connection.eval("try({" + expression + "}, silent=TRUE)");
	    if (r.inherits("try-error")) throw new RException(r.asString());
	    return r;
    }
	
   /**
    * Abfrage der Variable name
    * @param name: Name einer Variable in R
    * @return
    */
    public String[] getValueAsStrings(String name)
    {	
    	try {
			return eval(name).asStrings();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * Abfrage der Variable name
     * @param name: Name einer Variable in R
     * @return
     */
    public int[] getValueAsIntegers(String name)
    {
    	try {
			return eval(name).asIntegers();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;    	
    }
    
    /**
     * Abfrage der Variable name
     * @param name: Name einer Variable in R
     * @return
     */
    public byte[] getValueAsBytes(String name)
    {
    	try {
			return eval(name).asBytes();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;    	
    }

    /**
     * Abfrage der Variable name
     * @param name: Name einer Variable in R
     * @return
     */
    public double[] getValueAsDoubles(String name)
    {
    	try {
			return eval(name).asDoubles();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;    	
    }

    /**
     * Abfrage der Variable name
     * @param name: Name einer Variable in R
     * @return
     */
    public double[][] getValueAsDoubleMatrix(String name)
    {
    	try {
			return eval(name).asDoubleMatrix();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;    	
    }

    /**
     * Abfrage der Variable name
     * @param name: Name einer Variable in R
     * @return
     */
    public REXP getValue(String name)
    {
    	return eval(name);
    }
        
    private void openRserve() 
    {
    	waitingObject = new Object();
    	ssrs = new StartStopRserve(waitingObject);
    	ssrs.start();
    	synchronized(waitingObject)
    	{
	    	try {
				waitingObject.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    } 
    
	static public boolean isRserveReady()
	{
		return RserveReady;
	}
		
	static public void closeRserve()
	{
        /* Windows */
        if (osName.toLowerCase().contains("windows"))
        {
        	System.out.println("Shutting down Windows OS process(es) Rserve...");
            try {
            	Runtime.getRuntime().exec("taskkill /F /IM rserve.exe /T");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        /* Other OS than Windows. Must be Linux-based. Otherwise,
         * an exception is thrown.
         */
        else
        {
            System.out.println("Shutting down Linux OS process(es) Rserve...");
            try {
				Runtime.getRuntime().exec("kill `ps -ef | grep -i rserve | grep -v grep | awk '{print $2}'`");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}

	class StartStopRserve extends Thread{
		
		public StartStopRserve(Object waitObj){
			waitingObject = waitObj;
			Properties prop = System.getProperties( );
			osName =  prop.getProperty( "os.name" );
		}
		public void run()
		{	
			if(osName.toLowerCase().contains("windows"))
			{
				try
				{
					ProcessBuilder pb = new ProcessBuilder("reg", "query", "\"HKEY_LOCAL_MACHINE\\SOFTWARE\\R-Core\\R\"", "/v", "InstallPath");
					Process p = pb.start();
					Scanner scanner = new Scanner(p.getInputStream()).useDelimiter("    \\w+\\s+\\w+\\s+");
					scanner.next();
					rdir = scanner.next();
					rdir = rdir.trim() +  "\\bin\\";
				}
				catch(Exception e)
				{
					
				}
			}
			//TODO: Linux & Co (R Installation finden, das Ausführen an sich sollte dann wieder funktionieren (siehe unten))
			
			
		   	try {
		   		File f = new File(ReleaseInfo.getAppFolderWithFinalSep()+"openRserveAndQuit.R");
		   		if(!f.exists())
		   		{
					BufferedWriter out = new BufferedWriter(new FileWriter(
							ReleaseInfo.getAppFolderWithFinalSep() + "openRserveAndQuit.R"));
					out.write("library(Rserve)\nRserve()\nq()");
					out.close();
		   		}
		   		ProcessBuilder pb = new ProcessBuilder(rdir+"R", "--file="+ReleaseInfo.getAppFolderWithFinalSep()+"openRserveAndQuit.R");
		   		Process p = pb.start();
		   		Scanner scanner = new Scanner(p.getInputStream());
		   		while(scanner.hasNext())
		   		{
		   			if(scanner.next().equals("q()"))
		   			{
		   				synchronized(waitingObject)
		   				{
		   					waitingObject.notify();
		   				}
		   			}
		   		}
		    }
		    catch (Exception e ) {
		    }		
		}
	}
}