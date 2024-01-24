package alphabotv3;

import java.util.*;
import battlecode.common.*;

public class MainPhase{
	
	public static void runMainPhase(RobotController rc) throws GameActionException {
		//try to buy action and capturing upgrades
		if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
			rc.buyGlobal(GlobalUpgrade.HEALING);
		} if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
			rc.buyGlobal(GlobalUpgrade.ACTION);
		}

		
		if ((RobotPlayer.turnCount - Pathfind.turnJustHadFlag > 2) && Pathfind.justHadFlag){
			Pathfind.justHadFlag = false;
		}
		
		FlagInfo[] nearbyEnemyFlags = rc.senseNearbyFlags(1, rc.getTeam().opponent());
		for (FlagInfo i: nearbyEnemyFlags){
			if ((rc.canPickupFlag(i.getLocation()) && !i.isPickedUp()) && !Pathfind.justHadFlag){
				rc.pickupFlag(i.getLocation());
				Pathfind.justHadFlag = false;
			}
		}
		
		//attack enemies, prioitizing enemies that have your flag
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1 , rc.getTeam().opponent());
		for(RobotInfo robot : nearbyEnemies) {
			if(robot.hasFlag()) {
				Pathfind.moveTowards(rc,robot.getLocation());
				if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
			}
		}
		
		for(RobotInfo robot: nearbyEnemies) {
			if(rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
		}
		
		//try to heal friendly robots
		for(RobotInfo robot: rc.senseNearbyRobots(-1, rc.getTeam())){
			if(rc.canHeal(robot.getLocation())) rc.heal(robot.getLocation());
		}
		
		
		if(!rc.hasFlag()) {

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
			if(closestFlag != null && !Pathfind.justHadFlag) {
				Pathfind.moveTowards(rc, closestFlag);
				if(rc.canPickupFlag(closestFlag)) {
					rc.pickupFlag(closestFlag);

				}
			}
			
			//if there is no flag to capture, explore randomly
			Pathfind.explore(rc);
			
		} else {
			//if we have flag, move towards closest allyl spawn zone
			MapLocation[] spawnLocs = rc.getAllySpawnLocations();
			MapLocation closestSpawn = findClosestLocation(rc.getLocation(),Arrays.asList(spawnLocs));
			Pathfind.moveTowardsWithFlag(rc, closestSpawn);
		}
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