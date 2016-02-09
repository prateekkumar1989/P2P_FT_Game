package game; 

import java.awt.Point;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;

public class GameImpl implements Game,Runnable{

	GameDetails gd;
	Thread t;

	/*default constructor*/
	public GameImpl()
	{

	}

	public GameImpl(int n1, int k1) throws RemoteException		//take n and k from command line
	{
		gd = new GameDetails(n1, k1);
		t = new Thread(this, "Client Purge Thread");
		t.start();
	}


	/*Returns the player its starting position, list of treasures and unique id if the player joins within 20 seconds of
	 the first request*/
	@Override
	public GameDetails joingame() throws RemoteException 
	{

		if (!gd.first)
		{
			gd.first= true;
			gd.starttime = System.currentTimeMillis();
		}

		if(System.currentTimeMillis()-gd.starttime <=20000)
		{

			Point pos = getPlayerPosition(gd.n);
			gd.playerid++;
			gd.no_of_players++;

			gd.pos = new Point(pos);

			//System.out.println("Total Players joined:"+gd.no_of_players);

			if((gd.playerid!=10000)&&(gd.playerid!=10001))	//primary and backup ids shud not be included in listids
				gd.listIDS.add(gd.playerid);

			gd.player_pos.put(gd.playerid, pos);			//random initial position
			gd.player_treasure.put(gd.playerid,0);			// initially no treasure with anyone


			return gd;
		}

		else
			return null;
	}


	/*Position of other players plus the menu after the game starts*/
	@Override
	public String strtgame(Point pos) throws RemoteException
	{
		// TODO Auto-generated method stub
		while(System.currentTimeMillis()-gd.starttime <=20000);

		String other_players = new String();

		for(Point p : gd.initial_pos)
		{
			if(!p.equals(pos))
				other_players+=p.x+","+p.y+"|";

		}

		return(other_players);
	}


	/*Generate the initial random position for each player*/
	public Point getPlayerPosition(int n)
	{
		Random position = new Random();
		Point point;
		do
		{
			point = new Point();
			point.x = position.nextInt(n);
			point.y = position.nextInt(n);

		}while(gd.initial_pos.contains(point) || (gd.treasure_pos.contains(point)));		// to generate unique starting position for each player

		gd.initial_pos.add(point);
		return point;
	}


	/*Moves in the desired direction if possible and returns the result*/
	@Override
	synchronized public GameDetails move(int id, char dir) throws RemoteException 
	{
		// TODO Auto-generated method stub

		// game already over
		if (gd.gamewon)
			return gd;


		//invalid entry
		if(gd.player_pos.get(id)!=null)
		{
			if(!checkValidity(id, dir))
				return null;						// null means invalid


			//System.out.println("In Move-1");
			int x_pos = gd.player_pos.get(id).x;
			int y_pos = gd.player_pos.get(id).y;
			int treasurecount = 0;

			//System.out.println("In Move-2");
			// new position
			if ((dir == 'n')||(dir == 'N'))
				y_pos++;
			if ((dir == 's')||(dir == 'S'))
				y_pos--;
			if ((dir == 'w')||(dir == 'W'))
				x_pos--;
			if ((dir == 'e')||(dir == 'E'))
				x_pos++;

			//System.out.println("In Move-3");
			while(!gd.avail_player_pos && !gd.avail_player_treasure && !gd.avail_treasure_pos);

			gd.avail_player_pos = false;
			gd.avail_player_treasure = false;
			gd.avail_treasure_pos = false;

			//System.out.println("In Move-4");
			//update player position
			gd.player_pos.remove(id);
			//System.out.println("In Move-5");
			gd.player_pos.put(id, new Point(x_pos,y_pos));
			//System.out.println("In Move-6");

			// remove treasure from treasure list
			Iterator<Point> iter = gd.treasure_pos.iterator();
			while (iter.hasNext()) 
			{
				if (iter.next().equals(gd.player_pos.get(id))) 
				{
					treasurecount++;
					iter.remove();
				}
			}


			// update treasure count of each player
			if(treasurecount>0)
			{
				treasurecount += gd.player_treasure.get(id);
				gd.player_treasure.remove(id);
				gd.player_treasure.put(id, treasurecount);
			}

			if(gd.treasure_pos.size()==0)
			{

				gd.gamewon=true;
				gd.id_won = findmax(gd.player_treasure);
				//System.out.println("id of winning player: "+gd.id_won);
			}

			p_to_back();

			System.out.println("Primary updated!! by "+id+"at "+new Date().toString());


			gd.avail_player_pos = true;
			gd.avail_player_treasure = true;
			gd.avail_treasure_pos = true;
		}

		//System.out.println(id+":"+ gd.player_pos.get(id)+","+gd.player_treasure.get(id));
		return gd;
	}


	public void p_to_back()
	{
		Registry registry;
		try 
		{
			registry = LocateRegistry.getRegistry(gd.ip_backup, gd.backup_id);
			Game stub = (Game) registry.lookup("Game");
			stub.Primary_updatebackup(gd);
		} 
		catch (Exception e) 
		{
			gd.listIDS.remove(gd.backup_id);

			gd.ipAddress.remove(gd.backup_id);
			gd.player_pos.remove(gd.backup_id);

			gd.backup_down = true;
			for(Integer i: gd.listIDS)
			{
				if((i!=gd.backup_id)&&(i!=gd.primary_id))
				{
					gd.backup_id = i;
					gd.ip_backup = gd.ipAddress.get(i);
					gd.listIDS.remove(i);
					try		//to tell client that u r new backup 
					{
						registry = LocateRegistry.getRegistry(gd.ipAddress.get(i), i);
						Game client_stub = (Game)registry.lookup("clientstub"+i);
						client_stub.backup_updateClient(gd);
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

			for(Integer i: gd.listIDS)		//for all other clients
			{
				if(i!=gd.primary_id)
				{
					try 
					{
						registry = LocateRegistry.getRegistry(gd.ipAddress.get(i), i);
						Game client_stub = (Game)registry.lookup("clientstub"+i);
						client_stub.backup_updateClient(gd);
					} 
					catch (RemoteException e1) 
					{
						// TODO Auto-generated catch block
						//e1.printStackTrace();
					} catch (NotBoundException e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
					}

				}
			}


		} 
	}


	/*To find which player won*/
	public int findmax(ConcurrentHashMap<Integer, Integer> a)
	{
		int max = Integer.MIN_VALUE;
		int id=-1;
		for( int i : a.keySet())
		{
			if (a.get(i)>max)
			{
				max = a.get(i);
				id = i;
			}
		}

		return id;
	}


	/*Checks whether it is a valid move or not*/
	public boolean checkValidity(int id , char dir)
	{
		if((dir == 'u')||(dir == 'U'))
			return true;

		if(((dir == 'n')||(dir == 'N'))&&(checkplayer(gd.player_pos.get(id).x , gd.player_pos.get(id).y+1)))
			if(gd.player_pos.get(id).y < (gd.n-1))
				return true;

		if(((dir == 's')||(dir == 'S'))&&(checkplayer(gd.player_pos.get(id).x , gd.player_pos.get(id).y-1)))
			if(gd.player_pos.get(id).y > 0)
				return true;

		if(((dir == 'w')||(dir == 'W'))&&(checkplayer(gd.player_pos.get(id).x-1 , gd.player_pos.get(id).y)))
			if(gd.player_pos.get(id).x > 0)
				return true;

		if(((dir == 'e')||(dir == 'E'))&&(checkplayer(gd.player_pos.get(id).x+1 , gd.player_pos.get(id).y)))
			if(gd.player_pos.get(id).x < (gd.n-1))
				return true;

		return false;
	}


	/*Check if another player is at that position*/
	public boolean checkplayer(int x,int y)
	{
		if(gd.player_pos.contains(new Point(x,y)))
			return false;

		else
			return true;

	}


	/*Returns whether game is still going on or not*/
	@Override
	public boolean gameresult() throws RemoteException 
	{
		// TODO Auto-generated method stub
		return gd.gamewon;
	}



	/*Update the backup server*/
	@Override
	public GameDetails updateBackup(int id, String ip) throws RemoteException {
		// TODO Auto-generated method stub

		if(!gd.isbackup)
		{
			gd.isbackup= true;
			gd.backup_id = id;
			gd.ip_backup = ip;
		}

		gd.ipAddress.put(id, ip);

		return gd;
	}



	/*Primary updating backup info*/
	@Override
	public void Primary_updatebackup(GameDetails g) throws RemoteException {
		// TODO Auto-generated method stub
		gd.treasure_pos = g.treasure_pos;
		gd.gamewon = g.gamewon;
		gd.id_won = g.id_won;
		gd.no_of_players = g.no_of_players;
		gd.player_pos = g.player_pos;
		gd.initial_pos = g.initial_pos;
		gd.player_treasure = g.player_treasure;

		gd.backup_id = g.backup_id;
		gd.primary_id = g.primary_id;
		gd.ip_backup = g.ip_backup;
		gd.ip_primary = g.ip_primary;
		gd.ipAddress = g.ipAddress;
		gd.listIDS = g.listIDS;

		System.out.println("Primary updated backup!! at "+new Date().toString());
	}



	@Override
	public void updatebackupDown() throws RemoteException {
		// TODO Auto-generated method stub
		gd.backup_down = false;
	}

	@Override
	public GameDetails updateInfo() throws RemoteException {
		// TODO Auto-generated method stub
		return gd;
	}


	@Override
	public void run() 						//run by server
	{
		// TODO Auto-generated method stub

		Registry registry = null;
		Game client_stub = null;
		int crashed = 0;

		try 
		{
			Thread.sleep(2000);
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true)
		{
			for(int n : gd.player_pos.keySet())
			{
				try 
				{
					crashed = n;
					if( n!= gd.primary_id)
					{
						registry = LocateRegistry.getRegistry(gd.ipAddress.get(n), n);
						client_stub = (Game)registry.lookup("clientstub"+n);
						client_stub.updateInfo();
						//System.out.println("Try:"+gd.ipAddress.get(n));
					}
				}

				catch (RemoteException e1) 
				{
					gd.player_pos.remove(crashed);
					gd.player_treasure.remove(crashed);
					gd.listIDS.remove(crashed);
					gd.ipAddress.remove(crashed);
					//System.out.println("Catch:"+gd.ipAddress.get(n));
					System.out.println("Player Crash detected. Primary updated!!");
					p_to_back();
				} 
				catch (NotBoundException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} 

			try 
			{
				Thread.sleep(2000);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}



	/*Backup updates primary info on clients*/
	@Override
	public void backup_updateClient(GameDetails g) throws RemoteException {
		// TODO Auto-generated method stub
		gd.treasure_pos = g.treasure_pos;
		gd.gamewon = g.gamewon;
		gd.id_won = g.id_won;
		gd.no_of_players = g.no_of_players;
		gd.player_pos = g.player_pos;
		gd.initial_pos = g.initial_pos;
		gd.player_treasure = g.player_treasure;

		gd.backup_id = g.backup_id;
		gd.primary_id = g.primary_id;
		gd.ip_backup = g.ip_backup;
		gd.ip_primary = g.ip_primary;
		gd.ipAddress = g.ipAddress;

		gd.backup_down = g.backup_down;

		gd.listIDS = g.listIDS;

		System.out.println("Backup updated client after primary failure!! at "+new Date().toString()+" "+gd.backup_id+" "+gd.backup_down);

	}

	@Override
	public void runthread() throws RemoteException {
		// TODO Auto-generated method stub
		Thread t = new Thread(this, "Client Purge Thread");
		t.start();
	}




}
