import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public final class Obstacle {
    private double x;
    private final int width, height;
    public Obstacle(double x, int width, int height) {
        this.x = x; this.width = width; this.height = height;
    }
    public void update(double distance) { x -= distance; }
    public boolean isOffscreen() { return x + width < -20; }
    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x + 3, GamePanel.GROUND - height + 3, width - 6, height - 3);
    }
    public void draw(Graphics2D g) {
        int left = (int) x, top = GamePanel.GROUND - height;
        g.setColor(new Color(255, 123, 107));
        g.fillRoundRect(left, top, width, height, 7, 7);
        g.setColor(new Color(255, 192, 147));
        g.fillRoundRect(left + 4, top + 4, width - 8, 5, 3, 3);
        g.setColor(new Color(167, 65, 77));
        for (int i = 14; i < height - 4; i += 13) g.fillRect(left + 6, top + i, width - 12, 4);
    }
}
