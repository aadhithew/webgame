import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public final class Player {
    public static final double X = 155, WIDTH = 38, HEIGHT = 54;
    private double y, velocity, animation;
    private boolean grounded = true;

    public Player() { reset(); }
    public void reset() {
        y = GamePanel.GROUND - HEIGHT;
        velocity = 0;
        animation = 0;
        grounded = true;
    }
    public void jump() {
        if (grounded) { velocity = -760; grounded = false; }
    }
    public void update(double seconds) {
        animation += seconds;
        velocity += 2100 * seconds;
        y += velocity * seconds;
        if (y >= GamePanel.GROUND - HEIGHT) {
            y = GamePanel.GROUND - HEIGHT;
            velocity = 0;
            grounded = true;
        }
    }
    public Rectangle2D bounds() {
        // Slightly forgiving collision bounds, inside the drawn character.
        return new Rectangle2D.Double(X + 5, y + 4, WIDTH - 10, HEIGHT - 7);
    }
    public void draw(Graphics2D g) {
        int px = (int) X, py = (int) y;
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval(px - 8, GamePanel.GROUND - 5, 55, 9);
        g.setColor(new Color(95, 237, 202));
        g.fillRoundRect(px, py, 38, 39, 12, 12);
        g.setColor(new Color(22, 39, 63));
        g.fillRoundRect(px + 18, py + 9, 24, 12, 5, 5);
        g.setColor(new Color(223, 255, 246));
        g.fillRect(px + 29, py + 11, 7, 4);
        int step = grounded ? (int) (Math.sin(animation * 22) * 6) : 0;
        g.setColor(new Color(57, 190, 166));
        g.fillRoundRect(px + 4, py + 34, 10, 20 + step, 5, 5);
        g.fillRoundRect(px + 25, py + 34, 10, 20 - step, 5, 5);
        g.setColor(new Color(193, 255, 229));
        g.fillRoundRect(px - 7, py + 24, 18, 8, 5, 5);
    }
}
