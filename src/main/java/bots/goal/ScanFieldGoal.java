package bots.goal;

import bots.enemy.TrackedEnemy;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;

/**
 * Keeps every identified enemy in the radar picture.
 * Locks the beam on the only contact; otherwise sweeps the field.
 */
public final class ScanFieldGoal implements Goal {

    @Override
    public void pursue(GoalContext context) {
        AdvancedRobot robot = context.robot();
        if (context.enemies().count() != 1) {
            robot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            return;
        }

        TrackedEnemy target = context.enemies().only();
        double width = robot.getBattleFieldWidth();
        double height = robot.getBattleFieldHeight();
        long time = robot.getTime();
        double enemyX = target.xAt(time, width);
        double enemyY = target.yAt(time, height);
        double absoluteBearing = Math.atan2(enemyX - robot.getX(), enemyY - robot.getY());
        double radarTurn = Utils.normalRelativeAngle(absoluteBearing - robot.getRadarHeadingRadians());
        double distance = Math.max(1.0, Math.hypot(enemyX - robot.getX(), enemyY - robot.getY()));
        double extra = Math.min(Math.atan(36.0 / distance), Rules.RADAR_TURN_RATE_RADIANS);
        if (radarTurn == 0) {
            radarTurn = extra;
        } else {
            radarTurn += Math.copySign(extra, radarTurn);
        }
        robot.setTurnRadarRightRadians(radarTurn);
    }
}
