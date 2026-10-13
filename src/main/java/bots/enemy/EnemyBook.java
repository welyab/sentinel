package bots.enemy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import robocode.ScannedRobotEvent;

/**
 * Enemies identified by radar scans.
 */
public final class EnemyBook {

    private final Map<String, TrackedEnemy> enemies = new LinkedHashMap<String, TrackedEnemy>();

    public void observe(ScannedRobotEvent event, double robotX, double robotY, double robotHeading, long time) {
        double absoluteBearing = robotHeading + event.getBearingRadians();
        double x = robotX + Math.sin(absoluteBearing) * event.getDistance();
        double y = robotY + Math.cos(absoluteBearing) * event.getDistance();

        TrackedEnemy enemy = enemies.get(event.getName());
        if (enemy == null) {
            enemy = new TrackedEnemy(event.getName());
            enemies.put(event.getName(), enemy);
        }
        enemy.update(x, y, event.getHeadingRadians(), event.getVelocity(), time);
    }

    public void forget(String name) {
        enemies.remove(name);
    }

    public int count() {
        return enemies.size();
    }

    public TrackedEnemy only() {
        if (enemies.size() != 1) {
            return null;
        }
        return enemies.values().iterator().next();
    }

    public TrackedEnemy closestTo(double x, double y, long time, double fieldWidth, double fieldHeight) {
        TrackedEnemy closest = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (TrackedEnemy enemy : enemies.values()) {
            double dx = enemy.xAt(time, fieldWidth) - x;
            double dy = enemy.yAt(time, fieldHeight) - y;
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = enemy;
            }
        }
        return closest;
    }

    public Collection<TrackedEnemy> all() {
        return enemies.values();
    }
}
