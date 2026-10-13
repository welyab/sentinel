package bots.goal;

/**
 * Movement direction shared by goals that react to collisions and walls.
 */
public final class Drive {

    private static final long REVERSE_COOLDOWN = 30;

    private int direction = 1;
    private long lastReverse;
    private boolean collision;

    public void noteCollision() {
        collision = true;
    }

    /** 1 is forward, -1 is backward. Reverses after a wall or a collision, with a short cooldown. */
    public int direction(long time, boolean nearWall) {
        if ((collision || nearWall) && time - lastReverse >= REVERSE_COOLDOWN) {
            direction = -direction;
            lastReverse = time;
        }
        collision = false;
        return direction;
    }
}
