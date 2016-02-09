package game; 

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class GameDetails implements Serializable{

	int n,k;										// size of grid, number of treasures
	int playerid;
	Point pos;
	ArrayList<Point> treasure_pos;					// position of treasures
	boolean gamewon;								// whether game is over or not
	int id_won;										// player id of winner
	
	
	boolean first;
	long starttime;
	int no_of_players;
	
	boolean avail_player_treasure;
	boolean avail_player_pos;
	boolean avail_treasure_pos;

	ConcurrentHashMap<Integer, Point> player_pos;			// player position
	HashSet<Point> initial_pos;						//initial player position chosen by server(unique for each player)
	ConcurrentHashMap<Integer, Integer> player_treasure;	// no of treasures held by each player
	HashSet<Integer> listIDS;						// list of all available clients
	
	boolean isbackup;
	boolean isprimary;
	boolean backup_down;
	boolean primary_down;
	boolean isThreadworking;
	int backup_id;
	int primary_id;
	String ip_primary;
	String ip_backup;
	
	ConcurrentHashMap<Integer,String> ipAddress;					// ipaddress of all clients
	
	public GameDetails()
	{
		
	}
	
	public GameDetails(int n1, int k1)
	{
		n =n1;
		k =k1;
		playerid = 10000;
		first = false;
		no_of_players=0;

		player_pos = new ConcurrentHashMap<Integer,Point>();
		player_treasure = new ConcurrentHashMap<Integer,Integer>();
		treasure_pos = getTreasureList(k,n);
		listIDS = new HashSet<Integer>();
		initial_pos = new HashSet<>();

		backup_down = false;				// there is a backup
		primary_down = false;
		gamewon = false;
		id_won = -1;

		avail_player_treasure = true;
		avail_player_pos = true;
		avail_treasure_pos = true;
		
		isbackup = false;
		isprimary = false;
		
		ipAddress = new ConcurrentHashMap<Integer,String>();
	
	}

	
	
	
	/*Returns a list of points containing the treasures*/
	public ArrayList<Point> getTreasureList(int k,int n)
	{

		ArrayList<Point> arr = new ArrayList<Point>();
		Random position = new Random();

		for(int i=0;i<k;i++)
		{
			Point pt = new Point();
			pt.x = position.nextInt(n);
			pt.y = position.nextInt(n);
			arr.add(pt);
		}

		return arr;
	}
}
