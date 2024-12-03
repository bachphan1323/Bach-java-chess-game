package main;

import javax.swing.JFrame;

public class Main {
	
	public static void main(String[] args) {
		JFrame gameWindow = new JFrame("Chess?Chess.");
		gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		gameWindow.setResizable(false);
		
		//add GamePanel to window
		GamePanel gp = new GamePanel();
		gameWindow.add(gp);
		gameWindow.pack();//the window adjust its size to the GamePanel
		
		gameWindow.setLocationRelativeTo(null);//so the window will be centered
		gameWindow.setVisible(true);
		
		gp.launchGame();
	}
}
