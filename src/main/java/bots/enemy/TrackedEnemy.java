package bots.enemy;

/**
 * Last measured pose of an enemy, projected forward so callers can follow it between scans.
 */
public final class TrackedEnemy {

    private static final double BODY_MARGIN = 18;

    private final String name;
    private double x;
    private double y;
    private double heading;
    private double velocity;
    private long lastScan;

    TrackedEnemy(String name) {
        this.name = name;
    }

    void update(double x, double y, double heading, double velocity, long time) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.velocity = velocity;
        this.lastScan = time;
    }

    public String name() {
        return name;
    }

    public double heading() {
        return heading;
    }

    public double velocity() {
        return velocity;
    }

    public double xAt(long time, double fieldWidth) {
        double dt = Math.max(0, time - lastScan);
        return clamp(x + Math.sin(heading) * velocity * dt, BODY_MARGIN, fieldWidth - BODY_MARGIN);
    }

    public double yAt(long time, double fieldHeight) {
        double dt = Math.max(0, time - lastScan);
        return clamp(y + Math.cos(heading) * velocity * dt, BODY_MARGIN, fieldHeight - BODY_MARGIN);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
