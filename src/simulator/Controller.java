package simulator;

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class Controller {
	private static Controller _instance;
	protected UI _ui;
	
	private Controller() {
		_ui = new UI();
		
	}

	public static Controller getInstance() {
		if (_instance == null) {
			_instance = new Controller();
		}
		return _instance;
	}
	
	public void run() {
		_ui.setVisible(true);
	}
	
	public void toggleObstacle(JButton[][] mapGrids, int x, int y) {
		if (mapGrids[x][y].getBackground() == Color.ORANGE) {
			mapGrids[x][y].setBackground(Color.BLACK);
		} else {
			mapGrids[x][y].setBackground(Color.ORANGE);
		}
	}
	
	public void switchComboBox(JComboBox cb, JPanel cardPanel) {
		CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
		cardLayout.show(cardPanel, (String) cb.getSelectedItem());
	}
	
	public void loadMap() {
		//TODO implement method
		System.out.println("loading map...");
	}
	
	public void clearMap(JButton[][] mapGrids) {
		for (int x = 0; x < UI.MAP_WIDTH; x++) {
			for (int y = 0; y < UI.MAP_LENGTH; y++) {
				if (mapGrids[x][y].getBackground() == Color.BLACK) {
					mapGrids[x][y].setBackground(Color.ORANGE);
				}
			}
		}
	}
}
