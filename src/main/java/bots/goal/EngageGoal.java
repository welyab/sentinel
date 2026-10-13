package bots.goal;

import bots.enemy.TrackedEnemy;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;

/**
 * Aims at the closest enemy and fires when the gun is close to the predicted point.
 */
public final class EngageGoal implements Goal {

    private static final double BODY_MARGIN = 18;

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

        double enemyX = target.xAt(time, width);
        double enemyY = target.yAt(time, height);
        double power = bulletPower(robot, Math.hypot(enemyX - robot.getX(), enemyY - robot.getY()));
        double aimPower = power > 0 ? power : 2.0;
        double bulletSpeed = Rules.getBulletSpeed(aimPower);
        double heading = target.heading();
        double velocity = target.velocity();

        for (int tick = 0; tick < 80; tick++) {
            if (tick * bulletSpeed >= Math.hypot(enemyX - robot.getX(), enemyY - robot.getY())) {
                break;
            }
            enemyX += Math.sin(heading) * velocity;
            enemyY += Math.cos(heading) * velocity;
            if (outsideField(enemyX, enemyY, width, height)) {
                enemyX = clamp(enemyX, BODY_MARGIN, width - BODY_MARGIN);
                enemyY = clamp(enemyY, BODY_MARGIN, height - BODY_MARGIN);
                break;
            }
        }

        double gunTurn = Utils.normalRelativeAngle(
                Math.atan2(enemyX - robot.getX(), enemyY - robot.getY()) - robot.getGunHeadingRadians());
        robot.setTurnGunRightRadians(gunTurn);

        if (power > 0 && robot.getGunHeat() == 0 && Math.abs(robot.getGunTurnRemainingRadians()) < Math.toRadians(12)) {
            robot.setFire(power);
        }
    }

    private static double bulletPower(AdvancedRobot robot, double distance) {
        double power = Math.min(Rules.MAX_BULLET_POWER, 450.0 / distance);
        if (robot.getEnergy() < 20) {
            power = Math.min(power, 1.0);
        }
        if (power < Rules.MIN_BULLET_POWER || power >= robot.getEnergy()) {
            return 0;
        }
        return power;
    }

    private static boolean outsideField(double x, double y, double width, double height) {
        return x < BODY_MARGIN || y < BODY_MARGIN || x > width - BODY_MARGIN || y > height - BODY_MARGIN;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
