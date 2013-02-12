package vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import vision.Kmeans;
import vision.interfaces.VideoReceiver;
import vision.interfaces.VisionDebugReceiver;
import vision.interfaces.WorldStateReceiver;

/**
 * The main class for showing the video feed and processing the video data.
 * Identifies ball and robot locations, and robot orientations.
 * 
 * @author s0840449
 */
// TODO: separate the GUI - can combine it with VisionGUI.java
public class Vision implements VideoReceiver {

	// Variables used in processing video
	private final PitchConstants pitchConstants;
	private final WorldState worldState;
	private ArrayList<VisionDebugReceiver> visionDebugReceivers = new ArrayList<VisionDebugReceiver>();
	private ArrayList<WorldStateReceiver> worldStateReceivers = new ArrayList<WorldStateReceiver>();

	/**
	 * Default constructor.
	 * 
	 * @param worldState
	 * @param pitchConstants
	 * @param pitchConstants
	 * 
	 */
	public Vision(WorldState worldState, PitchConstants pitchConstants) {
		// Set the state fields.
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
	}

	/**
	 * @return The current world state
	 */
	public WorldState getWorldState() {
		return worldState;
	}

	/**
	 * Adds a receiver to the list of objects which receive the vision debug
	 * overlay
	 * 
	 * @param receiver
	 */
	public void addVisionDebugReceiver(VisionDebugReceiver receiver) {
		visionDebugReceivers.add(receiver);
	}

	/**
	 * Sends the vision system the next frame to process
	 */
	public void sendFrame(BufferedImage frame, int frameRate, int frameCounter) {
		processAndUpdateImage(frame, frameRate, frameCounter);
	}

	// TODO: Find out what this is.
	public void addWorldStateReceiver(WorldStateReceiver receiver) {
		worldStateReceivers.add(receiver);
	}

	/**
	 * Determines if a pixel is part of the blue T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	private boolean isBlue(Color colour, float[] hsbvals) {
		return hsbvals[0] <= pitchConstants.getHueUpper(PitchConstants.BLUE)
				&& hsbvals[0] >= pitchConstants
						.getHueLower(PitchConstants.BLUE)
				&& hsbvals[1] <= pitchConstants
						.getSaturationUpper(PitchConstants.BLUE)
				&& hsbvals[1] >= pitchConstants
						.getSaturationLower(PitchConstants.BLUE)
				&& hsbvals[2] <= pitchConstants
						.getValueUpper(PitchConstants.BLUE)
				&& hsbvals[2] >= pitchConstants
						.getValueLower(PitchConstants.BLUE)
				&& colour.getRed() <= pitchConstants
						.getRedUpper(PitchConstants.BLUE)
				&& colour.getRed() >= pitchConstants
						.getRedLower(PitchConstants.BLUE)
				&& colour.getGreen() <= pitchConstants
						.getGreenUpper(PitchConstants.BLUE)
				&& colour.getGreen() >= pitchConstants
						.getGreenLower(PitchConstants.BLUE)
				&& colour.getBlue() <= pitchConstants
						.getBlueUpper(PitchConstants.BLUE)
				&& colour.getBlue() >= pitchConstants
						.getBlueLower(PitchConstants.BLUE);
	}

	/**
	 * Determines if a pixel is part of the yellow T, based on input RGB colours
	 * and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the yellow T), false otherwise.
	 */
	private boolean isYellow(Color colour, float[] hsbvals) {
		return hsbvals[0] <= pitchConstants.getHueUpper(PitchConstants.YELLOW)
				&& hsbvals[0] >= pitchConstants
						.getHueLower(PitchConstants.YELLOW)
				&& hsbvals[1] <= pitchConstants
						.getSaturationUpper(PitchConstants.YELLOW)
				&& hsbvals[1] >= pitchConstants
						.getSaturationLower(PitchConstants.YELLOW)
				&& hsbvals[2] <= pitchConstants
						.getValueUpper(PitchConstants.YELLOW)
				&& hsbvals[2] >= pitchConstants
						.getValueLower(PitchConstants.YELLOW)
				&& colour.getRed() <= pitchConstants
						.getRedUpper(PitchConstants.YELLOW)
				&& colour.getRed() >= pitchConstants
						.getRedLower(PitchConstants.YELLOW)
				&& colour.getGreen() <= pitchConstants
						.getGreenUpper(PitchConstants.YELLOW)
				&& colour.getGreen() >= pitchConstants
						.getGreenLower(PitchConstants.YELLOW)
				&& colour.getBlue() <= pitchConstants
						.getBlueUpper(PitchConstants.YELLOW)
				&& colour.getBlue() >= pitchConstants
						.getBlueLower(PitchConstants.YELLOW);
	}

	/**
	 * Determines if a pixel is part of the ball, based on input RGB colours and
	 * hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the ball), false otherwise.
	 */
	private boolean isBall(Color colour, float[] hsbvals) {
		return hsbvals[0] <= pitchConstants.getHueUpper(PitchConstants.BALL)
				&& hsbvals[0] >= pitchConstants
						.getHueLower(PitchConstants.BALL)
				&& hsbvals[1] <= pitchConstants
						.getSaturationUpper(PitchConstants.BALL)
				&& hsbvals[1] >= pitchConstants
						.getSaturationLower(PitchConstants.BALL)
				&& hsbvals[2] <= pitchConstants
						.getValueUpper(PitchConstants.BALL)
				&& hsbvals[2] >= pitchConstants
						.getValueLower(PitchConstants.BALL)
				&& colour.getRed() <= pitchConstants
						.getRedUpper(PitchConstants.BALL)
				&& colour.getRed() >= pitchConstants
						.getRedLower(PitchConstants.BALL)
				&& colour.getGreen() <= pitchConstants
						.getGreenUpper(PitchConstants.BALL)
				&& colour.getGreen() >= pitchConstants
						.getGreenLower(PitchConstants.BALL)
				&& colour.getBlue() <= pitchConstants
						.getBlueUpper(PitchConstants.BALL)
				&& colour.getBlue() >= pitchConstants
						.getBlueLower(PitchConstants.BALL);
	}

	/**
	 * Determines if a pixel is part of either grey circle, based on input RGB
	 * colours and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of a grey circle), false otherwise.
	 */
	private boolean isGrey(Color colour, float[] hsbvals) {
		return hsbvals[0] <= pitchConstants.getHueUpper(PitchConstants.GREY)
				&& hsbvals[0] >= pitchConstants
						.getHueLower(PitchConstants.GREY)
				&& hsbvals[1] <= pitchConstants
						.getSaturationUpper(PitchConstants.GREY)
				&& hsbvals[1] >= pitchConstants
						.getSaturationLower(PitchConstants.GREY)
				&& hsbvals[2] <= pitchConstants
						.getValueUpper(PitchConstants.GREY)
				&& hsbvals[2] >= pitchConstants
						.getValueLower(PitchConstants.GREY)
				&& colour.getRed() <= pitchConstants
						.getRedUpper(PitchConstants.GREY)
				&& colour.getRed() >= pitchConstants
						.getRedLower(PitchConstants.GREY)
				&& colour.getGreen() <= pitchConstants
						.getGreenUpper(PitchConstants.GREY)
				&& colour.getGreen() >= pitchConstants
						.getGreenLower(PitchConstants.GREY)
				&& colour.getBlue() <= pitchConstants
						.getBlueUpper(PitchConstants.GREY)
				&& colour.getBlue() >= pitchConstants
						.getBlueLower(PitchConstants.GREY);
	}

	/**
	 * Determines if a pixel is part of either green plate, based on input RGB
	 * colours and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * 
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of a green plate), false otherwise.
	 */
	private boolean isGreen(Color colour, float[] hsbvals) {
		return hsbvals[0] <= pitchConstants.getHueUpper(PitchConstants.GREEN)
				&& hsbvals[0] >= pitchConstants
						.getHueLower(PitchConstants.GREEN)
				&& hsbvals[1] <= pitchConstants
						.getSaturationUpper(PitchConstants.GREEN)
				&& hsbvals[1] >= pitchConstants
						.getSaturationLower(PitchConstants.GREEN)
				&& hsbvals[2] <= pitchConstants
						.getValueUpper(PitchConstants.GREEN)
				&& hsbvals[2] >= pitchConstants
						.getValueLower(PitchConstants.GREEN)
				&& colour.getRed() <= pitchConstants
						.getRedUpper(PitchConstants.GREEN)
				&& colour.getRed() >= pitchConstants
						.getRedLower(PitchConstants.GREEN)
				&& colour.getGreen() <= pitchConstants
						.getGreenUpper(PitchConstants.GREEN)
				&& colour.getGreen() >= pitchConstants
						.getGreenLower(PitchConstants.GREEN)
				&& colour.getBlue() <= pitchConstants
						.getBlueUpper(PitchConstants.GREEN)
				&& colour.getBlue() >= pitchConstants
						.getBlueLower(PitchConstants.GREEN);
	}

	/**
	 * Processes an input image, extracting the ball and robot positions and
	 * robot orientations from it, and then displays the image (with some
	 * additional graphics layered on top for debugging) in the vision frame.
	 * 
	 * @param image
	 *            The image to process and then show.
	 * @param counter
	 * @throws NoAngleException
	 */
	public void processAndUpdateImage(BufferedImage frame, int frameRate,
			int counter) {
		BufferedImage debugOverlay = new BufferedImage(frame.getWidth(),
				frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics debugGraphics = debugOverlay.getGraphics();

		int ballX = 0;
		int ballY = 0;
		int numBallPos = 0;

		int blueX = 0;
		int blueY = 0;
		int numBluePos = 0;

		int yellowX = 0;
		int yellowY = 0;
		int numYellowPos = 0;

		int greenX = 0;
		int greenY = 0;
		int numGreenPos = 0;

		ArrayList<Integer> ballXPoints = new ArrayList<Integer>();
		ArrayList<Integer> ballYPoints = new ArrayList<Integer>();
		ArrayList<Integer> blueXPoints = new ArrayList<Integer>();
		ArrayList<Integer> blueYPoints = new ArrayList<Integer>();
		ArrayList<Integer> yellowXPoints = new ArrayList<Integer>();
		ArrayList<Integer> yellowYPoints = new ArrayList<Integer>();
		ArrayList<Integer> greenXPoints = new ArrayList<Integer>();
		ArrayList<Integer> greenYPoints = new ArrayList<Integer>();

		int topBuffer = pitchConstants.getTopBuffer();
		int bottomBuffer = pitchConstants.getBottomBuffer();
		int leftBuffer = pitchConstants.getLeftBuffer();
		int rightBuffer = pitchConstants.getRightBuffer();

		/**
		 * For every pixel within the pitch, test to see if it belongs to the
		 * ball, the yellow T, the blue T, either green plate or a grey circle.
		 */

		for (int row = topBuffer; row < frame.getHeight() - bottomBuffer; row++) {
			for (int column = leftBuffer; column < frame.getWidth()
					- rightBuffer; column++) {

				// The RGB colours and hsv values for the current pixel.
				Color c = new Color(frame.getRGB(column, row));
				float hsbvals[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);

				if (pitchConstants.debugMode(PitchConstants.GREY)
						&& isGrey(c, hsbvals)) {
					debugOverlay.setRGB(column, row, 0xFFFF0099);
				}

				// TODO: Do we really need this? If we have the check in 396-398
				if (pitchConstants.debugMode(PitchConstants.GREEN)
						&& isGreen(c, hsbvals)) {
					debugOverlay.setRGB(column, row, 0xFFFF0099);
				}

				/** Checking if the pixel is a part of the Blue T */
				if (isBlue(c, hsbvals)) {
					blueX += column;
					blueY += row;
					numBluePos++;

					blueXPoints.add(column);
					blueYPoints.add(row);

					// If we're in the "Blue" tab, we show what pixels we're
					// looking at, for debugging and to help with threshold
					// setting.
					if (pitchConstants.debugMode(PitchConstants.BLUE)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				}

				/** Checking if the pixel is a part of the Yellow T */
				if (isYellow(c, hsbvals)) {
					yellowX += column;
					yellowY += row;
					numYellowPos++;

					yellowXPoints.add(column);
					yellowYPoints.add(row);

					// If we're in the "Yellow" tab, we show what pixels we're
					// looking at, for debugging and to help with threshold
					// setting.
					if (pitchConstants.debugMode(PitchConstants.YELLOW)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				}

				/** Checking if the pixel is a part of the Green Plate */
				if (isGreen(c, hsbvals)) {
					greenX += column;
					greenY += row;
					numGreenPos++;

					greenXPoints.add(column);
					greenYPoints.add(row);

					// If we're in the "Green Plate" tab, we show what pixels
					// we're
					// looking at, for debugging and to help with threshold
					// setting.
					if (pitchConstants.debugMode(PitchConstants.GREEN)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				}

				/** Checking if the pixel is a part of the Ball */
				if (isBall(c, hsbvals)) {
					ballX += column;
					ballY += row;
					numBallPos++;

					ballXPoints.add(column);
					ballYPoints.add(row);

					// If we're in the "Ball" tab, we show what pixels we're
					// looking at, for debugging and to help with threshold
					// setting.
					if (pitchConstants.debugMode(PitchConstants.BALL)) {
						debugOverlay.setRGB(column, row, 0xFF000000);
					}
				}
			}
		}

		/** Calculating the centre points of the different obejct on the pitch */
		// Position objects to hold the centre point of the ball and both
		// robots.
		Position ball;
		Position blue;
		Position yellow;
		Position green;
		double angle = 0;

		/** Ball */
		// If we have only found a few 'Ball' pixels, chances are that the ball
		// has not actually been detected.
		if (numBallPos > 0) {
			ballX /= numBallPos;
			ballY /= numBallPos;

			ball = new Position(ballX, ballY);
			ball.fixValues(worldState.getBallX(), worldState.getBallY());
			ball.filterPoints(ballXPoints, ballYPoints);
		} else {
			ball = new Position(worldState.getBallX(), worldState.getBallY());
		}

		// TODO: Find out how the remove outliers works and maybe clearing the
		// point list is a better idea?
		Point ballP = new Point(ballX, ballY);
		ArrayList<Point> goodPoints = Position.removeOutliers(ballXPoints,
				ballYPoints, ballP);
		ballXPoints = new ArrayList<Integer>();
		ballYPoints = new ArrayList<Integer>();
		for (int k = 0; k < goodPoints.size(); k++) {
			ballXPoints.add((int) goodPoints.get(k).getX());
			ballYPoints.add((int) goodPoints.get(k).getY());
		}

		/** Blue plate */
		// If we have only found a few 'Blue' pixels, chances are that the ball
		// has not actually been detected.
		if (numBluePos > 0) {
			blueX /= numBluePos;
			blueY /= numBluePos;

			blue = new Position(blueX, blueY);
			blue.fixValues(worldState.getBlueX(), worldState.getBlueY());
			blue.filterPoints(blueXPoints, blueYPoints);
		} else {
			blue = new Position(worldState.getBlueX(), worldState.getBlueY());
		}

		// TODO: No outlier removal in this case? Maybe implement it

		/** Yellow plate */
		// If we have only found a few 'Yellow' pixels, chances are that the
		// yellow T has not actually been detected.
		if (numYellowPos > 0) {
			yellowX /= numYellowPos;
			yellowY /= numYellowPos;

			yellow = new Position(yellowX, yellowY);
			yellow.fixValues(worldState.getYellowX(), worldState.getYellowY());
			yellow.filterPoints(yellowXPoints, yellowYPoints);
		} else {
			yellow = new Position(worldState.getYellowX(),
					worldState.getYellowY());
		}

		// TODO: Find out how the remove outliers works and maybe clearing the
		// point list is a better idea?
		Point yellowP = new Point(yellowX, yellowY);
		goodPoints = Position.removeOutliers(yellowXPoints, yellowYPoints,
				yellowP);
		yellowXPoints = new ArrayList<Integer>();
		yellowYPoints = new ArrayList<Integer>();
		for (int k = 0; k < goodPoints.size(); k++) {
			yellowXPoints.add((int) goodPoints.get(k).getX());
			yellowYPoints.add((int) goodPoints.get(k).getY());
		}

		/** Green plate */
		// If we have only found a few 'Green' pixels, chances are that the ball
		// has not actually been detected.
		if (numGreenPos > 0) {
			greenX /= numGreenPos;
			greenY /= numGreenPos;

			green = new Position(greenX, greenY);
			green.fixValues(worldState.getGreenX(), worldState.getGreenY());
			green.filterPoints(greenXPoints, greenYPoints);
		} else {
			green = new Position(worldState.getGreenX(), worldState.getGreenY());
		}

		
		
	
		 

		/** Finding the corners of the Green plates */

		Position[] greenPlatePoints = null;
		try {

			// The constant 1400 passed is the max squared distance from the
			// centroid in which the farthest points can be located.
			greenPlatePoints = findFurthest(debugOverlay, green, greenXPoints,
					greenYPoints, 1400);
			
			
			/* TODO: For the kMeans implementation. Needs to be tested that it gets the correct means
			 * and that it doesn't crash (:
			int[] greenMean = { green.getX(), green.getY() };
			System.out.println(Kmeans.sumsquarederror(greenXPoints,greenYPoints,
					 greenMean));
			double sumSqrdError = Kmeans.sumsquarederror(greenXPoints,greenYPoints,
					 greenMean);
			
			//Check that we actually have 2 plates before attempting to kmeans them.
			if (sumSqrdError > Kmeans.errortarget){
				int[] mean1 ={greenPlatePoints[0].getX(), greenPlatePoints[0].getY()};
				int[] mean2 = {greenPlatePoints[1].getX(), greenPlatePoints[1].getY()};
				
				int[] greenCendroids = Kmeans.dokmeans(greenXPoints, greenYPoints, mean1, mean2 );
				Position plate1mean = new Position(greenCendroids[0],greenCendroids[1]);
				Position plate2mean = new Position(greenCendroids[2],greenCendroids[3]);
				
			}
			
			*/
			
			
			
			
			

			// Finding the shortest sides of the plates and returns their
			// average values in order to draw the line in the middle of the
			// plate
			Position[] avgPts = findAvgPtOfTwoShortestSides(greenPlatePoints);
			Position avg1 = avgPts[0];
			Position avg2 = avgPts[1];

			// Determining the colour of the centroid of the plate to determine
			// the colour of the robot
			Color colour = new Color(frame.getRGB(green.getX(), green.getY()));
			float[] colourHSV = Color.RGBtoHSB(colour.getRed(),
					colour.getGreen(), colour.getBlue(), null);

			Position tCentroid;
			// TODO: Now it's the same for both robots
			boolean isBlue = isBlue(colour, colourHSV);
			if (isBlue) {
				tCentroid = blue;
			} else
				tCentroid = yellow;

			/**
			 * Determining the orientation of the plate by looking at square at
			 * around the top 1/7 of the plate and bottom 1/7 to find the grey
			 * circle.
			 */

			int searchPtX = (6 * avg1.getX() + avg2.getX()) / 7;
			int searchPtY = (6 * avg1.getY() + avg2.getY()) / 7;
			Position searchPt1 = new Position(searchPtX, searchPtY);

			searchPtX = (avg1.getX() + 6 * avg2.getX()) / 7;
			searchPtY = (avg1.getY() + 6 * avg2.getY()) / 7;
			Position searchPt2 = new Position(searchPtX, searchPtY);

			// Try one (short) side
			int searchPt1GreyPoints = 0;
			int xMin = searchPt1.getX() - 5, xMax = xMin + 10;
			int yMin = searchPt1.getY() - 5, yMax = yMin + 10;
			for (int x = xMin; x < xMax; ++x) {
				for (int y = yMin; y < yMax; ++y) {
					colour = new Color(frame.getRGB(x, y));
					colourHSV = Color.RGBtoHSB(colour.getRed(),
							colour.getGreen(), colour.getBlue(), colourHSV);
					if (isGrey(colour, colourHSV)) {
						++searchPt1GreyPoints;
					}
				}
			}
			// Try the other side
			int searchPt2GreyPoints = 0;
			xMin = searchPt2.getX() - 5;
			xMax = xMin + 10;
			yMin = searchPt2.getY() - 5;
			yMax = yMin + 10;
			for (int x = xMin; x < xMax; ++x) {
				for (int y = yMin; y < yMax; ++y) {
					colour = new Color(frame.getRGB(x, y));
					colourHSV = Color.RGBtoHSB(colour.getRed(),
							colour.getGreen(), colour.getBlue(), colourHSV);
					if (isGrey(colour, colourHSV)) {
						++searchPt2GreyPoints;
					}
				}
			}

			double xvector;
			double yvector;

			// Checking which side has more "grey" points - that side is the
			// back, the other is the fron
			Position front, back;
			if (searchPt1GreyPoints > searchPt2GreyPoints) {
				front = searchPt2;
				back = searchPt1;
				xvector = avg1.getX() - avg2.getX();
				yvector = avg1.getY() - avg2.getY();
			} else if (searchPt1GreyPoints < searchPt2GreyPoints) {
				xvector = avg2.getX() - avg1.getX();
				yvector = avg2.getY() - avg1.getY();
				front = searchPt1;
				back = searchPt2;
			} else
				throw new NoAngleException("Can't distinguish front vs back");

			angle = Math.acos(yvector
					/ Math.sqrt(xvector * xvector + yvector * yvector));
			if (xvector > 0)
				angle = 2.0 * Math.PI - angle;

			
			/**Debugging shapes drawn on the debugging layer of the video feed*/
			debugGraphics.setColor(Color.magenta);
			debugGraphics.drawRect(front.getX() - 5, front.getY() - 5, 10, 10);
			debugGraphics.setColor(Color.black);
			debugGraphics.drawRect(back.getX() - 5, back.getY() - 5, 10, 10);

			debugGraphics.setColor(Color.white);
			debugGraphics.drawLine(avg1.getX(), avg1.getY(), avg2.getX(),
					avg2.getY());
			debugGraphics.drawOval(green.getX(), green.getY(), 2, 2);
			debugGraphics.drawOval(greenPlatePoints[0].getX() - 1,
					greenPlatePoints[0].getY() - 1, 2, 2);
			debugGraphics.drawOval(greenPlatePoints[1].getX() - 1,
					greenPlatePoints[1].getY() - 1, 2, 2);
			debugGraphics.drawOval(greenPlatePoints[2].getX() - 1,
					greenPlatePoints[2].getY() - 1, 2, 2);
			debugGraphics.drawOval(greenPlatePoints[3].getX() - 1,
					greenPlatePoints[3].getY() - 1, 2, 2);

			
			/**Updating the world state*/
			worldState.setBallX(ball.getX());
			worldState.setBallY(ball.getY());
			worldState.setGreenX(green.getX());
			worldState.setGreenY(green.getY());
			worldState.setBlueX(green.getX());
			worldState.setBlueY(green.getY());
			worldState.setYellowX(green.getX());
			worldState.setYellowY(green.getY());
			worldState.setBlueOrientation(angle);
			worldState.setYellowOrientation(angle);
			worldState.updateCounter();
			worldState.setOurRobot();
			worldState.setTheirRobot();
			worldState.setBall();

			// Only display these markers in non-debug mode.
			boolean anyDebug = false;
			for (int i = 0; i < 5; ++i) {
				if (pitchConstants.debugMode(i)) {
					anyDebug = true;
					break;
				}
			}

			if (!anyDebug) {
				debugGraphics.setColor(Color.red);
				debugGraphics.drawLine(0, ball.getY(), 640, ball.getY());
				debugGraphics.drawLine(ball.getX(), 0, ball.getX(), 480);
				debugGraphics.setColor(Color.blue);
				debugGraphics.drawOval(blue.getX() - 15, blue.getY() - 15, 30,
						30);
				debugGraphics.setColor(Color.yellow);
				debugGraphics.drawOval(yellow.getX() - 15, yellow.getY() - 15,
						30, 30);
				debugGraphics.setColor(Color.white);
			}
		} catch (NoAngleException e) {
			debugGraphics.setColor(Color.red);
			debugGraphics.drawString(e.getMessage(), 320, 240);
		}

		for (VisionDebugReceiver receiver : visionDebugReceivers)
			receiver.sendDebugOverlay(debugOverlay);
		for (WorldStateReceiver receiver : worldStateReceivers)
			receiver.sendWorldState(worldState);
	}

	/**
	 * Finds four farthest points from the given centroid in the set of points
	 * given. (Used to find the four corners of the Green plate)
	 * 
	 * @param debugOverlay
	 *            The second top layer on top of the video feed, that is used to
	 *            draw on for debugging.
	 * @param xpoints
	 *            Set of x coordinates of points from which the farthest points
	 *            are selected.
	 * @param ypoints
	 *            Set of y coordinates of points from which the farthest points
	 *            are selected.
	 * @param distMax
	 *            The max distance squared from the centroid in which the
	 *            farthest points can be found.
	 */
	public Position[] findFurthest(BufferedImage debugOverlay,
			Position centroid, ArrayList<Integer> xpoints,
			ArrayList<Integer> ypoints, int distMax) throws NoAngleException {
		if (xpoints.size() < 5) {
			throw new NoAngleException(
					"List of points is too small to calculate angle");
		}
		// Intialising the array of four points
		Position[] points = new Position[4];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Position(0, 0);
			points[i] = new Position(0, 0);
		}

		double dist = 0;
		int index = 0;
		// First farthest point
		for (int i = 0; i < xpoints.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid.getX(),
					centroid.getY(), xpoints.get(i), ypoints.get(i));

			if (currentDist > dist && currentDist < distMax) {
				dist = currentDist;
				index = i;
			}
		}
		points[0].setX(xpoints.get(index));
		points[0].setY(ypoints.get(index));

		index = 0;
		dist = 0;
		// Second farthest point
		for (int i = 0; i < xpoints.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid.getX(),
					centroid.getY(), xpoints.get(i), ypoints.get(i));

			double distTo0 = Position.sqrdEuclidDist(points[0].getX(),
					points[0].getY(), xpoints.get(i), ypoints.get(i));

			if (currentDist > dist && currentDist < distMax && distTo0 > 500) {
				dist = currentDist;
				index = i;
			}
		}
		points[1].setX(xpoints.get(index));
		points[1].setY(ypoints.get(index));

		index = 0;
		dist = 0;

		// Third farthest point
		for (int i = 0; i < xpoints.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid.getX(),
					centroid.getY(), xpoints.get(i), ypoints.get(i));

			double distTo0 = Position.sqrdEuclidDist(points[0].getX(),
					points[0].getY(), xpoints.get(i), ypoints.get(i));
			double distTo1 = Position.sqrdEuclidDist(points[1].getX(),
					points[1].getY(), xpoints.get(i), ypoints.get(i));

			if (currentDist > dist && currentDist < distMax && distTo0 > 500
					&& distTo1 > 500) {
				dist = currentDist;
				index = i;
			}
		}
		points[2].setX(xpoints.get(index));
		points[2].setY(ypoints.get(index));

		index = 0;
		dist = 0;

		// Fourth farthest point
		for (int i = 0; i < xpoints.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid.getX(),
					centroid.getY(), xpoints.get(i), ypoints.get(i));

			double distTo0 = Position.sqrdEuclidDist(points[0].getX(),
					points[0].getY(), xpoints.get(i), ypoints.get(i));
			double distTo1 = Position.sqrdEuclidDist(points[1].getX(),
					points[1].getY(), xpoints.get(i), ypoints.get(i));
			double distTo2 = Position.sqrdEuclidDist(points[2].getX(),
					points[2].getY(), xpoints.get(i), ypoints.get(i));

			if (currentDist > dist && currentDist < distMax && distTo0 > 500
					&& distTo1 > 500 && distTo2 > 500) {
				dist = currentDist;
				index = i;
			}
		}
		points[3].setX(xpoints.get(index));
		points[3].setY(ypoints.get(index));

		// Display the four farthest points in the
		Graphics debugGraphics = debugOverlay.getGraphics();
		for (int i = 0; i < points.length; i++)
			debugGraphics.drawOval(points[i].getX(), points[i].getY(), 3, 3);

		return points;
	}

	/**
	 * Finds the two mean points of the two shortest sides.
	 * 
	 * @param points
	 *            The set of four points. (Representing the corners of the Green
	 *            plate)
	 */
	public Position[] findAvgPtOfTwoShortestSides(Position[] points) {

		// Initialise the minimum distance to be the maximum value possible
		int distMin = Integer.MAX_VALUE;
		// The first pair of points with the shortest distance in between
		int pairApt1 = -1;
		int pairApt2 = -1;
		// The second pair of point with the second shortest distance in between
		int pairBpt1;
		int pairBpt2;
		// Boolean array that helps to distinguish pairA from pairB
		boolean[] used = new boolean[4];

		// Finding the indices of the two points between which the difference is
		// the shortest
		for (int i = 0; i < points.length; i++)
			for (int j = 0; j < i; j++) {
				int dist = Position.sqrdEuclidDist(points[i].getX(),
						points[i].getY(), points[j].getX(), points[j].getY());
				if (dist < distMin) {
					pairApt1 = i;
					pairApt2 = j;
					distMin = dist;
				}
			}

		// Marking the found pair true; and finding the other pair.
		used[pairApt1] = true;
		used[pairApt2] = true;

		int i;
		for (i = 0; used[i]; ++i)
			;
		pairBpt1 = i;
		for (i = pairBpt1 + 1; used[i]; ++i)
			;
		pairBpt2 = i;

		// Calculating the means between each of the two points, which are then
		// returned.
		Position top = new Position(
				(points[pairBpt1].getX() + points[pairBpt2].getX()) / 2,
				(points[pairBpt1].getY() + points[pairBpt2].getY()) / 2);
		Position bottom = new Position(
				(points[pairApt1].getX() + points[pairApt2].getX()) / 2,
				(points[pairApt1].getY() + points[pairApt2].getY()) / 2);
		Position[] result = { top, bottom };
		return result;
	}
}
