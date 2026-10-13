package bots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import bots.enemy.EnemyBook;
import bots.goal.Drive;
import bots.goal.EngageGoal;
import bots.goal.Goal;
import bots.goal.GoalContext;
import bots.goal.ScanFieldGoal;
import bots.goal.StrafeGoal;
import bots.paint.PaintScene;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * Pursues three goals each turn: keep enemies identified, fire on the closest one, and strafe.
 * Identified enemies are marked with squares that follow their movement.
 */
public class Sentinel extends AdvancedRobot {

    private final EnemyBook enemies = new EnemyBook();
    private final Drive drive = new Drive();
    private final PaintScene scene = new PaintScene();
    private final List<Goal> goals = Arrays.asList(
            new ScanFieldGoal(),
            new EngageGoal(),
            new StrafeGoal());

    @Override
    public void run() {
        setBodyColor(new Color(20, 24, 33));
        setGunColor(new Color(200, 48, 40));
        setRadarColor(new Color(240, 176, 48));
        setBulletColor(new Color(255, 214, 80));
        setScanColor(new Color(240, 176, 48));

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        while (true) {
            GoalContext context = new GoalContext(this, enemies, drive);
            for (Goal goal : goals) {
                goal.pursue(context);
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        enemies.observe(event, getX(), getY(), getHeadingRadians(), getTime());
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        enemies.forget(event.getName());
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        drive.noteCollision();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        drive.noteCollision();
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if (event.isMyFault()) {
            drive.noteCollision();
        }
    }

    @Override
    public void onPaint(Graphics2D graphics) {
        scene.follow(enemies, getTime(), getBattleFieldWidth(), getBattleFieldHeight());
        scene.paint(graphics);
    }
}
