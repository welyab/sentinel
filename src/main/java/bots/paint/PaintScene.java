package bots.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import bots.enemy.EnemyBook;
import bots.enemy.TrackedEnemy;

/**
 * Drawing components currently on the battlefield.
 */
public final class PaintScene {

    private static final Color[] PALETTE = {
            new Color(240, 176, 48),
            new Color(80, 200, 255),
            new Color(255, 96, 96),
            new Color(120, 230, 140),
            new Color(200, 140, 255),
            new Color(255, 160, 64)
    };

    private final Map<String, EnemyMarker> markers = new LinkedHashMap<String, EnemyMarker>();

    /**
     * Adds a square for each identified enemy, drops squares for enemies that are gone,
     * and moves the remaining squares onto the projected positions.
     */
    public void follow(EnemyBook enemies, long time, double fieldWidth, double fieldHeight) {
        Set<String> alive = new HashSet<String>();
        for (TrackedEnemy enemy : enemies.all()) {
            alive.add(enemy.name());
            EnemyMarker marker = markers.get(enemy.name());
            if (marker == null) {
                Color color = PALETTE[markers.size() % PALETTE.length];
                marker = new EnemyMarker(enemy, color);
                markers.put(enemy.name(), marker);
            }
            marker.follow(time, fieldWidth, fieldHeight);
        }
        markers.keySet().retainAll(alive);
    }

    public void paint(Graphics2D graphics) {
        for (EnemyMarker marker : markers.values()) {
            marker.paint(graphics);
        }
    }
}
