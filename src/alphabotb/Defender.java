
package alphabotb;



import java.util.*;


import battlecode.common.*;

public class Defender {

    public static MapLocation[] patrolSpots = new MapLocation[9];
    public static MapLocation flagLoc;
    public static boolean isDefender = false;
    
    public static boolean determineDefender(RobotController rc) throws GameActionException{
        if (RobotPlayer.turnCount == 0 && rc.senseNearbyRobots(2, rc.getTeam()).length  == 0){
            FlagInfo[] flags = rc.senseNearbyFlags(2);
            if (flags.length==0){
                return false;
            }
            else{
                flagLoc = flags[0].getLocation();
                return true;
            }
            
        }
        return false;
    }

    public static void runDefender(RobotController rc) throws GameActionException{
        int goRand = RobotPlayer.random.nextInt(9);
        Pathfind.moveTowards(rc, patrolSpots[goRand]);
    }

    public static void initPatrol(RobotController rc) throws GameActionException{
        MapLocation loc = rc.getLocation();
        for (int i = 7; i >= 0; i--){
            patrolSpots[i] = flagLoc.add(RobotPlayer.directions[i]);
        }
        patrolSpots[8] = flagLoc;
    }
}
