package simulator;

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class Controller {
	private static Controller _instance;
	private UI _ui;
	private Boolean[][] _layout;
	
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
		if (mapGrids[x][y].getBackground() == Color.GREEN) {
			mapGrids[x][y].setBackground(Color.RED);
		} else {
			mapGrids[x][y].setBackground(Color.GREEN);
		}
	}
	
	public void switchComboBox(JComboBox cb, JPanel cardPanel) {
		CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
		cardLayout.show(cardPanel, (String) cb.getSelectedItem());
	}
	
	public void loadMap(JButton[][] mapGrids) {
		_layout = new Boolean[UI.MAP_WIDTH][UI.MAP_LENGTH];
		for (int x = 0; x < UI.MAP_WIDTH; x++) {
			for (int y = 0; y < UI.MAP_LENGTH; y++) {
				if (mapGrids[x][y].getBackground() == Color.RED) {
					_layout[x][y] = true;
				} else {
					_layout[x][y] = false;
				}
			}
		}
		_ui.setStatus("finished map loading");
	}
	
	public void clearMap(JButton[][] mapGrids) {
		for (int x = 0; x < UI.MAP_WIDTH; x++) {
			for (int y = 0; y < UI.MAP_LENGTH; y++) {
				if (mapGrids[x][y].getBackground() == Color.RED) {
					mapGrids[x][y].setBackground(Color.GREEN);
				}
			}
		}
		_ui.setStatus("finished map clearing");
	}
}
