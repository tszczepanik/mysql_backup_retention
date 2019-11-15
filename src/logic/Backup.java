package logic;

public class Backup
{
	String end_time;
	String delete_time;
	String Status;
	String Path;
	String ID;
	
	
	
	public Backup (String vID, String etime,String vp)
	{
		ID=vID;
		Path=vp;
		end_time=etime;
		delete_time="";
		
		
	}
	public void setStatus(boolean t)
	{
		if ( t )
		{
			Status="OK";
		}
		else
			Status = "Error";
	}
}
