package alphabot;

import battlecode.common.*;

public class Pathfind {
	
	static Direction direction;
	
	public static MapLocation lastLoc;
	public static boolean stuck = false;

	
	public static void moveTowards(RobotController rc, MapLocation loc) throws GameActionException {
		//if can move towards location, move towards location
		Direction dir = rc.getLocation().directionTo(loc);
		
		if(rc.canMove(dir)) rc.move(dir);
		else if(rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
		else if(rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
		else if(rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
		else {
			Direction randomDir = Direction.allDirections()[RobotPlayer.random.nextInt(8)];
			if(rc.canMove(randomDir)) rc.move(randomDir);
		}
	}
	
	public static void moveAround(RobotController rc, MapLocation loc) throws GameActionException {
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
				if(!rc.getLocation().add(dir).equals(lastLoc) && rc.canMove(dir)) {
					rc.move(dir);
					stuck = false;
				} else {
					dir = dir.rotateLeft();
				}
			}
		}
		
		lastLoc = rc.getLocation();
	}


	public static void moveTowardsGreedy(RobotController rc, MapLocation loc) throws GameActionException{
		
	}

	public static int otherDistance(MapLocation m1, MapLocation m2){
		return (Math.max(Math.abs(m1.x-m2.x),Math.abs(m1.y-m2.y)));
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
	
	//bugNavZero traces around obstacle until can move foreward, can get stuck in cycle
	public static void bugNavZero(RobotController rc, MapLocation destination) throws GameActionException{
		Direction bugDir = rc.getLocation().directionTo(destination);
		
		if(rc.canMove(bugDir)) {
			rc.move(bugDir);
		} else {
			for(int i = 0; i < 8; i++) {
				if(rc.canFill(rc.getLocation().add(bugDir))) rc.fill(rc.getLocation().add(bugDir));
				if(rc.canMove(bugDir)) {
					rc.move(bugDir);
					break;
				} else {
					bugDir = bugDir.rotateLeft();
				}
			}
		}
	}
	
	
	
	
	
}