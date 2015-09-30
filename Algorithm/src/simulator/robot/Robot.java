package simulator.robot;

import datatypes.Orientation;
import simulator.Controller;
import simulator.arena.Arena;

public class Robot {
	private static Robot _instance;
	private int _speed;
	
	private Robot() {

	}
	
	public static Robot getInstance() {
		if (_instance == null) {
			_instance = new Robot();
		}
		return _instance;
	}
	
	public void setSpeed(int speed) {
		_speed = speed;
	}
	
	
	public int senseFront(int[] sensorPosition, Orientation robotOrientation) {
		Arena arena = Arena.getInstance();
		int numOfClearGrids;
		Orientation sensorOri = robotOrientation;
		numOfClearGrids = arena.getNumOfClearGrids(sensorPosition, sensorOri);
		if (numOfClearGrids > Sensor.LONG_RANGE) {
			numOfClearGrids = Sensor.LONG_RANGE;
		}
		return numOfClearGrids;
	}
	
	public int senseSideFront (int[] sensorPosition, Orientation robotOrientation) {
		Arena arena = Arena.getInstance();
		int numOfClearGrids;
		Orientation sensorOri = robotOrientation;
		numOfClearGrids = arena.getNumOfClearGrids(sensorPosition, sensorOri);
		if (numOfClearGrids > Sensor.SHORT_RANGE) {
			numOfClearGrids = Sensor.SHORT_RANGE;
		}
		return numOfClearGrids;
	}
	
	
	public int senseLeft(int[] sensorPosition, Orientation robotOrientation) {
		Arena arena = Arena.getInstance();
		int numOfClearGrids;
		Orientation sensorOri;
		switch (robotOrientation) {
			case NORTH:
				sensorOri = Orientation.WEST;
				break;
			case SOUTH:
				sensorOri = Orientation.EAST;
				break;
			case EAST:
				sensorOri = Orientation.NORTH;
				break;
			case WEST:
				sensorOri = Orientation.SOUTH;
				break;
			default:
				sensorOri = null;
		}
		numOfClearGrids = arena.getNumOfClearGrids(sensorPosition, sensorOri);
		if (numOfClearGrids > Sensor.SHORT_RANGE) {
			numOfClearGrids = Sensor.SHORT_RANGE;
		}
		return numOfClearGrids;
	}
	
	public int senseRight(int[] sensorPosition, Orientation robotOrientation) {
		Arena arena = Arena.getInstance();
		int numOfClearGrids;
		Orientation sensorOri;
		switch (robotOrientation) {
			case NORTH:
				sensorOri = Orientation.EAST;
				break;
			case SOUTH:
				sensorOri = Orientation.WEST;
				break;
			case EAST:
				sensorOri = Orientation.SOUTH;
				break;
			case WEST:
				sensorOri = Orientation.NORTH;
				break;
			default:
				sensorOri = null;
		}
		numOfClearGrids = arena.getNumOfClearGrids(sensorPosition, sensorOri);
		if (numOfClearGrids > Sensor.SHORT_RANGE) {
			numOfClearGrids = Sensor.SHORT_RANGE;
		}
		return numOfClearGrids;
	}

	public void turnRight() {
		int stepTime = 1000 / _speed;
		try {
			Thread.sleep(stepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Controller controller = Controller.getInstance();
		controller.turnRobotRight();
	}

	public void moveForward() {
		int stepTime = 1000 / _speed;
		try {
			Thread.sleep(stepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Controller controller = Controller.getInstance();
		controller.moveRobotForward();
		
	}

	public void turnLeft() {
		int stepTime = 1000 / _speed;
		try {
			Thread.sleep(stepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Controller controller = Controller.getInstance();
		controller.turnRobotLeft();
	}

	public void moveForward(int count) {
		int stepTime = 1000 / _speed;
		Controller controller = Controller.getInstance();
		
		for (int i = 0; i < count; i++) {
			try {
				Thread.sleep(stepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
			controller.moveRobotForward();
		}
		
	}
}
