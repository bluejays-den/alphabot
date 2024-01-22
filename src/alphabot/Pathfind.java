package alphabot;

import battlecode.common.*;

public class Pathfind {
	
	static Direction direction;
	
	public static MapLocation lastLoc;
	public static boolean stuck = false;

	
	public static void moveTowards(RobotController rc, MapLocation loc) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(loc);
		if(!stuck) {
			if(rc.canMove(dir)) rc.move(dir);
			else if(rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
			else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
			else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
			else {
				stuck = true;
			}
		} else {
			while(stuck) {
				dir = dir.rotateLeft();
				if(!rc.getLocation().add(dir).equals(lastLoc) && rc.canMove(dir)) {
					rc.move(dir);
					stuck = false;
				}
			}
		}
		
		lastLoc = rc.getLocation();
		//if can move towards location, move towards location
//		Direction dir = rc.getLocation().directionTo(loc);
//		
//		if(rc.canMove(dir)) rc.move(dir);
//		else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
//		else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
//		else if(rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
//		else {
//			Direction randomDir = Direction.allDirections()[RobotPlayer.random.nextInt(8)];
//			if(rc.canMove(randomDir)) rc.move(randomDir);
//		}
	}
	
	public static void explore(RobotController rc) throws GameActionException{
		//try to move towards crumbs, otherwise move in current direction 
		//if cant continue in current direction, pick a new random direction
		MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
		if(crumbLocs.length > 0) {
			moveTowards(rc, crumbLocs[0]);
		}
		
		
		if(rc.isMovementReady()) {
			if(direction != null && rc.canMove(direction)) rc.move(direction);
			else {
				direction = Direction.allDirections()[RobotPlayer.random.nextInt(8)];
				if(rc.canMove(direction)) rc.move(direction);
			}
		}
	}
}