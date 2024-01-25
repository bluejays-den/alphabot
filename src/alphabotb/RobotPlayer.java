//THIS IS MINE

package alphabotb;


import java.util.*;


import battlecode.common.*;

public class RobotPlayer {

    
    //We will use this variable to count the number of turns this robot has been alive.
    static int turnCount = 0;
    //We will use this RNG to make some random moves. 
    public static Random random = null;
   

    // Array containing all the possible movement directions.
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };
    
    //variables for bugNavOne
    private int bugState = 0;
    private MapLocation closestObstacle = null;
    private int closestObstacleDistance = 10000;
    
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

    	
        while (true) {
            try {
            	//spawn in one bot first to better see bot behavior
            	if(rc.readSharedArray(0) == 0) {
            		rc.writeSharedArray(0, rc.getID());
            	}
            	if(rc.readSharedArray(0) == rc.getID()) trySpawn(rc);
            	
            	if(random == null) random = new Random(rc.getID());
            	
            	if(!rc.isSpawned()) {
            		Pathfind.resetBug();
                    turnCount = 0;
                    Defender.isDefender = false;
            	}
            	//regular spawn comment out if want to test one duck
            	trySpawnRandom(rc);
            	if(rc.isSpawned()) {
            	   //check round num and call setupt / main phase logic
            	   int round = rc.getRoundNum();
            	   if(round <= GameConstants.SETUP_ROUNDS) {
                    if (round == 1){
                        Setup.writeWall(rc);
                    }
                   
                    Setup.runSetup(rc);
                   }
            	   else {
                    if (Defender.determineDefender(rc)){
                        Defender.initPatrol(rc);
                        //Defender.isDefender = true;
                    }
                    if (Defender.isDefender){
                        Defender.runDefender(rc);
                    }
                    else{
                        MainPhase.runMainPhase(rc);
                    }
                    
                   }
               }
            } catch (GameActionException e) {
            	//my fault cuh
            	System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
            	//game dev is dumbdumb
            	System.out.println("Exception");
                e.printStackTrace();
            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                turnCount++;
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    
    private static void trySpawn(RobotController rc) throws GameActionException{
    	MapLocation[] locations = rc.getAllySpawnLocations();
    	for(MapLocation loc: locations) {
    		if(rc.canSpawn(loc)) {
    			rc.spawn(loc);
    			break;
    		}
    	}
    }

    private static void trySpawnRandom(RobotController rc) throws GameActionException{
    	MapLocation[] locations = rc.getAllySpawnLocations();
    	while (!rc.isSpawned()){
            int randSpawn = random.nextInt(locations.length);
    		if(rc.canSpawn(locations[randSpawn])) {
    			rc.spawn(locations[randSpawn]);
    			break;
    		}
    	}
    }
    
    
    
    
    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
