package bots.paint;

import java.awt.Graphics2D;

/**
 * Something the battle view can paint in battlefield coordinates.
 */
public interface Drawable {

    void paint(Graphics2D graphics);
}
