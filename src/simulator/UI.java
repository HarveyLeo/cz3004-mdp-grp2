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
		 * Add left panel: the real map and three control buttons (load/export/clear).
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
				_mapGrids[x][y].setActionCommand("SetObstacleAt " + x + "," + y);
	            _mapGrids[x][y].setBorder(BorderFactory.createLineBorder(Color.GRAY));
	            _mapGrids[x][y].setBackground(Color.ORANGE);
	            _mapGrids[x][y].addActionListener(this);
	            map.add(_mapGrids[x][y]); 
			}
		}
		_mapPane.add(map);
		JButton loadMap = new JButton("Load");
		loadMap.setActionCommand("LoadMap");
		loadMap.addActionListener(this);
		JButton exportMap = new JButton("Export");
		exportMap.setActionCommand("ExportMap");
		exportMap.addActionListener(this);
		JButton clearMap = new JButton("Clear");
		clearMap.setActionCommand("ClearMap");
		clearMap.addActionListener(this);
		_mapPane.add(loadMap);
		_mapPane.add(exportMap);
		_mapPane.add(clearMap);
		contentPane.add(_mapPane,BorderLayout.WEST);
		
		//Add control panel to the interface.
		
		//Add control switch
		_ctrlPane = new JPanel(new BorderLayout());
		_ctrlPane.setBorder(new EmptyBorder(50, 20, 20, 20));
		String comboBoxItems[] = { EXPLORE_PANEL, FFP_PANEL };
		JComboBox cbCtrlSwitch = new JComboBox(comboBoxItems);
		cbCtrlSwitch.setFont(new Font("Tahoma", Font.BOLD, 16));
		cbCtrlSwitch.setEditable(false);
		cbCtrlSwitch.addActionListener(this);
		cbCtrlSwitch.setActionCommand("SwitchCtrl");
		_ctrlPane.add(cbCtrlSwitch, BorderLayout.NORTH);
		
		//Create control panel for exploring.
		JLabel[] exploreCtrlLabels = new JLabel[4];
	    JTextField[] exploreCtrlTextFields = new JTextField[4];
	    JButton exploreBtn = new JButton("Explore");
	    exploreBtn.setFont(new Font("Tahoma", Font.PLAIN, 15));
	    exploreBtn.setBackground(SystemColor.inactiveCaption);
	    exploreCtrlLabels[0] = new JLabel("Robot initial position: ");
	    exploreCtrlLabels[1] = new JLabel("Speed (steps/sec): ");
	    exploreCtrlLabels[2] = new JLabel("Target coverage (%): ");
	    exploreCtrlLabels[3] = new JLabel("Time limit (sec): ");
	    for (int i = 0; i < 4; i++) {
	    	exploreCtrlTextFields[i] = new JTextField(10);
        }

	    JPanel exploreCtrlPane = new JPanel(new GridBagLayout());
		GridBagConstraints exploreGridConstraints = new GridBagConstraints();
		exploreGridConstraints.weighty = 0.25;
		exploreGridConstraints.anchor = GridBagConstraints.LINE_END;
		exploreGridConstraints.gridx = 0;
		exploreGridConstraints.gridy = 0;
	    exploreCtrlPane.add(exploreCtrlLabels[0], exploreGridConstraints);
	    exploreCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    exploreGridConstraints.gridx = 0;
	    exploreGridConstraints.gridy = 1;
		exploreCtrlPane.add(exploreCtrlLabels[1], exploreGridConstraints);
		exploreCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreGridConstraints.gridx = 0;
		exploreGridConstraints.gridy = 2;
		exploreCtrlPane.add(exploreCtrlLabels[2], exploreGridConstraints);
		exploreCtrlLabels[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreGridConstraints.gridx = 0;
		exploreGridConstraints.gridy = 3;
		exploreCtrlPane.add(exploreCtrlLabels[3], exploreGridConstraints);
		exploreCtrlLabels[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		exploreGridConstraints.anchor = GridBagConstraints.LINE_START;
		exploreGridConstraints.gridx = 1;
		exploreGridConstraints.gridy = 0;
	    exploreCtrlPane.add(exploreCtrlTextFields[0], exploreGridConstraints);
	    exploreCtrlTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    exploreGridConstraints.gridx = 1;
	    exploreGridConstraints.gridy = 1;
		exploreCtrlPane.add(exploreCtrlTextFields[1], exploreGridConstraints);
		exploreCtrlTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreGridConstraints.gridx = 1;
		exploreGridConstraints.gridy = 2;
		exploreCtrlPane.add(exploreCtrlTextFields[2], exploreGridConstraints);
		exploreCtrlTextFields[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
		exploreGridConstraints.gridx = 1;
		exploreGridConstraints.gridy = 3;
		exploreCtrlPane.add(exploreCtrlTextFields[3], exploreGridConstraints);
		exploreCtrlTextFields[3].setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		exploreGridConstraints.weighty = 3;
		exploreGridConstraints.gridwidth = 2;
		exploreGridConstraints.gridx = 0;
		exploreGridConstraints.gridy = 4;
		exploreGridConstraints.anchor = GridBagConstraints.CENTER;
		exploreCtrlPane.add(exploreBtn, exploreGridConstraints);
		exploreCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));
	    
	    //Create control panel for finding fastest path.
	    JLabel[] ffpCtrlLabels = new JLabel[2];
	    JTextField[] ffpCtrlTextFields = new JTextField[2];
	    JButton ffpBtn = new JButton("Find");
	    ffpBtn.setFont(new Font("Tahoma", Font.PLAIN, 15));
	    ffpBtn.setBackground(SystemColor.inactiveCaption);
	    ffpCtrlLabels[0] = new JLabel("Speed (steps/sec): ");
	    ffpCtrlLabels[1] = new JLabel("Time limit (sec): ");
	    for (int i = 0; i < 2; i++) {
	    	ffpCtrlTextFields[i] = new JTextField(10);
        }
	    JPanel ffpCtrlPane = new JPanel(new GridBagLayout());
	    GridBagConstraints ffpGridConstraints = new GridBagConstraints();
	    ffpGridConstraints.weighty = 0.1;
		ffpGridConstraints.anchor = GridBagConstraints.LINE_END;
		ffpGridConstraints.gridx = 0;
		ffpGridConstraints.gridy = 0;
	    ffpCtrlPane.add(ffpCtrlLabels[0], ffpGridConstraints);
	    ffpCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    ffpGridConstraints.gridx = 0;
	    ffpGridConstraints.gridy = 1;
		ffpCtrlPane.add(ffpCtrlLabels[1], ffpGridConstraints);
		ffpCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		ffpGridConstraints.anchor = GridBagConstraints.LINE_START;
		ffpGridConstraints.gridx = 1;
		ffpGridConstraints.gridy = 0;
	    ffpCtrlPane.add(ffpCtrlTextFields[0], ffpGridConstraints);
	    ffpCtrlTextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
	    ffpGridConstraints.gridx = 1;
	    ffpGridConstraints.gridy = 1;
		ffpCtrlPane.add(ffpCtrlTextFields[1], ffpGridConstraints);
		ffpCtrlTextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
		ffpGridConstraints.weighty = 1.4;
		ffpGridConstraints.gridwidth = 2;
		ffpGridConstraints.gridx = 0;
		ffpGridConstraints.gridy = 4;
		ffpGridConstraints.anchor = GridBagConstraints.CENTER;
		ffpCtrlPane.add(ffpBtn, ffpGridConstraints);
		ffpCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));
	    
		JPanel cardPane = new JPanel(new CardLayout());
	    cardPane.add(exploreCtrlPane, EXPLORE_PANEL);
	    cardPane.add(ffpCtrlPane, FFP_PANEL);
	    _ctrlPane.add(cardPane, BorderLayout.CENTER);
		contentPane.add(_ctrlPane, BorderLayout.CENTER); 
		
		//Add maze panel to the interface.
		_mazePane = new JPanel();
		_mazePane.setLayout(new GridLayout(MAP_WIDTH, MAP_LENGTH));
		_mazePane.setPreferredSize(new Dimension(450, 600));
		_mazeGrids = new JButton[MAP_WIDTH][MAP_LENGTH];
		for (int x = 0; x < MAP_WIDTH; x++) {
			for (int y = 0; y < MAP_LENGTH; y++) {
				_mazeGrids[x][y] = new JButton();
				_mazeGrids[x][y].setEnabled(false);
	            _mazeGrids[x][y].setBorder(BorderFactory.createEtchedBorder());
	            _mazeGrids[x][y].setBackground(Color.BLACK);
	            _mazePane.add(_mazeGrids[x][y]); 
			}
		}
		contentPane.add(_mazePane,BorderLayout.EAST);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Controller controller = Controller.getInstance();
		if (cmd.matches("SetObstacleAt [0-9]+,[0-9]+")) {
			int index = cmd.indexOf(",");
			int x = Integer.parseInt(cmd.substring(14, index));
			int y = Integer.parseInt(cmd.substring(index + 1));
			if (_mapGrids[x][y].getBackground() == Color.ORANGE) {
				_mapGrids[x][y].setBackground(Color.BLACK);
			} else {
				_mapGrids[x][y].setBackground(Color.ORANGE);
			}
		} else if (cmd.equals("SwitchCtrl")) {
			JComboBox cb = (JComboBox) e.getSource();
			JPanel cardPanel = (JPanel) _ctrlPane.getComponent(1);
			CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
			cardLayout.show(cardPanel, (String) cb.getSelectedItem());
		} else if (cmd.equals("LoadMap")) {
			controller.loadMap();
		} else if (cmd.equals("ExportMap")) {
			controller.exportMap();
		} else if (cmd.equals("ClearMap")) {
			controller.clearMap();
		}
	}
	

}
