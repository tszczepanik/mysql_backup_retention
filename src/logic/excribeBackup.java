package logic;


import java.io.IOException;
import java.sql.*;  

public class excribeBackup {

	static ResultSet rsAvailBackups;
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException 
	{
		
		
		
		RetentionLogic R= null;
	
	    if ( args.length == 0 )
	    {
	    	System.out.println("Erro caminho para o arquivode configuracao nao encontrado: ");
	    	System.out.println("Ex para o Windows : usando -> C:\temp\\  chamada ->  java -jar escribinha.jar c:\temp\\" );
	    	System.out.println("Ex para o Linux : usando /tmp/ chamda -> java -jar escribinha.jar /tmp/   " );
			System.exit(1);		
	    	
	    }
		String ConfigPath = args[0];
		R = new RetentionLogic(ConfigPath);
		R.CheckBackups();
		
	}

}
