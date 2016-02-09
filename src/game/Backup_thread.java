package game; 

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Backup_thread implements Runnable 
{

	Game stub;
	GameDetails response;
	
	public Backup_thread(Game r,GameDetails res) 
	{
		stub = r;
		response = res;
	}

	public void run() 
	{
		GameDetails g = null;
		Game stub1 = null;
		GameDetails g1 = null;
		
		while(true)
		{
			Registry registry;
			try 
			{
				registry = LocateRegistry.getRegistry(response.ipAddress.get(response.playerid), response.playerid);
				stub1 = (Game)registry.lookup("clientstub"+response.playerid);
		
			} 
			catch (RemoteException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			catch (NotBoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try 
			{
				Thread.sleep(1000);
				g = stub1.updateInfo();
				
				
				if(g.backup_down)
				{
					stub1.updatebackupDown();
					
					if(response.playerid == g.backup_id)
					{
						p2p.backupregCreation(g);
						Runnable r1 = new Backup_chks_Primary(stub1,g);
						new Thread(r1).start();
						
						System.out.println("You are the backup now!!");
						registry = LocateRegistry.getRegistry(g.ip_primary, g.primary_id);
						stub1 = (Game)registry.lookup("Game");
						stub1.move(response.playerid, 'u');
						stub1.updatebackupDown();
						
						for(int n : g.player_pos.keySet())
						{
							try 
							{
								registry = LocateRegistry.getRegistry(g.ipAddress.get(n), n);
								stub1 = (Game)registry.lookup("clientstub"+n);
								stub1.updatebackupDown();
							} 
							catch (RemoteException e1) 
							{
								// TODO Auto-generated catch block
								//e1.printStackTrace();
							} 
							catch (NotBoundException e1)
							{
								// TODO Auto-generated catch block
								//e1.printStackTrace();
							}
						}
					}
				}
				
				/*g1 = stub.updateInfo();
				if(g1.backup_down)
				{
					if(response.playerid == g1.backup_id)
					{
						p2p.backupregCreation(response);
						Runnable r1 = new Backup_chks_Primary(stub1,g1);
						new Thread(r1).start();
						
						System.out.println("You are the backup now!!");
						stub.move(response.playerid, 'u');
						stub.updatebackupDown();
					}
				}*/
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (RemoteException e) // if primary is down, go to backup
			{
				//// TODO Auto-generated catch block
				//e.printStackTrace();
				
			} 
			catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}