package simulator;

public class Controller {
	private static Controller _instance;
	protected UI _ui;
	
	private Controller() {
		_ui = new UI();
		
	}

	public static Controller getInstance() {
		if (_instance == null) {
			_instance = new Controller();
		}
		return _instance;
	}
	
	public void run() {
		_ui.setVisible(true);
	}
	
	public void loadMap() {
		//TODO implement method
		System.out.println("loading map...");
	}
	
	public void exportMap() {
		//TODO implement method
		System.out.println("exporting map...");
	}
	
	public void clearMap() {
		//TODO implement method
		System.out.println("clearing map...");
	}
}
