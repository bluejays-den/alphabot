package alphabot;

import battlecode.common.*;

public class Setup {
	
	private static final int EXPLORE_ROUNDS = 150;
	
	public static void runSetup(RobotController rc) throws GameActionException {
		
		if(rc.getRoundNum() < EXPLORE_ROUNDS) {
			Pathfind.explore(rc);
		} else {
			//try to place flag if it is far enough away from other flags
			if(rc.senseLegalStartingFlagPlacement(rc.getLocation())){
				if(rc.canDropFlag(rc.getLocation())) rc.dropFlag(rc.getLocation());
			}
			
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
			else Pathfind.explore(rc);
			
		}
	}
}