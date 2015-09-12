package algorithms;

import datatypes.Orientation;
import simulator.arena.Arena;
import simulator.robot.Robot;
import simulator.robot.Sensor;

public class MazeExplorer {
	
	public static final int UNEXPLORED = -1;
	public static final int IS_OBSTACLE = 1;
	public static final int IS_EMPTY = 0;

	private static MazeExplorer _instance;
	private Robot _robot;
	private Boolean[][] _isExplored;
	private int[][] _mazeRef;
	
	private MazeExplorer() {
		_robot = new Robot();
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
	
	public static MazeExplorer getInstance() {
		if (_instance == null) {
			_instance = new MazeExplorer();
		}
		return _instance;
	}
	
	public void explore(int[] robotPosition, int speed, int coverage, int timeLimit) {
		for (int i = robotPosition[0] - 1; i <= robotPosition[0] + 1; i++) {
			for (int j = robotPosition[1] - 1; j <= robotPosition[1] + 1; j++) {
				_isExplored[i][j] = true;
			}
		}
		setIsExplored(robotPosition, _robot.getOrientation());
		
		/* Testing module */
		   for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				if (_isExplored[i][j]) {
					System.out.print("[" + i + "][" + j + "] ");
				}
			}
		}
	}
	
	private void exploreAlongWall (int[] startPos, int[] goalPos) {
		int curCoverage, curTime;
		int[] curPos = new int[2];
		curPos[0] = startPos[0];
		curPos[1] = startPos[1];
		int totalNumOfGrids = Arena.MAP_LENGTH * Arena.MAP_WIDTH;

		while (!isGoalPos(curPos, goalPos)) {
			if (hasAccessibleRight(curPos, _robot.getOrientation())) {
				_robot.moveForward();
			}
			
		}
	}

	private boolean isGoalPos (int[] curPos, int[] goalPos) {
		if (curPos[0] == goalPos[0] && curPos[1] == goalPos[1]) {
			return true;
		}
		return false;
	}
	
	private boolean hasAccessibleRight(int[] curPos, Orientation ori) {
		int[] rightPos = new int[2];
		switch (ori) {
			case NORTH:
				rightPos[0] = curPos[0] + 1;
				rightPos[1] = curPos[1];
				if (rightPos[0] + 1 >= Arena.MAP_LENGTH) {
					return false;
				}
				for (int j = rightPos[1] - 1; j <= rightPos[1] + 1; j++) {
					if (_mazeRef[rightPos[0] + 1][j] == IS_OBSTACLE) {
							return false;
					}
				}
				break;
			case SOUTH:
				rightPos[0] = curPos[0] - 1;
				rightPos[1] = curPos[1];
				if (rightPos[0] - 1 < 0) {
					return false;
				}
				for (int j = rightPos[1] - 1; j <= rightPos[1] + 1; j++) {
					if (_mazeRef[rightPos[0] - 1][j] == IS_OBSTACLE) {
							return false;
					}
				}
				break;
			case EAST:
				rightPos[0] = curPos[0];
				rightPos[1] = curPos[1] - 1;
				if (rightPos[1] - 1 < 0) {
					return false;
				}
				for (int j = rightPos[0] - 1; j <= rightPos[0] + 1; j++) {
					if (_mazeRef[j][rightPos[1] - 1] == IS_OBSTACLE) {
							return false;
					}
				}
				break;
			case WEST:
				rightPos[0] = curPos[0];
				rightPos[1] = curPos[1] + 1;
				if (rightPos[1] + 1 >= Arena.MAP_WIDTH) {
					return false;
				}
				for (int j = rightPos[0] - 1; j <= rightPos[0] + 1; j++) {
					if (_mazeRef[j][rightPos[1] + 1] == IS_OBSTACLE) {
							return false;
					}
				}
		}
		_robot.turnRight();
		setIsExplored(curPos, ori);
		if (hasAccessibleFront(curPos, ori)) {
			return true;
		}
		return false;
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
			frontSensorPosition[1] = robotPosition[1] + 1;
			frontleftSensorPosition[0] = robotPosition[0] - 1;
			frontleftSensorPosition[1] = robotPosition[1] + 1;
			frontrightSensorPosition[0] = robotPosition[0] + 1;
			frontrightSensorPosition[1] = robotPosition[1] + 1;
			leftSensorPosition[0] = robotPosition[0];
			leftSensorPosition[1] = robotPosition[1] + 1;
			rightSensorPosition[0] = robotPosition[0];
			rightSensorPosition[1] = robotPosition[1] + 1;
			
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0]][robotPosition[1] + 1 + i] = true;
				_mazeRef[robotPosition[0]][robotPosition[1] + 1 + i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[1] + numOfClearGrids + 2 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0]][robotPosition[1] + numOfClearGrids + 2] = true;
				_mazeRef[robotPosition[0]][robotPosition[1] + numOfClearGrids + 2] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + 1 + i] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] + 1 + i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 2 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + numOfClearGrids + 2] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] + numOfClearGrids + 2] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + 1 + i] = true;
				_mazeRef[robotPosition[0] + 1][robotPosition[1] + 1 + i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 2 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + numOfClearGrids + 2] = true;
				_mazeRef[robotPosition[0] + 1][robotPosition[1] + numOfClearGrids + 2] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - i][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] - i][robotPosition[1] + 1] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 1][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] - numOfClearGrids - 1][robotPosition[1] + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + i][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] + i][robotPosition[1] + 1] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 1][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] + numOfClearGrids + 1][robotPosition[1] + 1] = IS_OBSTACLE;
			}
			break;
		case SOUTH:
			frontSensorPosition[0] = robotPosition[0];
			frontSensorPosition[1] = robotPosition[1] - 1;
			frontleftSensorPosition[0] = robotPosition[0] + 1;
			frontleftSensorPosition[1] = robotPosition[1] - 1;
			frontrightSensorPosition[0] = robotPosition[0] - 1;
			frontrightSensorPosition[1] = robotPosition[1] - 1;
			leftSensorPosition[0] = robotPosition[0];
			leftSensorPosition[1] = robotPosition[1] - 1;
			rightSensorPosition[0] = robotPosition[0];
			rightSensorPosition[1] = robotPosition[1] - 1;
			
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0]][robotPosition[1] - 1 - i] = true;
				_mazeRef[robotPosition[0]][robotPosition[1] - 1 - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[1] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0]][robotPosition[1] - numOfClearGrids - 2] = true;
				_mazeRef[robotPosition[0]][robotPosition[1] - numOfClearGrids - 2] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - 1 - i] = true;
				_mazeRef[robotPosition[0] + 1][robotPosition[1] - 1 - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - numOfClearGrids - 2] = true;
				_mazeRef[robotPosition[0] + 1][robotPosition[1] - numOfClearGrids - 2] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - 1 - i] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] - 1 - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - numOfClearGrids - 2] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] - numOfClearGrids - 2] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + i][robotPosition[1] - 1] = true;
				_mazeRef[robotPosition[0] + i][robotPosition[1] - 1] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 1][robotPosition[1] - 1] = true;
				_mazeRef[robotPosition[0] + numOfClearGrids + 1][robotPosition[1] - 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - i][robotPosition[1] - 1] = true;
				_mazeRef[robotPosition[0] - i][robotPosition[1] - 1] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 1][robotPosition[1] - 1] = true;
				_mazeRef[robotPosition[0] - numOfClearGrids - 1][robotPosition[1] - 1] = IS_OBSTACLE;
			}
			break;
		case EAST:
			frontSensorPosition[0] = robotPosition[0] + 1;
			frontSensorPosition[1] = robotPosition[1];
			frontleftSensorPosition[0] = robotPosition[0] + 1;
			frontleftSensorPosition[1] = robotPosition[1] + 1;
			frontrightSensorPosition[0] = robotPosition[0] + 1;
			frontrightSensorPosition[1] = robotPosition[1] - 1;
			leftSensorPosition[0] = robotPosition[0] + 1;
			leftSensorPosition[1] = robotPosition[1];
			rightSensorPosition[0] = robotPosition[0] + 1;
			rightSensorPosition[1] = robotPosition[1];
			
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1 + i][robotPosition[1]] = true;
				_mazeRef[robotPosition[0] + 1 + i][robotPosition[1]] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[0] + numOfClearGrids + 2 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 2][robotPosition[1]] = true;
				_mazeRef [robotPosition[0] + numOfClearGrids + 2][robotPosition[1]] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1 + i][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] + 1 + i][robotPosition[1] + 1] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 2 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 2][robotPosition[1] + 1] = true;
				_mazeRef [robotPosition[0] + numOfClearGrids + 2][robotPosition[1] + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1 + i][robotPosition[1] - 1] = true;
				_mazeRef [robotPosition[0] + 1 + i][robotPosition[1] - 1]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 2 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 2][robotPosition[1] - 1] = true;
				_mazeRef [robotPosition[0] + numOfClearGrids + 2][robotPosition[1] - 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + i] = true;
				_mazeRef [robotPosition[0] + 1][robotPosition[1] + i]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 1 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef [robotPosition[0] + 1][robotPosition[1] + numOfClearGrids + 1] = IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - i] = true;
				_mazeRef [robotPosition[0] + 1][robotPosition[1] - i]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef [robotPosition[0] + 1][robotPosition[1] - numOfClearGrids - 1] = IS_OBSTACLE;
			}
			break;
		case WEST:
			frontSensorPosition[0] = robotPosition[0] - 1;
			frontSensorPosition[1] = robotPosition[1];
			frontleftSensorPosition[0] = robotPosition[0] - 1;
			frontleftSensorPosition[1] = robotPosition[1] - 1;
			frontrightSensorPosition[0] = robotPosition[0] - 1;
			frontrightSensorPosition[1] = robotPosition[1] + 1;
			leftSensorPosition[0] = robotPosition[0] - 1;
			leftSensorPosition[1] = robotPosition[1];
			rightSensorPosition[0] = robotPosition[0] - 1;
			rightSensorPosition[1] = robotPosition[1];
	
			numOfClearGrids = _robot.senseFront(frontSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1 - i][robotPosition[1]] = true;		
				_mazeRef[robotPosition[0] - 1 - i][robotPosition[1]]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[0] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 2][robotPosition[1]] = true;
				_mazeRef[robotPosition[0] - numOfClearGrids - 2][robotPosition[1]]= IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1 - i][robotPosition[1] - 1] = true;
				_mazeRef[robotPosition[0] - 1 - i][robotPosition[1] - 1]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 2][robotPosition[1] - 1] = true;
				_mazeRef[robotPosition[0] - numOfClearGrids - 2][robotPosition[1] - 1]= IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1 - i][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] - 1 - i][robotPosition[1] + 1]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 2][robotPosition[1] + 1] = true;
				_mazeRef[robotPosition[0] - numOfClearGrids - 2][robotPosition[1] + 1]= IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - i] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] - i] = IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - numOfClearGrids - 1] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] - numOfClearGrids - 1]= IS_OBSTACLE;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + i] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] + i]= IS_EMPTY;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + numOfClearGrids + 1] = true;
				_mazeRef[robotPosition[0] - 1][robotPosition[1] + numOfClearGrids + 1]= IS_OBSTACLE;
			}
	}
		

	}
	
}
