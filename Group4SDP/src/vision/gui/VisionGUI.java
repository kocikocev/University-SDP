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
import java.awt.Toolkit;
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
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import vision.DistortionFix;
import vision.PitchConstants;
import vision.Position;
import vision.VideoStream;
import vision.VisionInterface;
import vision.WorldState;

@SuppressWarnings("serial")
public class VisionGUI extends JFrame implements VisionInterface {
	private final int videoWidth;
	private final int videoHeight;

	// Pitch dimension selector variables
	private boolean selectionActive = false;
	private Point anchor;
	private int a;
	private int b;
	private int c;
	private int d;

	// Mouse listener variable
	int mouse_event = 0;
	boolean letterAdjustment = false;
	int mouseX;
	int mouseY;
	String adjust = "";
	BufferedImage t = null;
	double locationX;
	double locationY;
	int rotation = 0;
	ArrayList<Integer> xList = new ArrayList<Integer>();
	ArrayList<Integer> yList = new ArrayList<Integer>();
	boolean theT = false;

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

		// Image T
		File img = new File("icons/Tletter.png");
		BufferedImage t = null;
		try {
			this.t = ImageIO.read(img);
			locationX = this.t.getWidth(null) / 2;
			locationY = this.t.getHeight(null) / 2;
		} catch (IOException e) {
			System.out.println("No T image found");
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
				System.out.println("Key pressed");
			}

			public void keyReleased(KeyEvent ke) {
				System.out.println("Key released");
				adjust = KeyEvent.getKeyText(ke.getKeyCode());
			}

			public void keyTyped(KeyEvent e) {
				System.out.println("Key typed");
			}
		});

		MouseInputAdapter mouseSelector = new MouseInputAdapter() {
			Rectangle selection;

			public void mousePressed(MouseEvent e) {
				// Mouse clicked
				mouse_event = 2;
				selectionActive = true;
				switch (mouse_event) {
				case 0:
					break;
				case 1:
					// Pitch dimension selector
					anchor = e.getPoint();
					System.out.println(anchor.x);
					System.out.println(anchor.y);
					selection = new Rectangle(anchor);
					break;
				case 2:
					videoDisplay.grabFocus();
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				}

			}

			public void mouseDragged(MouseEvent e) {

				switch (mouse_event) {
				case 0:
					break;
				case 1:
					selection.setBounds((int) Math.min(anchor.x, e.getX()),
							(int) Math.min(anchor.y, e.getY()),
							(int) Math.abs(e.getX() - anchor.x),
							(int) Math.abs(e.getY() - anchor.y));
					a = (int) Math.min(anchor.x, e.getX());
					b = (int) Math.min(anchor.y, e.getY());
					c = (int) Math.abs(e.getX() - anchor.x);
					d = (int) Math.abs(e.getY() - anchor.y);
					break;
				case 2:
					mouseX = e.getX();
					mouseY = e.getY();
					break;
				}
			}

			public void mouseReleased(MouseEvent e) {
				selectionActive = false;

				switch (mouse_event) {
				case 0:
					break;
				case 1:
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
						}
						repaint();
					}
					break;
				case 2:
					letterAdjustment = true;
					break;
				}

				mouse_event = 0;
			}
		};

		this.videoDisplay.addMouseListener(mouseSelector);
		this.videoDisplay.addMouseMotionListener(mouseSelector);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(windowAdapter);
	}

	@Override
	public void sendFrame(BufferedImage frame, int fps, Point ball,
			Point blueRobot, double blueOrientation, Point yellowRobot,
			double yellowOrientation) {
		Graphics videoGraphics = videoDisplay.getGraphics();
		Graphics imageGraphics = frame.getGraphics();

		// Draw the line around the pitch dimensions
		if (selectionActive) {
			imageGraphics.setColor(Color.YELLOW);
			imageGraphics.drawRect(a, b, c, d);
		}

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Graphics2D g2d = (Graphics2D) imageGraphics;


		if (letterAdjustment) {

			if (adjust.equals("Up")) {
				mouseY--;
			} else if (adjust.equals("Down")) {
				mouseY++;
			} else if (adjust.equals("Left")) {
				mouseX--;
			} else if (adjust.equals("Right")) {
				mouseX++;
			} else if (adjust.equals("Enter")) {
				letterAdjustment = false;
				getColourRange(frame, PitchConstants.BLUE );
			} else if (adjust.equals("Z")) {

				double rotationRequired = Math.toRadians((double) rotation--);

				AffineTransform tx = AffineTransform.getRotateInstance(
						rotationRequired, locationX, locationY);
				AffineTransformOp op = new AffineTransformOp(tx,
						AffineTransformOp.TYPE_BILINEAR);
				File img = new File("icons/Tletter.png");

				try {
					t = ImageIO.read(img);
					locationX = this.t.getWidth(null) / 2;
					locationY = this.t.getHeight(null) / 2;
				} catch (IOException e) {

				}

				t = op.filter(t, null);

			} else if (adjust.equals("X")) {

				double rotationRequired = Math.toRadians((double) rotation++);

				AffineTransform tx = AffineTransform.getRotateInstance(
						rotationRequired, locationX, locationY);
				AffineTransformOp op = new AffineTransformOp(tx,
						AffineTransformOp.TYPE_BILINEAR);
				File img = new File("icons/Tletter.png");

				try {
					t = ImageIO.read(img);
					locationX = this.t.getWidth(null) / 2;
					locationY = this.t.getHeight(null) / 2;
				} catch (IOException e) {

				}
				t = op.filter(t, null);

			}
			adjust = "";
		}

		// TODO: Show T colour selector
		if (selectionActive && mouse_event == 2 || letterAdjustment) {
			g2d.drawImage(t, mouseX, mouseY, null);
		}

		// Eliminating area around the pitch dimensions
		if (!selectionActive) {
			// Making the pitch surroundings transparent
			Composite originalComposite = g2d.getComposite();
			int type = AlphaComposite.SRC_OVER;
			AlphaComposite alphaComp = (AlphaComposite.getInstance(type, 0.6f));
			g2d.setComposite(alphaComp);
			imageGraphics.setColor(Color.BLACK);
			// Rectangle covering the BOTTOM
			imageGraphics.fillRect(0, 0, videoWidth, b);
			// Rectangle covering the LEFT
			imageGraphics.fillRect(0, b, a, videoHeight);
			// Rectangle covering the BOTTOM
			imageGraphics.fillRect(a + c, b, videoWidth - a, videoHeight - b);
			// Rectangle covering the RIGHT
			imageGraphics.fillRect(a, b + d, c, videoHeight - d);
			// Setting back normal settings
			g2d.setComposite(originalComposite);
		}

		// Draw the line around the pitch dimensions
		if (selectionActive) {
			imageGraphics.setColor(Color.YELLOW);
			imageGraphics.drawRect(a, b, c, d);
		}

		// Display the FPS that the vision system is running at
		imageGraphics.setColor(Color.white);
		imageGraphics.drawString("FPS: " + fps, 15, 15);

		// Display Ball & Robot Positions
		imageGraphics.drawString("Ball: (" + ball.x + ", " + ball.y + ")", 15,
				30);
		imageGraphics.drawString("Blue: (" + blueRobot.x + ", " + blueRobot.y
				+ ") Orientation: " + Math.toDegrees(blueOrientation), 15, 45);
		imageGraphics
				.drawString(
						"Yellow: (" + yellowRobot.x + ", " + yellowRobot.y
								+ ") Orientation: "
								+ Math.toDegrees(yellowOrientation), 15, 60);

		videoGraphics.drawImage(frame, 0, 0, null);
	}

	public void getColourRange(BufferedImage frame, int object) {
		
		ArrayList<Integer> redList = new ArrayList<Integer>();
		ArrayList<Integer> greenList = new ArrayList<Integer>();
		ArrayList<Integer> blueList = new ArrayList<Integer>();
		ArrayList<Float> hueList = new ArrayList<Float>();
		ArrayList<Float> satList = new ArrayList<Float>();
		ArrayList<Float> valList = new ArrayList<Float>();

		int lX = (int)locationX;
		int lY = (int)locationY;

		//Top part of the letter T 
		for (int x = 0 - lX; x < 27 - lX; x++)
			for (int y = 0 - lY; y < 11 - lY; y++) {
				double xR = x * Math.cos(Math.toRadians((double) rotation)) - y
						* Math.sin(Math.toRadians((double) rotation));
				double yR = x * Math.sin(Math.toRadians((double) rotation)) + y
						* Math.cos(Math.toRadians((double) rotation));

				xList.add(mouseX + lX + (int) xR);
				yList.add(mouseY + lY + (int) yR);

				Color c = new Color(frame.getRGB(mouseX + lX + (int) xR, mouseY
						+ lY + (int) yR));

				float[] hsbvals = c.RGBtoHSB(c.getRed(), c.getGreen(),
						c.getBlue(), null);
				
				hueList.add(hsbvals[0]);
				satList.add(hsbvals[1]);
				valList.add(hsbvals[2]);
				redList.add(c.getRed());
				greenList.add(c.getGreen());
				blueList.add(c.getBlue());
				// frame.setRGB(mouseX + lX + (int) xR, mouseY + lY + (int) yR,
				// 65535);

			}

		//Bottom part of the letter T 
		for (int x = 10 - lX; x < 20 - lX; x++)
			for (int y = 10 - lY; y < 32 - lY; y++) {
				double xR = x * Math.cos(Math.toRadians((double) rotation)) - y
						* Math.sin(Math.toRadians((double) rotation));
				double yR = x * Math.sin(Math.toRadians((double) rotation)) + y
						* Math.cos(Math.toRadians((double) rotation));

				xList.add(mouseX + lX + (int) xR);
				yList.add(mouseY + lY + (int) yR);

				Color c = new Color(frame.getRGB(mouseX + lX + (int) xR, mouseY
						+ lY + (int) yR));

				float[] hsbvals = c.RGBtoHSB(c.getRed(), c.getGreen(),
						c.getBlue(), null);			
				redList.add(c.getRed());
				greenList.add(c.getGreen());
				blueList.add(c.getBlue());
				hueList.add(hsbvals[0]);
				satList.add(hsbvals[1]);
				valList.add(hsbvals[2]);
			
				// frame.setRGB(mouseX + lX + (int) xR, mouseY + lY + (int) yR,
				// 65535);
			}

		//Mean and Standard deviation calculations
		double meanR = calcMean(redList);
		double stdevR = calcStandardDeviation(redList);
		double meanG = calcMean(greenList);
		double stdevG = calcStandardDeviation(greenList);
		double meanB = calcMean(blueList);
		double stdevB = calcStandardDeviation(blueList);
		double meanH = calcMeanFloat(hueList);
		double stdevH = calcStandardDeviationFloat(hueList);
		double meanS = calcMeanFloat(satList);
		double stdevS = calcStandardDeviationFloat(satList);
		double meanV = calcMeanFloat(valList);
		double stdevV = calcStandardDeviationFloat(valList);

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

		//Setting the sliders
		pitchConstants.setRedLower(object, Math.max(PitchConstants.RGBMIN,(int) (meanR - stdevR)));
		pitchConstants.setRedUpper(object, Math.min(PitchConstants.RGBMAX, (int)(meanR + stdevR)));
		
		pitchConstants.setGreenLower(object, Math.max(PitchConstants.RGBMIN,(int) (meanG - stdevG)));
		pitchConstants.setGreenUpper(object, Math.min(PitchConstants.RGBMAX, (int)(meanG + stdevG)));
		
		pitchConstants.setBlueLower(object, Math.max(PitchConstants.RGBMIN,(int) (meanB - stdevB)));
		pitchConstants.setBlueUpper(object, Math.min(PitchConstants.RGBMAX, (int)(meanB + stdevB)));
		
		pitchConstants.setHueLower(object, Math.max(PitchConstants.RGBMIN,(float)(meanH - stdevH)));
		pitchConstants.setHueUpper(object, Math.min(PitchConstants.RGBMAX, (float)(meanH + stdevH)));
		
		pitchConstants.setSaturationLower(object, Math.max(PitchConstants.RGBMIN,(float) (meanS - stdevS)));
		pitchConstants.setSaturationUpper(object, Math.min(PitchConstants.RGBMAX, (float)(meanS + stdevS)));
		
		pitchConstants.setValueLower(object, Math.max(PitchConstants.RGBMIN,(float) (meanV - stdevV)));
		pitchConstants.setValueUpper(object, Math.min(PitchConstants.RGBMAX, (float)(meanV + stdevV)));
		
		

		theT = true;
		rotation = 0;
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
