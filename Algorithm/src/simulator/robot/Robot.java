package simulator.robot;

public class Robot {
	private Sensor _frontLong;
	private Sensor _frontleftShort, _frontrightShort;
	private Sensor _rightShort, _leftShort;
	
	public Robot() {
		_frontLong.setRange(2, 8);
		_frontleftShort.setRange(1, 5);
		_frontrightShort.setRange(1, 5);
		_leftShort.setRange(1, 5);
		_rightShort.setRange(1, 5);
	}
	
	public int senseFront() {
		//TODO implement
		return 0;
	}
	
	public int senseFrontLeft() {
		//TODO implement
		return 0;
	}
	
	public int senseFrontRight() {
		//TODO implement
		return 0;
	}
	
	public int senseLeft() {
		//TODO implement
		return 0;
	}
	
	public int senseRight() {
		//TODO implement
		return 0;
	}
}
