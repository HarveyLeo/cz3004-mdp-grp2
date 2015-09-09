import java.awt.EventQueue;

import simulator.Controller;

public class RobotSystem {
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new RobotSystem();
			}
		});
	}
	
	public RobotSystem() {
		Controller c = Controller.getInstance();
		c.run();
	}

}
