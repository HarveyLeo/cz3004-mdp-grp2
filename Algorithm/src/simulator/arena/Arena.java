package simulator.arena;

import java.awt.Color;

import javax.swing.JButton;

import simulator.Controller;
import simulator.UI;


public class Arena {
	
	public static final int MAP_WIDTH = 20;
	public static final int MAP_LENGTH = 15;
	
	private static Arena _instance;
	private Boolean[][] _layout;
	
	private Arena() {
	}
	
	public static Arena getInstance() {
		if (_instance == null) {
			_instance = new Arena();
		}
		return _instance;
	}
	
	public Boolean[][] getLayout() {
		return _layout;
	}
	
	public void setLayout(JButton[][] mapGrids) {
		_layout = new Boolean[MAP_LENGTH][MAP_WIDTH];
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_LENGTH; y++) {
				if (mapGrids[x][y].getBackground() == Color.RED) {
					_layout[y][19-x] = true;
				} else {
					_layout[y][19-x] = false;
				}
			}
		}
		
	}
}
