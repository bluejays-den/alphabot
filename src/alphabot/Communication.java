package alphabot;

import battlecode.common.*;

public class Communication {

    static final int LAST_FLAG_IDX = 5;


    public static void updateEnemyInfo(RobotController rc, MapLocation loc, int numEnemies){
        //implement later
    }

    //SHARED ARRAY: 64 INDCIES
    //first 6 indicies: flag infos
    //last index: used for giving robots a personal id
    public static void updateFlagInfo(RobotController rc, MapLocation loc, boolean isCarried, Team team, int idx){
        return;
    }

    public static MapLocation getLocation(RobotController rc, int idx) throws GameActionException{
        //return new MapLocation(0,0);

        int value = rc.readSharedArray(idx);
        int locNum = value >> 4;
        MapLocation loc = intToLocation(rc, locNum);
        return loc;

    }

    public static Team getTeam(RobotController rc, int idx){
        return Team.NEUTRAL;
    }

    public static void setUnupdated(RobotController rc, int idx){
        return;
    }

    public static boolean getIfUpdated(RobotController rc, int idx){
        return true;
    }


    public static int locationToInt(RobotController rc, MapLocation loc) {
        if(loc == null)
            return 0;
        return  1 + loc.x + loc.y * rc.getMapWidth();

    }

    public static MapLocation intToLocation(RobotController rc, int m){
        if (m == 0)
            return null;
        return new MapLocation((m-1) % rc.getMapWidth(), (m-1) / rc.getMapWidth());
    }

}
