package basicplayer;
import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    public static Random random = null;
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
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
    private static int bugState = 0;
    private static MapLocation closestObstacle = null;
    private static int closestObstacleDist = 100000;
    private static Direction lastDir = null;
    
    public static void resetBug() {
    	bugState = 0;
        closestObstacle = null;
        closestObstacleDist = 100000;
        lastDir = null;
    }
    
    public static Direction mainDir = null;
    public static int numRounds = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
        	if (random==null) {
        		random = new Random(rc.getID());
        	}
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    // Pick a random spawn location to attempt spawning in.
                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                    if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
                    if (RobotPlayer.random.nextInt()%11==0) {
                    	if (RobotPlayer.random.nextInt()%3==0) {
                    		if (rc.canWriteSharedArray(10,spawnLocs[0].x)) {
	                    		rc.writeSharedArray(10,spawnLocs[0].x);
	                    	}
                    		if (rc.canWriteSharedArray(42,spawnLocs[0].y)) {
	                    		rc.writeSharedArray(42,spawnLocs[0].y);
	                    	}
                    	}
                    	else if (RobotPlayer.random.nextInt()%2==0) {
                    		if (rc.canWriteSharedArray(11,spawnLocs[1].x)) {
	                    		rc.writeSharedArray(11,spawnLocs[1].x);
	                    	}
                    		if (rc.canWriteSharedArray(43,spawnLocs[1].y)) {
	                    		rc.writeSharedArray(43,spawnLocs[1].y);
	                    	}
                    	}
                    	else {
                    		if (rc.canWriteSharedArray(12,spawnLocs[2].x)) {
	                    		rc.writeSharedArray(12,spawnLocs[2].x);
	                    	}
                    		if (rc.canWriteSharedArray(44,spawnLocs[2].y)) {
	                    		rc.writeSharedArray(44,spawnLocs[2].y);
	                    	}
                    	}
                    }
                }
                else{
                	if (rc.getRoundNum()<GameConstants.SETUP_ROUNDS) {
                		MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
                		for (MapLocation crumb: crumbLocations) {
                			Direction dir = rc.getLocation().directionTo(crumb);
                			for (int i = 0; i < 8; i++) {
                				if (rc.canMove(dir)) {
                					rc.move(dir);
                					break;
                				}
                				else {
                					dir = dir.rotateRight();
                				}
                			}
                		}
                	}
                	
                    if (rc.canPickupFlag(rc.getLocation())&&rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        rc.pickupFlag(rc.getLocation());
                        rc.setIndicatorString("Holding a flag!");
                    }
                    
                    // If we are holding an enemy flag, singularly focus on moving towards
                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
                    // to make sure setup phase has ended.
                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        MapLocation firstLoc = spawnLocs[0];
                        Direction dir = rc.getLocation().directionTo(firstLoc);
                        if (bugState==0) {
                        	lastDir = dir;
                        	if (rc.canMove(lastDir)) {
                        		rc.move(lastDir);
                        	}
                        	else {
                        		bugState = 1;
                        		closestObstacle = null;
                        		closestObstacleDist = 10000;
                        	}
                        }
                        else {
                        	if (rc.getLocation().equals(closestObstacle)) {
                        		bugState = 0;
                        	}
                        	if (rc.getLocation().distanceSquaredTo(firstLoc)<closestObstacleDist) {
                        		closestObstacleDist = rc.getLocation().distanceSquaredTo(firstLoc);
                        		closestObstacle=rc.getLocation();
                        	}
                        	for (int i = 0; i < 8; i++) {
                        		if (rc.canMove(lastDir)) {
                        			rc.move(lastDir);
                        			lastDir=lastDir.rotateRight();
                        			break;
                        		}
                        		else {
                        			lastDir=lastDir.rotateLeft();
                        		}
                        	}
                        }
       
                    }
                    Direction dirr = directions[(rc.getID()%8)];
            		for (int i = 0; i < 8; i++) {
            			if (rc.canFill(rc.getLocation().add(dirr))) {
    						rc.fill(rc.getLocation().add(dirr));
    						break;
    					}
            			else {
            				dirr = dirr.rotateRight();
            			}
            		}
                    if (mainDir!=null&&numRounds<=30) {
                    	if (rc.canMove(mainDir)) {
                    		rc.move(mainDir);
                    	}
                    	numRounds++;
                    }
                    FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);
                    for (FlagInfo flag: nearbyFlags) {
                    	if (RobotPlayer.random.nextInt()%3==1&&flag.getTeam()!=rc.getTeam()&&!flag.isPickedUp()) {
                    		for (int i = 0; i < 6; i++) {
                    			if (rc.canWriteSharedArray((i+rc.getID()%6),flag.getLocation().x)) {
		                    		rc.writeSharedArray((i+rc.getID()%6),flag.getLocation().x);
		                    	}
		                    	if (rc.canWriteSharedArray((i+rc.getID()%6)+32,flag.getLocation().y)) {
		                    		rc.writeSharedArray((i+rc.getID()%6)+32,flag.getLocation().y);
		                    	}
                    		}
	                    		
                    	}
	                    	
                    }
                    // Move and attack randomly if no objective.
                    RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
                    RobotInfo[] nearbyFriends = rc.senseNearbyRobots(-1,rc.getTeam());
                    int numFriends = 0;
                    int numEnemies = 0;
                    for (RobotInfo enemy: nearbyEnemies) {
                    	int randomxIndex = ((RobotPlayer.random.nextInt())%7)+3;
                    	int randomyIndex = randomxIndex+32;
                    	if (rc.canWriteSharedArray(randomxIndex,enemy.getLocation().x)&&RobotPlayer.random.nextInt()%3==1) {
                    		rc.writeSharedArray(randomxIndex,enemy.getLocation().x);
                    		rc.writeSharedArray(randomyIndex,enemy.getLocation().y);
                    	}
                    	
                    	numEnemies += 1;
                    	Direction dir = rc.getLocation().directionTo(enemy.getLocation());
                    	if (rc.canAttack(enemy.getLocation())) {
                    		rc.attack(enemy.getLocation());
                    		break;
                    	}
                    	else if (rc.canMove(dir)) {
                    		rc.move(dir);
                    		break;
                    	}
                    }
                    for (RobotInfo friend: nearbyFriends) {
                    	numFriends += 1;
                    	if (rc.canHeal(friend.getLocation())) {
                    		rc.heal(friend.getLocation());
                    		break;
                    	}
                    }
                	for (int i = 0; i < 13; i++) {
                		int xco = rc.readSharedArray((i+rc.getID())%13);
                		int yco = rc.readSharedArray(((i+rc.getID())%13)+32);
                		if (xco!=0&&yco!=0) {
                			xco += (RobotPlayer.random.nextInt()%5)-2;
                			xco = Math.min(xco,rc.getMapWidth()-1);
                			xco = Math.max(xco,0);
                			yco += (RobotPlayer.random.nextInt()%5)-2;
                			yco = Math.min(yco,rc.getMapHeight()-1);
                			yco = Math.max(yco,0);
                			MapLocation target = new MapLocation(xco,yco);
                			Direction dir = rc.getLocation().directionTo(target);
                			if (rc.canMove(dir)) {
                				rc.move(dir);
                				if (rc.getID()%10==0) {
                					mainDir = dir;
	                				numRounds = 0;
                				}
                			}
                			break;
                		}
                	}
                	for (RobotInfo friend: nearbyFriends) {
                		Direction dir = rc.getLocation().directionTo(friend.getLocation());
                		
                		if (RobotPlayer.random.nextInt()%3==0) {
                			dir = dir.rotateRight();
                		}
                		else if (RobotPlayer.random.nextInt()%2==0) {
                			dir = dir.rotateLeft();
                		}
                		if (rc.canMove(dir.opposite())) {
                			rc.move(dir.opposite());
                			if (rc.getID()%10==0) {
            					mainDir = dir.opposite();
                				numRounds = 0;
            				}
                		}
                	}
                    
                    

                    if (rc.canBuild(TrapType.STUN, rc.getLocation()) &&numEnemies>numFriends)
                        rc.build(TrapType.STUN, rc.getLocation());
                    // We can also move our code into different methods or classes to better organize it!
                    updateEnemyRobots(rc);
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
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
