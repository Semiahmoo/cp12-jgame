package tutorial.example4;

import jgame.*;
import jgame.platform.*;

/**
 * Tutorial example 4: tiles and collision. Like example 3, only the pac-mans
 * now collide with each other and with tiles. This example illustrates the use
 * of collision IDs (aka cids or colids). Both tiles and objects have collision
 * IDs, which can be used to check different kinds of collision between
 * different objects/tiles.
 */
public class Example4 extends StdGame {

	private static final int TILE_SIZE = 16;
	private static final int Y_TILES = 15;
	private static final int X_TILES = 20;
	private static final String BACKGROUND_IMAGE_NAME = "mybackground";
	private static final String pacmanImageSheet = "pacman_sheet";

	// game window is zoomed in 2.5x
	private static final double ZOOM = 2.5;

	public static void main(String[] args) {
		new Example4();
	}

	public Example4() {
		initEngine((int) (X_TILES * TILE_SIZE * ZOOM), (int) (Y_TILES * TILE_SIZE * ZOOM));
	}

	public void initCanvas() {
		// we set the background colour to same colour as the splash background
		setCanvasSettings(X_TILES, Y_TILES, TILE_SIZE, TILE_SIZE, JGColor.black, null, null);
	}

	public void initGame() {
		setFrameRate(35, 2);

		// "#" gets used to create a stone tile
		defineImage("mytile", "#", CollissionIds.BLOCK, "marble16.gif", "-");
		// "." gets used to skip a space
		defineImage("emptytile", ".", CollissionIds.NONE, "null", "-");

		defineImage(BACKGROUND_IMAGE_NAME, "-", CollissionIds.NONE, "twirly-192.gif", "-");
		setBGImage(BACKGROUND_IMAGE_NAME);

		// Load the 3 pacman images -- They're all in one file
		// starting from (0,0),
		// cut a 16x16 image
		// with 0x0 pixels as a gap between images
		defineImageMap(pacmanImageSheet, "munchie-r.gif", 0, 0, 16, 16, 0, 0);

		// split the sheet into 3 separate images (for the left direction)
		String left1 = Pacman.PACMAN_ANIMATE_LEFT + "1";
		String left2 = Pacman.PACMAN_ANIMATE_LEFT + "2";
		String left3 = Pacman.PACMAN_ANIMATE_LEFT + "3";

		defineImage(left1, "-", 0, pacmanImageSheet, 0, "-");
		defineImage(left2, "-", 0, pacmanImageSheet, 1, "-");
		defineImage(left3, "-", 0, pacmanImageSheet, 2, "-");

		// split the sheet into 3 separate images (for the right direction)
		// (and use "x" to flip horizontal)
		String right1 = Pacman.PACMAN_ANIMATE_RIGHT + "1";
		String right2 = Pacman.PACMAN_ANIMATE_RIGHT + "2";
		String right3 = Pacman.PACMAN_ANIMATE_RIGHT + "3";
		defineImage(right1, "-", 0, pacmanImageSheet, 0, "x");
		defineImage(right2, "-", 0, pacmanImageSheet, 1, "x");
		defineImage(right3, "-", 0, pacmanImageSheet, 2, "x");

		defineAnimation(Pacman.PACMAN_ANIMATE_LEFT, new String[] { left1, left2, left3 }, 0.3, true);
		defineAnimation(Pacman.PACMAN_ANIMATE_RIGHT, new String[] { right1, right2, right3 }, 0.3, true);

		// create some game objects
		for (int i = 0; i < 20; i++) {
			double x = random(3 * 16, pfWidth() - 4 * 16);
			double y = random(3 * 16, pfHeight() - 4 * 16);
			new Pacman(this, x, y);
		}
		// create some tiles. "#" is our marble tile, "." is an empty space.
		setTiles(2, // tile x index
				2, // tile y index
				new String[] { "#####", "#", "#", "#" }
		// A series of tiles. Each String represents a line of tiles.
		);
		setTiles(13, 2, new String[] { "#####", "....#", "....#", "....#" });
		setTiles(13, 9, new String[] { "....#", "....#", "....#", "#####" });
		setTiles(2, 9, new String[] { "#....", "#....", "#....", "#####" });
		// define the off-playfield tiles
		setTileSettings("#", // tile that is found out of the playfield bounds
				CollissionIds.BLOCK, // tile cid found out of playfield bounds
				0 // which cids to preserve when setting a tile (not used here).
		);

	}

	public void doFrame() {
		moveObjects(null, 0);
		// check object-object collision
		// ie: Pacman collides with Pacman
		checkCollision(CollissionIds.PACMAN, CollissionIds.PACMAN);

		// check object-tile collision
		// ie: Pacman collides with the blocks
		checkBGCollision(CollissionIds.BLOCK, CollissionIds.PACMAN);
	}

	@Override
	public void paintFrame() {
		super.paintFrame();
	}

}
