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
	
	public int[][] getMazeRef() {
		return _mazeRef;
	}
	
    public static MazeExplorer getInstance() {
        if (_instance == null) {
            _instance = new MazeExplorer();
        }
        return _instance;
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
		exploreAlongWall (START);
		
		adjustOrientationToNorth();

	}

	private void adjustOrientationToNorth() {
		switch(_robotOrientation) {
			case NORTH:
				break;
			case SOUTH:
				_robot.turnRight();
				_robot.turnRight();
				break;
			case EAST:
				_robot.turnLeft();
				break;
			case WEST:
				_robot.turnRight();
		}
		
	}

	private void init(int[] robotPosition) {
		_robot = Robot.getInstance();
		_robotPosition = new int[2];
		_robotPosition[0] = robotPosition[0];
		_robotPosition[1] = robotPosition[1];
		_robotOrientation = Orientation.NORTH;
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

		while (!isGoalPos(_robotPosition, goalPos)) {
			int rightStatus = checkRightSide(_robotPosition, _robotOrientation);
			if (rightStatus != RIGHT_NO_ACCESS) {
				if (rightStatus == RIGHT_UNSURE_ACCESS) {
					updateRobotOrientation(Movement.TURN_RIGHT);
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.turnRight();
					if (hasAccessibleFront(_robotPosition, _robotOrientation)) {
						updateRobotPositionAfterMF();
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
					updateRobotPositionAfterMF();
					setIsExplored(_robotPosition, _robotOrientation);
					_robot.moveForward();
				}
			} else if (hasAccessibleFront(_robotPosition, _robotOrientation)){ 
				updateRobotPositionAfterMF();
				setIsExplored(_robotPosition, _robotOrientation);
				_robot.moveForward();
			} else {
				updateRobotOrientation(Movement.TURN_LEFT);
				setIsExplored(_robotPosition, _robotOrientation);
				_robot.turnLeft();
			}
		}
	}

	private void updateRobotOrientation (Movement move) {
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
	}
	
	private boolean isGoalPos (int[] curPos, int[] goalPos) {
		if (curPos[0] == goalPos[0] && curPos[1] == goalPos[1]) {
			return true;
		}
		return false;
	}
	
	private void updateRobotPositionAfterMF() {
		switch (_robotOrientation) {
			case NORTH:
				_robotPosition[0] = _robotPosition[0];
				_robotPosition[1] = _robotPosition[1] + 1;
				break;
			case SOUTH:
				_robotPosition[0] = _robotPosition[0];
				_robotPosition[1] = _robotPosition[1] - 1;
				break;
			case EAST:
				_robotPosition[0] = _robotPosition[0] + 1;
				_robotPosition[1] = _robotPosition[1];
				break;
			case WEST:
				_robotPosition[0] = _robotPosition[0] - 1;
				_robotPosition[1] = _robotPosition[1];
		}
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

	private void setIsExplored(int[] robotPosition, Orientation ori) {
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
