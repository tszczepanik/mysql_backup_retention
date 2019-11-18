package logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class config {


	Properties prop = new Properties();
//	String fileName = "EscribaRetention.config";
	 
	   
public config (String SPath) throws IOException
{
	   
    	
		InputStream is = null;
		System.out.println(SPath);
		is = new FileInputStream(SPath);
		
		prop.load(is);
	
	}

public String getSProperty ( String P)
{
	
	String S = prop.getProperty("esc."+P);
	return S;
}

}
