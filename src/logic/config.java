package logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class config {


	Properties prop = new Properties();
	String fileName = "EscribaRetention.config";
	 
	   
public config () throws IOException
{
	  File dir1 = new File (".");
	//   System.out.println ("Current dir : " + dir1.getCanonicalPath());
	   
    	
		InputStream is = null;
		is = new FileInputStream(dir1.getCanonicalPath()+"\\"+fileName);
		
		prop.load(is);
	
	}

public String getSProperty ( String P)
{
	
	String S = prop.getProperty("esc."+P);
	return S;
}
}
