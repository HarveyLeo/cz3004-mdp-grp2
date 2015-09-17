package simulator;

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import algorithms.MazeExplorer;
import datatypes.Orientation;
import simulator.arena.Arena;


public class Controller {
	private static Controller _instance;
	private UI _ui;
	private int[] _robotPosition = new int[2];
	private Orientation _robotOrientation;
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
			_robotPosition[0] = x - 1;
			_robotPosition[1] = y - 1;
			_robotOrientation = Orientation.NORTH;
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
		_ui.refreshInput();
		Arena arena = Arena.getInstance();
		MazeExplorer explorer = MazeExplorer.getInstance();
		if (arena.getLayout() == null) {
			_ui.setStatus("warning: no layout loaded yet");
		} else {
			_ui.setStatus("robot exploring");
			SwingWorker<Void, Void> exploreMaze = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					explorer.explore(_robotPosition, _speed, _coverage, _timeLimit);
					return null;
				}

				@Override
				public void done() {
					_ui.setStatus("robot exploration completed");
				}
			};
			
			exploreMaze.execute();
		}
	}

	public void turnRobotRight() {
		JButton[][] mazeGrids = _ui.getMazeGrids();

		switch (_robotOrientation) {
			case NORTH:
				mazeGrids[18 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.CYAN);
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] + 1].setBackground(Color.PINK);
				_robotOrientation = Orientation.EAST;
				break;
			case SOUTH:
				mazeGrids[20 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.CYAN);
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] - 1].setBackground(Color.PINK);
				_robotOrientation = Orientation.WEST;
				break;
			case EAST:
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] + 1].setBackground(Color.CYAN);
				mazeGrids[20 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.PINK);
				_robotOrientation = Orientation.SOUTH;
				break;
			case WEST:
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] - 1].setBackground(Color.CYAN);
				mazeGrids[18 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.PINK);
				_robotOrientation = Orientation.NORTH;
		}
		updateMazeColor();
	}

	public void moveRobotForward() {
		JButton[][] mazeGrids = _ui.getMazeGrids();

		switch (_robotOrientation) {
			case NORTH:
				for (int i = 17 - _robotPosition[1]; i <= 19 - _robotPosition[1]; i++) {
					for (int j = _robotPosition[0] - 1; j <= _robotPosition[0] + 1; j++) {
						if (i == 17 - _robotPosition[1] && j == _robotPosition[0]) {
							mazeGrids[i][j].setBackground(Color.PINK);
						} else {
							mazeGrids[i][j].setBackground(Color.CYAN);
						}
					}
				}
				_robotPosition[1] = _robotPosition[1] + 1;
				break;
			case SOUTH:
				for (int i = 19 - _robotPosition[1]; i <= 21 - _robotPosition[1]; i++) {
					for (int j = _robotPosition[0] - 1; j <= _robotPosition[0] + 1; j++) {
						if (i == 21 - _robotPosition[1] && j == _robotPosition[0]) {
							mazeGrids[i][j].setBackground(Color.PINK);
						} else {
							mazeGrids[i][j].setBackground(Color.CYAN);
						}
					}
				}
				_robotPosition[1] = _robotPosition[1] - 1;
				break;
			case EAST:
				for (int i = 18 - _robotPosition[1]; i <= 20 - _robotPosition[1]; i++) {
					for (int j = _robotPosition[0]; j <= _robotPosition[0] + 2; j++) {
						if (i == 19 - _robotPosition[1] && j == _robotPosition[0] + 2) {
							mazeGrids[i][j].setBackground(Color.PINK);
						} else {
							mazeGrids[i][j].setBackground(Color.CYAN);
						}
					}
				}
				_robotPosition[0] = _robotPosition[0] + 1;
				break;
			case WEST:
				for (int i = 18 - _robotPosition[1]; i <= 20 - _robotPosition[1]; i++) {
					for (int j = _robotPosition[0] - 2; j <= _robotPosition[0]; j++) {
						if (i == 19 - _robotPosition[1] && j == _robotPosition[0] - 2) {
							mazeGrids[i][j].setBackground(Color.PINK);
						} else {
							mazeGrids[i][j].setBackground(Color.CYAN);
						}
					}
				}
				_robotPosition[0] = _robotPosition[0] - 1;
		}
		updateMazeColor();
	}

	public void turnRobotLeft() {
		JButton[][] mazeGrids = _ui.getMazeGrids();

		switch (_robotOrientation) {
			case NORTH:
				mazeGrids[18 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.CYAN);
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] - 1].setBackground(Color.PINK);
				_robotOrientation = Orientation.WEST;
				break;
			case SOUTH:
				mazeGrids[20 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.CYAN);
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] + 1].setBackground(Color.PINK);
				_robotOrientation = Orientation.EAST;
				break;
			case EAST:
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] + 1].setBackground(Color.CYAN);
				mazeGrids[18 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.PINK);
				_robotOrientation = Orientation.NORTH;
				break;
			case WEST:
				mazeGrids[19 - _robotPosition[1]][_robotPosition[0] - 1].setBackground(Color.CYAN);
				mazeGrids[20 - _robotPosition[1]][_robotPosition[0]].setBackground(Color.PINK);
				_robotOrientation = Orientation.SOUTH;
		}
		updateMazeColor();
	}
	
	private void updateMazeColor() {
		JButton[][] mazeGrids = _ui.getMazeGrids();
		MazeExplorer explorer = MazeExplorer.getInstance();
		int[][] mazeRef = explorer.getMazeRef();
		for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				if (mazeRef[i][j] == MazeExplorer.IS_EMPTY) {
					if (i < _robotPosition[0] - 1 || i > _robotPosition[0] + 1 ||
					j < _robotPosition[1] - 1 || j > _robotPosition[1] + 1) {
						if ((i >= MazeExplorer.START[0] - 1 && i <= MazeExplorer.START[0] + 1
								&& j >= MazeExplorer.START[1] - 1 && j <= MazeExplorer.START[1] + 1)
								|| (i >= MazeExplorer.GOAL[0] - 1 && i <= MazeExplorer.GOAL[0] + 1
										&& j >= MazeExplorer.GOAL[1] - 1 && j <= MazeExplorer.GOAL[1] + 1)) {
							mazeGrids[19-j][i].setBackground(Color.ORANGE);
						} else {
							mazeGrids[19-j][i].setBackground(Color.GREEN);
						}
					}
				} else if (mazeRef[i][j] == MazeExplorer.IS_OBSTACLE) {
					mazeGrids[19-j][i].setBackground(Color.RED);
				}
			}
		}
		//testing
		System.out.println("UI's robot position: " + _robotPosition[0] + " " + _robotPosition[1]);
	}
}
