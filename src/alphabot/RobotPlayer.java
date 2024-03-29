//THIS IS MINE

package alphabot;


import java.util.*;


import battlecode.common.*;

public class RobotPlayer {

    
    //We will use this variable to count the number of turns this robot has been alive.
    static int turnCount = 0;
    //We will use this RNG to make some random moves. 
    public static Random random = null;
    public static int personalID = -1;


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
        Util.init(rc);
        tempPathfind.init(rc);
        while (true) {
            try {
                tempPathfind.initTurn();
                //Creates ducks 0-49
                if (personalID == -1){
                    personalID = rc.readSharedArray(63);
                    rc.writeSharedArray(63,personalID + 1);
                }

            	if(random == null) random = new Random(rc.getID());
            	
            	if(!rc.isSpawned()) {
            		Pathfind.resetBugTwo();
                    trySpawnRandom(rc);
                }
            	if(rc.isSpawned()) {
            	   //check round num and call setupt / main phase logic
            	   int round = rc.getRoundNum();
            	   if(round <= 150) Setup.runSetup(rc);
                   else if(rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) Setup.lineUp(rc);
            	   else MainPhase.runMainPhase(rc);
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
        int randSpawn = Math.abs(random.nextInt()) % locations.length;
        if(rc.canSpawn(locations[randSpawn])) {
            rc.spawn(locations[randSpawn]);
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
