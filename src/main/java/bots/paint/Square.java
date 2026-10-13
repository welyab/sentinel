package bots.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Axis-aligned square. Call {@link #moveTo(double, double)} to place it.
 */
public final class Square implements Drawable {

    private final double side;
    private final Color stroke;
    private final Color fill;
    private double centerX;
    private double centerY;

    public Square(double side, Color color) {
        this.side = side;
        this.stroke = color;
        this.fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 48);
    }

    public void moveTo(double x, double y) {
        this.centerX = x;
        this.centerY = y;
    }

    @Override
    public void paint(Graphics2D graphics) {
        int originX = (int) Math.round(centerX - side / 2.0);
        int originY = (int) Math.round(centerY - side / 2.0);
        int size = (int) Math.round(side);

        graphics.setStroke(new BasicStroke(2f));
        graphics.setColor(fill);
        graphics.fillRect(originX, originY, size, size);
        graphics.setColor(stroke);
        graphics.drawRect(originX, originY, size, size);
    }
}
