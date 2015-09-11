package algorithms;

import simulator.UI;
import simulator.robot.Robot;

public class ExploreMaze {

	private Robot _robot;
	private static Boolean[][] _isExplored;
	
	public static void explore(int[] robotPosition, int speed, int coverage, int timeLimit) {
		initIsExplored(robotPosition);
		// TODO Auto-generated method stub
		if (robotPosition[0] <= 8) {
	
			
		}
	}

	private static void initIsExplored(int[] robotPosition) {
		_isExplored = new Boolean[UI.MAP_LENGTH][UI.MAP_WIDTH];
		for (int i = 0; i < 2; i++) {
			for (int j=0; j <2;j++) {
				_isExplored[i][j] = true;
			}
		}

		
	}
	
}
