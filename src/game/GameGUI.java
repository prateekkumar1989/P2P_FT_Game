package game; 

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.border.*;
import javax.xml.ws.Response;

public class GameGUI {

    private final JPanel gui = new JPanel(new BorderLayout(3, 3));
    private JButton[][] gameBoardSquares = new JButton[100][100];
    private JPanel gameBoard;
    private final JLabel message = new JLabel("Peer");
    int n;
    GameDetails o = new GameDetails();
    volatile boolean ifnorth = false;
    volatile boolean ifsouth = false;
    volatile boolean ifeast = false;
    volatile boolean ifwest = false;
    volatile boolean ifnomove = false;
    
    GameGUI(GameDetails response) {
    	n = response.n;
    	o = response;
        initializeGui();
    }

    synchronized public final void initializeGui() {
        // set up the main GUI
        gui.setBorder(new EmptyBorder(5, 5, 5, 5));
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        gui.add(tools, BorderLayout.PAGE_START);
        
        JButton north = new JButton("North");
        JButton south = new JButton("South");
        JButton east = new JButton("East");
        JButton west = new JButton("West");
        JButton nomove = new JButton("NoMove");
        
        tools.add(north); 
        tools.add(south); 
        tools.add(east); 
        tools.add(west); 
        tools.addSeparator();
        tools.add(nomove);
        tools.addSeparator();
        tools.add(message);

        gameBoard = new JPanel(new GridLayout(0, n+1));
        gameBoard.setBorder(new LineBorder(Color.BLACK));
        gui.add(gameBoard);

        Insets buttonMargin = new Insets(0,0,0,0);
        for (int i = 0; i < gameBoardSquares.length; i++) {
            for (int j = 0; j < gameBoardSquares[i].length; j++) {
                JButton b = new JButton();
                b.setMargin(buttonMargin);

                ImageIcon icon = new ImageIcon(
                        new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB));
                b.setIcon(icon);
                b.setBackground(Color.LIGHT_GRAY);
                gameBoardSquares[i][j] = b;
            }
        }
        
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                switch (j) {
                    case 0:
                        gameBoard.add(new JLabel("" + (n-i-1),
                                SwingConstants.CENTER));
                    default:
                        gameBoard.add(gameBoardSquares[i][j]);
                }
            }
        }
        gameBoard.add(new JLabel(""));
        for (int i = 0; i < n; i++) {
        	gameBoard.add(new JLabel("" + i,
                    SwingConstants.CENTER));
        }
        
        for(Point p : o.treasure_pos)
		{
        	gameBoardSquares[n-p.y-1][p.x].setBackground(Color.GREEN); //x,y -> n-y-1,x
		}
        for(Point p : o.initial_pos)
		{
        	gameBoardSquares[n-p.y-1][p.x].setBackground(Color.RED); //x,y -> n-y-1,x
		}
        gameBoardSquares[n-o.pos.y-1][o.pos.x].setBackground(Color.BLUE);
        
        north.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
        	ifnorth = true; }});
        south.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
        	ifsouth = true; }});
        east.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
        	ifeast = true; }});
        west.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
        	ifwest = true; }});
        nomove.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
        	ifnomove = true;
        	}});
    }

    synchronized public final void repaintGui(GameDetails obj) {
    	
    	for(int i=0;i<obj.treasure_pos.size();i++)
    		System.out.print(obj.treasure_pos.get(i)+" ");
    	
    	message.setText("Peer");
    	//System.out.println("Repainting GUI for id" + o.playerid); 
    	for (int i = 0; i < o.n; i++) {
            for (int j = 0; j < o.n; j++) {
            	gameBoardSquares[i][j].setBackground(Color.LIGHT_GRAY);
            }
    	}
    	
    	for(int k : obj.player_pos.keySet())
		{
    		//System.out.println("Player pos red at " + obj.player_pos.get(k).x + "," + obj.player_pos.get(k).x);
    		gameBoardSquares[n-(obj.player_pos.get(k).y)-1][obj.player_pos.get(k).x].setBackground(Color.RED); //x,y -> n-y-1,x
		}
    	
    	//System.out.println("Player " + o.playerid +" blue at " + obj.player_pos.get(o.playerid).x + "," + obj.player_pos.get(o.playerid).y);
        gameBoardSquares[n-(obj.player_pos.get(o.playerid).y)-1][obj.player_pos.get(o.playerid).x].setBackground(Color.BLUE);
    	//gameBoardSquares[n-o.pos.y-1][o.pos.x].setBackground(Color.BLUE);
       
        for(Point p : obj.treasure_pos)
		{
    		//System.out.println("Treasure pos green at " + p.x + ", " + p.y);
        	gameBoardSquares[n-p.y-1][p.x].setBackground(Color.GREEN); //x,y -> n-y-1,x
		}
        if(o.playerid == obj.primary_id) message.setText("Primary");
        if(o.playerid == obj.backup_id) message.setText("Backup");
        
        message.setText( message.getText() + " | #=" + obj.player_treasure.get(o.playerid));
        if(obj.gamewon && o.playerid == obj.id_won) 
        { message.setText( message.getText() + " | WINNER!" );  }
        else
        { message.setText( message.getText() + " | Play" );  }
    }
    
    public final JComponent getgameBoard() {
        return gameBoard;
    }

    public final JComponent getGui() {
        return gui;
    }
}