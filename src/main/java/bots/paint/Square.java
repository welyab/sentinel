package bots.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Square centered on a point. Call {@link #moveTo(double, double)} to place it
 * and {@link #turnTo(double)} to align it with a heading.
 */
public final class Square implements Drawable {

    private final double side;
    private final Color stroke;
    private final Color fill;
    private double centerX;
    private double centerY;
    private double heading;

    public Square(double side, Color color) {
        this.side = side;
        this.stroke = color;
        this.fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 48);
    }

    public void moveTo(double x, double y) {
        this.centerX = x;
        this.centerY = y;
    }

    /**
     * @param headingRadians clockwise from north, the same convention as a robot heading
     */
    public void turnTo(double headingRadians) {
        this.heading = headingRadians;
    }

    @Override
    public void paint(Graphics2D graphics) {
        // Robocode 1.11.1's Graphics2D.create() copies its command buffer onto itself
        // and throws IllegalArgumentException on current JDKs, so the transform is
        // applied here and put back afterward.
        AffineTransform previousTransform = graphics.getTransform();
        Stroke previousStroke = graphics.getStroke();
        Color previousColor = graphics.getColor();

        graphics.translate(centerX, centerY);
        // Battlefield Y grows upward, so a positive Graphics2D rotation is counterclockwise.
        graphics.rotate(-heading);

        double half = side / 2.0;
        Rectangle2D body = new Rectangle2D.Double(-half, -half, side, side);
        graphics.setStroke(new BasicStroke(2f));
        graphics.setColor(fill);
        graphics.fill(body);
        graphics.setColor(stroke);
        graphics.draw(body);

        graphics.setTransform(previousTransform);
        graphics.setStroke(previousStroke);
        graphics.setColor(previousColor);
    }
}
