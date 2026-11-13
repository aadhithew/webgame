import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;
import javax.swing.*;

public final class GamePanel extends JPanel {
    public static final int WIDTH = 960, HEIGHT = 600, GROUND = 455;
    private enum State { READY, RUNNING, PAUSED, OVER }
    private State state = State.READY;
    private final Player player = new Player();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Random random = new Random();
    private final javax.swing.Timer timer;
    private Preferences preferences;
    private boolean savedBest = true, jumpHeld;
    private int best;
    private double elapsed, distance, spawnIn = 1.4;
    private long previous = System.nanoTime();
    private final Color ink = new Color(234, 241, 255), muted = new Color(155, 174, 203);

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(14, 22, 41));
        setFocusable(true);
        try {
            preferences = Preferences.userNodeForPackage(Main.class).node("skyline-runner");
            best = Math.max(0, preferences.getInt("best", 0));
        } catch (RuntimeException ex) { savedBest = false; }
        bind("SPACE", "jump", () -> { if (!jumpHeld) { jumpHeld = true; if (state == State.RUNNING) player.jump(); } });
        bind("UP", "jumpUp", () -> { if (!jumpHeld) { jumpHeld = true; if (state == State.RUNNING) player.jump(); } });
        bind("released SPACE", "release", () -> jumpHeld = false);
        bind("released UP", "releaseUp", () -> jumpHeld = false);
        bind("ENTER", "start", () -> { if (state == State.READY || state == State.OVER) start(); });
        bind("P", "pause", this::togglePause);
        bind("ESCAPE", "pauseEsc", this::togglePause);
        bind("R", "restart", () -> { if (state != State.READY) start(); });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (state == State.READY || state == State.OVER) start();
                else if (state == State.RUNNING) player.jump();
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    SwingUtilities.getWindowAncestor(this).addWindowFocusListener(new WindowAdapter() {
                        @Override public void windowLostFocus(WindowEvent event) {
                            jumpHeld = false;
                            if (state == State.RUNNING) { state = State.PAUSED; repaint(); }
                        }
                    });
                }
            }
        });
        timer = new javax.swing.Timer(16, e -> tick());
        timer.start();
    }
    private void bind(String key, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }
    private void start() {
        player.reset(); obstacles.clear(); elapsed = 0; distance = 0;
        spawnIn = 1.4; jumpHeld = false; state = State.RUNNING;
        previous = System.nanoTime(); repaint();
    }
    private void togglePause() {
        if (state == State.RUNNING) state = State.PAUSED;
        else if (state == State.PAUSED) { state = State.RUNNING; previous = System.nanoTime(); }
        repaint();
    }
    private int score() { return (int) (elapsed * 10); }
    private void tick() {
        long now = System.nanoTime();
        // Cap long stalls and substep physics to avoid tunneling through obstacles.
        double remaining = Math.min((now - previous) / 1_000_000_000.0, 0.05);
        previous = now;
        while (state == State.RUNNING && remaining > 0) {
            double dt = Math.min(remaining, 1.0 / 120);
            updateGame(dt); remaining -= dt;
        }
        repaint();
    }
    private void updateGame(double dt) {
        elapsed += dt;
        double speed = Math.min(530, 300 + elapsed * 3);
        distance += speed * dt;
        player.update(dt);
        spawnIn -= dt;
        if (spawnIn <= 0) {
            obstacles.add(new Obstacle(WIDTH + 30, 28 + random.nextInt(15), 35 + random.nextInt(27)));
            // Minimum 1.25 seconds between obstacles exceeds the ~0.72s jump.
            spawnIn = 1.25 + random.nextDouble() * 0.65;
        }
        for (Obstacle obstacle : obstacles) {
            obstacle.update(speed * dt);
            if (obstacle.bounds().intersects(player.bounds())) {
                state = State.OVER;
                if (score() > best) {
                    best = score();
                    if (preferences != null) {
                        try { preferences.putInt("best", best); preferences.flush(); }
                        catch (Exception ex) { savedBest = false; }
                    }
                }
                break;
            }
        }
        obstacles.removeIf(Obstacle::isOffscreen);
    }
    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        g.translate((getWidth() - WIDTH * scale) / 2, (getHeight() - HEIGHT * scale) / 2);
        g.scale(scale, scale);
        g.clipRect(0, 0, WIDTH, HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawWorld(g);
        for (Obstacle obstacle : obstacles) obstacle.draw(g);
        player.draw(g);
        text(g, "SKYLINE / RUNNER", 38, 45, 15, Font.BOLD, ink);
        text(g, "A LITTLE FURTHER, EVERY TIME.", 38, 66, 10, Font.PLAIN, muted);
        text(g, "SCORE", 695, 34, 10, Font.BOLD, muted);
        text(g, String.format("%05d", score()), 695, 62, 25, Font.BOLD, ink);
        text(g, "BEST", 833, 34, 10, Font.BOLD, muted);
        text(g, String.format("%05d", best), 833, 62, 25, Font.BOLD, new Color(95, 237, 202));
        text(g, "SPACE / UP  Jump       P / ESC  Pause       R  Restart", 38, 557, 12, Font.PLAIN, muted);
        text(g, "JAVA EDITION  /  01", 785, 557, 10, Font.BOLD, muted);
        if (state != State.RUNNING) drawOverlay(g);
        g.dispose();
    }
    private void drawWorld(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(14, 22, 41), 0, GROUND, new Color(37, 55, 78)));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(205, 223, 241, 130));
        for (int i = 0; i < 42; i++) {
            int x = (i * 137 + 59) % WIDTH, y = 90 + (i * 73) % 215;
            g.fillOval(x, y, i % 5 == 0 ? 3 : 2, 2);
        }
        g.setColor(new Color(230, 234, 220)); g.fillOval(757, 120, 63, 63);
        g.setColor(new Color(20, 31, 51));
        for (int i = -1; i < 12; i++) {
            int x = i * 100 - (int) (distance * 0.12 % 100);
            int height = 70 + Math.floorMod(i * 73, 140);
            g.fillRoundRect(x, GROUND - height, 75, height, 4, 4);
            g.setColor(new Color(106, 137, 158, 40));
            for (int j = 18; j < height - 10; j += 23) { g.fillRect(x + 14, GROUND - height + j, 8, 7); g.fillRect(x + 45, GROUND - height + j, 8, 7); }
            g.setColor(new Color(20, 31, 51));
        }
        g.setColor(new Color(26, 43, 62));
        for (int i = -1; i < 9; i++) {
            int x = i * 145 - (int) (distance * 0.28 % 145);
            int height = 45 + Math.floorMod(i * 43, 77);
            g.fillRect(x, GROUND - height, 104, height);
        }
        g.setColor(new Color(14, 25, 38)); g.fillRect(0, GROUND, WIDTH, HEIGHT - GROUND);
        g.setColor(new Color(95, 237, 202)); g.fillRect(0, GROUND, WIDTH, 3);
        g.setColor(new Color(66, 99, 111));
        for (int i = -1; i < 25; i++) g.fillRect(i * 50 - (int) (distance % 50), GROUND + 28, 20, 2);
        g.setColor(new Color(54, 72, 89)); g.drawLine(38, 519, WIDTH - 38, 519);
    }
    private void drawOverlay(Graphics2D g) {
        g.setColor(new Color(8, 15, 29, 190)); g.fillRect(0, 88, WIDTH, 421);
        g.setColor(new Color(27, 42, 62));
        g.fill(new RoundRectangle2D.Double(208, 145, 544, 310, 22, 22));
        String label = state == State.READY ? "ONE MORE JUMP. ONE MORE RUN." : state == State.PAUSED ? "TAKE A BREATHER" : "THAT WAS A GOOD RUN";
        centered(g, label, 191, 11, Font.BOLD, new Color(95, 237, 202));
        centered(g, state == State.READY ? "Chase the skyline." : state == State.PAUSED ? "On pause." : "Go a little further.", 246, 40, Font.BOLD, ink);
        centered(g, state == State.READY ? "Jump over barriers. Find your rhythm. Beat your best." : state == State.PAUSED ? "Your run is right where you left it." : "Score: " + score() + "     /     Best: " + best, 285, 14, Font.PLAIN, muted);
        g.setColor(new Color(95, 237, 202)); g.fillRoundRect(345, 315, 270, 49, 12, 12);
        centered(g, state == State.PAUSED ? "PRESS P TO RESUME" : state == State.READY ? "PRESS ENTER TO START" : "PRESS ENTER TO TRY AGAIN", 345, 12, Font.BOLD, new Color(15, 38, 44));
        centered(g, state == State.PAUSED ? "R starts a fresh run" : "SPACE / UP to jump  |  Click to start or jump", 393, 12, Font.PLAIN, muted);
        if (!savedBest) centered(g, "Best score available for this session only.", 427, 11, Font.PLAIN, muted);
    }
    private void text(Graphics2D g, String value, int x, int y, int size, int style, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, style, size)); g.setColor(color); g.drawString(value, x, y);
    }
    private void centered(Graphics2D g, String value, int y, int size, int style, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, style, size));
        int x = (WIDTH - g.getFontMetrics().stringWidth(value)) / 2;
        text(g, value, x, y, size, style, color);
    }
}
