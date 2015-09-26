package simulator;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import algorithms.AStarPathFinder;
import algorithms.MazeExplorer;
import algorithms.Path;
import datatypes.Orientation;
import simulator.arena.Arena;
import simulator.arena.FileReaderWriter;
import simulator.robot.Robot;


public class Controller {
	
	public static final String ARENA_DESCRIPTOR_PATH = System.getProperty("user.dir") + "/map-descriptors/arena.txt";
	private static final int THRESHOLD_BUFFER_TIME = 3;
	
	private static Controller _instance;
	private UI _ui;
	private Timer _timer;
	private int[] _robotPosition = new int[2];
	private Orientation _robotOrientation;
	private int _speed, _targetCoverage, _timeLimit;
	private boolean _hasReachedStart, _hasReachedTimeThreshold;

	private Controller() {
		_ui = new UI();
	}
	
	public boolean hasReachedTimeThreshold() {
		return _hasReachedTimeThreshold;
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
		
		Boolean[][] layout = arena.getLayout();
		String arenaDescriptor = "";
		
		for (int i = 0; i < Arena.MAP_WIDTH; i++) {
			for (int j = 0; j < Arena.MAP_LENGTH; j++) {
				if (layout[j][i] == true) {
					arenaDescriptor += "1";
				} else {
					arenaDescriptor += "0";
				}
			}
			arenaDescriptor = arenaDescriptor + System.getProperty("line.separator");
		}
		
		try {
			FileReaderWriter fileWriter = new FileReaderWriter(FileSystems.getDefault().getPath(ARENA_DESCRIPTOR_PATH));
			fileWriter.write(arenaDescriptor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		
		Arena arena = Arena.getInstance();
		arena.setLayout(mapGrids);
		
		_ui.setStatus("finished map clearing");
	}

	public void resetRobotInMaze(JButton[][] mazeGrids, int x, int y) {
		if (x < 2 || x > 14 || y < 2 || y > 9) {
			_ui.setStatus("warning: robot position out of range");
			resetMaze(mazeGrids);
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

	public void resetMaze(JButton[][] mazeGrids) {
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
			_targetCoverage = coverage;
			_ui.setStatus("target coverage set");
		}
	}

	public void setExploreTimeLimit(int limit) {
		_timeLimit = limit;
		_ui.setStatus("exploring time limit set");
	}

	class TimeClass implements ActionListener {
		int _counter; 
		float _threshold;
		
		public TimeClass(int timeLimit, int speed) {
			_counter = timeLimit;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_counter--;
			_ui.setTimeCounter(_counter);
			
			if (_counter >= 0) {
				SwingWorker<Void, Void> getThreshold = new SwingWorker<Void, Void>() {
					Path _backPath;
					@Override
					protected Void doInBackground() throws Exception {
						MazeExplorer explorer = MazeExplorer.getInstance();
						AStarPathFinder pathFinder = AStarPathFinder.getInstance();
						_backPath = pathFinder.findFastestPath(_robotPosition[0], _robotPosition[1], MazeExplorer.START[0], MazeExplorer.START[1], explorer.getMazeRef());
						_threshold = _backPath.getNumOfSteps() * (1 / (float)_speed) + THRESHOLD_BUFFER_TIME;
						//testing
//						System.out.println("threshold: " + _threshold);
						return null;
					}
				};
				getThreshold.execute();
				if (_counter <= _threshold) {
					_hasReachedTimeThreshold = true;
				}

				
				if (_counter == 0) {
					_timer.stop();
					Toolkit.getDefaultToolkit().beep();
				}
			} 
		}
	}
	
	public void exploreMaze() {
		_ui.refreshInput();
		Arena arena = Arena.getInstance();
		MazeExplorer explorer = MazeExplorer.getInstance();
		Robot robot = Robot.getInstance();
		if (arena.getLayout() == null) {
			_ui.setStatus("warning: no layout loaded yet");
		} else {
			TimeClass timeActionListener = new TimeClass(_timeLimit, _speed);
			SwingWorker<Void, Void> exploreMaze = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					robot.setSpeed(_speed);
					_hasReachedStart = false;
					explorer.explore(_robotPosition);
					return null;
				}
				@Override
				public void done() {
					_hasReachedStart = true;
					_ui.setStatus("robot exploration completed");
				}
			};
			SwingWorker<Void, Void> updateCoverage = new SwingWorker<Void, Void>() {
				float _coverage;
				@Override
				protected Void doInBackground() throws Exception {
					int numExplored;
					JButton[][] mazeGrids = _ui.getMazeGrids();
					while (!_hasReachedStart) { 
						numExplored = 0;
						for (int x = 0; x < Arena.MAP_WIDTH; x++) {
							for (int y = 0; y < Arena.MAP_LENGTH; y++) {
								if (mazeGrids[x][y].getBackground() != Color.BLACK) {
									numExplored ++;
								}
							}
						}
						_coverage = (float)(100 * numExplored) / (float)(Arena.MAP_LENGTH * Arena.MAP_WIDTH);
						_ui.setCoverage(String.format("%.1f", _coverage));
					}
					if (_timer.isRunning()) {
						_timer.stop();
					}
					return null;
				}
				@Override
				public void done() {
					if (_coverage >= _targetCoverage) {
						_ui.setStatus("target coverage reached through exploration");
					} else {
						_ui.setStatus("target coverage not reached through exploration");
					}
				}
			};
			
		
			_timer = new Timer(1000, timeActionListener);
			_timer.start();
			_ui.setStatus("robot exploring");
			_hasReachedTimeThreshold = false;
			exploreMaze.execute();
			updateCoverage.execute();
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
	}

	public void findFastestPath() {
		AStarPathFinder pathFinder = AStarPathFinder.getInstance();
		
		SwingWorker<Void, Void> findFastestPath = new SwingWorker<Void, Void>() {
			Path _fastestPath;
			@Override
			protected Void doInBackground() throws Exception {
				MazeExplorer explorer = MazeExplorer.getInstance();
				_fastestPath = pathFinder.findFastestPath(MazeExplorer.START[0], MazeExplorer.START[1], 
						MazeExplorer.GOAL[0], MazeExplorer.GOAL[1], explorer.getMazeRef());
				pathFinder.moveRobotAlongFastestPath(_fastestPath, Orientation.NORTH);
				ArrayList<Path.Step> steps = _fastestPath.getSteps();
				JButton[][] mazeGrids = _ui.getMazeGrids();
				for (Path.Step step : steps) {
					int x = step.getX();
					int y = step.getY();
					mazeGrids[19-y][x].setBackground(Color.YELLOW);
				}
				return null;
			}
			@Override
			public void done() {
				_ui.setStatus("fastest path found");
			}
		};
		findFastestPath.execute();
		
	}

}
