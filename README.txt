SKYLINE RUNNER - Java desktop game

A small endless runner with hand-drawn Java2D graphics, scrolling skyline,
increasing difficulty, collision detection, pause, restart, and a saved best score.
No images, external libraries, network connection, or paid tools are needed.

REQUIREMENT
Install Java version 17 or newer to play the included SkylineRunner.jar.
Make sure java is available on your PATH.
To edit and rebuild the source, use a Java Development Kit (JDK) 17 or newer.
Java downloads: https://adoptium.net/temurin/releases/

WINDOWS
Extract this entire folder and double-click Run-Game.bat.
You can also run: java -jar SkylineRunner.jar

MANUAL BUILD (from this folder)
  mkdir build
  javac -encoding UTF-8 -d build Main.java GamePanel.java Player.java Obstacle.java
  java -cp build Main

CONTROLS
  Enter       Start or try again after game over
  Space / Up  Jump (release before jumping again)
  P / Escape  Pause or resume
  R           Restart a run
  Mouse click Start or jump

Avoid the coral-colored barriers. Score increases while playing; paused time
does not count. Speed increases gradually and is capped. Losing window focus
pauses the game automatically. Resizing preserves the game's proportions.

Your best score is saved using Java Preferences, outside this folder in the
current user's application preferences. If saving is unavailable, the game
continues with a session-only best score.

This is a Java desktop game. GitHub Pages cannot run it directly in a browser.

FILES
  Main.java       Window and application entry point
  GamePanel.java  Game loop, controls, scenery, menus, score, and state
  Player.java     Player drawing, jump physics, and collision bounds
  Obstacle.java   Obstacle movement, drawing, and collision bounds
