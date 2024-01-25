package alphabotb;

import battlecode.common.*;

public class Setup {
	
	private static final int EXPLORE_ROUNDS = 100;
	private static final int BUILD_TRAPS_ROUNDS = 145;
	private static final int LINEUP_ROUNDS = 200;
	public static MapLocation goToFlag;
	
	public static void runSetup(RobotController rc) throws GameActionException {
		
		if(rc.getRoundNum() < EXPLORE_ROUNDS) {
			Pathfind.explore(rc);
		} else if (rc.getRoundNum() > 1000*BUILD_TRAPS_ROUNDS) {
			
			
			//search for nearby placed flag
			FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
			
			FlagInfo target = null;
			for(FlagInfo flag: flags) {
				if(!flag.isPickedUp()) {
					target = flag;
					break;
				}
			}
			
			//if there is a placed flag nearby, move closer and build defenses
			if(target != null) {
				Pathfind.moveTowards(rc, target.getLocation());
				
				if(rc.getLocation().distanceSquaredTo(target.getLocation())<9) {
					if(rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())) {
						rc.build(TrapType.EXPLOSIVE, rc.getLocation());
					} else {
						//cant build trap then make random water around our flag
						MapLocation water = rc.getLocation().add(Direction.allDirections()[RobotPlayer.random.nextInt(8)]);
					}
						
				}
			}
			//else Pathfind.explore(rc);
			
		}
		else{
			//oppurtunity to save bytcode if use the shared array
			if (rc.getRoundNum() == BUILD_TRAPS_ROUNDS + 1){
				MapLocation[] enemyflags = rc.senseBroadcastFlagLocations();
				int minDist = rc.getLocation().distanceSquaredTo(enemyflags[0]);
				MapLocation closestFlag = enemyflags[0];
				if (rc.getLocation().distanceSquaredTo(enemyflags[1]) < minDist){
					minDist = rc.getLocation().distanceSquaredTo(enemyflags[1]);
					closestFlag = enemyflags[1];
				}
				if (rc.getLocation().distanceSquaredTo(enemyflags[2]) < minDist){
					closestFlag = enemyflags[2];
				}	
				goToFlag = closestFlag;	
			}
			Pathfind.bugNavOne(rc, goToFlag);
			

		}
	}

	public static void lineUp(RobotController rc) throws GameActionException{
		//0 is NORTH SOUTH WALL
		// 1 is EAST WEST WALL
	}
}