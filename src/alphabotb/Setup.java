package alphabotb;

import battlecode.common.*;

public class Setup {
	
	private static final int EXPLORE_ROUNDS = 100;
	private static final int BUILD_TRAPS_ROUNDS = 145;
	private static final int LINEUP_ROUNDS = 200;
	public static MapLocation goToFlag;
	public static boolean stop = false;
	
	public static void runSetup(RobotController rc) throws GameActionException {
		
		if(rc.getRoundNum() < EXPLORE_ROUNDS) {
			Pathfind.explore(rc);
		} else if (rc.getRoundNum() < BUILD_TRAPS_ROUNDS) {
			
			
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
				MapLocation[] enemyflags = new MapLocation[3];
				enemyflags[0] = decode(rc.readSharedArray(0));
				enemyflags[1] = decode(rc.readSharedArray(1));
				enemyflags[2] = decode(rc.readSharedArray(2));
				for (int i = 0; i < 3; i++){
					if (enemyflags[i].x == 0 && enemyflags[i].y==0){
						MapLocation m = new MapLocation(59*rc.getMapWidth()/2,rc.getMapHeight()/2);
						enemyflags[i]= m;
					}
				}
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
			//Add shit about if it seees the dam it stops.
			if (stop){
				MapInfo[] info = rc.senseNearbyMapInfos(2);
				for (MapInfo i : info){
					if (i.isDam()){
						stop = true;
						break;
					}
				}
				Pathfind.bugNavZero(rc, goToFlag);
			}
			else{
				
				Pathfind.moveTowards(rc, goToFlag);
			}
			
			

			

		}
	}

	public static void writeWall(RobotController rc) throws GameActionException{
		FlagInfo[] flags = rc.senseNearbyFlags(2);
		int xc = rc.getMapWidth()/2;
		int yc = rc.getMapHeight()/2;
		int enemyX = 2*xc - flags[0].getLocation().x;
		int enemyY = 2*yc - flags[0].getLocation().y;
		for (int i = 0; i < 3; i++){
			int temp = rc.readSharedArray(i);
			if (temp == 0){
				if (rc.canWriteSharedArray(i,59*enemyX + enemyY)) rc.canWriteSharedArray(i,59*enemyX + enemyY);
			}
			if (Math.abs(temp/59-enemyX) <= 2 && Math.abs(temp%59-enemyY)<=2){
				if (rc.canWriteSharedArray(i,59*enemyX + enemyY)) rc.canWriteSharedArray(i,59*enemyX + enemyY);
			}
		}
	}

	public static MapLocation decode( int message) throws GameActionException{
		int x = message/59;
		int y = message%59;
		MapLocation ans = new MapLocation(x,y);
		return ans;
	}
}