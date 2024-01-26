package alphabot;

import java.util.*;
import battlecode.common.*;

public class MainPhase{
	private static MapLocation spawnFirst = null;
	static int[] flagIDs; //0 0 0
	
	public static void runMainPhase(RobotController rc) throws GameActionException {
		flagIDs = new int[6];
		//try to buy action and capturing upgrades
		if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
			rc.buyGlobal(GlobalUpgrade.ACTION);
		} if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
			rc.buyGlobal(GlobalUpgrade.HEALING);
		}
		boolean left = true;

		//player 0 resets first bit in comm array list
		if (RobotPlayer.personalID == 0){
			for (int i = 0; i < 64; i++){
				Communication.setUnupdated(rc, i);
			}
		}

		//attack enemies, prioritize enemies that have your flag
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1 , rc.getTeam().opponent());
		for(RobotInfo robot : nearbyEnemies) {
			if(robot.hasFlag()) {
				tempPathfind.move(robot.getLocation());
				if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
			}
		}
		//need separate for loop bc rc.attack is used up
		for(RobotInfo robot: nearbyEnemies) {
			if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
		}
		//not ready to use this yet
		//Communication.updateEnemyInfo(rc, rc.getLocation(), nearbyEnemies.length);

		//try to heal friendly robots
		//reminder to heal robot with the lowest hp
		for(RobotInfo robot: rc.senseNearbyRobots(-1, rc.getTeam())){
			if(rc.canHeal(robot.getLocation())) rc.heal(robot.getLocation());
		}

		FlagInfo[] allFlags = rc.senseNearbyFlags(-1);
		for(FlagInfo flag : allFlags){
			int flagID = flag.getID();
			int idx = flagIDToIdx(rc, flagID);
			Communication.updateFlagInfo(rc, flag.getLocation(), flag.isPickedUp(), flag.getTeam(), idx);
		}
		
		if(!rc.hasFlag()) {
			//if we dont have a flag, find closest enemy flag (including broadcast locations)
			ArrayList<MapLocation> flagLocs = new ArrayList<>();
			FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
			for(FlagInfo flag: enemyFlags) {
				flagLocs.add(flag.getLocation());
			}
			if(flagLocs.isEmpty()) {
				for (int i = 0; i <= Communication.LAST_FLAG_IDX; i++){
					if (Communication.getTeam(rc, i) == rc.getTeam().opponent() && Communication.getIfUpdated(rc,i)){
						flagLocs.add(Communication.getLocation(rc, i));
					}
				}
				if(flagLocs.isEmpty()){
					//maybe use broadcastLocs later for setup lineup
					MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
					for(MapLocation flagLoc : broadcastLocs) flagLocs.add(flagLoc);
				}
			}
			
			//if we found the closest enemy flag, move towards and try to pick it up
			MapLocation closestFlag = findClosestLocation(rc.getLocation(), flagLocs);
			
			if(closestFlag != null) {
				tempPathfind.move(closestFlag);
				if(rc.canPickupFlag(closestFlag)) rc.pickupFlag(closestFlag);
				MapLocation[] spawnLocs = rc.getAllySpawnLocations();
				spawnFirst = findClosestLocation(rc.getLocation(),Arrays.asList(spawnLocs));
				left = isLeft(rc, spawnFirst);
			}
			//if there is no flag to capture, explore randomly
			Pathfind.explore(rc);
		} else {
			//if we have flag, move towards closest ally spawn zone
			tempPathfind.move(spawnFirst);
		}
	}

	public static int flagIDToIdx(RobotController rc, int flagID){
		for(int i = 0; i < flagIDs.length; i++){
			if (flagIDs[i] == 0) {
				flagIDs[i] = flagID;
				return i;
			} else if (flagIDs[i] == flagID){
				return i;
			}
			else continue;
		}
		return 0;
	}

	public static boolean isLeft(RobotController rc, MapLocation destination) {
		return (destination.x - rc.getLocation().x)*(rc.getMapHeight()/2 - rc.getLocation().y) - (destination.y - rc.getLocation().y)*(rc.getMapWidth()/2 - rc.getLocation().x) > 0;
	}

	
	public static MapLocation findClosestLocation(MapLocation me, List<MapLocation> otherLocs) {
		MapLocation closest = null;
		int minDist = Integer.MAX_VALUE;
		for(MapLocation loc: otherLocs) {
			int dist = me.distanceSquaredTo(loc);
			if(dist < minDist) {
				minDist = dist;
				closest = loc;
			}
		}
		return closest;
	}
}