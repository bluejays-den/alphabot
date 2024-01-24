package alphabotv3;

import battlecode.common.*;
import java.util.ArrayList;

public class Pathfind {
	
	static Direction direction;
	
	public static MapLocation lastLoc;
	public static boolean stuck = false;
	public static boolean justHadFlag = false;
	public static int turnJustHadFlag;

	
	public static void moveTowards(RobotController rc, MapLocation loc) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(loc);
		if(!stuck) {
			//can maybe be more conservative on fills to reduce byte code
			if(rc.canMove(dir) && !rc.getLocation().add(dir).equals(lastLoc)) Pathfind.moveUpdateLastLoc(rc,dir);
			else if(rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
			else if(rc.canMove(dir.rotateLeft()) && !rc.getLocation().add(dir).equals(lastLoc)) Pathfind.moveUpdateLastLoc(rc,dir.rotateLeft());
			else if(rc.canMove(dir.rotateRight()) && !rc.getLocation().add(dir).equals(lastLoc)) Pathfind.moveUpdateLastLoc(rc,dir.rotateRight());
			else if(rc.canFill(rc.getLocation().add(dir.rotateLeft()))) rc.fill(rc.getLocation().add(dir.rotateLeft()));
			else if(rc.canFill(rc.getLocation().add(dir.rotateRight()))) rc.fill(rc.getLocation().add(dir.rotateRight()));
			else {
				stuck = true;
			}
		} 
		for (int i = 0; i < 5; i++) {
			dir = dir.rotateLeft();
			if (!rc.getLocation().add(dir).equals(lastLoc)){
				if (rc.canMove(dir)){
					Pathfind.moveUpdateLastLoc(rc,dir);
					stuck = false;
				}
			}
		}
		
		
		lastLoc = rc.getLocation();

	}

	public static void moveTowardsWithFlag(RobotController rc, MapLocation loc) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(loc);
		boolean goHeroMode = false;
		int distanceToBase = 10000;
		RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
			if (nearbyFriends.length < 3){
				goHeroMode = true;
			}
		if(!stuck) {


			//can maybe be more conservative on fills to reduce byte code
			boolean passToMate = false;
			if(rc.canMove(dir) && !rc.getLocation().add(dir).equals(lastLoc)) rc.move(dir);
			else if(rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
			else if(rc.canMove(dir.rotateLeft())&& !rc.getLocation().add(dir).equals(lastLoc)) rc.move(dir.rotateLeft());
			else if(rc.canMove(dir.rotateRight())&& !rc.getLocation().add(dir).equals(lastLoc)) rc.move(dir.rotateRight());
			else if(rc.canFill(rc.getLocation().add(dir.rotateLeft()))) rc.fill(rc.getLocation().add(dir.rotateLeft()));
			else if(rc.canFill(rc.getLocation().add(dir.rotateRight()))) rc.fill(rc.getLocation().add(dir.rotateRight()));
			else{
				passToMate = true;
			}
			if (passToMate && !goHeroMode){
				nearbyFriends = rc.senseNearbyRobots(1, rc.getTeam());
				ArrayList<MapLocation> nearbyFriendsForward = new ArrayList<MapLocation>();
				for (RobotInfo i : nearbyFriends){
					Direction friendDir = rc.getLocation().directionTo(i.getLocation());
					if (friendDir == dir || friendDir == dir.rotateLeft() || friendDir == dir.rotateRight()){
						nearbyFriendsForward.add(i.getLocation());
					}
				}
				if (nearbyFriendsForward.size() == 0){
					writeFlagStuck(rc, rc.getLocation());
				}
				else{
					for (MapLocation i : nearbyFriendsForward){
						if(rc.canDropFlag(i)){
							rc.dropFlag(i);
							justHadFlag = true;
							turnJustHadFlag = RobotPlayer.turnCount;
						}
					}
				}
			}
			else {
				stuck = true;
				distanceToBase = distanceTo(rc, loc);
			}
		} 
		else{
			for (int i = 0; i < 7; i++) {
			
			boolean b = rc.hasFlag();
			if (!rc.getLocation().add(dir).equals(lastLoc)){
				if (rc.canMove(dir)){
					Pathfind.moveUpdateLastLoc(rc,dir);
				}
				else if ((b && rc.canDropFlag(rc.getLocation().add(dir)) && !goHeroMode)){
					rc.dropFlag(rc.getLocation().add(dir));
					justHadFlag = true;
					turnJustHadFlag = RobotPlayer.turnCount;
				}
				if (distanceTo(rc,loc) < distanceToBase){
					stuck = false;
				}
			}
			dir = dir.rotateLeft();
		}
		}
		
		
		
		//lastLoc = rc.getLocation();
	}

	
	
	public static void explore(RobotController rc) throws GameActionException{
		//try to move towards crumbs, otherwise move in current direction 
		//if cant continue in current direction, pick a new random direction
		MapLocation[] crumbLocs = rc.senseNearbyCrumbs(-1);
		if(crumbLocs.length > 0) {
			moveTowards(rc, crumbLocs[0]);
		}
		
		
		if(rc.isMovementReady()) {
			if(direction != null && rc.canMove(direction)) Pathfind.moveUpdateLastLoc(rc,direction);
			else {
				direction = Direction.allDirections()[RobotPlayer.random.nextInt(8)];
				if(rc.canMove(direction)) Pathfind.moveUpdateLastLoc(rc,direction);
			}
		}
	}

	public static void writeFlagStuck(RobotController rc, MapLocation loc) throws GameActionException{
		for (int i = -1; i > -4; i--){
			int curry = rc.readSharedArray(i);
			if (curry == 0){
				int code = 59 * loc.x + loc.y;
				if (rc.canWriteSharedArray(i,code)){
					rc.writeSharedArray(i,code);
				}
			}
			
		}
	}

	public static void moveUpdateLastLoc(RobotController rc, Direction dir) throws GameActionException{
		lastLoc = rc.getLocation();
		rc.move(dir);
	}

	public static int distanceTo(RobotController rc, MapLocation loc){
		return(Math.max(rc.getLocation().x - loc.x, rc.getLocation().y-loc.y));
	}
}