package algorithms;

import datatypes.Movement;
import datatypes.Orientation;
import simulator.Controller;
import simulator.arena.Arena;
import simulator.robot.Robot;
import simulator.robot.Sensor;

public class MazeExplorer {
	
	public static final int UNEXPLORED = -1;
	public static final int IS_OBSTACLE = 1;
	public static final int IS_EMPTY = 0;
	private static final int RIGHT_NO_ACCESS = -1;
	private static final int RIGHT_UNSURE_ACCESS = -2;
	private static final int RIGHT_CAN_ACCESS = -3;
	public static final int[] GOAL = {13, 18};
	public static final int[] START = {1, 1};
	
	private static MazeExplorer _instance;
	private Boolean[][] _isExplored;
	private int[][] _mazeRef;
	private Robot _robot;
	private int[] _robotPosition;
	private Orientation _robotOrientation;
	private boolean _hasExploredTillGoal;
	
	private MazeExplorer() {
		
	}
	
	public int[][] getMazeRef() {
		return _mazeRef;
	}
	
	public Boolean[][] getIsExplored() {
		return _isExplored;
	}
	
	public boolean hasExploredTillGoal() {
		return _hasExploredTillGoal;
	}
	
    public static MazeExplorer getInstance() {
        if (_instance == null) {
            _instance = new MazeExplorer();
        }
        return _instance;
    }
    
	public String getP2Descriptor() {
		String P2Binary = "";
		for (int j = 0; j < Arena.MAP_WIDTH; j++) {
			for (int i = 0; i < Arena.MAP_LENGTH; i++) {
				if (_mazeRef[i][j] == IS_OBSTACLE) {
					P2Binary = P2Binary + 1;
				} else if (_mazeRef[i][j] == IS_EMPTY) {
					P2Binary = P2Binary + 0;
				}
			}
		}
		
		int remainder = P2Binary.length() % 8;
		
		
		for (int i = 0; i < 8 - remainder; i++) {
			P2Binary = P2Binary + "0";
		}
		
		String temp, P2HexStr = "";
		int index = 0;
		
		while (index < P2Binary.length()) {
			temp = "";
			for (int i = 0; i < 4; i++) {
				temp = temp + P2Binary.charAt(index);
				index++;
			}
			P2HexStr = P2HexStr + Integer.toString(Integer.parseInt(temp, 2), 16);
		}
		
		return P2HexStr;
	}
	
	public String getP1Descriptor() {
		String P1Binary = "11";
		int value;
		for (int j = 0; j < Arena.MAP_WIDTH; j++) {
			for (int i = 0; i < Arena.MAP_LENGTH; i++) {
				value = (_isExplored[i][j]) ? 1 : 0;
				P1Binary = P1Binary + value;
			}
		}
		P1Binary = P1Binary + "11";
		String temp, P1HexStr = "";
		int index = 0;
		
		while (index < P1Binary.length()) {
			temp = "";
			for (int i = 0; i < 4; i++) {
				temp = temp + P1Binary.charAt(index);
				index++;
			}
			P1HexStr = P1HexStr + Integer.toString(Integer.parseInt(temp, 2), 16);
		}

		return P1HexStr;
	}
    
	public void explore(int[] robotPosition) {

		init(robotPosition);
		
		for (int i = robotPosition[0] - 1; i <= robotPosition[0] + 1; i++) {
			for (int j = robotPosition[1] - 1; j <= robotPosition[1] + 1; j++) {
				_isExplored[i][j] = true;
				_mazeRef[i][j] = IS_EMPTY;
			}
		}
		
		setIsExplored(robotPosition, _robotOrientation);

		exploreAlongWall (GOAL);
		
		Controller controller = Controller.getInstance();
		
		if (!controller.hasReachedTimeThreshold()) {
			_hasExploredTillGoal = true;
			exploreAlongWall (START);
		} else {
			_hasExploredTillGoal = false; //Timeout before reaching goal
		}

		if (!isGoalPos(_robotPosition, START)) { //Timeout before reaching start
			AStarPathFinder pathFinder = AStarPathFinder.getInstance();
			Path backPath = pathFinder.findFastestPath(_robotPosition[0], _robotPosition[1], START[0], START[1], _mazeRef);
			_robotOrientation = pathFinder.moveRobotAlongFastestPath(backPath, _robotOrientation, true);
		} else {
			while (!controller.hasReachedTimeThreshold()) {
				ExploreNextRound(_robotPosition);
			}
		}

		adjustOrientationTo(Orientation.NORTH);

	}

	private void ExploreNextRound(int[] currentRobotPosition) {
		VirtualMap virtualMap = VirtualMap.getInstance();
		AStarPathFinder pathFinder = AStarPathFinder.getInstance();
		Path fastestPath;
		int[] nextRobotPosition;
		virtualMap.updateVirtualMap(_mazeRef);
		for (int obsY = 0; obsY < Arena.MAP_WIDTH; obsY++) {
			for (int obsX = 0; obsX < Arena.MAP_LENGTH; obsX++) {
				if (_mazeRef[obsX][obsY] == UNEXPLORED){
					System.out.println("start to find nearest robot position to " + obsX + " " + obsY);
					nextRobotPosition = getNearestRobotPositionTo(obsX, obsY, virtualMap);
					
					//testing
					if (nextRobotPosition == null) {
						System.out.println("null");
					} else {
						System.out.println("robot will move to " + nextRobotPosition[0] + " " + nextRobotPosition[1]);
					}
					
					if (nextRobotPosition == null) {
						continue;
					}
					
					fastestPath = pathFinder.findFastestPath(currentRobotPosition[0], currentRobotPosition[1], nextRobotPosition[0], nextRobotPosition[1], _mazeRef);
					
					//Testing
					for (Path.Step step: fastestPath.getSteps()) {
						System.out.print(step.getX() + " " + step.getY() + "      ");
					}
					
					_robotOrientation = pathFinder.moveRobotAlongFastestPath(fastestPath, _robotOrientation, true);
					
					if (_robotPosition[0] > obsX) {
						adjustOrientationTo(Orientation.WEST);
					} else if (_robotPosition[0] < obsX) {
						adjustOrientationTo(Orientation.EAST);
					} else if  (_robotPosition[1] > obsY) {
						adjustOrientationTo(Orientation.SOUTH);
					} else if  (_robotPosition[1] < obsY) {
						adjustOrientationTo(Orientation.NORTH);
					}
					currentRobotPosition = nextRobotPosition;
				//Testing
//					if (_robotPosition[0] == 7) {
//						return;
//					}
					System.out.println("robot position from MazeExplorer: " + _robotPosition[0] + " " + _robotPosition[1]);
					System.out.println("robot ori from MazeExplorer: " + _robotOrientation);
					/*return;*/
				}
			}
		}
	}

	private int[] getNearestRobotPositionTo (int obsX, int obsY, VirtualMap virtualMap) {
		int nearestPosition[] = new int[2];
		boolean[][] cleared = virtualMap.getCleared();
		boolean isClearedAhead;
		
		for (int radius = 2; radius < Arena.MAP_WIDTH; radius ++) {
			for (int y = 0; y < Arena.MAP_WIDTH; y++) {
				for (int x = 0; x < Arena.MAP_LENGTH; x++) {
					if (x == obsX - radius || x == obsX + radius || y == obsY - radius || y == obsY + radius) {
						if (x >= 0 && y >= 0 && x < Arena.MAP_LENGTH && y < Arena.MAP_WIDTH) {
							if (cleared[x][y]) {
								if ((x + y - obsX - obsY == radius || obsX + obsY - x - y == radius) && (x == obsX || y == obsY)) {
									isClearedAhead = true;
									if (x > obsX) {
										for(int i = obsX + 1; i < x; i++) {
												if (_mazeRef[i][y] != IS_EMPTY) {
													isClearedAhead = false;
											}
										}
									} else if (x < obsX) {
										for(int i = x + 1; i < obsX; i++) {
											if (_mazeRef[i][y] != IS_EMPTY) {
												isClearedAhead = false;
											}
										}
									} else if (y > obsY) {
										for(int j = obsY + 1; j < y; j++) {
											if (_mazeRef[x][j] != IS_EMPTY) {
												isClearedAhead = false;
											}
										}
									} else if (y < obsY) {
										for(int j = y + 1; j < obsY; j++) {
											if (_mazeRef[x][j] != IS_EMPTY) {
												isClearedAhead = false;
											}
										}
									}
									if (isClearedAhead) {
										nearestPosition[0] = x;
										nearestPosition[1] = y;
										return nearestPosition;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private void adjustOrientationTo(Orientation ori) {
		switch (ori) {
			case NORTH:
				if (_robotOrientation == Orientation.SOUTH) {
					_robotOrientation = Orientation.WEST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					_robotOrientation = Orientation.NORTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
				} else if (_robotOrientation == Orientation.EAST) {
					_robotOrientation = Orientation.NORTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnLeft();
				} else if (_robotOrientation == Orientation.WEST) {
					_robotOrientation = Orientation.NORTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
				}
				break;
			case SOUTH:
				if (_robotOrientation == Orientation.NORTH) {
					_robotOrientation = Orientation.EAST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					_robotOrientation = Orientation.SOUTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
				} else if (_robotOrientation == Orientation.EAST) {
					_robotOrientation = Orientation.SOUTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
				} else if (_robotOrientation == Orientation.WEST) {
					_robotOrientation = Orientation.SOUTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnLeft();
				}
				break;
			case EAST:
				if (_robotOrientation == Orientation.NORTH) {
					_robotOrientation = Orientation.EAST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
				} else if (_robotOrientation == Orientation.SOUTH) {
					_robotOrientation = Orientation.EAST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnLeft();
				} else if (_robotOrientation == Orientation.WEST) {
					_robotOrientation = Orientation.NORTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robotOrientation = Orientation.EAST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					_robot.turnRight();
				}
				break;
			case WEST:
				if (_robotOrientation == Orientation.NORTH) {
					_robotOrientation = Orientation.WEST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnLeft();
				} else if (_robotOrientation == Orientation.SOUTH) {
					_robotOrientation = Orientation.WEST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
				} else if (_robotOrientation == Orientation.EAST) {
					_robotOrientation = Orientation.SOUTH;
					setIsExplored(_robotPosition, _robotOrientation);
					_robotOrientation = Orientation.WEST;
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					_robot.turnRight();
				}
		}
		
		
	}

	private void init(int[] robotPosition) {
		_robot = Robot.getInstance();
		_robotPosition = new int[2];
		_robotPosition[0] = robotPosition[0];
		_robotPosition[1] = robotPosition[1];
		_robotOrientation = Orientation.NORTH;
		_hasExploredTillGoal = false;
		_isExplored = new Boolean[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		_mazeRef = new int[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				_isExplored[i][j] = false;
			}
		}
		for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				_mazeRef[i][j] = UNEXPLORED;
			}
		}
	}
	
	private void exploreAlongWall (int[] goalPos) {
		
		Controller controller = Controller.getInstance();

		while (!isGoalPos(_robotPosition, goalPos) && !controller.hasReachedTimeThreshold()) {
			int rightStatus = checkRightSide(_robotPosition, _robotOrientation);
			if (rightStatus != RIGHT_NO_ACCESS) {
				if (rightStatus == RIGHT_UNSURE_ACCESS) {
					updateRobotOrientation(Movement.TURN_RIGHT);
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					if (hasAccessibleFront(_robotPosition, _robotOrientation)) {
						updateRobotPositionAfterMF(_robotOrientation, _robotPosition);
						setIsExplored(_robotPosition, _robotOrientation);
						_robot.moveForward();
					} else {
						updateRobotOrientation(Movement.TURN_LEFT);
						_robot.turnLeft();
					}
				} else { //rightStatus == RIGHT_CAN_ACCESS
					updateRobotOrientation(Movement.TURN_RIGHT);
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					updateRobotPositionAfterMF(_robotOrientation, _robotPosition);
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.moveForward();
				}
			} else if (hasAccessibleFront(_robotPosition, _robotOrientation)){ 
				updateRobotPositionAfterMF(_robotOrientation, _robotPosition);
				setIsExplored(_robotPosition, _robotOrientation);
				_robot.moveForward();
			} else {
				updateRobotOrientation(Movement.TURN_LEFT);
				setIsExplored(_robotPosition, _robotOrientation);
				_robot.turnLeft();
			}
		}
	}

	public Orientation updateRobotOrientation (Movement move) {
		switch (move) {
			case TURN_LEFT:
				if (_robotOrientation == Orientation.NORTH) {
					_robotOrientation = Orientation.WEST;
				} else if (_robotOrientation == Orientation.SOUTH) {
					_robotOrientation = Orientation.EAST;
				} else if (_robotOrientation == Orientation.EAST) {
					_robotOrientation = Orientation.NORTH;
				} else {
					_robotOrientation = Orientation.SOUTH;
				}
				break;
			case TURN_RIGHT:
				if (_robotOrientation == Orientation.NORTH) {
					_robotOrientation = Orientation.EAST;
				} else if (_robotOrientation == Orientation.SOUTH) {
					_robotOrientation = Orientation.WEST;
				} else if (_robotOrientation == Orientation.EAST) {
					_robotOrientation = Orientation.SOUTH;
				} else {
					_robotOrientation = Orientation.NORTH;
				}
				break;
			case MOVE_FORWARD:
				
		}
		return _robotOrientation;
	}
	
	private boolean isGoalPos (int[] curPos, int[] goalPos) {
		if (curPos[0] == goalPos[0] && curPos[1] == goalPos[1]) {
			return true;
		}
		return false;
	}
	
	public int[] updateRobotPositionAfterMF(Orientation robotOrientation, int[] curRobotPosition) {
		switch (robotOrientation) {
			case NORTH:
				_robotPosition[0] = curRobotPosition[0];
				_robotPosition[1] = curRobotPosition[1] + 1;
				break;
			case SOUTH:
				_robotPosition[0] = curRobotPosition[0];
				_robotPosition[1] = curRobotPosition[1] - 1;
				break;
			case EAST:
				_robotPosition[0] = curRobotPosition[0] + 1;
				_robotPosition[1] = curRobotPosition[1];
				break;
			case WEST:
				_robotPosition[0] = curRobotPosition[0] - 1;
				_robotPosition[1] = curRobotPosition[1];
		}
		return _robotPosition;
	}
	
	private int checkRightSide (int[] curPos, Orientation ori) {
		int[] rightPos = new int[2];
		boolean hasUnexplored = false;
		switch (ori) {
			case NORTH:
				rightPos[0] = curPos[0] + 1;
				rightPos[1] = curPos[1];
				if (rightPos[0] + 1 >= Arena.MAP_LENGTH) {
					return RIGHT_NO_ACCESS;
				}
				for (int j = rightPos[1] - 1; j <= rightPos[1] + 1; j++) {
					if (_mazeRef[rightPos[0] + 1][j] == IS_OBSTACLE) {
							return RIGHT_NO_ACCESS;
					} else if (_mazeRef[rightPos[0] + 1][j] == UNEXPLORED) {
						hasUnexplored = true;
					}
				}
				break;
			case SOUTH:
				rightPos[0] = curPos[0] - 1;
				rightPos[1] = curPos[1];
				if (rightPos[0] - 1 < 0) {
					return RIGHT_NO_ACCESS;
				}
				for (int j = rightPos[1] - 1; j <= rightPos[1] + 1; j++) {
					if (_mazeRef[rightPos[0] - 1][j] == IS_OBSTACLE) {
							return RIGHT_NO_ACCESS;
					} else if (_mazeRef[rightPos[0] - 1][j] == UNEXPLORED) {
						hasUnexplored = true;
					}
				}
				break;
			case EAST:
				rightPos[0] = curPos[0];
				rightPos[1] = curPos[1] - 1;
				if (rightPos[1] - 1 < 0) {
					return RIGHT_NO_ACCESS;
				}
				for (int j = rightPos[0] - 1; j <= rightPos[0] + 1; j++) {
					if (_mazeRef[j][rightPos[1] - 1] == IS_OBSTACLE) {
							return RIGHT_NO_ACCESS;
					} else if (_mazeRef[j][rightPos[1] - 1] == UNEXPLORED) {
						hasUnexplored = true;
					}
				}
				break;
			case WEST:
				rightPos[0] = curPos[0];
				rightPos[1] = curPos[1] + 1;
				if (rightPos[1] + 1 >= Arena.MAP_WIDTH) {
					return RIGHT_NO_ACCESS;
				}
				for (int j = rightPos[0] - 1; j <= rightPos[0] + 1; j++) {
					if (_mazeRef[j][rightPos[1] + 1] == IS_OBSTACLE) {
							return RIGHT_NO_ACCESS;
					} else if (_mazeRef[j][rightPos[1] + 1] == UNEXPLORED) {
						hasUnexplored = true;
					}
				}
		}
		
		if (hasUnexplored) {
			return RIGHT_UNSURE_ACCESS;
		} else {
			return RIGHT_CAN_ACCESS;
		}
	}
	
	private boolean hasAccessibleFront(int[] curPos, Orientation ori) {
		int[] frontPos = new int[2];

		switch (ori) {
			case NORTH:
				frontPos[0] = curPos[0];
				frontPos[1] = curPos[1] + 1;
				if (frontPos[1] + 1 >= Arena.MAP_WIDTH) {
					return false;
				}
				for (int i = frontPos[0] - 1; i <= frontPos[0] + 1; i++) {
					if (_mazeRef[i][frontPos[1] + 1] == IS_OBSTACLE) {
							return false;
					}
				}
				return true;
			case SOUTH:
				frontPos[0] = curPos[0];
				frontPos[1] = curPos[1] - 1;
				if (frontPos[1] - 1 < 0) {
					return false;
				}
				for (int i = frontPos[0] - 1; i <= frontPos[0] + 1; i++) {
					if (_mazeRef[i][frontPos[1] - 1] == IS_OBSTACLE) {
							return false;
					}
				}
				return true;
			case EAST:
				frontPos[0] = curPos[0] + 1;
				frontPos[1] = curPos[1];
				if (frontPos[0] + 1 >= Arena.MAP_LENGTH) {
					return false;
				}
				for (int i = frontPos[1] - 1; i <= frontPos[1] + 1; i++) {
					if (_mazeRef[frontPos[0] + 1][i] == IS_OBSTACLE) {
							return false;
					}
				}
				return true;
			case WEST:
				frontPos[0] = curPos[0] - 1;
				frontPos[1] = curPos[1];
				if (frontPos[0] - 1 < 0) {
					return false;
				}
				for (int i = frontPos[1] - 1; i <= frontPos[1] + 1; i++) {
					if (_mazeRef[frontPos[0] - 1][i] == IS_OBSTACLE) {
							return false;
					}
				}
				return true;
		}
		return false;
	}

	public void setIsExplored(int[] robotPosition, Orientation ori) {
		int[] frontSensorPosition = new int[2];
		int[] frontleftSensorPosition = new int[2];
		int[] frontrightSensorPosition = new int[2];
		int[] leftSensorPosition = new int[2];
		int[] rightSensorPosition = new int[2];
		int numOfClearGrids;

		switch (ori) {
		case NORTH:
			frontSensorPosition[0] = robotPosition[0];
			frontSensorPosition[1] = robotPosition[1];
			frontleftSensorPosition[0] = robotPosition[0] - 1;
			frontleftSensorPosition[1] = robotPosition[1];
			frontrightSensorPosition[0] = robotPosition[0] + 1;
			frontrightSensorPosition[1] = robotPosition[1];
			leftSensorPosition[0] = robotPosition[0];
			leftSensorPosition[1] = robotPosition[1] + 1;
			rightSensorPosition[0] = robotPosition[0];
			rightSensorPosition[1] = robotPosition[1] + 1;
			
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[frontSensorPosition[0]][frontSensorPosition[1] + i] = true;
				_mazeRef[frontSensorPosition[0]][frontSensorPosition[1] + i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && frontSensorPosition[1] + numOfClearGrids + 1 < Arena.MAP_WIDTH) {
				_isExplored[frontSensorPosition[0]][frontSensorPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef[frontSensorPosition[0]][frontSensorPosition[1] + numOfClearGrids + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontleftSensorPosition[0]][frontleftSensorPosition[1] + i] = true;
				_mazeRef[frontleftSensorPosition[0]][frontleftSensorPosition[1] + i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontleftSensorPosition[1] + numOfClearGrids + 1 < Arena.MAP_WIDTH) {
				_isExplored[frontleftSensorPosition[0]][frontleftSensorPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef[frontleftSensorPosition[0]][frontleftSensorPosition[1] + numOfClearGrids + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontrightSensorPosition[0]][frontrightSensorPosition[1] + i] = true;
				_mazeRef[frontrightSensorPosition[0]][frontrightSensorPosition[1] + i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontrightSensorPosition[1] + numOfClearGrids + 1 < Arena.MAP_WIDTH) {
				_isExplored[frontrightSensorPosition[0]][frontrightSensorPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef[frontrightSensorPosition[0]][frontrightSensorPosition[1] + numOfClearGrids + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[leftSensorPosition[0] - i][leftSensorPosition[1]] = true;
				_mazeRef[leftSensorPosition[0] - i][leftSensorPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && leftSensorPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[leftSensorPosition[0] - numOfClearGrids - 1][leftSensorPosition[1]] = true;
				_mazeRef[leftSensorPosition[0] - numOfClearGrids - 1][leftSensorPosition[1]] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[rightSensorPosition[0] + i][rightSensorPosition[1]] = true;
				_mazeRef[rightSensorPosition[0] + i][rightSensorPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && rightSensorPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[rightSensorPosition[0] + numOfClearGrids + 1][rightSensorPosition[1]] = true;
				_mazeRef[rightSensorPosition[0] + numOfClearGrids + 1][rightSensorPosition[1]] = IS_OBSTACLE;
			}
			break;
			
		case SOUTH:
			frontSensorPosition[0] = robotPosition[0];
			frontSensorPosition[1] = robotPosition[1];
			frontleftSensorPosition[0] = robotPosition[0] + 1;
			frontleftSensorPosition[1] = robotPosition[1];
			frontrightSensorPosition[0] = robotPosition[0] - 1;
			frontrightSensorPosition[1] = robotPosition[1];
			leftSensorPosition[0] = robotPosition[0];
			leftSensorPosition[1] = robotPosition[1] - 1;
			rightSensorPosition[0] = robotPosition[0];
			rightSensorPosition[1] = robotPosition[1] - 1;
			
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[frontSensorPosition[0]][frontSensorPosition[1] - i] = true;
				_mazeRef[frontSensorPosition[0]][frontSensorPosition[1] - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && frontSensorPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[frontSensorPosition[0]][frontSensorPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef[frontSensorPosition[0]][frontSensorPosition[1] - numOfClearGrids - 1] = IS_OBSTACLE;
			}
			
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontleftSensorPosition[0]][frontleftSensorPosition[1] - i] = true;
				_mazeRef[frontleftSensorPosition[0]][frontleftSensorPosition[1] - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontleftSensorPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[frontleftSensorPosition[0]][frontleftSensorPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef[frontleftSensorPosition[0]][frontleftSensorPosition[1] - numOfClearGrids - 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontrightSensorPosition[0]][frontrightSensorPosition[1] - i] = true;
				_mazeRef[frontrightSensorPosition[0]][frontrightSensorPosition[1] - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontrightSensorPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[frontrightSensorPosition[0]][frontrightSensorPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef[frontrightSensorPosition[0]][frontrightSensorPosition[1] - numOfClearGrids - 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[leftSensorPosition[0] + i][leftSensorPosition[1]] = true;
				_mazeRef[leftSensorPosition[0] + i][leftSensorPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && leftSensorPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[leftSensorPosition[0] + numOfClearGrids + 1][leftSensorPosition[1]] = true;
				_mazeRef[leftSensorPosition[0] + numOfClearGrids + 1][leftSensorPosition[1]] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[rightSensorPosition[0] - i][rightSensorPosition[1]] = true;
				_mazeRef[rightSensorPosition[0] - i][rightSensorPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && rightSensorPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[rightSensorPosition[0] - numOfClearGrids - 1][rightSensorPosition[1]] = true;
				_mazeRef[rightSensorPosition[0] - numOfClearGrids - 1][rightSensorPosition[1]] = IS_OBSTACLE;
			}
			break;
		case EAST:
			frontSensorPosition[0] = robotPosition[0];
			frontSensorPosition[1] = robotPosition[1];
			frontleftSensorPosition[0] = robotPosition[0];
			frontleftSensorPosition[1] = robotPosition[1] + 1;
			frontrightSensorPosition[0] = robotPosition[0];
			frontrightSensorPosition[1] = robotPosition[1] - 1;
			leftSensorPosition[0] = robotPosition[0] + 1;
			leftSensorPosition[1] = robotPosition[1];
			rightSensorPosition[0] = robotPosition[0] + 1;
			rightSensorPosition[1] = robotPosition[1];
			
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[frontSensorPosition[0] + i][frontSensorPosition[1]] = true;
				_mazeRef[frontSensorPosition[0] + i][frontSensorPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && frontSensorPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[frontSensorPosition[0] + numOfClearGrids + 1][frontSensorPosition[1]] = true;
				_mazeRef [frontSensorPosition[0] + numOfClearGrids + 1][frontSensorPosition[1]] = IS_OBSTACLE;
			}
			
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontleftSensorPosition[0] + i][frontleftSensorPosition[1]] = true;
				_mazeRef[frontleftSensorPosition[0] + i][frontleftSensorPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontleftSensorPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[frontleftSensorPosition[0] + numOfClearGrids + 1][frontleftSensorPosition[1]] = true;
				_mazeRef [frontleftSensorPosition[0] + numOfClearGrids + 1][frontleftSensorPosition[1]] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontrightSensorPosition[0] + i][frontrightSensorPosition[1]] = true;
				_mazeRef [frontrightSensorPosition[0] + i][frontrightSensorPosition[1]]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontrightSensorPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[frontrightSensorPosition[0] + numOfClearGrids + 1][frontrightSensorPosition[1]] = true;
				_mazeRef [frontrightSensorPosition[0] + numOfClearGrids + 1][frontrightSensorPosition[1]] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[leftSensorPosition[0]][leftSensorPosition[1] + i] = true;
				_mazeRef [leftSensorPosition[0]][leftSensorPosition[1] + i]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && leftSensorPosition[1] + numOfClearGrids + 1 < Arena.MAP_WIDTH) {
				_isExplored[leftSensorPosition[0]][leftSensorPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef [leftSensorPosition[0]][leftSensorPosition[1] + numOfClearGrids + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[rightSensorPosition[0]][rightSensorPosition[1] - i] = true;
				_mazeRef [rightSensorPosition[0]][rightSensorPosition[1] - i]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && rightSensorPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[rightSensorPosition[0]][rightSensorPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef [rightSensorPosition[0]][rightSensorPosition[1] - numOfClearGrids - 1] = IS_OBSTACLE;
			}
			break;
		case WEST:
			frontSensorPosition[0] = robotPosition[0];
			frontSensorPosition[1] = robotPosition[1];
			frontleftSensorPosition[0] = robotPosition[0];
			frontleftSensorPosition[1] = robotPosition[1] - 1;
			frontrightSensorPosition[0] = robotPosition[0];
			frontrightSensorPosition[1] = robotPosition[1] + 1;
			leftSensorPosition[0] = robotPosition[0] - 1;
			leftSensorPosition[1] = robotPosition[1];
			rightSensorPosition[0] = robotPosition[0] - 1;
			rightSensorPosition[1] = robotPosition[1];
	
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[frontSensorPosition[0] - i][frontSensorPosition[1]] = true;		
				_mazeRef[frontSensorPosition[0] - i][frontSensorPosition[1]]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && frontSensorPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[frontSensorPosition[0] - numOfClearGrids - 1][frontSensorPosition[1]] = true;
				_mazeRef[frontSensorPosition[0] - numOfClearGrids - 1][frontSensorPosition[1]]= IS_OBSTACLE;
			}
			
	
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontleftSensorPosition[0] - i][frontleftSensorPosition[1]] = true;
				_mazeRef[frontleftSensorPosition[0] - i][frontleftSensorPosition[1]]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontleftSensorPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[frontleftSensorPosition[0] - numOfClearGrids - 1][frontleftSensorPosition[1]] = true;
				_mazeRef[frontleftSensorPosition[0] - numOfClearGrids - 1][frontleftSensorPosition[1]]= IS_OBSTACLE;
			}
			
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[frontrightSensorPosition[0] - i][frontrightSensorPosition[1]] = true;
				_mazeRef[frontrightSensorPosition[0] - i][frontrightSensorPosition[1]]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && frontrightSensorPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[frontrightSensorPosition[0] - numOfClearGrids - 1][frontrightSensorPosition[1]] = true;
				_mazeRef[frontrightSensorPosition[0] - numOfClearGrids - 1][frontrightSensorPosition[1]]= IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[leftSensorPosition[0]][leftSensorPosition[1] - i] = true;
				_mazeRef[leftSensorPosition[0]][leftSensorPosition[1] - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && leftSensorPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[leftSensorPosition[0]][leftSensorPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef[leftSensorPosition[0]][leftSensorPosition[1] - numOfClearGrids - 1]= IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[rightSensorPosition[0]][rightSensorPosition[1] + i] = true;
				_mazeRef[rightSensorPosition[0]][rightSensorPosition[1] + i]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && rightSensorPosition[1] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[rightSensorPosition[0]][rightSensorPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef[rightSensorPosition[0]][rightSensorPosition[1] + numOfClearGrids + 1]= IS_OBSTACLE;
			}
	}
		

	}
	
}
