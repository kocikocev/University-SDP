package strategy.planning;

import communication.BluetoothRobot;

import world.state.Ball;
import world.state.Robot;
import world.state.WorldState;
import strategy.movement.TurnToBall;

public class DribbleBall4 {

	private static final double xthreshold = 70;
	private static final double ythreshold = 70;
	private static double dribbleDistance = 200;
	private MoveToPoint moveToPoint = new MoveToPoint();

	public void dribbleBall(WorldState worldState, BluetoothRobot robot)
			throws InterruptedException {
		// Get robot and ball from world
		worldState.setOurRobot();
		Robot us = worldState.ourRobot;
		Ball ball = worldState.ball;

		if (us.x > (ball.x - 70)) {
			// Calculate the target point which appears behind the ball
			double xvalue = ball.x - xthreshold;
			double yvalue;
			if (ball.y < 250) {
				yvalue = ball.y + ythreshold;
			} else {
				yvalue = ball.y - ythreshold;
			}
			// Sends the the robot to the target point
			moveToPoint.moveToPoint(worldState, robot, xvalue, yvalue);
		}

		// Make an adjustment to face the door
		// comment that ------
		double angle = TurnToBall.AngleTurner(us, 5400, us.y);
		int attempt = 0;
		robot.stop();
		while (Math.abs(angle) > 15 && attempt < 10) {
			if ((Math.abs(angle) > 15) && (Math.abs(angle) < 50)) {
				robot.rotate((int) (angle / 2));
			} else if (Math.abs(angle) > 50) {
				robot.rotate((int) angle);
			}
			++attempt;
			angle = TurnToBall.AngleTurner(us, 5400, us.y);
		}

		// Move either up or down along the y axis until aligning properly
		if (us.y > ball.y) {
			System.out.println("Moving up the y axis"); // Robot's left

			while (us.y > ball.y) {
				robot.move(-100, 0);
				Thread.sleep(100);
			}
		} else {
			System.out.println("Moving down the y axis"); // Robot's right
			while (us.y < ball.y) {
				robot.move(100, 0);
				Thread.sleep(100);
			}
		}
		double temp = ball.x;
		// Now dribble
		while (us.x < temp + dribbleDistance && us.x < 500.0) {
			robot.move(0, 50);
			Thread.sleep(100);
		}
		robot.stop();
	}
}
