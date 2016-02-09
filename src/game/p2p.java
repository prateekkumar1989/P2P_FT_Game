package game; 

import java.awt.Point;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JFrame;


public class p2p{

	public static int id;
	public static Point position;
	public int no_of_treasures;
	public static ArrayList<Point> treasure_pos;
	public static int n;
	public static int k;

	public static void DisplayMenu()
	{
		System.out.println();
		System.out.println("=====================================");
		System.out.println("1)N: To go North");
		System.out.println("2)S: To go South");
		System.out.println("3)W: To go West");
		System.out.println("4)E: To go East");
		System.out.println("0)U: For updated status of game");
		System.out.println("=====================================");

	}


	//response contains correct player id, gd gets updated info. ignore player id in gd
	public static void StartGame(Game stub, GameDetails response) throws AlreadyBoundException, RemoteException, NotBoundException
	{
		char ch = 0;
		Registry reg = null;
		GameGUI gb = new GameGUI(response);

        JFrame f = new JFrame("Peer ID " + response.playerid);
        f.add(gb.getGui());
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLocationByPlatform(true);
        f.pack(); 
        f.setMinimumSize(f.getSize());
        f.setVisible(true);
        
        reg = LocateRegistry.getRegistry(response.ip_primary, response.primary_id);
		stub = (Game)reg.lookup("Game");
		
        GameDetails gd = stub.move(response.playerid, 'u'); 
        gb.repaintGui(gd);
		
        while(true)
		{
			/*Scanner sc = new Scanner(System.in);
			String str = "";
			while(str.equals(""))
			{
				str = sc.nextLine();
			}
			char ch = str.charAt(0);*/
			
			reg = null;
			try 
			{
				reg = LocateRegistry.getRegistry(response.ip_primary, response.primary_id);
				stub = (Game)reg.lookup("Game");
				
				
				while(!gb.ifnorth && !gb.ifsouth && !gb.ifeast && !gb.ifwest && !gb.ifnomove);

				if(gb.ifnorth == true) { gb.ifnorth = false; ch = 'n'; }
				if(gb.ifsouth == true) { gb.ifsouth = false; ch = 's'; }
				if(gb.ifeast == true) { gb.ifeast = false; ch = 'e'; }
				if(gb.ifwest == true) { gb.ifwest = false; ch = 'w'; }
				if(gb.ifnomove == true) { gb.ifnomove = false; ch = 'u'; }

				while(stub.updateInfo().backup_down);
				
				gd = stub.move(response.playerid, ch); 

				if(gd != null)
				{
					gb.repaintGui(gd);
					if(gd.gamewon)
					{
						if(response.playerid == gd.id_won)
							System.out.println("Game over. You won");
						else
							System.out.println("Game over. Player id:"+gd.id_won+" won");

						break;
					}

					else
					{
						tokenise_response(stub,gd,response);
					}
				}
				
				else
				{
					System.out.println("Invalid Move");
				}
			} 
			catch (RemoteException e) {
				// TODO Auto-generated catch block
				//System.out.println("Remote Exception-1");
				//e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
			
		}
	}

	
	/*To tokenise the response sent by the servNer and interpret it*/
	// response is of the form = new_pos|treasures collected| position of remaining treasures|
	public static void tokenise_response(Game stub,GameDetails res,GameDetails response) throws RemoteException, AlreadyBoundException
	{
		System.out.println("Current Position:("+ res.player_pos.get(response.playerid).x+","+res.player_pos.get(response.playerid).y+")");
		System.out.println("No of treasures collected so far:"+res.player_treasure.get(response.playerid));
		System.out.println("Position of remaining treasures:");

		for(int i=0;i<res.treasure_pos.size();i++)
			System.out.print("("+res.treasure_pos.get(i).x+","+res.treasure_pos.get(i).y+") ");
		System.out.println();

		for(int n : res.player_pos.keySet())
		{
			System.out.print("Player id:"+ n +", Player Position:("+res.player_pos.get(n).x+","+res.player_pos.get(n).y+"), No of treasures:"+res.player_treasure.get(n));
			System.out.println();
		}
		
		System.out.println("Primary id:"+res.primary_id+",Backup id:"+res.backup_id);
		System.out.println("Backup_down:"+res.backup_down);
		/*if(res.backup_down)
		{
			if(response.playerid == res.backup_id)
			{
				Runnable r1 = new Backup_chks_Primary(stub,response);
				new Thread(r1).start();
				backupregCreation(response);
				System.out.println("You are the backup now!!");
				stub.move(response.playerid, 'u');
				
				stub.updatebackupDown();
			}
		}*/
		
	}

	/*Special method for server. To copy details*/		//obj is main object. g is a copy of the object specially for server
	
	public static GameDetails copyresponse(GameDetails s)
	{
		GameDetails g = new GameDetails();
		
		g.avail_player_pos = s.avail_player_pos;
		g.avail_player_treasure = s.avail_player_treasure;
		g.avail_treasure_pos = s.avail_treasure_pos;
		g.first = s.first;
		g.gamewon= s.gamewon;
		g.id_won = s.id_won;
		g.initial_pos = s.initial_pos;
		g.k= s.k;
		g.n = s.n;
		g.no_of_players = s.no_of_players;
		g.player_pos = s.player_pos;
		g.player_treasure = s.player_treasure;
		g.playerid = s.playerid;
		g.pos = s.pos;
		g.starttime = s.starttime;
		g.treasure_pos = s.treasure_pos;
		g.primary_id = s.primary_id;
		g.ip_primary = s.ip_primary;
		return g;
	}

	
	/*Create initial backup registry on backup server*/
	public static void backupregCreation(GameDetails obj)
	{
		GameImpl obj1 = new GameImpl();
		obj1.gd = obj;
		Registry reg_backup;
		try 
		{
			reg_backup = LocateRegistry.getRegistry(obj.playerid);
			Game stub = (Game) UnicastRemoteObject.exportObject(obj1, obj.playerid);
			reg_backup.bind("Game", stub);
		} 
		catch (RemoteException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} 
		catch (AlreadyBoundException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException, UnknownHostException{
		// TODO Auto-generated method stub

		GameImpl obj = null;
		Game stub = null;
		GameImpl Client_impl_obj = new GameImpl();
		Game client_stub = null;

		Registry registry = null;
		
		try 	// for clients
		{
			registry = LocateRegistry.getRegistry("172.23.123.62", 10001);		//initial bootstrap point
			stub = (Game) registry.lookup("Game");			
			GameDetails response = stub.joingame();

			
			if(response !=null)
			{
				Client_impl_obj.gd = response;
				Registry reg_backup = LocateRegistry.createRegistry(response.playerid) ;	//client's rmi registry
				System.out.println("Exporting gameImpl"+response.playerid);
				client_stub  = (Game) UnicastRemoteObject.exportObject(Client_impl_obj, response.playerid);
				reg_backup.bind("clientstub"+response.playerid, client_stub);
				
				System.out.println("Join Successful. Waiting for other players to join");
				GameDetails new_res = stub.updateBackup(response.playerid,InetAddress.getLocalHost().getHostAddress());	//to update backup info
				
				response.backup_id =new_res.backup_id;
				response.ip_backup=new_res.ip_backup;
				
				
				if(new_res.backup_id == response.playerid)
				{
					response.isbackup=true;
					
					System.out.println("You are backup!Primary ip:"+new_res.ip_primary+",Primary id:"+new_res.primary_id);
					backupregCreation(response);
					
				}
				else
					System.out.println("Backup ip:"+new_res.ip_backup+",Backup id:"+new_res.backup_id+"Primary ip:"+new_res.ip_primary+",Primary id:"+new_res.primary_id);
								
				
				//System.out.println(response);	//for verification
				System.out.println("Yourid:"+response.playerid+"\nInitial Position:("+response.pos.x+" "+response.pos.y+")");

				System.out.println("The positions of treasures are:\n");
				for(int i=0;i<response.treasure_pos.size();i++)
					System.out.print("("+response.treasure_pos.get(i).x+","+response.treasure_pos.get(i).y+") ");


				String strt = stub.strtgame(response.pos);
				GameDetails g = stub.updateInfo();
				
				if(response.playerid == g.backup_id)
				{
					Runnable r1 = new Backup_chks_Primary(stub,g);
					new Thread(r1, "backup_checksprimary").start();
				}
				
				StringTokenizer tokenizer1 = new StringTokenizer(strt,"|");
				System.out.println("\n The positions of other players are:");
				while(tokenizer1.hasMoreTokens())
					System.out.println("("+tokenizer1.nextToken()+")");

				
				Runnable r = new Backup_thread(stub,response);
				new Thread(r,"backup_thread").start();
				
				DisplayMenu();
				StartGame(stub,response);

			}

			else
			{
				System.out.println("Sorry can't join. Game already started");
			}

		} 

		catch (RemoteException e) // if server
		{

			Scanner sc = new Scanner(System.in);		// take n and k input from user
			
			n = sc.nextInt();
			k = sc.nextInt();
			obj = new GameImpl(n,k);
			obj.gd.isprimary = true;
			obj.gd.ip_primary = new String(InetAddress.getLocalHost().getHostAddress());

			System.setProperty("java.rmi.server.hostname", obj.gd.ip_primary);
			registry = LocateRegistry.createRegistry(10001) ;
			stub = (Game) UnicastRemoteObject.exportObject(obj, 10001);
			registry.bind("Game", stub);

			GameDetails response = obj.joingame();
			response.primary_id = response.playerid;
			GameDetails res_copy = copyresponse(response);		// to make a copy of the object
			
			obj.gd.ipAddress.put(res_copy.playerid, res_copy.ip_primary);
			System.out.println("You are the server!!.\n Yourid:"+res_copy.playerid+"\nInitial Position:("+res_copy.pos.x+" "+res_copy.pos.y+")");

			System.out.println("The positions of treasures are:\n");
			for(int i=0;i<res_copy.treasure_pos.size();i++)
				System.out.print("("+res_copy.treasure_pos.get(i).x+","+res_copy.treasure_pos.get(i).y+") ");


			String strt = obj.strtgame(res_copy.pos);
			StringTokenizer tokenizer1 = new StringTokenizer(strt,"|");
			System.out.println("\n The positions of other players are:");
			while(tokenizer1.hasMoreTokens())
				System.out.println("("+tokenizer1.nextToken()+")");


			DisplayMenu();
			StartGame(obj,res_copy);
			// server functions

		}
	}

}
