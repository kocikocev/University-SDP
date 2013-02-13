package vision.gui;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vision.DistortionFix;
import vision.PitchConstants;
import vision.VideoStream;
import vision.WorldState;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

/**
 * Creates and maintains the swing-based Control GUI, which 
 * provides both control manipulation (pitch choice, direction,
 * etc) and threshold setting. Also allows the saving/loading of
 * threshold values to a file.
 * 
 * @author s0840449 (original)
 * @author Alex Adams (heavy refactoring & improvements)
 */
@SuppressWarnings("serial")
public class VisionSettingsPanel extends JPanel {
	public static final int MOUSE_MODE_OFF = 0;
	public static final int MOUSE_MODE_PITCH_BOUNDARY = 1;
	public static final int MOUSE_MODE_BLUE_T = 2;
	public static final int MOUSE_MODE_YELLOW_T = 3;
	
	// A PitchConstants class used to load/save constants for the pitch
	private final PitchConstants pitchConstants;
	
	// Stores information about the current world state, such as shooting
	// direction, ball location, etc
	private final WorldState worldState;
	
	private final DistortionFix distortionFix;
	
	private int mouseMode;
	
	// Load/Save buttons
	private JButton saveButton;
	private JButton loadButton;
	
	// Tabs
	private final JTabbedPane tabPane = new JTabbedPane();
	private final JPanel mainTabPanel = new JPanel();
	private final CameraSettingsPanel camPanel;
	private final ThresholdsPanel[] tabPanels = new ThresholdsPanel[] {
		new ThresholdsPanel(),
		new ThresholdsPanel(),
		new ThresholdsPanel(),
		new ThresholdsPanel(),
		new ThresholdsPanel()
	};
	private final ChangeListener tabChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			// Update the debug overlay settings
			int index = tabPane.getSelectedIndex();
			
			switch(index) {
			// Main tab
			case(0):
				// Disable all debug modes
				pitchConstants.setDebugMode(PitchConstants.BALL, false);
				pitchConstants.setDebugMode(PitchConstants.BLUE, false);
				pitchConstants.setDebugMode(PitchConstants.YELLOW, false);
				pitchConstants.setDebugMode(PitchConstants.GREY, false);
				pitchConstants.setDebugMode(PitchConstants.GREEN, false);
				break;
			// Camera tab
			case(1):
				// Disable all debug modes
				pitchConstants.setDebugMode(PitchConstants.BALL, false);
				pitchConstants.setDebugMode(PitchConstants.BLUE, false);
				pitchConstants.setDebugMode(PitchConstants.YELLOW, false);
				pitchConstants.setDebugMode(PitchConstants.GREY, false);
				pitchConstants.setDebugMode(PitchConstants.GREEN, false);
				break;
			// Ball tab
			case(2):
				// Enable only Ball debug mode
				pitchConstants.setDebugMode(PitchConstants.BALL, true, false);
				break;
			// Blue tab
			case(3):
				// Enable only Blue Robot debug mode
				pitchConstants.setDebugMode(PitchConstants.BLUE, true, false);
				break;
			// Yellow tab
			case(4):
				// Enable only Yellow Robot debug mode
				pitchConstants.setDebugMode(PitchConstants.YELLOW, true, false);
				break;
			// Grey Circle tab
			case(5):
				// Enable only Grey Circle debug mode
				pitchConstants.setDebugMode(PitchConstants.GREY, true, false);
				break;
			// Green Plate tab
			case(6):
				// Enable only Green Plate debug mode
				pitchConstants.setDebugMode(PitchConstants.GREEN, true, false);
				break;
			default:
				System.err.println("VisionGUI: Invalid tab index");
				System.exit(1);
			}
		}
	};

	// Radio buttons and their change listeners
	private final JRadioButton rdbtnPitch0 = new JRadioButton("Main");
	private final JRadioButton rdbtnPitch1 = new JRadioButton("Side Room");
	private final ActionListener pitchActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update the world state and pitch constants
			int pitchNum = rdbtnPitch0.isSelected() ? 0 : 1;
			worldState.setMainPitch(rdbtnPitch0.isSelected());
			worldState.setPitch(pitchNum);
			pitchConstants.setPitchNum(pitchNum);
			reloadSliderDefaults();
		}
	};

	private final JRadioButton rdbtnYellow = new JRadioButton("Yellow");
	private final JRadioButton rdbtnBlue = new JRadioButton("Blue");
	private final ActionListener colourActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update which colour is ours
			worldState.setColour(rdbtnBlue.isSelected() ? 1 : 0);
			worldState.setWeAreBlue(rdbtnBlue.isSelected());
		}
	};

	private final JRadioButton rdbtnRight = new JRadioButton("Right");
	private final JRadioButton rdbtnLeft = new JRadioButton("Left");
	private final ActionListener directionActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update which direction the other team's goal is in
			int isLeft = rdbtnLeft.isSelected() ? 1 : 0;
			worldState.setDirection(isLeft);
			worldState.setWeAreOnLeft(rdbtnLeft.isSelected());
		}
	};
	
	private final JRadioButton rdbtnDistortOn = new JRadioButton("On");
	private final JRadioButton rdbtnDistortOff = new JRadioButton("Off");
	private final ActionListener distortionActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update whether distortion is active
			if (rdbtnDistortOn.isSelected()){
				distortionFix.setActive(true);
			}
			else {
				distortionFix.setActive(false);
			}
		}
	};
	
	private final JRadioButton rdbtnMouseModeOff = new JRadioButton();
	private final JRadioButton rdbtnMouseModePitch = new JRadioButton();
	private final JRadioButton rdbtnMouseModeBlue = new JRadioButton();
	private final JRadioButton rdbtnMouseModeYellow = new JRadioButton();
	
	private abstract class BaseSliderChangeListener implements ChangeListener {
		protected int index;
		public BaseSliderChangeListener(int index) {
			this.index = index;
		}
	}

	private class RedSliderChangeListener extends BaseSliderChangeListener {
		public RedSliderChangeListener(int index) {
			super(index);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			int[] lowerUpper = tabPanels[super.index].getRedSliderValues();
			pitchConstants.setRedLower(super.index, Math.max(0, lowerUpper[0]));
			pitchConstants.setRedUpper(super.index, lowerUpper[1]);
		}
	}
	
	private class GreenSliderChangeListener extends BaseSliderChangeListener {
		public GreenSliderChangeListener(int index) {
			super(index);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			int[] lowerUpper = tabPanels[super.index].getGreenSliderValues();
			pitchConstants.setGreenLower(super.index, Math.max(0, lowerUpper[0]));
			pitchConstants.setGreenUpper(super.index, lowerUpper[1]);
		}
	}
	
	private class BlueSliderChangeListener extends BaseSliderChangeListener {
		public BlueSliderChangeListener(int index) {
			super(index);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			int[] lowerUpper = tabPanels[super.index].getBlueSliderValues();
			pitchConstants.setBlueLower(super.index, Math.max(0, lowerUpper[0]));
			pitchConstants.setBlueUpper(super.index, lowerUpper[1]);
		}
	}
	
	private class HueSliderChangeListener extends BaseSliderChangeListener {
		public HueSliderChangeListener(int index) {
			super(index);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			int[] lowerUpper = tabPanels[super.index].getHueSliderValues();
			pitchConstants.setHueLower(super.index, (float) Math.max(0, lowerUpper[0]) / 255.0f);
			pitchConstants.setHueUpper(super.index, (float) lowerUpper[1] / 255.0f);
		}
	}
	
	private class SaturationSliderChangeListener extends BaseSliderChangeListener {
		public SaturationSliderChangeListener(int index) {
			super(index);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			int[] lowerUpper = tabPanels[super.index].getSaturationSliderValues();
			pitchConstants.setSaturationLower(super.index, (float) Math.max(0, lowerUpper[0]) / 255.0f);
			pitchConstants.setSaturationUpper(super.index, (float) lowerUpper[1] / 255.0f);
		}
	}

	private class ValueSliderChangeListener extends BaseSliderChangeListener {
		public ValueSliderChangeListener(int index) {
			super(index);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			int[] lowerUpper = tabPanels[super.index].getValueSliderValues();
			pitchConstants.setValueLower(super.index, (float) Math.max(0, lowerUpper[0]) / 255.0f);
			pitchConstants.setValueUpper(super.index, (float) lowerUpper[1] / 255.0f);
		}
	}
	
	/**
	 * Default constructor. 
	 * 
	 * @param worldState		A WorldState object to update the pitch choice, shooting
	 * 							direction, etc.
	 * @param pitchConstants	A PitchConstants object to allow saving/loading of data.
	 */
	public VisionSettingsPanel(WorldState worldState, final PitchConstants pitchConstants,
			final VideoStream vStream, final DistortionFix distortionFix) {
		// Both state objects must not be null.
		assert (worldState != null) : "worldState is null";
		assert (pitchConstants != null) : "pitchConstants is null";
		
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.distortionFix = distortionFix;
		this.camPanel = new CameraSettingsPanel(vStream,
				System.getProperty("user.dir") + "/constants/pitch" + 
				pitchConstants.getPitchNum() + "camera");

        // The main (default) tab
        mainTabPanel.setLayout(new BoxLayout(mainTabPanel, BoxLayout.Y_AXIS));
        setUpMainPanel();
        
        // The five threshold tabs
        for (int i = 0; i < PitchConstants.NUM_THRESHOLDS; ++i) {
            tabPanels[i].setRedSliderChangeListener(new RedSliderChangeListener(i));
            tabPanels[i].setGreenSliderChangeListener(new GreenSliderChangeListener(i));
            tabPanels[i].setBlueSliderChangeListener(new BlueSliderChangeListener(i));
            tabPanels[i].setHueSliderChangeListener(new HueSliderChangeListener(i));
            tabPanels[i].setSaturationSliderChangeListener(new SaturationSliderChangeListener(i));
            tabPanels[i].setValueSliderChangeListener(new ValueSliderChangeListener(i));
        }
        
        tabPane.addTab("Main", mainTabPanel);
        tabPane.addTab("Camera", camPanel);
        tabPane.addTab("Ball", tabPanels[PitchConstants.BALL]);
        tabPane.addTab("Blue Robot", tabPanels[PitchConstants.BLUE]);
        tabPane.addTab("Yellow Robot", tabPanels[PitchConstants.YELLOW]);
        tabPane.addTab("Grey Circles", tabPanels[PitchConstants.GREY]);
        tabPane.addTab("Green Plates", tabPanels[PitchConstants.GREEN]);
        
        tabPane.addChangeListener(tabChangeListener);
        this.add(tabPane);
        this.setSize(this.getPreferredSize());
        
		reloadSliderDefaults();
	}
	
	/**
	 * Sets up the main tab, adding in the pitch choice, the direction
	 * choice, the robot-colour choice and save/load buttons.
	 */
	private void setUpMainPanel() {
		// Pitch choice
		JPanel pitchPanel = new JPanel();
		JLabel pitchLabel = new JLabel("Pitch:");
		pitchPanel.add(pitchLabel);
		
		ButtonGroup pitchChoice = new ButtonGroup();
		pitchChoice.add(rdbtnPitch0);
		pitchChoice.add(rdbtnPitch1);
		pitchPanel.add(rdbtnPitch0);
		pitchPanel.add(rdbtnPitch1);
		
		rdbtnPitch0.addActionListener(pitchActionListener);
		rdbtnPitch1.addActionListener(pitchActionListener);
		
		mainTabPanel.add(pitchPanel);
		
		// Colour choice
		JPanel colourPanel = new JPanel();
		JLabel colourLabel = new JLabel("Our colour:");
		colourPanel.add(colourLabel);
		
		ButtonGroup colourChoice = new ButtonGroup();
		colourChoice.add(rdbtnYellow);
		colourPanel.add(rdbtnYellow);
		colourChoice.add(rdbtnBlue);
		colourPanel.add(rdbtnBlue);
		
		
		rdbtnYellow.addActionListener(colourActionListener);
		rdbtnBlue.addActionListener(colourActionListener);
		
		mainTabPanel.add(colourPanel);
		
		// Direction choice
		JPanel directionPanel = new JPanel();
		JLabel directionLabel = new JLabel("Our shoot direction:");
		directionPanel.add(directionLabel);
		
		ButtonGroup directionChoice = new ButtonGroup();
		directionChoice.add(rdbtnRight);
		directionPanel.add(rdbtnRight);
		directionChoice.add(rdbtnLeft);
		directionPanel.add(rdbtnLeft);
		
		rdbtnRight.addActionListener(directionActionListener);
		rdbtnLeft.addActionListener(directionActionListener);
		
		mainTabPanel.add(directionPanel);
		
		// Distortion
		JPanel distortionPanel = new JPanel();
		JLabel distortionLabel = new JLabel("Distortion Fix:");
		distortionPanel.add(distortionLabel);
		
		ButtonGroup distortionChoice = new ButtonGroup();
		distortionChoice.add(rdbtnDistortOn);
		distortionPanel.add(rdbtnDistortOn);
		distortionChoice.add(rdbtnDistortOff);
		distortionPanel.add(rdbtnDistortOff);
		
		rdbtnDistortOn.addActionListener(distortionActionListener);
		rdbtnDistortOff.addActionListener(distortionActionListener);
		
		mainTabPanel.add(distortionPanel);
		
		// Mouse mode selector
		JPanel mouseModePanel = new JPanel();
		GridBagLayout gbl_mouseModePanel = new GridBagLayout();
		gbl_mouseModePanel.columnWidths = new int[]{41, 0, 0};
		gbl_mouseModePanel.rowHeights = new int[]{36, 0, 0, 19, 0, 0};
		gbl_mouseModePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE, 1.0};
		gbl_mouseModePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		mouseModePanel.setLayout(gbl_mouseModePanel);
		JLabel mouseModeLabel = new JLabel("Mouse Mode");
		GridBagConstraints gbc_mouseModeLabel = new GridBagConstraints();
		gbc_mouseModeLabel.gridwidth = 2;
		gbc_mouseModeLabel.fill = GridBagConstraints.VERTICAL;
		gbc_mouseModeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeLabel.gridx = 0;
		gbc_mouseModeLabel.gridy = 0;
		mouseModePanel.add(mouseModeLabel, gbc_mouseModeLabel);
		
		ButtonGroup mouseModeChoice = new ButtonGroup();
		mainTabPanel.add(mouseModePanel);
		mouseModeChoice.add(rdbtnMouseModeOff);
		mouseModeChoice.add(rdbtnMouseModePitch);
		mouseModeChoice.add(rdbtnMouseModeBlue);
		mouseModeChoice.add(rdbtnMouseModeYellow);
		
		GridBagConstraints gbc_rdbtnMouseModeOff = new GridBagConstraints();
		gbc_rdbtnMouseModeOff.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeOff.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeOff.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeOff.gridx = 0;
		gbc_rdbtnMouseModeOff.gridy = 1;
		mouseModePanel.add(rdbtnMouseModeOff, gbc_rdbtnMouseModeOff);
		rdbtnMouseModeOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (rdbtnMouseModeOff.isSelected())
					setMouseMode(MOUSE_MODE_OFF);
			}
		});
		
		GridBagConstraints gbc_mouseModeOffLabel = new GridBagConstraints();
		gbc_mouseModeOffLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeOffLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeOffLabel.gridx = 1;
		gbc_mouseModeOffLabel.gridy = 1;
		JLabel mouseModeOffLabel = new JLabel("Off");
		mouseModeOffLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeOffLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				rdbtnMouseModeOff.doClick();
			}
		});
		mouseModePanel.add(mouseModeOffLabel, gbc_mouseModeOffLabel);
		
		GridBagConstraints gbc_rdbtnMouseModePitch = new GridBagConstraints();
		gbc_rdbtnMouseModePitch.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModePitch.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModePitch.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModePitch.gridx = 0;
		gbc_rdbtnMouseModePitch.gridy = 2;
		mouseModePanel.add(rdbtnMouseModePitch, gbc_rdbtnMouseModePitch);
		rdbtnMouseModePitch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (rdbtnMouseModePitch.isSelected())
					setMouseMode(MOUSE_MODE_PITCH_BOUNDARY);
			}
		});
		
		GridBagConstraints gbc_mouseModePitchLabel = new GridBagConstraints();
		gbc_mouseModePitchLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModePitchLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModePitchLabel.gridx = 1;
		gbc_mouseModePitchLabel.gridy = 2;
		JLabel mouseModePitchLabel = new JLabel("Pitch Boundary Selection");
		mouseModePitchLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModePitchLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				rdbtnMouseModePitch.doClick();
			}
		});
		mouseModePanel.add(mouseModePitchLabel, gbc_mouseModePitchLabel);
		
		GridBagConstraints gbc_rdbtnMouseModeBlue = new GridBagConstraints();
		gbc_rdbtnMouseModeBlue.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeBlue.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeBlue.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeBlue.gridx = 0;
		gbc_rdbtnMouseModeBlue.gridy = 3;
		mouseModePanel.add(rdbtnMouseModeBlue, gbc_rdbtnMouseModeBlue);
		rdbtnMouseModeBlue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (rdbtnMouseModeBlue.isSelected())
					setMouseMode(MOUSE_MODE_BLUE_T);
			}
		});
		
		GridBagConstraints gbc_mouseModeBlueLabel = new GridBagConstraints();
		gbc_mouseModeBlueLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeBlueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeBlueLabel.gridx = 1;
		gbc_mouseModeBlueLabel.gridy = 3;
		JLabel mouseModeBlueLabel = new JLabel("Blue T Plate Selection");
		mouseModeBlueLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeBlueLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				rdbtnMouseModeBlue.doClick();
			}
		});
		mouseModePanel.add(mouseModeBlueLabel, gbc_mouseModeBlueLabel);
		
		GridBagConstraints gbc_rdbtnMouseModeYellow = new GridBagConstraints();
		gbc_rdbtnMouseModeYellow.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeYellow.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnMouseModeYellow.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeYellow.gridx = 0;
		gbc_rdbtnMouseModeYellow.gridy = 4;
		mouseModePanel.add(rdbtnMouseModeYellow, gbc_rdbtnMouseModeYellow);
		rdbtnMouseModeYellow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (rdbtnMouseModeYellow.isSelected())
					setMouseMode(MOUSE_MODE_YELLOW_T);
			}
		});
		
		GridBagConstraints gbc_mouseModeYellowLabel = new GridBagConstraints();
		gbc_mouseModeYellowLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeYellowLabel.insets = new Insets(0, 0, 0, 5);
		gbc_mouseModeYellowLabel.gridx = 1;
		gbc_mouseModeYellowLabel.gridy = 4;
		JLabel mouseModeYellowLabel = new JLabel("Yellow T Plate Selection");
		mouseModeYellowLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeYellowLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				rdbtnMouseModeYellow.doClick();
			}
		});
		mouseModePanel.add(mouseModeYellowLabel, gbc_mouseModeYellowLabel);
		
		// Save/load buttons
		JPanel saveLoadPanel = new JPanel();
		
		saveButton = new JButton("Save Settings");
		saveButton.addMouseListener(new MouseAdapter() {
			// Attempt to write all of the current thresholds to a file with a name 
			// based on the currently selected pitch.
			@Override
			public void mouseClicked(MouseEvent e) {
				int pitchNum = pitchConstants.getPitchNum();
				
				int result = JOptionPane.showConfirmDialog(saveButton,
						"Are you sure you want to save current constants for pitch " + pitchNum + "?");
				
				if (result == JOptionPane.NO_OPTION || result == JOptionPane.CANCEL_OPTION) return;
				
				pitchConstants.saveConstants(System.getProperty("user.dir") + "/constants/pitch" + pitchNum);
				camPanel.saveSettings(System.getProperty("user.dir") + "/constants/pitch" + pitchNum + "camera");
			}
		});
		
		saveLoadPanel.add(saveButton);
		
		loadButton = new JButton("Load Settings");
		loadButton.addMouseListener(new MouseAdapter() {
			// Override the current threshold settings from those set in
			// the correct constants file for the current pitch.
			@Override
			public void mouseClicked(MouseEvent e) {
				int pitchNum = rdbtnPitch0.isSelected() ? 0 : 1;
				
				int result = JOptionPane.showConfirmDialog(loadButton, "Are you sure you want to load " +
						"pre-saved constants for pitch " + pitchNum + "?");
				
				if (result == JOptionPane.NO_OPTION || result == JOptionPane.CANCEL_OPTION) return;
				
				pitchConstants.setPitchNum(pitchNum);
				camPanel.loadSettings(System.getProperty("user.dir") + "/constants/pitch" + pitchNum + "camera");
				reloadSliderDefaults();
			}
		});
		
		saveLoadPanel.add(loadButton);
		
		mainTabPanel.add(saveLoadPanel);

		rdbtnPitch0.setSelected(true);
		rdbtnPitch0.doClick();
		rdbtnBlue.setSelected(true);
		rdbtnBlue.doClick();
		rdbtnRight.setSelected(true);
		rdbtnRight.doClick();
		rdbtnDistortOff.setSelected(true);
		rdbtnDistortOff.doClick();
		rdbtnMouseModeOff.setSelected(true);
		rdbtnMouseModeOff.doClick();
	}
	
	/**
	 * Reloads the default values for the sliders from the PitchConstants file.
	 */
	public void reloadSliderDefaults() {
		for (int i = 0; i < PitchConstants.NUM_THRESHOLDS; ++i)
			tabPanels[i].setSliderValues(i, pitchConstants);
	}

	public int getMouseMode() {
		return mouseMode;
	}

	public void setMouseMode(int mouseMode) {
		this.mouseMode = mouseMode;
	}
}
