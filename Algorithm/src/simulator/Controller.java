package simulator;

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import algorithms.ExploreMaze;
import simulator.arena.Arena;


public class Controller {
	private static Controller _instance;
	private UI _ui;
	private int[] _robotPosition = new int[2];
	private int _speed, _coverage, _timeLimit;

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
		Arena arena = Arena.getInstance();
		arena.setLayout(mapGrids);
		_ui.setStatus("finished map loading");
	}

	public void clearMap(JButton[][] mapGrids) {
		for (int x = 0; x < Arena.MAP_WIDTH; x++) {
			for (int y = 0; y < Arena.MAP_LENGTH; y++) {
				if (mapGrids[x][y].getBackground() == Color.RED) {
					mapGrids[x][y].setBackground(Color.GREEN);
				}
			}
		}
		_ui.setStatus("finished map clearing");
	}

	public void InitRobotInMaze(JButton[][] mazeGrids, int x, int y) {
		if (x < 2 || x > 14 || y < 2 || y > 9) {
			_ui.setStatus("warning: robot position out of range");
			initMaze(mazeGrids);
		} else {
			for (int i = x - 1; i <= x + 1; i++) {
				for (int j = y - 1; j <= y + 1; j++) {
					if (i == x && j == y + 1) {
						mazeGrids[20 - j][i - 1].setBackground(Color.PINK);
					} else {
						mazeGrids[20 - j][i - 1].setBackground(Color.CYAN);
					}
				}
			}
			_robotPosition[0] = x;
			_robotPosition[1] = y;
			_ui.setStatus("robot initial position set");
		}
	}

	public void initMaze(JButton[][] mazeGrids) {
		for (int x = 0; x < Arena.MAP_WIDTH; x++) {
			for (int y = 0; y < Arena.MAP_LENGTH; y++) {
				mazeGrids[x][y].setBackground(Color.BLACK);
				if ((x >= 0 & x <= 2 & y >= 12 & y <= 14) || (y >= 0 & y <= 2 & x >= 17 & x <= 19)) {
					mazeGrids[x][y].setBackground(Color.ORANGE);
				}
			}
		}
	}

	public void setExploreSpeed(int speed) {
		_speed = speed;
		_ui.setStatus("robot speed set");
	}

	public void setCoverage(int coverage) {
		if (coverage > 100) {
			_ui.setStatus("warning: target coverage out of range");
		} else {
			_coverage = coverage;
			_ui.setStatus("target coverage set");
		}
	}

	public void setExploreTimeLimit(int limit) {
		_timeLimit = limit;
		_ui.setStatus("exploring time limit set");
	}

	public void exploreMaze() {
		Arena arena = Arena.getInstance();
		if (arena.getLayout() == null) {
			_ui.setStatus("warning: no layout loaded yet");
		} else {
			ExploreMaze.explore(_robotPosition, _speed, _coverage, _timeLimit);
		}
	}
}
