package algorithms;

import java.util.ArrayList;

public class Path {
	
	private ArrayList<Step> _steps = new ArrayList<Step>();

	public void prependStep(int x, int y) {
		_steps.add(0, new Step(x, y));
	}

	public String toString() {
		String pathString = "";
		for (Step step : _steps) {
			pathString = pathString + "(" + step.getX() + "," + step.getY() + ")";
		}
		return pathString;
	}
	
	private class Step {
		private int _x;
		private int _y;
		public Step(int x, int y) {
			_x = x;
			_y = y;
		}
		public int getX() {
			return _x;
		}
		public int getY() {
			return _y;
		}
	}
	
}
