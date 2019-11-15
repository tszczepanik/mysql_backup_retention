package logic;


import java.io.File;
import java.io.IOException;

import java.sql.*;
import java.util.Iterator;
import java.util.Vector;




public class RetentionLogic
{
	static ResultSet rsAvailBackups;
	static ResultSet rsBackupDeleted;
	static String MaxDateDelete="";
	static String MinDateDelete="";
	int BackupToBeDelete=0;
	static int NFullBackups=0;
	static int NINCBackups=0;
	static int NRentention=3;
	static MyLog MLog;  
	static String SignalFirstExec="";
	static Connection con;
	String NotFirstRuN = "";
	
	Vector<Backup> VBackups = new Vector<Backup>();
	
	  public static boolean isNullOrEmpty(String str) {
	        if(str != null && !str.isEmpty())
	            return false;
	        return true;
	    }
	  
	
	public RetentionLogic () 
	{
		//Load the JDBC Driver
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Impossivel carregar o Driver JDBC , por favor verifique : mysql-connector.jar ");
			System.exit(1);
		} 
		//Load Config File
		config C = null;
		try {
			C = new config();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Nao existe o arquivo de configuracao, por favor verifique EscribaRetention.config");
			System.exit(1);
		}
		
		
		String vlocalhost = "";
		String vPort = new String("");
		String vuser = "";
		String vPWD = "" ;
		String vLog = "" ;
		String VDebug = "";
		
		vlocalhost = C.getSProperty("host");
		vPort = C.getSProperty("porta");
		vuser = C.getSProperty("user");
		vPWD = C.getSProperty("pwd");
		vLog = C.getSProperty("DestinoLog");
		VDebug = C.getSProperty("Debug");
		
		
		if (isNullOrEmpty ( C.getSProperty("NFullBackupParaManter") ) )
		{
			System.out.println("NFullBackupParaManter Nao encontrado ou vazio em  EscribaRetention.config por favor verifique");
			System.exit(1);
		}
		else
		{
			NRentention = Integer.parseInt(C.getSProperty("NFullBackupParaManter").trim());  
		}
		if ( NRentention == 0)
		{
			System.out.println("Retencao de backup setada para ZERO por favor verifique");
			System.exit(1);
		}
		
		if (isNullOrEmpty ( vLog )  )
		{
			System.out.println("DestinoLog Nao encontrado ou vazio em  EscribaRetention.config por favor verifique");
			System.exit(1);
		}
		if ( isNullOrEmpty ( vlocalhost ) )
		{
			System.out.println("Host Nao encontrado ou vazio em  EscribaRetention.config por favor verifique");
			System.exit(1);
		}
		
		if ( isNullOrEmpty (vPort ) )
		{
			System.out.println("Porta Nao encontrado ou vazio em  EscribaRetention.config por favor verifique");
			System.exit(1);
		}
		
		if ( isNullOrEmpty ( vuser ) )
		{
			System.out.println("user Nao encontrado ou  vazio em  EscribaRetention.config por favor verifique");
			System.exit(1);
		}
		if (  isNullOrEmpty ( vPWD ) )
		{
			System.out.println("vPWD Nao encontrado ou vazio em  EscribaRetention.config por favor verifique");
			System.exit(1);
		}

		
		try {
			if (  isNullOrEmpty ( VDebug ) )
			{
				MLog = new MyLog(false,vLog);
			}
			else
			{
				boolean b = Boolean.parseBoolean(VDebug);			
				MLog = new MyLog(b,vLog);
			}
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Problemas ao inicializar o log pf verifique : DestinoLog ");
			System.out.println("Exemplo the valores validos : ");
			System.out.println("Para windows ->  c:\\Pasta\\Subpasta\\  ");
			System.out.println("Para Linux -> /pasta/subpast/ ");
			System.out.println("Se continuar tendo problemas , pf verifque q applicativo pode escrever no caminho desginado  ");
			
			System.exit(1);
		}
		
		MLog.printLog("Inicio Rotina de Retencao de Backup",1);	
		try {
		
			String connStr = "jdbc:mysql://"+vlocalhost+":"+vPort+"/mysql";		
			MLog.printLog(connStr,0);			
			con=DriverManager.getConnection( connStr,vuser,vPWD);
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MLog.printLog("Erro ao conectar no banco", 3);
			MLog.printLog(e.toString(), 3);
			e.printStackTrace();
			System.exit(1);
		} 
	}
	public void LoadBackupsSummaryInfo() 
	{
		
		Statement stmt;
		try {
			stmt = con.createStatement();
			//System.out.println("select count(*) from mysql.backup_history where backup_type = 'FULL' and exit_state = 'SUCCESS' and end_time >= '"+MinDateDelete+"'");
			MLog.printLog("NFullBackups -> select count(*) from mysql.backup_history where backup_type = 'FULL' and exit_state = 'SUCCESS' and end_time "+SignalFirstExec+"'"+MinDateDelete+"'", 0);
			
			ResultSet rs=stmt.executeQuery("select count(*) from mysql.backup_history where backup_type = 'FULL' and exit_state = 'SUCCESS' and end_time "+SignalFirstExec+" '"+MinDateDelete+"'"); 
			rs.next();
			NFullBackups = rs.getInt(1);
			
			MLog.printLog("NFullBackups = "+NFullBackups,0);
			
			ResultSet rs1=stmt.executeQuery("select count(*) from mysql.backup_history where backup_type = 'INCREMENTAL' and exit_state = 'SUCCESS' and end_time "+SignalFirstExec+" '"+MinDateDelete+"'"); 
			rs1.next();
			NINCBackups = rs1.getInt(1);
			
			ResultSet rs3=stmt.executeQuery("select count(*) from mysql.backup_history where backup_type = 'FULL' and exit_state = 'SUCCESS' "); 
			rs3.next();
			int NTotalBackups = rs3.getInt(1);
		
			MLog.printLog("Numero de Backups Fulls Listado em backup_history : "+NTotalBackups,1);
			MLog.printLog("Backups as seram analisados  : ",1);
			MLog.printLog("FULL : " + NFullBackups ,1);
			MLog.printLog("INCREMENTAL: " + NINCBackups,1);
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MLog.printLog("Rotina LoadBackupSummaryInfo() - Erro ao acessa mysql.backup_history ",3);
			MLog.printLog(e.toString(), 3);
			e.printStackTrace();
			System.exit(1);
		}  
	
	}
	   
	public static boolean deleteDirectory(File dir)
    {
	        if (dir.isDirectory()) {
	            File[] children = dir.listFiles();
	            for (int i = 0; i < children.length; i++) {
	                boolean success = deleteDirectory(children[i]);
	                if (!success) {
	                    return false;
	                }
	            }
	        }
	       // System.out.println("removing file or directory : " + dir.getAbsolutePath());
	        return dir.delete();
    }
	   
	
	public void LoadBackupsDetail() 
	{
		Statement stmt;
		String MaxDeleteInc;
		try {
			stmt = con.createStatement();
			
			// Problema da primeira execucao : tenho q inclur o as duas ponts >= e <=
			ResultSet rs1=stmt.executeQuery ( "select count(*) from mysql.backup_history where end_time "+SignalFirstExec+" '"+MinDateDelete+"' and end_time <= '"+MaxDateDelete+"' and exit_state='SUCCESS' and backup_type='FULL'" );
			MLog.printLog( "select count(*) from mysql.backup_history where end_time "+SignalFirstExec+" '"+MinDateDelete+"' and end_time <= '"+MaxDateDelete+"' and exit_state='SUCCESS' and backup_type='FULL'" ,0);
			
			rs1.next();
			int i  = rs1.getInt(1);
			MLog.printLog("Crosschecking backups got -> "+i+" from table backup_history and BackupToBeDelete have: "+BackupToBeDelete,0);
			if ( BackupToBeDelete == i  )
			{
				
				ResultSet rs2=stmt.executeQuery ( "select backup_id,end_time,backup_destination from mysql.backup_history where end_time "+SignalFirstExec+" '"+MinDateDelete+"' and end_time <= '"+MaxDateDelete+"' and exit_state='SUCCESS' order by end_time" );
				MLog.printLog("Puxa os backup q devemos deletar -> select backup_id,end_time,backup_destination from mysql.backup_history where end_time "+SignalFirstExec+" '"+MinDateDelete+"' and end_time <= '"+MaxDateDelete+"' and exit_state='SUCCESS' order by end_time",0 );
				
				while (rs2.next()) 
				{
					
					 Backup B = new Backup(rs2.getString("backup_id"), 
							 rs2.getString("end_time"),
							 rs2.getString("backup_destination")
							 );
					 MLog.printLog("Carregando dados no Vetor:"+B.ID+" : "+B.Path,0);
					 VBackups.add(B);
				}     
				//Ajuste Para apagar backup InC depois do FULL
				//Procura pelo proximo full da tabela, nao adianta deixa o inc se estou apagando o full deles.
				
				
				MLog.printLog("Procura Backups INCS Entre o Backup FULL q vou delete ( ultimo da retencao ) e o Ultimo full ( mais antigo ) na tabela ) -> select backup_id,end_time,backup_destination from mysql.backup_history where end_time >'"+MaxDateDelete+"' and backup_type = 'FULL' and exit_state='SUCCESS' order by end_time limit 1",1);	
				
				ResultSet rs3=stmt.executeQuery ("select backup_id,end_time,backup_destination from mysql.backup_history where end_time > '"+MaxDateDelete+"' and backup_type = 'FULL' and exit_state='SUCCESS' order by end_time limit 1" );
			   if (rs3.next() == false) {
					MLog.printLog("Por favor verifique Nenhum Backup FULL encontrado na tabela",3);
				}
				else
				{
					MaxDeleteInc=rs3.getString("end_time");
					
					ResultSet rs4=stmt.executeQuery ( "select backup_id,end_time,backup_destination,backup_type from mysql.backup_history where end_time > '"+MaxDateDelete+"' and end_time < '"+MaxDeleteInc+"' and exit_state='SUCCESS' and backup_type='INCREMENTAL' order by end_time ");			
					MLog.printLog("Puxa os INC  backup q devemos deletar -> select backup_id,end_time,backup_destination,backup_type from mysql.backup_history where end_time > '"+MaxDateDelete+"' and end_time < '"+MaxDeleteInc+"' and exit_state='SUCCESS' and backup_type='INCREMENTAL' order by end_time ",0 );
					
					while (rs4.next()) 
					{
						
						 Backup B = new Backup(rs4.getString("backup_id"), 
								 rs4.getString("end_time"),
								 rs4.getString("backup_destination")
								 );
						 MLog.printLog("Carregando dados no Vetor:"+B.ID+" : "+B.Path,0);
						 VBackups.add(B);
					}
				
					
				}
				
				

			}
			else
			{
				MLog.printLog("Rotina LoadBackupsDetail - Erro Numeros de Backup incorrectos!!!! Por vefique " + BackupToBeDelete +" <> "+ i,3);
				System.exit(1);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MLog.printLog("Rotina LoadBackupsDetail() - Erro ao acessa mysql.backup_history ",3);
			MLog.printLog(e.toString(), 3);
			e.printStackTrace();
			System.exit(1);
		}  
		
		
	
		
	}
	
	public void ApplyRentention() throws SQLException
	{
	
		
		Statement stmt = con.createStatement();
		
	
		ResultSet rsBackupID ;
		
		
        // Creating an iterator 
        Iterator<Backup> value = VBackups.iterator(); 
  
        MLog.printLog("Numero de Backups :"+VBackups.size() +"as serem deletados",1);
        // Displaying the values 
        // after iterating through the vector 
   
        while (value.hasNext())
        { 
            Backup MB = (Backup) value.next(); 
      
            //let check if we already applied the retentation on this backup id : 
            rsBackupID = stmt.executeQuery("select count(*) from mysql.backup_rentention where backup_id = '"+MB.ID+"'");
           
            rsBackupID.next();
            
            int AlreadyTried = rsBackupID.getInt(1);
            
            if  ( AlreadyTried  ==  0 )
            {
            
	            File F = new File(MB.Path);
	            if ( deleteDirectory(F) )
	            {
	            	MLog.printLog("OK Deleted : "+MB.ID+" - "+MB.Path,1);
	            	stmt.executeUpdate("insert into mysql.backup_rentention values ("+MB.ID+",'"+MB.end_time+"',NOW(),'OK')");
	            }
	            else 
	            {
	            	MLog.printLog("NOK "+MB.ID+" - "+MB.Path,1);
	            	stmt.executeUpdate("insert into mysql.backup_rentention values ("+MB.ID+",'"+MB.end_time+"',NOW(),'NOK')");
	            }
	            File FLog = new File(MB.Path+".log");
	            if ( deleteDirectory(FLog) )
	            {
	            	MLog.printLog("Removendo Log : "+MB.ID+" - "+MB.Path+".log",1);
	            }
	            else
	            {
	            	MLog.printLog("Erro ao remover Log : "+MB.ID+" - "+MB.Path+".log",1);
	            }
            }
            else
            {
            	MLog.printLog("Backup : "+MB.ID+" - "+MB.Path+" detectada uma tentativa de retencao posterior... pulando para backup para essa execucao - delete manualmente se necessario: MB.Path  ",2);
            }
            
        }
     
        
        
        
	}
	
	public void CheckBackups () throws ClassNotFoundException
	{
		
		
		isFirstRUN();
		
		LoadBackupsSummaryInfo();
		
		if ( NFullBackups > NRentention )
		{
			
			MLog.printLog(" Numbero de backup : " + NFullBackups + " > " + NRentention,1 );
			BackupToBeDelete = NFullBackups - NRentention;
			MLog.printLog(" To be Deleted : " + BackupToBeDelete,1);
			Statement stmt2;
			try {
				stmt2 = con.createStatement();
				
				MLog.printLog(
						"Seleciona a Maior data ->	a rentecao vai ser at no maximo MDATE -> select MAX(MDate) as MaxDate from ( select backup_id,end_time as MDate   from mysql.backup_history  as a\r\n" + 
		                         "  where  backup_type = 'FULL' and exit_state = 'SUCCESS' and end_time "+SignalFirstExec+" '"+MinDateDelete+"' order by backup_id limit "+ BackupToBeDelete +" ) as dt order by backup_id desc "
		                         ,0);
				
				ResultSet rs2=stmt2.executeQuery("	select MAX(MDate) as MaxDate from ( select backup_id,end_time as MDate   from mysql.backup_history  as a\r\n" + 
						                         "  where  backup_type = 'FULL' and exit_state = 'SUCCESS' and end_time "+SignalFirstExec+" '"+MinDateDelete+"' order by backup_id limit "+ BackupToBeDelete +" ) as dt order by backup_id desc "); 
				rs2.next();
			    MaxDateDelete=rs2.getString(1);
			    MLog.printLog("Max Date :"+MaxDateDelete,0);
			    
			    
			    
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				MLog.printLog("Erro Rotina CheckBackups - "+e.toString(),3);
				e.printStackTrace();
				System.exit(1);
			}  
		
		 
		    LoadBackupsDetail();
		    
		    try {
				ApplyRentention();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				MLog.printLog("Erro Rotina ApplyRentention - "+e.toString(),3);
				e.printStackTrace();
				System.exit(1);
			}
		}
		else 
		{
			MLog.printLog("Numero de backups : "+NFullBackups+" disponivels , Nao exede a Retencao estabelicida: "+NRentention+"- Nada ser ver feito nessa execucao", 1);
			
		}
	
		
		
		
	}
	public void isFirstRUN() 
	{
		int NumberofBackupManaged=-1;
		Statement stmt=null;
		try 
		{

			stmt = con.createStatement();
			ResultSet rsBackupDeleted=stmt.executeQuery("select count(*) from mysql.backup_rentention where retention_state = 'OK' "); 
			rsBackupDeleted.next();
			NumberofBackupManaged=rsBackupDeleted.getInt(1);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			MLog.printLog("Erro ao acessar tabela : mysql.backup_rentention", 3);
			MLog.printLog(e.toString(), 3);
		
			e.printStackTrace();
			System.exit (1);
		}  
	
		

		String SQuery;
		if ( NumberofBackupManaged == 0 )
		{
			MLog.printLog("*** Primeiro Execucaco  *** ",2);
			MLog.printLog("Tabela de retencao com ZERO registros", 2);
			MLog.printLog(" Isso pode demorar algum tempo ",2 );
			SQuery = "select min(end_time) from mysql.backup_history where backup_type = 'FULL' and exit_state='SUCCESS'";
			SignalFirstExec=">=";
			
		}
		else
		{
			SQuery = "select max(backup_end_date) from mysql.backup_rentention where retention_state = 'OK' order by backup_end_date ";
			SignalFirstExec=">";
			
			MLog.printLog("Utilizando a maior data da tabela de Retencao ", 1);	
		}
		ResultSet rsRetentionDt;
		try {
			rsRetentionDt = stmt.executeQuery(SQuery);
			rsRetentionDt.next();
			MinDateDelete=rsRetentionDt.getString(1);
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			MLog.printLog("Erro ao acessar tabela : mysql.backup_rentention ou mysql.backup_rentention", 3);
			MLog.printLog(e.toString(), 3);	
			e.printStackTrace();
			System.exit (1);
		} 
		MLog.printLog("Mindate:"+SQuery, 0);
		
		
		MLog.printLog("Data selecionda como ultima para rentencao: "+SignalFirstExec+" "+ MinDateDelete, 1);
		
	}
	public void execRetention() 
	{
		try {
			CheckBackups();
		} catch (ClassNotFoundException  e) {
			// TODO Auto-generated catch block
			MLog.printLog("Erro durante a PreCheck", 3);
			e.printStackTrace();
		
		}
		
		
		try {
			cleanUP();
			MLog.printLog("Fim Rotina de Retencao de Backup",1);
			
		} catch (SQLException e) {
			
			MLog.printLog("Erro Rotina de CleanUP() - "+e.toString(),3);
			e.printStackTrace();
			System.exit(1);
		}
			
	}

	public void cleanUP() throws SQLException
	{
		con.close();  
	}
}
