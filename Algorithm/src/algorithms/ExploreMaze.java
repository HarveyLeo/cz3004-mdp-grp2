package algorithms;

import simulator.UI;
import simulator.arena.Arena;
import simulator.robot.Robot;

public class ExploreMaze {

	private static Robot _robot;
	private static Boolean[][] _isExplored = new Boolean[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
	
	public static void explore(int[] robotPosition, int speed, int coverage, int timeLimit) {
		for (int i = robotPosition[0] - 1; i < robotPosition[0] + 1; i++) {
			for (int j = robotPosition[1] - 1; j < robotPosition[1] + 1; j++) {
				_isExplored[i][j] = true;
			}
		}
		setIsExplored(robotPosition);
		if (robotPosition[0] <= 8) {
			
			
		}
	}

	private static void setIsExplored(int[] robotPosition) {
//		_robot.senseFront()
	}
	
}
