package simulator.robot;

public class Sensor {
	
	public static final int SHORT_RANGE = 2;
	public static final int LONG_RANGE = 5;
	
	private int _range;
	
	public Sensor (int range) {
		_range = range;
	}
	
	public int getRange() {
		return _range;
	}
	
}
