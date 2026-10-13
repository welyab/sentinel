package bots.paint;

import java.awt.Color;
import java.awt.Graphics2D;

import bots.enemy.TrackedEnemy;

/**
 * Square bound to one identified enemy. {@link #follow(long, double, double)} keeps the square on that enemy.
 */
public final class EnemyMarker implements Drawable {

    /** Slightly larger than a robot body (36px) so the outline stays visible around it. */
    static final double SIDE = 44;

    private final TrackedEnemy enemy;
    private final Square square;

    public EnemyMarker(TrackedEnemy enemy, Color color) {
        this.enemy = enemy;
        this.square = new Square(SIDE, color);
    }

    public String name() {
        return enemy.name();
    }

    public void follow(long time, double fieldWidth, double fieldHeight) {
        square.moveTo(enemy.xAt(time, fieldWidth), enemy.yAt(time, fieldHeight));
    }

    @Override
    public void paint(Graphics2D graphics) {
        square.paint(graphics);
    }
}
