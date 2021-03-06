package vision.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import computer.ControlGUI2;

import vision.DistortionFix;
import vision.PitchConstants;
import vision.Position;
import vision.VideoStream;
import vision.interfaces.VideoReceiver;
import vision.interfaces.VisionDebugReceiver;
import vision.interfaces.WorldStateReceiver;
import world.state.WorldState;

@SuppressWarnings("serial")
public class VisionGUI extends JFrame implements VideoReceiver,
		VisionDebugReceiver, WorldStateReceiver {
	private final int videoWidth;
	private final int videoHeight;

	// Pitch dimension selector variables
	private boolean selectionActive = false;
	private Point anchor;
	private int a;
	private int b;
	private int c;
	private int d;

	// Stored to only have rendering happen in one place
	private BufferedImage frame;
	private int fps;
	private int frameCounter;
	private BufferedImage debugOverlay;

	// Mouse listener variables
	boolean letterAdjustment = false;
	boolean yellowPlateAdjustment = false;
	boolean bluePlateAdjustment = false;
	boolean greyCircleAdjustment = false;
	boolean targetAdjustment = false;
	int mouseX;
	int mouseY;
	String adjust = "";
	File currentFile;
	File imgLetterT = new File("icons/Tletter2.png");
	File imgYellowPlate = new File("icons/YellowPlateSelector.png");
	File imgBluePlate = new File("icons/BluePlateSelector.png");
	File imgGreyCircle = new File("icons/GreyCircleSelector.png");
	BufferedImage selectorImage = null;
	BufferedImage letterTSelectorImage = null;
	BufferedImage yellowPlateSelectorImage = null;
	BufferedImage bluePlateSelectorImage = null;
	BufferedImage greyCircleSelectorImage = null;
	ArrayList<?>[] extractedColourSettings;
	double imageCenterX;
	double imageCenterY;
	int rotation = 0;
	ArrayList<Integer> xList = new ArrayList<Integer>();
	ArrayList<Integer> yList = new ArrayList<Integer>();

	private final PitchConstants pitchConstants;
	private final VisionSettingsPanel settingsPanel;
	private final JPanel videoDisplay = new JPanel();
	private final WindowAdapter windowAdapter = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			dispose();

			System.exit(0);
		}
	};

	public VisionGUI(final int videoWidth, final int videoHeight,
			WorldState worldState, final PitchConstants pitchConsts,
			final VideoStream vStream, final DistortionFix distortionFix) {

		super("Vision");
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;

		// Set pitch constraints
		this.pitchConstants = pitchConsts;
		this.a = pitchConstants.getLeftBuffer();
		this.b = pitchConstants.getTopBuffer();
		this.c = this.videoWidth - pitchConstants.getRightBuffer() - a;
		this.d = this.videoHeight - pitchConstants.getBottomBuffer() - b;

		try {
			// Image T
			this.letterTSelectorImage = ImageIO.read(this.imgLetterT);
			// Image Yellow plate
			this.yellowPlateSelectorImage = ImageIO.read(this.imgYellowPlate);
			// Image Blue plate
			this.bluePlateSelectorImage = ImageIO.read(this.imgBluePlate);
			// Image Grey circle
			this.greyCircleSelectorImage = ImageIO.read(this.imgGreyCircle);
		} catch (IOException e) {
			System.out.println("Images not found");
			e.printStackTrace();
		}

		Container contentPane = this.getContentPane();

		Dimension videoSize = new Dimension(videoWidth, videoHeight);
		BufferedImage blankInitialiser = new BufferedImage(videoWidth,
				videoHeight, BufferedImage.TYPE_INT_RGB);
		getContentPane().setLayout(null);
		videoDisplay.setLocation(0, 0);
		this.videoDisplay.setMinimumSize(videoSize);
		this.videoDisplay.setSize(videoSize);
		contentPane.add(videoDisplay);

		this.settingsPanel = new VisionSettingsPanel(worldState,
				pitchConstants, vStream, distortionFix);

		settingsPanel.setLocation(videoSize.width, 0);
		contentPane.add(settingsPanel);

		this.setVisible(true);
		this.getGraphics().drawImage(blankInitialiser, 0, 0, null);

		settingsPanel.setSize(settingsPanel.getPreferredSize());
		Dimension frameSize = new Dimension(videoWidth
				+ settingsPanel.getPreferredSize().width, Math.max(videoHeight,
				settingsPanel.getPreferredSize().height));
		contentPane.setSize(frameSize);
		this.setSize(frameSize.width + 8, frameSize.height + 30);
		// Wait for size to actually be set before setting resizable to false.
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		this.setResizable(false);
		videoDisplay.setFocusable(true);
		videoDisplay.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent ke) {
			}

			public void keyReleased(KeyEvent ke) {
				adjust = KeyEvent.getKeyText(ke.getKeyCode());
			}

			public void keyTyped(KeyEvent e) {
			}
		});

		MouseInputAdapter mouseSelector = new MouseInputAdapter() {
			Rectangle selection;

			public void mousePressed(MouseEvent e) {
				switch (settingsPanel.getMouseMode()) {
				case VisionSettingsPanel.MOUSE_MODE_OFF:
					break;
				case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:

					selectionActive = true;
					System.out.println("Initialised anchor");
					// Pitch dimension selector
					anchor = e.getPoint();
					System.out.println(anchor.x);
					System.out.println(anchor.y);
					selection = new Rectangle(anchor);
					break;
				case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
					videoDisplay.grabFocus();
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
					videoDisplay.grabFocus();
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
					videoDisplay.grabFocus();
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREY_CIRCLES:
					videoDisplay.grabFocus();
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_TARGET:
					videoDisplay.grabFocus();
					WorldState.targetX = e.getX();
					WorldState.targetY = e.getY();
					break;
				}

			}

			public void mouseDragged(MouseEvent e) {
				switch (settingsPanel.getMouseMode()) {
				case VisionSettingsPanel.MOUSE_MODE_OFF:
					break;
				case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
					selection.setBounds((int) Math.min(anchor.x, e.getX()),
							(int) Math.min(anchor.y, e.getY()),
							(int) Math.abs(e.getX() - anchor.x),
							(int) Math.abs(e.getY() - anchor.y));
					a = (int) Math.min(anchor.x, e.getX());
					b = (int) Math.min(anchor.y, e.getY());
					c = (int) Math.abs(e.getX() - anchor.x);
					d = (int) Math.abs(e.getY() - anchor.y);
					break;
				case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREY_CIRCLES:
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				}
			}

			public void mouseReleased(MouseEvent e) {

				switch (settingsPanel.getMouseMode()) {
				case VisionSettingsPanel.MOUSE_MODE_OFF:
					break;
				case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
					selectionActive = false;

					if (e.getPoint().distance(anchor) > 5) {
						Object[] options = { "Main Pitch", "Side Pitch",
								"Cancel" };
						int pitchNum = JOptionPane.showOptionDialog(
								getComponent(0),
								"The parameters are to be set for this pitch",
								"Picking a pitch",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);

						// If option wasn't Cancel and the dialog wasn't closed
						if (pitchNum != 2
								&& pitchNum != JOptionPane.CLOSED_OPTION) {
							System.out.println(pitchNum);
							try {
								int top = b;
								int bottom = videoHeight - d - b;
								int left = a;
								int right = videoWidth - c - a;

								if (top > 0 && bottom > 0 && left > 0
										&& right > 0) {
									// Update pitch constants
									pitchConstants.setTopBuffer(top);
									pitchConstants.setBottomBuffer(bottom);
									pitchConstants.setLeftBuffer(left);
									pitchConstants.setRightBuffer(right);

									// Writing the new dimensions to file
									FileWriter writer = new FileWriter(
											new File("constants/pitch"
													+ pitchNum + "Dimensions"));

									writer.write("" + top + "\n");
									writer.write("" + bottom + "\n");
									writer.write("" + left + "\n");
									writer.write("" + right + "\n");

									writer.close();
									System.out.println("Wrote pitch const");
								} else {
									System.out
											.println("Pitch selection NOT succesful");
								}
								System.out.print("Top: " + top + " Bottom "
										+ bottom);
								System.out.println(" Right " + right + " Left "
										+ left);
							} catch (IOException e1) {
								System.out
										.println("Error writing pitch dimensions to file");
								e1.printStackTrace();
							}

							System.out.println("A: " + a + " B: " + b + " C: "
									+ c + " D:" + d);
						} else if (pitchNum == JOptionPane.CLOSED_OPTION
								|| pitchNum == 2) {
							System.out.println("Closed option picked");
							a = pitchConstants.getLeftBuffer();
							b = pitchConstants.getTopBuffer();
							c = videoWidth - pitchConstants.getRightBuffer()
									- pitchConstants.getLeftBuffer();
							d = videoHeight - pitchConstants.getTopBuffer()
									- pitchConstants.getBottomBuffer();
						}
						repaint();
					}
					break;
				case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
					letterAdjustment = true;
					selectorImage = letterTSelectorImage;
					currentFile = imgLetterT;
					// Get the center coordinates of the selector image in use
					imageCenterX = selectorImage.getWidth(null) / 2;
					imageCenterY = selectorImage.getHeight(null) / 2;
					break;
				case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
					letterAdjustment = true;
					currentFile = imgLetterT;
					selectorImage = letterTSelectorImage;
					// Get the center coordinates of the selector image in use
					imageCenterX = selectorImage.getWidth(null) / 2;
					imageCenterY = selectorImage.getHeight(null) / 2;
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
					if (!bluePlateAdjustment) {
						yellowPlateAdjustment = true;
						currentFile = imgYellowPlate;
						selectorImage = yellowPlateSelectorImage;
						// Get the center coordinates of the selector image in
						// use
						imageCenterX = selectorImage.getWidth(null) / 2;
						imageCenterY = selectorImage.getHeight(null) / 2;
					}
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREY_CIRCLES:
					System.out.println("Grey mode");
					greyCircleAdjustment = true;
					currentFile = imgGreyCircle;
					selectorImage = greyCircleSelectorImage;
					// Get the center coordinates of the selector image in use
					imageCenterX = selectorImage.getWidth(null) / 2;
					imageCenterY = selectorImage.getHeight(null) / 2;
					break;
				case VisionSettingsPanel.MOUSE_MODE_TARGET:
					System.out.println("target mode");
					targetAdjustment = true;
					currentFile = imgGreyCircle;
					selectorImage = greyCircleSelectorImage;
					// Get the center coordinates of the selector image in use
					imageCenterX = selectorImage.getWidth(null) / 2;
					imageCenterY = selectorImage.getHeight(null) / 2;
					break;
				}
			}
		};

		this.videoDisplay.addMouseListener(mouseSelector);
		this.videoDisplay.addMouseMotionListener(mouseSelector);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(windowAdapter);
	}

	@Override
	public void sendFrame(BufferedImage frame, int fps, int frameCounter) {
		this.frame = frame;
		this.fps = fps;
		this.frameCounter = frameCounter;
	}

	@Override
	public void sendDebugOverlay(BufferedImage debug) {
		// Use the image passed if debug is enabled
		if (settingsPanel.isDebugEnabled()) {
			this.debugOverlay = debug;
		}
		// Otherwise discard it and create a new image to work with
		else {
			this.debugOverlay = new BufferedImage(debug.getWidth(),
					debug.getHeight(), debug.getType());
		}
		Graphics debugGraphics = debugOverlay.getGraphics();
		Graphics2D g2d = (Graphics2D) debugGraphics;

		// Selected mode in the Vision GUI
		boolean mouseModeBlueT = settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_BLUE_T;
		boolean mouseModeYellowT = settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_YELLOW_T;
		boolean mouseModeGreenPlates = settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES;
		boolean mouseModeGreyCircle = settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_GREY_CIRCLES;
		boolean mouseSelectTarget = settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_TARGET; // moo
		// If the colour selection mode is on (for colour calibration from the
		// image)
		if (mouseSelectTarget) {
			g2d.drawOval(WorldState.targetX, WorldState.targetY, 5, 5);
			ControlGUI2.op4field.setText("" + WorldState.targetX);
			ControlGUI2.op5field.setText("" + WorldState.targetY);
		}

		if (mouseModeBlueT || mouseModeYellowT || mouseModeGreenPlates
				|| mouseModeGreyCircle) {
			// Show the colour selector image
			if (letterAdjustment || yellowPlateAdjustment
					|| bluePlateAdjustment || greyCircleAdjustment) {
				g2d.drawImage(selectorImage, mouseX, mouseY, null);
			}
			// Controlling the selector image
			rotationControl(settingsPanel.getMouseMode());
		}
		// Eliminating area around the pitch dimensions
		if (!selectionActive) {
			int a = pitchConstants.getLeftBuffer();
			int b = pitchConstants.getTopBuffer();
			int c = videoWidth - pitchConstants.getRightBuffer() - a;
			int d = videoHeight - pitchConstants.getBottomBuffer() - b;
			// Making the pitch surroundings transparent
			Composite originalComposite = g2d.getComposite();
			int type = AlphaComposite.SRC_OVER;
			AlphaComposite alphaComp = (AlphaComposite.getInstance(type, 0.6f));
			g2d.setComposite(alphaComp);
			debugGraphics.setColor(Color.BLACK);
			// Rectangle covering the BOTTOM
			debugGraphics.fillRect(0, 0, videoWidth, b);
			// Rectangle covering the LEFT
			debugGraphics.fillRect(0, b, a, videoHeight);
			// Rectangle covering the BOTTOM
			debugGraphics.fillRect(a + c, b, videoWidth - a, videoHeight - b);
			// Rectangle covering the RIGHT
			debugGraphics.fillRect(a, b + d, c, videoHeight - d);
			// Setting back normal settings
			g2d.setComposite(originalComposite);
		}
		if (settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY) {
			// Draw the line around the pitch dimensions
			if (selectionActive) {
				debugGraphics.setColor(Color.YELLOW);
				debugGraphics.drawRect(a, b, c, d);
			}
		}
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Graphics frameGraphics = frame.getGraphics();

		// Draw overlay on top of raw frame
		frameGraphics.drawImage(debugOverlay, 0, 0, null);

		// Draw frame info and worldstate on top of the result
		// Display the FPS that the vision system is running at
		frameGraphics.setColor(Color.white);
		frameGraphics.drawString("Frame: " + frameCounter, 15, 15);
		frameGraphics.drawString("FPS: " + fps, 15, 30);

		// Display Ball & Robot Positions
		frameGraphics.drawString("Ball:", 15, 45);
		frameGraphics.drawString("(" + worldState.getBallX() + ", "
				+ worldState.getBallY() + ")", 60, 45);
		frameGraphics.drawString(
				"vel: (" + df.format(worldState.getBallXVelocity()) + ", "
						+ df.format(worldState.getBallYVelocity()) + ")", 140,
				45);

		frameGraphics.drawString("Blue:", 15, 60);
		frameGraphics.drawString("(" + worldState.getBlueX() + ", "
				+ worldState.getBlueY() + ")", 60, 60);
		frameGraphics.drawString(
				"vel: (" + df.format(worldState.getBlueXVelocity()) + ", "
						+ df.format(worldState.getBlueYVelocity()) + ")", 140,
				60);
		frameGraphics.drawString(
				"angle: "
						+ df.format(Math.toDegrees(worldState
								.getBlueOrientation())), 260, 60);

		frameGraphics.drawString("Yellow:", 15, 75);
		frameGraphics.drawString("(" + worldState.getYellowX() + ", "
				+ worldState.getYellowY() + ")", 60, 75);
		frameGraphics.drawString(
				"vel: (" + df.format(worldState.getYellowXVelocity()) + ", "
						+ df.format(worldState.getYellowYVelocity()) + ")",
				140, 75);
		frameGraphics.drawString(
				"angle: "
						+ df.format(Math.toDegrees(worldState
								.getYellowOrientation())), 260, 75);

		// Mark goals:
		Position leftGoal = worldState.goalInfo.getLeftGoalCenter();
		Position rightGoal = worldState.goalInfo.getRightGoalCenter();

		frameGraphics.setColor(Color.yellow);
		frameGraphics.drawOval(leftGoal.getX() - 2, leftGoal.getY() - 2, 4, 4);
		frameGraphics
				.drawOval(rightGoal.getX() - 2, rightGoal.getY() - 2, 4, 4);

		Position leftGoalTop = worldState.goalInfo.getLeftGoalTop();
		Position leftGoalBottom = worldState.goalInfo.getLeftGoalBottom();
		frameGraphics.drawLine(leftGoalTop.getX(), leftGoalTop.getY(),
				leftGoalBottom.getX(), leftGoalBottom.getY());

		Position rightGoalTop = worldState.goalInfo.getRightGoalTop();
		Position rightGoalBottom = worldState.goalInfo.getRightGoalBottom();
		frameGraphics.drawLine(rightGoalTop.getX(), rightGoalTop.getY(),
				rightGoalBottom.getX(), rightGoalBottom.getY());

		// Draw overall composite to screen
		Graphics videoGraphics = videoDisplay.getGraphics();
		videoGraphics.drawImage(frame, 0, 0, null);
	}

	public void rotationControl(int mouseMode) {

		int object = -1;

		switch (mouseMode) {
		case (VisionSettingsPanel.MOUSE_MODE_BLUE_T):
			object = PitchConstants.BLUE;
			break;
		case (VisionSettingsPanel.MOUSE_MODE_YELLOW_T):
			object = PitchConstants.YELLOW;
			break;
		case (VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES):
			object = PitchConstants.GREEN;
			break;
		case (VisionSettingsPanel.MOUSE_MODE_GREY_CIRCLES):
			object = PitchConstants.GREY;
			break;
		}

		// Control the selector images using the keyboard
		if (letterAdjustment || yellowPlateAdjustment || bluePlateAdjustment
				|| greyCircleAdjustment) {
			if (adjust.equals("Up")) {
				mouseY--;
			} else if (adjust.equals("Down")) {
				mouseY++;
			} else if (adjust.equals("Left")) {
				mouseX--;
			} else if (adjust.equals("Right")) {
				mouseX++;
			} else if (adjust.equals("Z")) {
				rotateSelectorImage(Math.toRadians((double) rotation--));
			} else if (adjust.equals("X")) {
				rotateSelectorImage(Math.toRadians((double) rotation++));
			} else if (adjust.equals("A")) {
				rotation -= 10;
				rotateSelectorImage(Math.toRadians((double) rotation));
			} else if (adjust.equals("S")) {
				rotation += 10;
				rotateSelectorImage(Math.toRadians((double) rotation));
			} else if (adjust.equals("Enter")) {

				if (letterAdjustment) {
					letterAdjustment = false;
					extractedColourSettings = getColourRange(frame, object);
					setColourRange(extractedColourSettings, object);
					clearArrayOfLists(extractedColourSettings);
				} else if (yellowPlateAdjustment) {
					yellowPlateAdjustment = false;
					bluePlateAdjustment = true;
					extractedColourSettings = getColourRange(frame, object);
					selectorImage = bluePlateSelectorImage;
					currentFile = imgBluePlate;
				} else if (bluePlateAdjustment) {
					bluePlateAdjustment = false;
					extractedColourSettings = getColourRange(frame, object);
					setColourRange(extractedColourSettings, object);
					clearArrayOfLists(extractedColourSettings);
				} else if (greyCircleAdjustment) {
					greyCircleAdjustment = false;
					extractedColourSettings = getColourRange(frame, object);
					setColourRange(extractedColourSettings, object);
					clearArrayOfLists(extractedColourSettings);
				}

			}
			adjust = "";
		}
	}

	public void rotateSelectorImage(double rotationRequired) {
		AffineTransform tx = AffineTransform.getRotateInstance(
				rotationRequired, imageCenterX, imageCenterY);
		AffineTransformOp op = new AffineTransformOp(tx,
				AffineTransformOp.TYPE_BILINEAR);
		// Reset the original selector image so it is not blurry
		try {
			selectorImage = ImageIO.read(currentFile);
		} catch (IOException e) {

		}
		selectorImage = op.filter(selectorImage, null);
	}

	public ArrayList<?>[] getColourRange(BufferedImage frame, int object) {

		ArrayList<Integer> redList = new ArrayList<Integer>();
		ArrayList<Integer> greenList = new ArrayList<Integer>();
		ArrayList<Integer> blueList = new ArrayList<Integer>();
		ArrayList<Float> hueList = new ArrayList<Float>();
		ArrayList<Float> satList = new ArrayList<Float>();
		ArrayList<Float> valList = new ArrayList<Float>();
		ArrayList<?>[] colourSettings = { redList, greenList, blueList, hueList,
				satList, valList };

		if (object == PitchConstants.BLUE || object == PitchConstants.YELLOW) {
			/** PROCESSING EITHER LETTER T */
			// Process top part of the letter T
			colourSettings = getColourValues(frame, colourSettings, 12, 35, 15,
					24);
			// Process bottom part of the letter T
			colourSettings = getColourValues(frame, colourSettings, 21, 30, 24,
					44);
		} else if (object == PitchConstants.GREEN) {
			/** PROCESSING EITHER OF THE GREEN PLATES */
			// Process the top left quadrant of the green plate
			colourSettings = getColourValues(frame, colourSettings, 0 + 15,
					10 + 15, 0 + 25, 15 + 25);
			// Process the top right quadrant of the green plate
			colourSettings = getColourValues(frame, colourSettings, 21 + 15,
					30 + 15, 0 + 25, 15 + 25);
			// Process the bottom left quadrant of the green plate
			colourSettings = getColourValues(frame, colourSettings, 0 + 15,
					10 + 15, 25 + 25, 50 + 25);
			// Process the bottom right quadrant of the green plate
			colourSettings = getColourValues(frame, colourSettings, 22 + 15,
					30 + 15, 25 + 25, 50 + 25);
		} else if (object == PitchConstants.GREY) {
			// Process the ball
			colourSettings = getColourValues(frame, colourSettings, 0, 8, 0, 8);
		}
		return colourSettings;
	}

	public ArrayList<?>[] getColourValues(BufferedImage frame,
			ArrayList[] colourSettings, int fromX, int toX, int fromY, int toY) {
		int lx = (int) imageCenterX;
		int ly = (int) imageCenterY;

		for (int x = fromX - lx; x < toX - lx; x++)
			for (int y = fromY - ly; y < toY - ly; y++) {

				// Getting the colour from pixels subject to rotation
				double xR = x * Math.cos(Math.toRadians((double) rotation)) - y
						* Math.sin(Math.toRadians((double) rotation));
				double yR = x * Math.sin(Math.toRadians((double) rotation)) + y
						* Math.cos(Math.toRadians((double) rotation));

				xList.add(mouseX + lx + (int) xR);
				yList.add(mouseY + ly + (int) yR);

				Color c = new Color(frame.getRGB(mouseX + lx + (int) xR, mouseY
						+ ly + (int) yR));

				float[] hsbvals = Color.RGBtoHSB(c.getRed(), c.getGreen(),
						c.getBlue(), null);

				colourSettings[0].add(c.getRed()); // RED
				colourSettings[1].add(c.getGreen()); // GREEN
				colourSettings[2].add(c.getBlue()); // BLUE

				colourSettings[3].add(hsbvals[0]); // HUE
				colourSettings[4].add(hsbvals[1]); // SATURATION
				colourSettings[5].add(hsbvals[2]); // VALUE

			}

		rotation = 0;
		return colourSettings;

	}

	public void setColourRange(ArrayList[] colourSettings, int object) {
		/** Mean and Standard deviation calculations for the RGB and HSB values */
		// RED LIST
		double meanR = calcMean(colourSettings[0]);
		double stdevR = calcStandardDeviation(colourSettings[0]);
		// GREEN LIST
		double meanG = calcMean(colourSettings[1]);
		double stdevG = calcStandardDeviation(colourSettings[1]);
		// BLUE LIST
		double meanB = calcMean(colourSettings[2]);
		double stdevB = calcStandardDeviation(colourSettings[2]);
		// HUE LIST
		double meanH = calcMeanFloat(colourSettings[3]);
		double stdevH = calcStandardDeviationFloat(colourSettings[3]);
		// SATURATION LIST
		double meanS = calcMeanFloat(colourSettings[4]);
		double stdevS = calcStandardDeviationFloat(colourSettings[4]);
		// VALUE LIST
		double meanV = calcMeanFloat(colourSettings[5]);
		double stdevV = calcStandardDeviationFloat(colourSettings[5]);

		System.out.println("Red mean " + meanR);
		System.out.println("Green mean " + meanG);
		System.out.println("Blue mean " + meanB);
		System.out.println("Red std " + stdevR);
		System.out.println("Green std " + stdevG);
		System.out.println("Blue std " + stdevB);
		System.out.println("H mean " + meanH);
		System.out.println("S mean " + meanS);
		System.out.println("V mean " + meanV);
		System.out.println("H std " + stdevH);
		System.out.println("S std " + stdevS);
		System.out.println("V std " + stdevV);

		/** Setting the sliders in the GUI */
		double stDevConstant = 2;

		pitchConstants.setRedLower(
				object,
				Math.max(PitchConstants.RGBMIN, (int) (meanR - stDevConstant
						* stdevR)));
		pitchConstants.setRedUpper(
				object,
				Math.min(PitchConstants.RGBMAX, (int) (meanR + stDevConstant
						* stdevR)));

		pitchConstants.setGreenLower(
				object,
				Math.max(PitchConstants.RGBMIN, (int) (meanG - stDevConstant
						* stdevG)));
		pitchConstants.setGreenUpper(
				object,
				Math.min(PitchConstants.RGBMAX, (int) (meanG + stDevConstant
						* stdevG)));

		pitchConstants.setBlueLower(
				object,
				Math.max(PitchConstants.RGBMIN, (int) (meanB - stDevConstant
						* stdevB)));
		pitchConstants.setBlueUpper(
				object,
				Math.min(PitchConstants.RGBMAX, (int) (meanB + stDevConstant
						* stdevB)));

		// Works best with the Hue range 0-1 for the blue and yellow Ts
		pitchConstants.setHueLower(object,
				Math.max(PitchConstants.HSVMIN, (float) (0)));
		pitchConstants.setHueUpper(object,
				Math.min(PitchConstants.HSVMAX, (float) (1)));

		pitchConstants.setSaturationLower(
				object,
				Math.max(PitchConstants.HSVMIN, (float) (meanS - stDevConstant
						* stdevS)));
		pitchConstants.setSaturationUpper(
				object,
				Math.min(PitchConstants.HSVMAX, (float) (meanS + stDevConstant
						* stdevS)));

		pitchConstants.setValueLower(
				object,
				Math.max(PitchConstants.HSVMIN, (float) (meanV - stDevConstant
						* stdevV)));
		pitchConstants.setValueUpper(
				object,
				Math.min(PitchConstants.HSVMAX, (float) (meanV + stDevConstant
						* stdevV)));

		settingsPanel.reloadSliderDefaults();

	}

	public void clearArrayOfLists(ArrayList<?>[] arrays) {
		for (int i = 0; i < arrays.length; i++)
			arrays[i].clear();
	}

	public double calcStandardDeviationFloat(ArrayList<Float> points) {

		double mean = calcMeanFloat(points);
		double sum = 0;
		for (int i = 0; i < points.size(); i++) {
			float p = points.get(i);
			double diff = p - mean;
			sum += diff * diff;
		}

		return Math.sqrt(sum / points.size());
	}

	public double calcMeanFloat(ArrayList<Float> points) {
		float sum = 0;
		for (int i = 0; i < points.size(); i++) {
			sum += points.get(i);
		}
		return (double) (sum) / points.size();
	}

	public double calcStandardDeviation(ArrayList<Integer> points) {

		double mean = calcMean(points);
		double sum = 0;
		for (int i = 0; i < points.size(); i++) {
			int p = points.get(i);
			double diff = p - mean;
			sum += diff * diff;
		}

		return Math.sqrt(sum / points.size());
	}

	public double calcMean(ArrayList<Integer> points) {
		int sum = 0;
		for (int i = 0; i < points.size(); i++) {
			sum += points.get(i);
		}
		return (double) (sum) / points.size();
	}

}
