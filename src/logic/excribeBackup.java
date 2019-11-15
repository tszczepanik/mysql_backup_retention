package logic;


import java.io.IOException;
import java.sql.*;  

public class excribeBackup {

	static ResultSet rsAvailBackups;
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException 
	{
		
		
		
		RetentionLogic R= null;
		R = new RetentionLogic();
		R.CheckBackups();
		
	}

}
