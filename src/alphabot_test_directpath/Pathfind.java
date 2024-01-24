package alphabot_test_directpath;

import battlecode.common.*;
import java.util.*;

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
	private static int bugState = 0;
	private static MapLocation closestObstacle = null;
	private static int closestObstacleDist = 10000;
	private static Direction bugDir = null;
	public static void resetBug() {
		bugState = 0;
		closestObstacle = null;
		closestObstacleDist = 10000;
		bugDir = null;
	}
	private static HashSet<MapLocation> line = null;
	private static int obstacleStartDist = 0;
	private static MapLocation prevDest = null;
	
	
	public static void bugNavTwo(RobotController rc, MapLocation destination) throws GameActionException{
		if(!destination.equals(prevDest)) {
			prevDest = destination;
			line = createLine(rc.getLocation(), destination);
		}
		
		for(MapLocation loc: line) {
			rc.setIndicatorDot(loc, 255,0,0);
		}
		
		if(bugState == 0) {
			bugDir = rc.getLocation().directionTo(destination);
			if(rc.canMove(bugDir)) {
				rc.move(bugDir);
			} else {
				bugState = 1;
				obstacleStartDist = rc.getLocation().distanceSquaredTo(destination);
				bugDir = rc.getLocation().directionTo(destination);
			}
		} else {			
			if(line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(destination) < obstacleStartDist) {
				bugState = 0;
			}
			
			for(int i = 0; i < 8; i++) {
				if(rc.canMove(bugDir)) {
					rc.move(bugDir);
					bugDir = bugDir.rotateRight();
				} else {
					bugDir = bugDir.rotateLeft();
				}
			}
		}
	}

	
	
	public static void bugNavOne(RobotController rc, MapLocation destination) throws GameActionException{
		if(bugState == 0) {
			rc.setIndicatorString("bugState = 0");
			bugDir = rc.getLocation().directionTo(destination);
			if(rc.canMove(bugDir)) {
				rc.setIndicatorDot(rc.getLocation(), 0,255,0);
				rc.move(bugDir);
			} else {
				bugState = 1;
				closestObstacle = null;
				closestObstacleDist = 10000;
			}
		} else {
			rc.setIndicatorString("bugState = 1");
			if(rc.getLocation().equals(closestObstacle)) {
				bugState = 0;
				rc.setIndicatorString("found closest");
			}
			
			if(rc.getLocation().distanceSquaredTo(destination) < closestObstacleDist) {
				closestObstacleDist = rc.getLocation().distanceSquaredTo(destination);
				closestObstacle = rc.getLocation();
			}
			
			for(int i = 0; i < 8; i++) {
				rc.setIndicatorDot(rc.getLocation(), 255,0,0);

				if(rc.canMove(bugDir)) {
					rc.move(bugDir);
					bugDir = bugDir.rotateRight();
					bugDir = bugDir.rotateRight();

					break;
				} else {
					bugDir = bugDir.rotateLeft();
				}
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
	
	//converts line until maplocations, dont need to know what the fuck happens
	private static HashSet<MapLocation> createLine(MapLocation a, MapLocation b){
		HashSet<MapLocation> locs = new HashSet<>();
		int x = a.x, y=a.y;
		int dx = b.x - a.x;
		int dy = b.y - a.y;
		int sx = (int) Math.signum(dx);
		int sy = (int) Math.signum(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		int d = Math.max(dx, dy);
		int r = d/2;
		if (dx > dy) {
			for (int i = 0; i < d; i++) {
				locs.add(new MapLocation(x,y));
				x += sx;
				r += dy;
				if (r >= dx) {
					locs.add(new MapLocation(x,y));
					y += sy;
					r -= dx;
				}
				
			}
		} else {
			for(int i = 0; i < d; i++) {
				locs.add(new MapLocation(x,y));
				y += sy;
				r += dx;
				if(r  >= dy) {
					locs.add(new MapLocation(x,y));
					x += sx;
					r -= dy;
				}
			}
		}
		locs.add(new MapLocation(x, y));
		return locs;
		
	}
	
	
	
}