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
	
	
}
