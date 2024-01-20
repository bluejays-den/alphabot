package alphabot;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public strictfp class RobotPlayer {
    static int turnCount = 0;
    static final int allocatedEnemyArray = 8;
    static final Random rng = new Random(6147);
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
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        System.out.println("I'm alive");
        rc.setIndicatorString("sup cuh");
        while (true){
            turnCount += 1;
            try{
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                }
                else{
                    //move and do shit
                }
            }
            /**
            catch(GameActionException e){
                System.out.println("GameActionException");
                e.printStackTrace();
            }
            **/
            catch(Exception e){
                System.out.println("Exception");
                e.printStackTrace();
            }
            finally{
                Clock.yield();
            }

        }
    }

    public static void ifWriteThenWrite(RobotController rc, int index, int message) throws GameActionException{
        if (rc.canWriteSharedArray(index,message)){
            rc.writeSharedArray(index,message);
        }
    }
    
    public static int[] averageLocation(RobotController rc, MapLocation[] locations){
        int[] array = new int[]{0,0};
        int n = locations.length;
        for (MapLocation i : locations){
            array[0]+=i.x;
            array[1]+=i.y;
        }
        array[0]/=n;
        array[1]/=n;
        return array;
    }
    
    public static void writeSharedArrayEnemyLocation(RobotController rc, int x, int y, int mag) throws GameActionException{
        int[][] allEnemyInfo = new int[allocatedEnemyArray][3];
        int minNumEnemies = 100;//arbitrary high number
        int minIndex=0;
        for (int i = 0; i < allocatedEnemyArray; i++){
            allEnemyInfo[i] = decodeEnemy(rc, i);
            //changes last in or first in depending on if < or <=
            if (minNumEnemies <= allEnemyInfo[i][2]){
                minNumEnemies = allEnemyInfo[i][2];
                minIndex = i;
            }
            //5 is just cluster value + some catch all value
            if (Math.abs(x-allEnemyInfo[i][0]) < 7 && Math.abs(y-allEnemyInfo[i][1])<7){
                rc.writeSharedArray(i, codeEnemy(rc, x, y, mag));
            }
        }
        if (mag >= minNumEnemies){
            rc.writeSharedArray(minIndex, codeEnemy(rc, x, y, mag));
        }
        
    }

    public static int[] decodeEnemy(RobotController rc, int index) throws GameActionException{
        int[] answer = new int[3];
        int n = rc.readSharedArray(index);
        answer[2] = n/1000;
        n/=1000;
        //change the 15 if cluster size becomes 3, not 4
        //change 4 if cluster size changes
        answer[0]=(n/15) * 4 + 1;
        answer[1]=(n%15) * 4 + 1;
        return(answer);
    }

    public static int codeEnemy(RobotController rc, int x, int y, int z){
        //change 15 if cluster size changes
        //change 4 if cluster size changes
        return(1000 * z + 15*(((int)(x-1)) /4) + 15 * (((int)(y-1))/4));
    }

    public static void updateEnemyLocations(RobotController rc) throws GameActionException{
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length!=0){
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            int[] averageEnemyLocation = averageLocation(rc, enemyLocations);
            writeSharedArrayEnemyLocation(rc, averageEnemyLocation[0], averageEnemyLocation[1], enemyRobots.length);   
        }
    }

}
