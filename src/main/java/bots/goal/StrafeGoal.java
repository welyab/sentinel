package bots.goal;

import bots.enemy.TrackedEnemy;
import robocode.AdvancedRobot;
import robocode.util.Utils;

/**
 * Strafes perpendicular to the closest enemy and reverses near walls.
 */
public final class StrafeGoal implements Goal {

    private static final double WALL_MARGIN = 80;

    @Override
    public void pursue(GoalContext context) {
        AdvancedRobot robot = context.robot();
        double width = robot.getBattleFieldWidth();
        double height = robot.getBattleFieldHeight();
        long time = robot.getTime();
        TrackedEnemy target = context.enemies().closestTo(robot.getX(), robot.getY(), time, width, height);
        if (target == null) {
            return;
        }

        double absoluteBearing = Math.atan2(
                target.xAt(time, width) - robot.getX(),
                target.yAt(time, height) - robot.getY());
        double bearing = Utils.normalRelativeAngle(absoluteBearing - robot.getHeadingRadians());
        robot.setTurnRightRadians(bearing + Math.PI / 2.0);

        boolean nearWall = robot.getX() < WALL_MARGIN
                || robot.getY() < WALL_MARGIN
                || robot.getX() > width - WALL_MARGIN
                || robot.getY() > height - WALL_MARGIN;
        robot.setAhead(140.0 * context.drive().direction(time, nearWall));
    }
}
