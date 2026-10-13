package bots.goal;

import bots.enemy.EnemyBook;
import robocode.AdvancedRobot;

/**
 * What a goal can read and command during one turn.
 */
public final class GoalContext {

    private final AdvancedRobot robot;
    private final EnemyBook enemies;
    private final Drive drive;

    public GoalContext(AdvancedRobot robot, EnemyBook enemies, Drive drive) {
        this.robot = robot;
        this.enemies = enemies;
        this.drive = drive;
    }

    public AdvancedRobot robot() {
        return robot;
    }

    public EnemyBook enemies() {
        return enemies;
    }

    public Drive drive() {
        return drive;
    }
}
