package tutorial.example4;

import jgame.JGObject;

/** Our user-defined object, which now bounces off other object and tiles. */
public class Pacman extends JGObject {

	private final Example4 game;
	private int lives;
	public static final String PACMAN_ANIMATE_LEFT = "PacAnim_l";
	public static final String PACMAN_ANIMATE_RIGHT = "PacAnim_r";
	private static final String PACMAN_NAME = "pac";

	public Pacman(Example4 game, double x, double y) {
		super(PACMAN_NAME, true, x, y, CollissionIds.PACMAN, PACMAN_ANIMATE_LEFT);
		this.game = game;
		xspeed = random(-2, 2);
		yspeed = random(-2, 2);
		lives = (int) random(10, 25);
	}

	/** Update the object. This method is called by moveObjects. */
	public void move() {
		if (xspeed < 0)
			setGraphic(PACMAN_ANIMATE_LEFT);
		else
			setGraphic(PACMAN_ANIMATE_RIGHT);
		// We don't need to check for the borders of the screen, like
		// we did in example 3. Border collision is now handled by hit_bg.
	}

	/**
	 * Handle collision with background. Called by checkBGCollision. Tilecid is the
	 * combined (ORed) CID of all tiles that this object collides with. Note: there
	 * are two other variants of hit_bg available, namely one passing tilecid plus
	 * tile coordinates for each tile that the object collides with, and one passing
	 * the tile range that the object overlaps with at the moment of collision.
	 */
	public void hit_bg(int tilecid) {
		// Look around to see which direction is free. If we find a free
		// direction, move that way.
		if (!and(checkBGCollision(-xspeed, yspeed), 3)) {
			xspeed = -xspeed;
		} else if (!and(checkBGCollision(xspeed, -yspeed), 3)) {
			yspeed = -yspeed;
		} else if (!and(checkBGCollision(xspeed, -yspeed), 3) && !and(checkBGCollision(-xspeed, -yspeed), 3)) {
			xspeed = -xspeed;
			yspeed = -yspeed;
		}
		// else do nothing. You might think this case never occurs
		// (otherwise, why would the object have collided?), but it
		// does occur because object-object collision might already
		// have reversed the direction of this object. This is the kind
		// of stuff that makes object interaction difficult.
	}

	/** Handle collision with other objects. Called by checkCollision. */
	public void hit(JGObject obj) {
		// As a reaction to an object collision, we bounce in the
		// direction we came from. We only do this when the area in that
		// direction seems clear of other objects, otherwise we might
		// start oscillating back and forth.
		// This collision problem is much more difficult than the tile
		// collision problem, because there may be multiple simultaneous
		// collisions, and the other objects are also moving at different
		// speeds.
		// We look ahead several steps in the opposite direction to see
		// if any other object is there.
		if (checkCollision(CollissionIds.PACMAN, -3 * xspeed, -3 * yspeed) == 0) {
			// reverse direction
			xspeed = -xspeed;
			yspeed = -yspeed;
			lives--;
			
			Pacman other = (Pacman) obj;
			other.lives--;

			checkDeath(this);
			checkDeath(other);
		}
	}

	private static void checkDeath(Pacman p) {
		if (p.lives <= 0) {
			p.remove();
		}
	}

	@Override
	public void paint() {
		super.paint();
		game.drawString("" + lives, x + 8, y + 8, 0);
	}

}