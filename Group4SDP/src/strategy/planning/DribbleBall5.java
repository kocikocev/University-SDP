package strategy.planning;

import movement.RobotMover;
import strategy.movement.TurnToBall;
import utility.SafeSleep;
import world.state.Ball;
import world.state.Robot;
import world.state.WorldState;

public class DribbleBall5 {

	public static boolean die = false;



	public void dribbleBall(WorldState worldState, RobotMover mover)
			throws InterruptedException {
		// Get robot and ball from world
		worldState.setOurRobot();
		Robot us = worldState.ourRobot;
		Ball ball = worldState.ball;


		// Determine a different position behind the ball depending on which
		// door we are shooting in
		if (die)
			return;
		if (worldState.areWeOnLeft()) {
			double slope = (242 - ball.y) / (610 - ball.x);
			if (ball.y > 240){
				slope = -slope;
			}
			mover.moveToAStar(ball.x - 70, ball.y + slope * 70, true, false);
			mover.delay(50);
			RobotMover.distanceThreshold = 10;
			mover.moveToAndStop(ball.x - 60, ball.y + slope * 60);
			mover.waitForCompletion();

		} else {
			double slope = (244 - ball.y) / (28 - ball.x);
			if (ball.y < 240){
				slope = -slope;
			}
			mover.moveToAStar(ball.x + 70, ball.y + 70 * slope, true, false);
			mover.delay(50);
			RobotMover.distanceThreshold = 10;
			mover.moveToAndStop(ball.x + 60, ball.y + slope * 60);
			mover.waitForCompletion();
		}
		double angle = TurnToBall.AngleTurner(us, ball.x, ball.y);
		SafeSleep.sleep(50);
		int attempt = 0;
		System.out.printf("Angle is, %d\n", (int) angle);
		while (Math.abs(angle) > 5 && attempt < 10) {
			if ((Math.abs(angle) > 5) && (Math.abs(angle) < 50)) {
				mover.rotate(Math.toRadians(angle / 2));
				mover.waitForCompletion();
			} else if (Math.abs(angle) > 50) {
				mover.rotate(Math.toRadians(angle / 2));
				mover.waitForCompletion();
			}
			++attempt;
			angle = TurnToBall.AngleTurner(us, ball.x, ball.y);
			System.out.println("Angle for rotation " + angle);
			SafeSleep.sleep(50);
			if (die)
				return;
		}

		System.out.println("Reached position behind the ball!");

		//In case the ball is not straight on the goal, we need to
		//Move near the ball, rotate and then move more.
		if (worldState.areWeOnLeft()) {
			angle = TurnToBall.AngleTurner(us, 610, 242);
		}else{
			angle = TurnToBall.AngleTurner(us, 28, 242);
		}
		//If the angle to the goal is > 10 degrees
		//if (angle > 10){
			mover.move(0, 40);
			System.out.println("Moving towards the ball");
			SafeSleep.sleep(600);
			System.out.println("Moving towards the ball finished");
			mover.stopRobot();
			mover.waitForCompletion();
			SafeSleep.sleep(50);
			mover.rotate(Math.toRadians(angle));
			mover.waitForCompletion();
		//}
		
		
		//Now we move to the ball and then we kick it.
		mover.move(0, 40);

		SafeSleep.sleep(1500);
		if (worldState.areWeOnLeft()){
			while (Math.abs(ball.x - 610) > 300){
				SafeSleep.sleep(50);
			}
		}else{
			while (Math.abs(ball.x - 28) > 300){
				SafeSleep.sleep(50);
			}
		}

		mover.kick();
		mover.delay(200);
		mover.stopRobot();
		mover.waitForCompletion();
		mover.delay(3000);
		if (!worldState.ballIsInGoal()){
			dribbleBall(worldState,mover);
		}

	}
}
