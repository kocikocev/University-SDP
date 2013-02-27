package strategy.planning;

import vision.WorldState;
import world.state.Robot;
import world.state.RobotController;

public abstract class StrategyInterface {
	public static boolean shouldidie = false;
	
	WorldState world;
	RobotController rc;
	Robot us;
	Robot them;
	
	public StrategyInterface(WorldState world, Robot us, Robot them, RobotController rc){
		this.world = world;
		this.rc = rc;
		this.us = us;
		this.them = them;
	}
	
	public void kill(){
		shouldidie = true;
		rc.clearBuff();
	}
	
}
