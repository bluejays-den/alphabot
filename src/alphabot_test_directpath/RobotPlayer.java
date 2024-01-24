//works on bottom player for pathfindopentest



package alphabot_test_directpath;

import java.util.*;

import alphabot_test_directpath.RobotPlayer;
import battlecode.common.*;

public class RobotPlayer {

    
    public static Random random = null;
    private static int ducknum = 0;
    private static MapLocation goal =  new MapLocation(14,12);

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
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
    	
        while (true) {
            try {
            	if(rc.getRoundNum() > 200) rc.resign();
            	//spawn in one bot first to better see bot behavior
            	if(rc.readSharedArray(0) == 0 && !rc.isSpawned()) {
            		trySpawn(rc, new MapLocation(1,2));
            		ducknum = 1;
            		rc.writeSharedArray(0, 1);
            		Pathfind.resetBug();
            	}

            	if(rc.readSharedArray(0) == 1 && !rc.isSpawned()) {
            		trySpawn(rc, new MapLocation(14,0));
            		ducknum = 2;
            		rc.writeSharedArray(0, 2);
            		Pathfind.resetBug();


            	}

            	if(rc.readSharedArray(0) == 2 && !rc.isSpawned()) {
            		trySpawn(rc, new MapLocation(28,2));
            		ducknum = 3;
            		rc.writeSharedArray(0, 3);
            		Pathfind.resetBug();

            	}

            	
            	if(random == null) random = new Random(rc.getID());
            	
            	if(rc.isSpawned() && ducknum == 1) {
            		Pathfind.bugNavTwo(rc, goal);     		
            	}
            	
            	if(rc.isSpawned() && ducknum == 2) {
            		Pathfind.bugNavTwo(rc, goal);     		
            	}

            	if(rc.isSpawned() && ducknum == 3) {
            		Pathfind.bugNavTwo(rc, goal);     		
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
    
    
    
    private static void trySpawn(RobotController rc, MapLocation spawn) throws GameActionException{
    	MapLocation[] locations = rc.getAllySpawnLocations();

    	for(MapLocation loc: locations) {
    		if(rc.canSpawn(spawn)) rc.spawn(spawn);
    	}
    }
        
}
