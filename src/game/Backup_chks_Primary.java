package game; 

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Backup_chks_Primary implements Runnable {


	Game stub;
	GameDetails response;

	public Backup_chks_Primary(Game r,GameDetails res) 
	{
		stub = r;
		response = res;
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		Registry registry = null;
		
		
		while(true)
		{
			try 
			{
				Thread.sleep(1000);
			}
			
			catch (InterruptedException e3) 
			{
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

			try 
			{
				registry = LocateRegistry.getRegistry(response.ip_primary, response.primary_id);
				stub = (Game)registry.lookup("Game");
				stub.updateInfo();
			} 
			catch (RemoteException e) 
			{
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Game client_stub;
				try 
				{
					registry = LocateRegistry.getRegistry(response.ip_backup, response.backup_id);
					client_stub = (Game)registry.lookup("Game");
					response = client_stub.updateInfo();
				} 
				catch (RemoteException e3) 
				{
					// TODO Auto-generated catch block
					//e3.printStackTrace();
				} catch (NotBoundException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
				
				System.out.println("You are the new primary server");
				response.player_treasure.remove(response.primary_id);
				response.listIDS.remove(response.primary_id);
				response.listIDS.remove(response.backup_id);
				
				response.ipAddress.remove(response.primary_id);
				response.player_pos.remove(response.primary_id);

				response.primary_id = response.backup_id;
				response.ip_primary = response.ip_backup;

				response.backup_down =true;

				
				for(Integer i: response.listIDS)
				{
					if(i!=response.primary_id)
					{
						response.backup_id = i;
						response.listIDS.remove(i);
						response.ip_backup = response.ipAddress.get(i);
						try			//to tell client it its new backup 
						{
							registry = LocateRegistry.getRegistry(response.ipAddress.get(i), i);
							Game client_stub1 = (Game)registry.lookup("clientstub"+i);
							client_stub1.backup_updateClient(response);
						} 
						catch (RemoteException e1) 
						{
							// TODO Auto-generated catch block
							//e1.printStackTrace();
						} catch (NotBoundException e1) {
							// TODO Auto-generated catch block
							//e1.printStackTrace();
						}
						
						
						break;
					}
				}
				
				try 	// to tell backup it is new primary
				{
					response.backup_down = false;
					registry = LocateRegistry.getRegistry(response.ip_primary, response.primary_id);
					client_stub = (Game)registry.lookup("Game");
					client_stub.backup_updateClient(response);
					
					client_stub = (Game)registry.lookup("clientstub"+response.primary_id);
					client_stub.backup_updateClient(response);
					
					client_stub.runthread();
				} 
				catch (RemoteException e2) 
				{
					// TODO Auto-generated catch block
					//e2.printStackTrace();
				} 
				catch (NotBoundException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}


				for(int n : response.listIDS)		// to update all other clients
				{
					//if( n!= response.primary_id)
					//{
						try 
						{
							registry = LocateRegistry.getRegistry(response.ipAddress.get(n), n);
							client_stub = (Game)registry.lookup("clientstub"+n);
							client_stub.backup_updateClient(response);
						} 
						catch (RemoteException e1) 
						{
							// TODO Auto-generated catch block
							//e1.printStackTrace();
						} 
						catch (NotBoundException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					//}
				}
				break;
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 


	}
}