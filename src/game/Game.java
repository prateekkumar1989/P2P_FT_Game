package game; 

import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Game extends Remote
{
	GameDetails joingame() throws RemoteException;
	String strtgame(Point pos) throws RemoteException;
	boolean gameresult() throws RemoteException;
	GameDetails move(int id, char direction) throws RemoteException;	//true means primary, false means backup
	GameDetails updateBackup(int id, String ip) throws RemoteException;
	void Primary_updatebackup(GameDetails g) throws RemoteException;
	void updatebackupDown() throws RemoteException;
	GameDetails updateInfo() throws RemoteException;
	void backup_updateClient(GameDetails g) throws RemoteException;
	void runthread() throws RemoteException;
}
