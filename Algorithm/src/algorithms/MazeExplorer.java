package algorithms;

import datatypes.Orientation;
import simulator.arena.Arena;
import simulator.robot.Robot;
import simulator.robot.Sensor;

public class MazeExplorer {

	private static MazeExplorer _instance;
	private Robot _robot;
	private Boolean[][] _isExplored;
	
	private MazeExplorer() {
		_robot = new Robot();
		_isExplored = new Boolean[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				_isExplored[i][j] = false;
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
		setIsExplored(robotPosition);
		
		/* Testing module */
		   for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				if (_isExplored[i][j]) {
					System.out.print("[" + i + "][" + j + "] ");
				}
			}
		}
	}

	private void setIsExplored(int[] robotPosition) {
		int[] frontSensorPosition = new int[2];
		int[] frontleftSensorPosition = new int[2];
		int[] frontrightSensorPosition = new int[2];
		int[] leftSensorPosition = new int[2];
		int[] rightSensorPosition = new int[2];
		int numOfClearGrids;
		Orientation ori = _robot.getOrientation();

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
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[1] + numOfClearGrids + 2 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0]][robotPosition[1] + numOfClearGrids + 2] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + 1 + i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 2 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + numOfClearGrids + 2] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + 1 + i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 2 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + numOfClearGrids + 2] = true;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - i][robotPosition[1] + 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 1][robotPosition[1] + 1] = true;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + i][robotPosition[1] + 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 1][robotPosition[1] + 1] = true;
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
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[1] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0]][robotPosition[1] - numOfClearGrids - 2] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - 1 - i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - numOfClearGrids - 2] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - 1 - i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - numOfClearGrids - 2] = true;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + i][robotPosition[1] - 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 1][robotPosition[1] - 1] = true;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - i][robotPosition[1] - 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 1][robotPosition[1] - 1] = true;
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
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[0] + numOfClearGrids + 2 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 2][robotPosition[1]] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1 + i][robotPosition[1] + 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 2 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 2][robotPosition[1] + 1] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1 + i][robotPosition[1] - 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] + numOfClearGrids + 2 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] + numOfClearGrids + 2][robotPosition[1] - 1] = true;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 1 < Arena.MAP_WIDTH) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] + numOfClearGrids + 1] = true;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] + 1][robotPosition[1] - numOfClearGrids - 1] = true;
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
			}
			if (numOfClearGrids < Sensor.LONG_RANGE && robotPosition[0] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 2][robotPosition[1]] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontleftSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1 - i][robotPosition[1] - 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 2][robotPosition[1] - 1] = true;
			}
			numOfClearGrids = _robot.senseSideFront(frontrightSensorPosition, ori);
			for (int i = 1; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1 - i][robotPosition[1] + 1] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[0] - numOfClearGrids - 2 >= 0) {
				_isExplored[robotPosition[0] - numOfClearGrids - 2][robotPosition[1] + 1] = true;
			}
			numOfClearGrids = _robot.senseLeft(leftSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] - numOfClearGrids - 1 >= 0) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] - numOfClearGrids - 1] = true;
			}
			numOfClearGrids = _robot.senseRight(rightSensorPosition, ori);
			for (int i = 2; i <= numOfClearGrids; i++) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + i] = true;
			}
			if (numOfClearGrids < Sensor.SHORT_RANGE && robotPosition[1] + numOfClearGrids + 1 < Arena.MAP_LENGTH) {
				_isExplored[robotPosition[0] - 1][robotPosition[1] + numOfClearGrids + 1] = true;
			}
	}
		

	}
	
}
