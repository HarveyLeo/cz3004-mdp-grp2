package simulator.robot;

public class Sensor {
	private int _shortestRange;
	private int _longestRange;
	
	public void setRange(int shortestRange, int longestRange) {
		_shortestRange = shortestRange;
		_longestRange = longestRange;
	}
	
}
