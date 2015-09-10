package simulator;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.SystemColor;



public class UI extends JFrame implements ActionListener {

	public static final int MAP_WIDTH = 20;
	public static final int MAP_LENGTH = 15;
	private static final String EXPLORE_PANEL = "Explore arena";
    private static final String FFP_PANEL = "Find fastest path";
	private static final long serialVersionUID = 1L;
	private JPanel _contentPane;
	private JPanel _mapPane;
	private JPanel _ctrlPane;
	private JPanel _mazePane;
	private JButton[][] _mapGrids;
	private JButton[][] _mazeGrids;



	/**
	 * Create the simulator.
	 */
	public UI() {
		super("MDP Simulator - Arena Exploration & Fastest Path Computation");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_contentPane = new JPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		_contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(_contentPane);
		initContentPane(_contentPane);
		pack();
	}

	private void initContentPane(JPanel contentPane) {
		
		/*
		 * Add left panel: the reference map and two control buttons (load/clear).
		 */
		_mapPane = new JPanel(new FlowLayout());
		_mapPane.setPreferredSize(new Dimension(450, 650));
		JPanel map = new JPanel();
		map.setLayout(new GridLayout(MAP_WIDTH, MAP_LENGTH));
		map.setPreferredSize(new Dimension(450, 600));
		_mapGrids = new JButton[MAP_WIDTH][MAP_LENGTH];
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_LENGTH; y++) {
				_mapGrids[x][y] = new JButton();
				_mapGrids[x][y].setActionCommand("ToggleObstacleAt " + x + "," + y);
	            _mapGrids[x][y].setBorder(BorderFactory.createLineBorder(Color.GRAY));
	            _mapGrids[x][y].setBackground(Color.ORANGE);
	            _mapGrids[x][y].addActionListener(this);
	            map.add(_mapGrids[x][y]); 
	            if ( (x >= 0 & x <= 2 & y >= 12 & y <= 14) || (y >= 0 & y <= 2 & x >= 17 & x <= 19)) {
	            	_mapGrids[x][y].setEnabled(false);
	            	_mapGrids[x][y].setBackground(Color.GREEN);
	            	if (x == 1 & y == 13) {
	            		_mapGrids[x][y].setText("G");
	            	} else if (x == 18 && y == 1) {
	            		_mapGrids[x][y].setText("S");
	            	}
	            }
			}
		}
		_mapPane.add(map);
		JButton loadMap = new JButton("Load");
		loadMap.setActionCommand("LoadMap");
		loadMap.addActionListener(this);
		JButton clearMap = new JButton("Clear");
		clearMap.setActionCommand("ClearMap");
		clearMap.addActionListener(this);
		_mapPane.add(loadMap);
		_mapPane.add(clearMap);
		contentPane.add(_mapPane,BorderLayout.WEST);
		
		/*
		 * Add middle panel: the explore/fastest path control panel.
		 */
		
		//Add control switch (combo box).
		_ctrlPane = new JPanel(new BorderLayout());
		_ctrlPane.setBorder(new EmptyBorder(50, 20, 50, 20));
		String comboBoxItems[] = { EXPLORE_PANEL, FFP_PANEL };
		JComboBox cbCtrlSwitch = new JComboBox(comboBoxItems);
		cbCtrlSwitch.setFont(new Font("Tahoma", Font.BOLD, 16));
		cbCtrlSwitch.setEditable(false);
		cbCtrlSwitch.addActionListener(this);
		cbCtrlSwitch.setActionCommand("SwitchCtrl");
		_ctrlPane.add(cbCtrlSwitch, BorderLayout.NORTH);
		
		//Add control panel for exploring.
		JLabel[] exploreCtrlLabels = new JLabel[4];
	    JTextField[] exploreCtrlTextFields = new JTextField[4];
	    JButton exploreBtn = new JButton("Explore");
	    exploreCtrlLabels[0] = new JLabel("Robot initial position: ");
	    exploreCtrlLabels[1] = new JLabel("Speed (steps/sec): ");
	    exploreCtrlLabels[2] = new JLabel("Target coverage (%): ");
	    exploreCtrlLabels[3] = new JLabel("Time limit (sec): ");
	    for (int i = 0; i < 4; i++) {
	    	exploreCtrlTextFields[i] = new JTextField(10);
        }
	    
	    JPanel exploreInputPane = new JPanel(new GridLayout(4, 2));
	    exploreInputPane.add(exploreCtrlLabels[0]);
	    exploreCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    exploreInputPane.add(exploreCtrlTextFields[0]);
	    exploreCtrlTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreInputPane.add(exploreCtrlLabels[1]);
		exploreCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreInputPane.add(exploreCtrlTextFields[1]);
		exploreCtrlTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreInputPane.add(exploreCtrlLabels[2]);
		exploreCtrlLabels[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreInputPane.add(exploreCtrlTextFields[2]);
		exploreCtrlTextFields[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreInputPane.add(exploreCtrlLabels[3]);
		exploreCtrlLabels[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreInputPane.add(exploreCtrlTextFields[3]);
		exploreCtrlTextFields[3].setFont(new Font("Tahoma", Font.PLAIN, 14));

		JPanel exploreBtnPane = new JPanel();
		exploreBtnPane.add(exploreBtn);

		JPanel exploreCtrlPane = new JPanel();
		exploreCtrlPane.add(exploreInputPane);
		exploreCtrlPane.add(exploreBtnPane);
		exploreCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));

		//Add control panel for finding fastest path.
		JLabel[] ffpCtrlLabels = new JLabel[2];
	    JTextField[] ffpCtrlTextFields = new JTextField[2];
	    JButton ffpBtn = new JButton("Navigate");
	    ffpCtrlLabels[0] = new JLabel("Speed (steps/sec): ");
	    ffpCtrlLabels[1] = new JLabel("Time limit (sec): ");
	    for (int i = 0; i < 2; i++) {
	    	ffpCtrlTextFields[i] = new JTextField(10);
        }
	    
	    JPanel ffpInputPane = new JPanel(new GridLayout(2, 2));
	    ffpInputPane.add(ffpCtrlLabels[0]);
	    ffpCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    ffpInputPane.add(ffpCtrlTextFields[0]);
	    ffpCtrlTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    ffpInputPane.add(ffpCtrlLabels[1]);
	    ffpCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    ffpInputPane.add(ffpCtrlTextFields[1]);
	    ffpCtrlTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		JPanel ffpBtnPane = new JPanel();
		ffpBtnPane.add(ffpBtn);

		JPanel ffpCtrlPane = new JPanel();
		ffpCtrlPane.add(ffpInputPane);
		ffpCtrlPane.add(ffpBtnPane);
		ffpCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));

		//Add card panel to switch between explore and shortest path panels.
		JPanel cardPane = new JPanel(new CardLayout());
	    cardPane.add(exploreCtrlPane, EXPLORE_PANEL);
	    cardPane.add(ffpCtrlPane, FFP_PANEL);
	    cardPane.setPreferredSize(new Dimension(280, 300));
	    _ctrlPane.add(cardPane, BorderLayout.CENTER);
	    
	    //Add status panel.
	    JPanel statusPane = new JPanel(new BorderLayout());
	    JLabel statusLabel = new JLabel("Status Console:");
	    statusPane.add(statusLabel, BorderLayout.NORTH);
	    JPanel statusConsole = new JPanel();
	    statusConsole.setBackground(Color.LIGHT_GRAY);
	    statusConsole.setPreferredSize(new Dimension(280, 100));
	    JLabel status = new JLabel("waiting for commands...");
	    statusConsole.add(status);
	    statusPane.add(statusConsole, BorderLayout.CENTER);
	    _ctrlPane.add(statusPane, BorderLayout.SOUTH);
	    
		contentPane.add(_ctrlPane, BorderLayout.CENTER); 
		
		/*
		 * Add right panel: the maze panel.
		 */
		_mazePane = new JPanel(new FlowLayout());
		_mazePane.setPreferredSize(new Dimension(450, 650));
		JPanel maze = new JPanel();
		maze.setLayout(new GridLayout(MAP_WIDTH, MAP_LENGTH));
		maze.setPreferredSize(new Dimension(450, 600));
		_mazeGrids = new JButton[MAP_WIDTH][MAP_LENGTH];
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_LENGTH; y++) {
				_mazeGrids[x][y] = new JButton();
				_mazeGrids[x][y].setEnabled(false);
	            _mazeGrids[x][y].setBorder(BorderFactory.createEtchedBorder());
	            _mazeGrids[x][y].setBackground(Color.BLACK);
	            maze.add(_mazeGrids[x][y]); 
			}
		}
		_mazePane.add(maze);
		contentPane.add(_mazePane,BorderLayout.EAST);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Controller controller = Controller.getInstance();
		if (cmd.matches("ToggleObstacleAt [0-9]+,[0-9]+")) {
			int index = cmd.indexOf(",");
			int x = Integer.parseInt(cmd.substring(17, index));
			int y = Integer.parseInt(cmd.substring(index + 1));
			controller.toggleObstacle (_mapGrids, x, y);
		} else if (cmd.equals("SwitchCtrl")) {
			JComboBox cb = (JComboBox) e.getSource();
			JPanel cardPanel = (JPanel) _ctrlPane.getComponent(1);
			controller.switchComboBox(cb, cardPanel);
		} else if (cmd.equals("LoadMap")) {
			controller.loadMap();
		} else if (cmd.equals("ClearMap")) {
			controller.clearMap(_mapGrids);
		}
	}
	

}
