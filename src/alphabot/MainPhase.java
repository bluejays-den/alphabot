package alphabot;

import java.util.*;
import battlecode.common.*;

public class MainPhase{
	private static MapLocation spawnFirst = null;
	
	public static void runMainPhase(RobotController rc) throws GameActionException {
		//try to buy action and capturing upgrades
		if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
			rc.buyGlobal(GlobalUpgrade.ACTION);
		} if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
			rc.buyGlobal(GlobalUpgrade.HEALING);
		}
		boolean left = true;


		//attack enemies, prioitizing enemies that have your flag
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1 , rc.getTeam().opponent());
		RobotInfo[] nearbyFriends = rc.senseNearbyRobots(-1 , rc.getTeam());
		
		ArrayList<MapLocation> enemyLocs = new ArrayList<>();
		for(RobotInfo enemy: nearbyEnemies) {
			//dont bother the ducks running back with their flags
			//if(!flag.isPickedUp()) flagLocs.add(flag.getLocation());
			enemyLocs.add(enemy.getLocation());
		}

		
		for(RobotInfo robot : nearbyEnemies) {
			if(robot.hasFlag()) {
				Pathfind.bugNavZero(rc,robot.getLocation());
				if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
			}
		}
		
		for(RobotInfo robot: nearbyEnemies) {
			Pathfind.bugNavZero(rc,robot.getLocation());
			if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
		}
		
		//try to heal friendly robots
		for(RobotInfo robot: rc.senseNearbyRobots(-1, rc.getTeam())){
			if(rc.canHeal(robot.getLocation())) rc.heal(robot.getLocation());
		}
		
		
		if(!rc.hasFlag()) {
//			for(RobotInfo robot : nearbyFriends) {
//				if(robot.hasFlag()) {
//					Pathfind.bugNavZero(rc , findClosestLocation(rc.getLocation(), enemyLocs));
//					if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
//				}
//			}

			
			
			
			//if we dont have a flag, find closest enemy flag (including broadcast locations)
			ArrayList<MapLocation> flagLocs = new ArrayList<>();
			FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
			for(FlagInfo flag: enemyFlags) {
				//dont bother the ducks running back with their flags
				//if(!flag.isPickedUp()) flagLocs.add(flag.getLocation());
				flagLocs.add(flag.getLocation());
			}
			
			if(flagLocs.size() == 0) {
				MapLocation[] broadcastLocs = rc.senseBroadcastFlagLocations();
				for(MapLocation flagLoc : broadcastLocs) flagLocs.add(flagLoc);
			}
			
			//if we found a closest enemy flag, move towards and try to pick it up
			MapLocation closestFlag = findClosestLocation(rc.getLocation(), flagLocs);
			
			if(closestFlag != null) {
				Pathfind.bugNavZero(rc, closestFlag);
				if(rc.canPickupFlag(closestFlag)) rc.pickupFlag(closestFlag);
				MapLocation[] spawnLocs = rc.getAllySpawnLocations();
				spawnFirst = findClosestLocation(rc.getLocation(),Arrays.asList(spawnLocs));
				left = isLeft(rc, spawnFirst);

				
			}
			//if there is no flag to capture, explore randomly
			Pathfind.explore(rc);
		} else {
			//if we have flag, move towards closest allyl spawn zone
			Pathfind.bugNavTwo(rc, spawnFirst, left);
		}
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