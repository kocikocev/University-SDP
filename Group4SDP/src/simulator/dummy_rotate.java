package simulator;

import geometry.Vector;
import strategy.movement.AvoidanceStrategy;
import strategy.movement.DistanceToBall;
import strategy.movement.TurnToBall;
import world.state.Ball;
import world.state.Robot;
import strategy.movement.AvoidanceStrategy;
import strategy.movement.DistanceToBall;
import strategy.movement.GoToPoint;

import world.state.Ball;
import world.state.Robot;
import simulator.gotoball;

import comms.control.ServerInterface;

public class dummy_rotate {
	private static final int distanceFromBallToStop = 60;
	/**
	 * @param args
	 */
	public static void dummy(SimWorld world, SimServer rc)
	{ 
		
		
	   	Robot us = world.getOurRobot();
    	Ball ball = world.getBall();
    	double angle = (us.bearing);
    	double angle2 = TurnToBall.Turner(us, ball);
    	double angle3 = TurnToBall.findBearing(us, ball);
    	try {
			gotoball.approach(world, rc);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

	}

}
