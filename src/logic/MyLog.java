package logic;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



public class MyLog
{
	
	static {
	      System.setProperty("java.util.logging.SimpleFormatter.format",
	              "[%1$tF %1$tT] [%4$-7s] %5$s %n");
	}
	
	boolean flag=false;
	private static final Logger LOGGER = Logger.getLogger(MyLog.class.getName());
	
	
	      
	 Handler consoleHandler = null;
     Handler fileHandler  = null;
     SimpleFormatter simpleFormatter = null;
     
	public MyLog ( boolean vflag, String LogPath) throws SecurityException, IOException
	{
		flag=vflag;
		System.out.println("Debug is : "+flag );
		
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
		 LocalDateTime now = LocalDateTime.now();  
		
		 String LogFileName = "EscReten-"+dtf.format(now)+".log";
		   
		
		String PathfileName = LogPath+LogFileName;
		
		consoleHandler = new ConsoleHandler();
        // Creating SimpleFormatter
        simpleFormatter = new SimpleFormatter( );
        // Setting formatter to the handler
       
      //  java.util.logging.SimpleFormatter.format='%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n';
        //%1$tF %1$tT
        		
        
   
        		
       
        
         
        fileHandler  = new FileHandler(PathfileName);
        fileHandler.setFormatter(simpleFormatter);
        
        // Setting Level to ALL
        fileHandler.setLevel(Level.ALL);
        
        LOGGER.addHandler(fileHandler);
        
        LOGGER.setLevel(Level.ALL);
        
        
        
	}
	public void printLog(String S , int t)
	{
		switch (t)
		{
			case 0:
				if ( flag ) { LOGGER.log(Level.INFO, "[DEBUG] "+S) ; } 
				break;
		    case 1:
		    	LOGGER.log(Level.INFO, S); 
		    	break;
		    case 2:
		    	LOGGER.log(Level.WARNING, S); 
		    	break;
		    case 3:
		    	LOGGER.log(Level.SEVERE, S);
		    	break;
		    default:
		    	LOGGER.log(Level.INFO, S); 
		}
			
		
	} 
			    	
}
