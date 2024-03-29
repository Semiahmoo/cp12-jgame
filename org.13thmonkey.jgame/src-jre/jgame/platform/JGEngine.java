package jgame.platform;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.vecmath.Point3d;

import ca.hapke.controller.data.DataReceive;
import ca.hapke.controller.data.IDataReceiveListener;
import ca.hapke.controller.serial.ConnectedSerialPort;
import ca.hapke.controller.serial.SerialCommManager;
import ca.hapke.controller.serial.SerialDataReceive;
import ca.hapke.controller.udp.UdpDataReceive;
import ca.hapke.gyro.GyroDataUpdater;
import ca.hapke.gyro.api.GyroStatus;
import ca.hapke.gyro.data.AbstractGyroDataType;
import ca.hapke.gyro.data.AdcDataType;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.gyro.data.DataType.InputType;
import ca.odell.glazedlists.EventList;
import jgame.JGColor;
import jgame.JGFont;
import jgame.JGImage;
import jgame.JGObject;
import jgame.JGPoint;
import jgame.JGRectangle;
import jgame.JGTimer;
import jgame.impl.AccelGyroMode;
import jgame.impl.Animation;
import jgame.impl.EngineLogic;
import jgame.impl.JGEngineInterface;
import jgame.impl.JGameError;

/**
 * Contains the main functionality of the game engine. Subclass it to write your
 * own game. The engine can be run as applet, application, or midlet. To run as
 * an application, have your main() construct an instance of your subclass
 * (using a constructor other than the zero-parameter one). Then call
 * initEngine(), with the desired window size, which will open a window, and
 * initialise. To run as an applet or midlet, ensure that your subclass has a
 * parameterless constructor which enables the browser to create it as an
 * applet. Within this constructor, you call initEngineApplet() to initialise.
 * After initialisation, initCanvas() will be called, within which you can call
 * setCanvasSettings and setScalingPreferences. Note that applet parameters will
 * only become available at this point, and not earlier.
 * 
 * <P>
 * Upon initialisation, the engine will create a new thread in which all game
 * action will run. First it calls initGame() in your subclass. Here, you can
 * define any initialisation code, such as variable initialisation and image
 * loading. You can configure the load screen using setProgressBar,
 * setProgressMessage, and setAuthorMessage, and by defining splash_image. The
 * progress bar is set automatically when defineMedia is loading a table. Then,
 * the engine will start producing frames. Each frame it will call doFrame() in
 * your subclass, where you can call engine functions to move the game objects,
 * check collisions, and everything else you want to do in a frame. Then, it
 * will draw the current frame on the screen, after which it calls paintFrame(),
 * where you can do any customised drawing on the frame, such as status
 * messages. The engine enables you to specify <i>states</i> that the game may
 * be in, say, the title screen state or the in-game state. When a state is set,
 * the engine will first try to call start<i>&lt;state&gt;</i>() once at the
 * beginning of the next frame. Then it will try to call additional
 * doFrame<i>&lt;state&gt;</i>() and paintFrame<i>&lt;state&gt;</i>() methods
 * every frame, where you can handle the game action for that specific state.
 * Multple states may be set simultaneously, resulting in multiple method calls
 * for each frame. Due to absence of the reflection framework, MIDlets do not
 * support arbitrary state names, but only the following state names:
 * "StartGame", "LevelDone", "InGame", "Title", "Highscore", "EnterHighscore",
 * "LifeLost", "GameOver", "Paused".
 * 
 * <P>
 * The engine manages a list of sprite objects, and a matrix of equal-sized
 * tiles. Its primary way to draw graphics is images. Images may be loaded from
 * Gifs, JPegs, or PNGs (PNGs not in java 1.2, and GIFs are not always supported
 * in MIDP, not even on new phones), which may contain either single images, or
 * regularly spaced arrays or matrices of images (i.e. sprite sheets, we will
 * call them image maps). An image is identified by a unique name, and is
 * defined by the image file it comes from, the index within that file in case
 * the file is an image map, and any flip and rotate actions that are to be be
 * done on the file image to arrive at the final image. Animations may be
 * defined in terms of sequences of images. Image maps, images, and animations
 * can be defined by calling resp. defineImageMap, defineImage, or
 * defineAnimation, or the definitions can be loaded from a text-file table file
 * using defineImages().
 * 
 * <P>
 * Objects are of the class JGObject; see this class for more information.
 * Objects are identified within the engine by unique String identifiers.
 * Creating a new object with the same identifier as an old object will replace
 * the old one. Objects are drawn on the screen in lexical order. The main
 * methods for handling objects are moveObjects(), checkCollision(), and
 * checkBGCollision().
 * 
 * <P>
 * Tiles can be used to define a background that the sprite objects can interact
 * with, using a set of shorthands for reading, writing, and colliding with
 * tiles. Tiles are uniquely identified by a short string (1-4 characters).
 * Tiles may be transparent; a decorative background image can be displayed
 * behind the tiles.
 * 
 * <P>
 * Collision is done by assigning collision IDs (cids) to both objects and
 * tiles. A cid is basically a bit string (usually, one allocates 1 bit per
 * basic object or tile type). Collision is done by specifying that a certain
 * set of object types should be notified of collision with a certain set of
 * object or tile types. Such a set is specified by a bit mask, which matches a
 * cid when cid&amp;mask != 0. JGObject defines collision methods (hit and
 * hit_bg) which can then be implemented to handle the collisions. Objects have
 * two bounding boxes: one for object-object collision (the sprite bounding box,
 * or simply bounding box), and one for object-tile collision (the tile bounding
 * box). The two bounding boxes may be dependent on the image (typically, you
 * want these bounding boxes to approximate the shape of each individual image).
 * For this purpose, you can define a collision bounding box with each image
 * definition. This image bounding box is the default bounding box used by the
 * object, but both sprite bbox and tile bbox can be overridden separately.
 * Often, you want to set the tile bbox to a constant value, such as equal to
 * the size of 1 tile.
 * 
 * <P>
 * Scrolling is done by defining a playfield that's larger than the game window,
 * using setPFSize(). The game window (or view) can then be panned across the
 * playfield using setViewOffset(). The game objects always move relative to the
 * playfield. The draw methods take coordinates either relative to the playfield
 * or to the view.
 * 
 * <P>
 * The engine supports arbitrary runtime scaling of the playfield. This means
 * that you can code your game for a specific "virtual" screen size, and have it
 * scale to any other screen size by simply supplying the desired "real" screen
 * size at startup. Applets will scale to the size as given by the width and
 * height fields in the HTML code. Midlets will scale to fit the available
 * canvas. The scale factor will in no way affect the behaviour of the game
 * (except performance-wise). Scaling is done using an anti-aliasing algorithm.
 * Sometimes however, graphics may look a bit blocky or jaggy, because the
 * engine uses a translucency-free scaling algorithm to ensure good performance.
 * 
 * <p>
 * The engine supports keyboard and mouse input. The state of the keyboard is
 * maintained in a keymap, which can be read by getKey. The mouse can be read
 * using getMouseButton and getMouseX/Y. The mouse buttons also have special
 * keycodes. In MIDP, touch screens will generate mouse events. Button 1 is
 * pressed when the user touches the screen. The mouse coordinates change only
 * when the user touches or swipes the screen.
 * 
 * <p>
 * Sound clips can be loaded using defineAudio or by appropriate entries in a
 * table loaded by defineMedia. playAudio and stopAudio can be used to control
 * clip playing. enableAudio and disableAudio can be used to globally turn audio
 * on and off for the entire application. In MIDP, sound always has only one
 * channel, so the channel parameter is ignored. Due to limitations in typical
 * MIDP implementations, playing a new sound while a sound is already playing
 * will not trigger the new sound, but the old sound will keep playing.
 * 
 * <p>
 * A game speed variable is used to determine the update speed of velocities and
 * timers. Game speed can be adapted by calling setGameSpeed, or is adapted
 * automatically in video synced frame rate mode (as set by
 * setVideoSyncedUpdate).
 * 
 * <p>
 * User statistics, game progress, and basic save game information can be stored
 * using the store...() API. It enables persistent storage of numbers and
 * Strings. It works with both MIDP and JRE, but JRE applets will need to be
 * signed because persistent storage requires access to the local file system.
 * 
 * <p>
 * The opts... methods provide a standard options menu API. When an option is
 * defined, a standard option menu item is created which is directly linked to a
 * persistent storage variable. Currently implemented on Android only.
 * 
 * <p>
 * Upon initialisation, the engine shows an initialisation screen with a
 * progress bar that reflects progress on the current graphics table being
 * loaded. A splash image can be displayed during the init screen, by defining
 * an image called splash_image. As soon as this image is defined, it is
 * displayed above the progress message. Typically, one defines splash_image at
 * the beginning of the graphics table, so that it displays as soon as possible
 * during the init screen.
 * 
 * <p>
 * JGame applications can be quit by pressing Shift-Esc.
 * 
 * <p>
 * JGame has a number of debug features. It is possible to display the game
 * state and the JGObjects' bounding boxes. There is also the possibility to
 * output debug messages, using dbgPrint. A debug message is associated with a
 * specific source (either a game object or the mainloop). Generated exceptions
 * will generally be treated as debug messages. The messages can be printed on
 * the playfield, next to the objects that output them. See dbgSetMessagesInPf
 * for more information. Debug facilities are not yet implemented in MIDP, so
 * most debug calls are ignored, except dbgPrint and dbgShowException, which
 * print to stdout.
 * 
 */
public abstract class JGEngine extends Applet implements JGEngineInterface {
	private static final long serialVersionUID = -7811960246934582983L;

	JREImage imageutil = new JREImage();

	EngineLogic el = new EngineLogic(imageutil, true, true);

	JREEngine jre = new JREEngine(el, this);
	// private JGListener listener = new JGListener();

	/** Path from where files can be loaded; null means not initialised yet */
	private Thread thread = null;
	JGCanvas canvas = null;

	/** Should engine thread run or halt? Set by start() / stop() */
	boolean running = true;

	/**
	 * True means the game is running as an applet, false means application.
	 */
	boolean i_am_applet = false;

	/// ** Keycode of cursor key. */
	// public static final int KeyUp=38,KeyDown=40,KeyLeft=37,KeyRight=39;
	// public static final int KeyShift=16;
	// public static final int KeyCtrl=17;
	// public static final int KeyAlt=18;
	// public static final int KeyEsc=27;
	// public static final int KeyEnter=10;
	public static final int KeyBackspace = KeyEvent.VK_BACK_SPACE;
	public static final int KeyTab = KeyEvent.VK_TAB;
	/// ** Keymap equivalent of mouse button. */
	// public static final int KeyMouse1=256, KeyMouse2=257, KeyMouse3=258;

	protected Graphics buf_gfx = null;

	/* images */

	/* screen state */
	Image background = null, buffer = null;

	Graphics bgg = null;

	/* ===== Accelerometer stuff... By Mr. Hapke */

//	private static final int ACCEL_AXIS = 0;
//	private static final int GYRO_AXIS = 1;
//	private static final int GYRO_ANGLE_AXIS = 1;
	private static final InputType DATA_CLUSTER_IN_USE = InputType.JavaGyro;

	private AccelGyroMode accelGyroMode = AccelGyroMode.None;
	protected final DataCluster cluster = new DataCluster();
	// TODO Make this smarter based on the incoming data
	protected final AbstractGyroDataType accelGyroStatus = (AbstractGyroDataType) cluster.getData(DATA_CLUSTER_IN_USE);
	protected final AdcDataType joystickStatus = (AdcDataType) cluster.getData(InputType.ADC);

	// USB->SERIAL.COMM MANAGEMENT
	protected boolean displaySerialInfo = false;
	protected boolean displayAngleInfo = false;
	protected int displayAngleInfoX = 350;
	protected int displayAngleInfoY = 20;
	protected int key_gyro_display = 61; // '=' key
	protected int key_serial_activate = 92; // '\' key
	protected int key_serial_choose_port = 93; // ']' key
	protected int key_serial_on_off = 91; // '[' key
	protected static final int SERIAL_OFF = 0;
	protected static final int SERIAL_SCANNING = 1;
	protected static final int SERIAL_ON = 2;

	protected SerialCommManager serialManager = new SerialCommManager();
	protected EventList<String> portsEvents = serialManager.getPortsEvents();
	protected ConnectedSerialPort serialConnection = null;

	protected String serialPortActive = "N/A";
	protected int serialMode = SERIAL_OFF;
	protected int portIndex = 0;

	/* ====== images ====== */

	@Override
	public JGImage getImage(String imgname) {
		return el.getImage(imgname);
	}

	@Override
	public JGPoint getImageSize(String imgname) {
		return el.getImageSize(imgname);
	}

	@Override
	public void defineImage(String name, String tilename, int collisionid, String imgfile, String img_op, int top,
			int left, int width, int height) {
		el.defineImage(this, name, tilename, collisionid, imgfile, img_op, top, left, width, height);
	}

	@Override
	public void defineImage(String imgname, String tilename, int collisionid, String imgfile, String img_op) {
		el.defineImage(this, imgname, tilename, collisionid, imgfile, img_op);
	}

	@Override
	public void defineImage(String imgname, String tilename, int collisionid, String imgmap, int mapidx, String img_op,
			int top, int left, int width, int height) {
		el.defineImage(imgname, tilename, collisionid, imgmap, mapidx, img_op, top, left, width, height);
	}

	@Override
	public void defineImage(String imgname, String tilename, int collisionid, String imgmap, int mapidx,
			String img_op) {
		el.defineImage(imgname, tilename, collisionid, imgmap, mapidx, img_op);
	}

	@Override
	public void defineImageRotated(String name, String tilename, int collisionid, String srcname, double angle) {
		el.defineImageRotated(this, name, tilename, collisionid, srcname, angle);
	}

	@Override
	public void defineImageFromData(String name, String tilename, int collisionid, int imgwidth, int imgheight,
			int[] imgdata, int imgdataofs, int linewidth, String img_op, int top, int left, int width, int height) {
		el.defineImageFromData(name, tilename, collisionid, imgwidth, imgheight, imgdata, imgdataofs, linewidth, img_op,
				top, left, width, height);
	}

	@Override
	public void defineImageMap(String mapname, String imgfile, int xofs, int yofs, int tilex, int tiley, int skipx,
			int skipy) {
		el.defineImageMap(this, mapname, imgfile, xofs, yofs, tilex, tiley, skipx, skipy);
	}

	@Override
	public JGRectangle getImageBBox(String imgname) {
		return el.getImageBBox(imgname);
	}

	@Override
	public void defineMedia(String filename) {
		el.defineMedia(this, filename);
	}

	/* ====== PF/view ====== */

	/* ====== objects from canvas ====== */

	@Override
	public void markAddObject(JGObject obj) {
		el.markAddObject(obj);
	}

	@Override
	public boolean existsObject(String index) {
		return el.existsObject(index);
	}

	@Override
	public JGObject getObject(String index) {
		return el.getObject(index);
	}

	@Override
	public void moveObjects(String prefix, int cidmask) {
		el.moveObjects(this, prefix, cidmask);
	}

	@Override
	public void moveObjects() {
		el.moveObjects(this);
	}

	@Override
	public void checkCollision(int srccid, int dstcid) {
		el.checkCollision(this, srccid, dstcid);
	}

	@Override
	public int checkCollision(int cidmask, JGObject obj) {
		return el.checkCollision(cidmask, obj);
	}

	@Override
	public int checkBGCollision(JGRectangle r) {
		return el.checkBGCollision(r);
	}

	@Override
	public void checkBGCollision(int tilecid, int objcid) {
		el.checkBGCollision(this, tilecid, objcid);
	}

	/* objects from engine */

	@Override
	public Vector<JGObject> getObjects(String prefix, int cidmask, boolean suspended_obj, JGRectangle bbox) {
		return el.getObjects(prefix, cidmask, suspended_obj, bbox);
	}

	@Override
	public void removeObject(JGObject obj) {
		el.removeObject(obj);
	}

	@Override
	public void removeObjects(String prefix, int cidmask) {
		el.removeObjects(prefix, cidmask);
	}

	@Override
	public void removeObjects(String prefix, int cidmask, boolean suspended_obj) {
		el.removeObjects(prefix, cidmask, suspended_obj);
	}

	@Override
	public int countObjects(String prefix, int cidmask) {
		return el.countObjects(prefix, cidmask);
	}

	@Override
	public int countObjects(String prefix, int cidmask, boolean suspended_obj) {
		return el.countObjects(prefix, cidmask, suspended_obj);
	}

	void drawObject(Graphics g, JGObject o) {
		if (!o.is_suspended) {
			// o.prepareForFrame();
			drawImage(g, (int) o.x, (int) o.y, o.getImageName(), true);
			try {
				o.paint();
			} catch (JGameError ex) {
				exitEngine(dbgExceptionToString(ex));
			} catch (Exception e) {
				dbgShowException(o.getName(), e);
			}
		}
		// note that the debug bbox of suspended objects will be visible
		if ((debugflags & JGEngine.BBOX_DEBUG) != 0) {
			setColor(g, el.fg_color);
			JGRectangle bbox = o.getBBox();
			if (bbox != null) { // bounding box defined
				// bbox.x -= xofs;
				// bbox.y -= yofs;
				bbox = el.scalePos(bbox, true);
				g.drawRect(bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
			}
			bbox = o.getTileBBox();
			if (bbox != null) { // tile bounding box defined
				// bbox.x -= xofs;
				// bbox.y -= yofs;
				bbox = el.scalePos(bbox, true);
				g.drawRect(bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
				setColor(g, debug_auxcolor1);
				bbox = o.getTileBBox();
				bbox = getTiles(bbox);
				bbox.setX(bbox.getX() * el.tilex);
				bbox.setY(bbox.getY() * el.tiley);
				// bbox.x -= xofs;
				// bbox.y -= yofs;
				bbox.setWidth(bbox.getWidth() * el.tilex);
				bbox.setHeight(bbox.getHeight() * el.tiley);
				bbox = el.scalePos(bbox, true);
				g.drawRect(bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
				setColor(g, debug_auxcolor2);
				bbox = o.getCenterTiles();
				bbox.setX(bbox.getX() * el.tilex);
				bbox.setY(bbox.getY() * el.tiley);
				// bbox.x -= xofs;
				// bbox.y -= yofs;
				bbox.setWidth(bbox.getWidth() * el.tilex);
				bbox.setHeight(bbox.getHeight() * el.tiley);
				bbox = el.scalePos(bbox, true);
				g.drawRect(bbox.getX() + 2, bbox.getY() + 2, bbox.getWidth() - 4, bbox.getHeight() - 4);
			}
		}
		// o.frameFinished();
	}

	/* ====== BG/tiles ====== */

	@Override
	public void setBGImage(String bgimg) {
		el.setBGImage(bgimg, 0, true, true);
	}

	@Override
	public void setBGImage(int depth, String bgimg, boolean wrapx, boolean wrapy) {
		el.setBGImage(bgimg, depth, wrapx, wrapy);
	}

	@Override
	public void setTileSettings(String out_of_bounds_tile, int out_of_bounds_cid, int preserve_cids) {
		el.setTileSettings(out_of_bounds_tile, out_of_bounds_cid, preserve_cids);
	}

	@Override
	public void fillBG(String filltile) {
		el.fillBG(filltile);
	}

	@Override
	public void setTileCid(int x, int y, int and_mask, int or_mask) {
		el.setTileCid(x, y, and_mask, or_mask);
	}

	@Override
	public void setTile(int x, int y, String tilestr) {
		el.setTile(x, y, tilestr);
	}

	void setColor(Graphics g, JGColor col) {
		col.impl = new Color(col.r, col.g, col.b);
		g.setColor((Color) col.impl);
	}

	/**
	 * xi,yi are tile indexes relative to the tileofs, that is, the top left of the
	 * bg, + 1. They must be within both the tilemap and the view.
	 */
	@Override
	public void drawTile(int xi, int yi, int tileid) {
		if (background == null)
			return;
		// determine position within bg
		int x = el.moduloFloor(xi + 1, el.viewnrtilesx + 3) * el.scaledtilex;
		int y = el.moduloFloor(yi + 1, el.viewnrtilesy + 3) * el.scaledtiley;
		// draw
		if (bgg == null)
			bgg = background.getGraphics();
		Integer tileid_obj = new Integer(tileid);
		JREImage img = (JREImage) el.getTileImage(tileid_obj);
		// define background behind tile in case the tile is null or
		// transparent.
		if (img == null || el.images_transp.containsKey(tileid_obj)) {
			EngineLogic.BGImage bg_image = (EngineLogic.BGImage) el.bg_images.get(0);
			if (bg_image == null) {
				setColor(bgg, el.bg_color);
				bgg.fillRect(x, y, el.scaledtilex, el.scaledtiley);
			} else {
				int xtile = el.moduloFloor(xi, bg_image.tiles.x);
				int ytile = el.moduloFloor(yi, bg_image.tiles.y);
				bgg.drawImage(((JREImage) el.getImage(bg_image.imgname)).img, x, y, x + el.scaledtilex,
						y + el.scaledtiley, xtile * el.scaledtilex, ytile * el.scaledtiley,
						(xtile + 1) * el.scaledtilex, (ytile + 1) * el.scaledtiley, (Color) el.bg_color.impl, null);
			}
		}
		if (img != null)
			bgg.drawImage(img.img, x, y, this);
		// System.out.println("Drawn tile"+tileid);
	}

	@Override
	public int countTiles(int tilecidmask) {
		return el.countTiles(tilecidmask);
	}

	@Override
	public int getTileCid(int xidx, int yidx) {
		return el.getTileCid(xidx, yidx);
	}

	@Override
	public String getTileStr(int xidx, int yidx) {
		return el.getTileStr(xidx, yidx);
	}

	@Override
	public int getTileCid(JGRectangle tiler) {
		return el.getTileCid(tiler);
	}

	@Override
	public JGRectangle getTiles(JGRectangle r) {
		return el.getTiles(r);
	}

	@Override
	public boolean getTiles(JGRectangle dest, JGRectangle r) {
		return el.getTiles(dest, r);
	}

	@Override
	public void setTileCid(int x, int y, int value) {
		el.setTileCid(x, y, value);
	}

	@Override
	public void orTileCid(int x, int y, int or_mask) {
		el.orTileCid(x, y, or_mask);
	}

	@Override
	public void andTileCid(int x, int y, int and_mask) {
		el.andTileCid(x, y, and_mask);
	}

	@Override
	public void setTile(JGPoint tileidx, String tilename) {
		el.setTile(tileidx, tilename);
	}

	@Override
	public void setTiles(int xofs, int yofs, String[] tilemap) {
		el.setTiles(xofs, yofs, tilemap);
	}

	@Override
	public void setTilesMulti(int xofs, int yofs, String[] tilemap) {
		el.setTilesMulti(xofs, yofs, tilemap);
	}

	@Override
	public int getTileCidAtCoord(double x, double y) {
		return el.getTileCidAtCoord(x, y);
	}

	@Override
	public int getTileCid(JGPoint center, int xofs, int yofs) {
		return el.getTileCid(center, xofs, yofs);
	}

	@Override
	public String getTileStrAtCoord(double x, double y) {
		return el.getTileStrAtCoord(x, y);
	}

	@Override
	public String getTileStr(JGPoint center, int xofs, int yofs) {
		return el.getTileStr(center, xofs, yofs);
	}

	@Override
	public int tileStrToID(String tilestr) {
		return el.tileStrToID(tilestr);
	}

	@Override
	public String tileIDToStr(int tileid) {
		return el.tileIDToStr(tileid);
	}

	void copyBGToBuf(Graphics bufg, int sx1, int sy1, int sx2, int sy2, int dx1, int dy1) {
		// System.out.println("("+sx1+","+sy1+")-("+sx2+","+sy2+")");
		if (sx2 <= sx1 || sy2 <= sy1)
			return;
		int barrelx = el.scaleXPos(el.moduloFloor(el.xofs, el.tilex), false);
		int barrely = el.scaleYPos(el.moduloFloor(el.yofs, el.tiley), false);
		int barreldx = (sx1 == 0) ? barrelx : 0;
		int barreldy = (sy1 == 0) ? barrely : 0;
		barrelx = (sx1 == 0) ? 0 : barrelx;
		barrely = (sy1 == 0) ? 0 : barrely;
		int dx2 = dx1 + sx2 - sx1;
		int dy2 = dy1 + sy2 - sy1;
		bufg.drawImage(background, dx1 * el.scaledtilex - barreldx, dy1 * el.scaledtiley - barreldy,
				dx2 * el.scaledtilex - barreldx, dy2 * el.scaledtiley - barreldy, barrelx + sx1 * el.scaledtilex,
				barrely + sy1 * el.scaledtiley, barrelx + sx2 * el.scaledtilex, barrely + sy2 * el.scaledtiley, this);
	}

	/* ====== math ====== */

	@Override
	public double moduloXPos(double x) {
		return el.moduloXPos(x);
	}

	@Override
	public double moduloYPos(double y) {
		return el.moduloYPos(y);
	}

	@Override
	public void setProgressBar(double pos) {
		canvas.setProgressBar(pos);
	}

	@Override
	public void setProgressMessage(String msg) {
		canvas.setProgressMessage(msg);
	}

	@Override
	public void setAuthorMessage(String msg) {
		canvas.setAuthorMessage(msg);
	}

	/**
	 * JGCanvas is internally used by JGEngine for updating and drawing objects and
	 * tiles, and handling keyboard/mouse events.
	 */
	class JGCanvas extends Canvas {
		private static final long serialVersionUID = 4304781400471635577L;

		// part of the "official" method of handling keyboard focus
		@Override
		public boolean isFocusTraversable() {
			return true;
		}

		/* ====== init stuff ====== */

		public JGCanvas(int winwidth, int winheight) {
			super();
			setSize(winwidth, winheight);
		}

		/**
		 * Determines whether repaint will show the game graphics or do nothing.
		 */
		boolean is_initialised = false;
		/**
		 * paint interface that is used when the canvas is not initialised (for
		 * displaying status info while starting up, loading files, etc.
		 */
		private ListCellRenderer<Graphics> initpainter = null;
		String progress_message = "Please wait, loading files .....";
		String author_message = "JGame " + JGameVersionString;
		/** for displaying progress bar, value between 0.0 - 1.0 */
		double progress_bar = 0.0;

		void setInitialised() {
			is_initialised = true;
			initpainter = null;
		}

		void setInitPainter(ListCellRenderer<Graphics> painter) {
			initpainter = painter;
		}

		void setProgressBar(double pos) {
			progress_bar = pos;
			if (!is_initialised && initpainter != null)
				repaint(100);
		}

		void setProgressMessage(String msg) {
			progress_message = msg;
			if (!is_initialised && initpainter != null)
				repaint(100);
		}

		void setAuthorMessage(String msg) {
			author_message = msg;
			if (!is_initialised && initpainter != null)
				repaint(100);
		}

		/* ====== paint ====== */

		/**
		 * Don't call directly. Use repaint().
		 */
		@Override
		public void update(Graphics g) {
			paint(g);
		}

		/**
		 * Don't call directly. Use repaint().
		 */
		@Override
		public void paint(Graphics g) {
			try {
				if (el.is_exited) {
					paintExitMessage(g);
					return;
				}
				if (!is_initialised) {
					if (initpainter != null) {
						// if (buffer==null) {
						// buffer=createImage(width,height);
						// }
						// if (incremental_repaint) {
						// initpainter.getListCellRendererComponent(null,
						// buffer.getGraphics(),0,true,false);
						// g.drawImage(buffer,0,0,this);
						// } else {
						initpainter.getListCellRendererComponent(null, getGraphics(), 0, false, false);
						// }
					}
					return;
				}
				/*
				 * Each frame before the paint operation, we check if our possibly volatile bg
				 * and buffer are still ok. If so, we don't re-validate every time, but leave
				 * them as persistent images until their contents are eventually destroyed. Then
				 * we always recreate them even if their status is RESTORED. Note that bg is
				 * indeed persistent, and things are incrementally drawn on it during the course
				 * of doFrames and paints. If the buffer to be rendered to screen is invalid
				 * when we render it to screen, we give up for this frame and don't retry until
				 * the next frame.
				 */
				if (background == null || !JREImage.isScratchImageValid(background)) {
					background = JREImage.createScratchImage(el.width + 3 * el.scaledtilex,
							el.height + 3 * el.scaledtiley);
					el.invalidateBGTiles();
				}
				if (buffer == null || !JREImage.isScratchImageValid(buffer)) {
					buffer = JREImage.createScratchImage(el.width, el.height);
				}
				if (buffer != null && background != null) {
					// block update thread
					synchronized (el.objects) {
						// paint any part of bg which is not yet defined
						el.repaintBG(JGEngine.this);
						/* clear buffer */
						Graphics bufg = buffer.getGraphics();
						buf_gfx = bufg; // enable objects to draw on buffer gfx.
						// bufg.setColor(getBackground());
						// draw background to buffer
						// bufg.drawImage(background,-scaledtilex,-scaledtiley,this);
						int tilexshift = el.moduloFloor(el.tilexofs + 1, el.viewnrtilesx + 3);
						int tileyshift = el.moduloFloor(el.tileyofs + 1, el.viewnrtilesy + 3);
						int sx1 = tilexshift + 1;
						int sy1 = tileyshift + 1;
						int sx2 = el.viewnrtilesx + 3;
						int sy2 = el.viewnrtilesy + 3;
						if (sx2 - sx1 > el.viewnrtilesx)
							sx2 = sx1 + el.viewnrtilesx;
						if (sy2 - sy1 > el.viewnrtilesy)
							sy2 = sy1 + el.viewnrtilesy;
						int bufmidx = sx2 - sx1;
						int bufmidy = sy2 - sy1;
						copyBGToBuf(bufg, sx1, sy1, sx2, sy2, 0, 0);
						sx1 = 0;
						sy1 = 0;
						sx2 = tilexshift - 1;
						sy2 = tileyshift - 1;
						copyBGToBuf(bufg, sx1, sy1, sx2, sy2, bufmidx, bufmidy);
						sx1 = 0;
						sy1 = tileyshift + 1;
						sx2 = tilexshift - 1;
						sy2 = el.viewnrtilesy + 3;
						if (sy2 - sy1 > el.viewnrtilesy)
							sy2 = sy1 + el.viewnrtilesy;
						copyBGToBuf(bufg, sx1, sy1, sx2, sy2, bufmidx, 0);
						sx1 = tilexshift + 1;
						sy1 = 0;
						sx2 = el.viewnrtilesx + 3;
						sy2 = tileyshift - 1;
						if (sx2 - sx1 > el.viewnrtilesx)
							sx2 = sx1 + el.viewnrtilesx;
						copyBGToBuf(bufg, sx1, sy1, sx2, sy2, 0, bufmidy);
						// Color defaultcolour=g.getColor();
						/// * sort objects */
						// ArrayList sortedkeys = new
						// ArrayList(el.objects.keySet());
						// Collections.sort(sortedkeys);
						// for (Iterator i=sortedkeys.iterator(); i.hasNext(); )
						// {
						for (int i = 0; i < el.objects.size; i++) {
							drawObject(bufg, (JGObject) el.objects.values[i]);
						}
						buf_gfx = null; // we're finished with the object
										// drawing
						/* draw status */
						if (bufg != null)
							paintFrame(bufg);
						// }/*synchronized */
						/* draw buffer */
						g.drawImage(buffer, 0, 0, this);
						// g.setColor(defaultcolour);
					}
					// don't block the update thread while waiting for sync
					Toolkit.getDefaultToolkit().sync();
				}
			} catch (JGameError e) {
				exitEngine("Error during paint:\n" + dbgExceptionToString(e));
			}
		}

	}

	/* ====== debug ====== */

	// XXX state variable that was originally static
	int debugflags = 8;
	static final int BBOX_DEBUG = 1;
	static final int GAMESTATE_DEBUG = 2;
	static final int FULLSTACKTRACE_DEBUG = 4;
	static final int MSGSINPF_DEBUG = 8;

	private static int dbgframelog_expiry = 80;
	private JGFont debugmessage_font = new JGFont("Arial", 0, 12);
	JGColor debug_auxcolor1 = JGColor.green;
	JGColor debug_auxcolor2 = JGColor.magenta;

	private Hashtable<String, Vector<String>> dbgframelogs = new Hashtable<String, Vector<String>>(); // old error msgs
	private Hashtable<String, Vector<String>> dbgnewframelogs = new Hashtable<String, Vector<String>>(); // new error
																											// msgs
	/** flags indicating messages are new */
	private Hashtable<String, String> dbgframelogs_new = new Hashtable<String, String>();
	/** objects that dbgframes correspond to (JGObject) */
	private Hashtable<String, JGObject> dbgframelogs_obj = new Hashtable<String, JGObject>();
	/** time that removed objects are dead (Integer) */
	private Hashtable<String, Integer> dbgframelogs_dead = new Hashtable<String, Integer>();

	/** Refresh message logs for this frame. */
	private void refreshDbgFrameLogs() {
		dbgframelogs_new = new Hashtable<String, String>(); // clear "new" flag
		for (Enumeration<String> e = dbgnewframelogs.keys(); e.hasMoreElements();) {
			String source = e.nextElement();
			Vector<String> log = dbgnewframelogs.get(source);
			dbgframelogs.put(source, log);
			dbgframelogs_new.put(source, "yes");
		}
		dbgnewframelogs = new Hashtable<String, Vector<String>>();
	}

	/** paint the messages */
	void paintDbgFrameLogs(Graphics g) {
		// we use an absolute font size
		Font dbgfont = new Font(debugmessage_font.name, debugmessage_font.style, (int) debugmessage_font.size);
		g.setFont(dbgfont);
		for (Enumeration<String> e = dbgframelogs.keys(); e.hasMoreElements();) {
			String source = e.nextElement();
			Vector<String> log = dbgframelogs.get(source);
			if (dbgframelogs_new.containsKey(source)) {
				// new message
				setColor(g, el.fg_color);
			} else {
				// message from previous frame
				setColor(g, debug_auxcolor1);
			}
			JGObject obj = el.getObject(source);
			if (obj == null) {
				// retrieve dead object
				obj = dbgframelogs_obj.get(source);
				// message from deleted object
				setColor(g, debug_auxcolor2);
				if (obj != null) {
					// tick dead timer
					int deadtime = 0;
					if (dbgframelogs_dead.containsKey(source))
						deadtime = dbgframelogs_dead.get(source).intValue();
					if (deadtime < dbgframelog_expiry) {
						dbgframelogs_dead.put(source, new Integer(deadtime + 1));
					} else {
						dbgframelogs_obj.remove(source);
						dbgframelogs_dead.remove(source);
					}
				}
			}
			int lineheight = debugmessage_font.getSize() + 1;
			if (obj != null) {
				JGPoint scaled = el.scalePos(obj.x - el.xofs, obj.y - el.yofs + lineheight / 3, false);
				scaled.y -= lineheight * log.size();
				for (Enumeration<String> f = log.elements(); f.hasMoreElements();) {
					g.drawString(f.nextElement(), scaled.x, scaled.y);
					scaled.y += lineheight;
				}
			} else {
				if (!source.equals("MAIN")) {
					dbgframelogs.remove(source);
				} else {
					if (dbgframelogs_new.containsKey(source)) {
						// new message
						setColor(g, el.fg_color);
					} else {
						// message from previous frame
						setColor(debug_auxcolor1);
					}
					int ypos = el.scaleYPos(el.viewHeight(), false);
					ypos -= lineheight * log.size();
					for (Enumeration<String> f = log.elements(); f.hasMoreElements();) {
						g.drawString(f.nextElement(), 0, ypos);
						ypos += lineheight;
					}
				}
			}
		}
	}

	@Override
	public void dbgShowBoundingBox(boolean enabled) {
		if (enabled)
			debugflags |= BBOX_DEBUG;
		else
			debugflags &= ~BBOX_DEBUG;
	}

	@Override
	public void dbgShowGameState(boolean enabled) {
		if (enabled)
			debugflags |= GAMESTATE_DEBUG;
		else
			debugflags &= ~GAMESTATE_DEBUG;
	}

	@Override
	public void dbgShowFullStackTrace(boolean enabled) {
		if (enabled)
			debugflags |= FULLSTACKTRACE_DEBUG;
		else
			debugflags &= ~FULLSTACKTRACE_DEBUG;
	}

	@Override
	public void dbgShowMessagesInPf(boolean enabled) {
		if (enabled)
			debugflags |= MSGSINPF_DEBUG;
		else
			debugflags &= ~MSGSINPF_DEBUG;
	}

	@Override
	public void dbgSetMessageExpiry(int ticks) {
		dbgframelog_expiry = ticks;
	}

	@Override
	public void dbgSetMessageFont(JGFont font) {
		debugmessage_font = font;
	}

	@Override
	public void dbgSetDebugColor1(JGColor col) {
		debug_auxcolor1 = col;
	}

	@Override
	public void dbgSetDebugColor2(JGColor col) {
		debug_auxcolor2 = col;
	}

	@Override
	public void dbgPrint(String msg) {
		dbgPrint("MAIN", msg);
	}

	@Override
	public void dbgPrint(String source, String msg) {
		if ((debugflags & MSGSINPF_DEBUG) != 0) {
			Vector<String> log = dbgnewframelogs.get(source);
			if (log == null)
				log = new Vector<String>(5, 15);
			if (log.size() < 19) {
				log.add(msg);
			} else if (log.size() == 19) {
				log.add("<messages truncated>");
			}
			dbgnewframelogs.put(source, log);
			JGObject obj = el.getObject(source);
			if (obj != null) { // store source object
				dbgframelogs_obj.put(source, obj);
				dbgframelogs_dead.remove(source);
			}
		} else {
			System.out.println(source + ": " + msg);
		}
	}

	@Override
	public void dbgShowException(String source, Throwable e) {
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(st));
		if ((debugflags & FULLSTACKTRACE_DEBUG) != 0) {
			dbgPrint(source, st.toString());
		} else {
			StringTokenizer toker = new StringTokenizer(st.toString(), "\n");
			if (toker.hasMoreTokens())
				dbgPrint(source, toker.nextToken());
			if (toker.hasMoreTokens())
				dbgPrint(source, toker.nextToken());
			if (toker.hasMoreTokens())
				dbgPrint(source, toker.nextToken());
		}
	}

	@Override
	public String dbgExceptionToString(Throwable e) {
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(st));
		if ((debugflags & FULLSTACKTRACE_DEBUG) != 0) {
			return st.toString();
		} else {
			StringTokenizer toker = new StringTokenizer(st.toString(), "\n");
			String ret = toker.nextToken() + "\n";
			ret += toker.nextToken() + "\n";
			if (toker.hasMoreTokens())
				ret += toker.nextToken();
			return ret;
		}
	}

	@Override
	public void exitEngine(String msg) {
		if (msg != null) {
			System.err.println(msg);
			el.exit_message = msg;
		}
		System.err.println("Exiting JGEngine.");
		if (!i_am_applet)
			System.exit(0);
		destroy();
		// repaint applet window so that exit error is displayed
		canvas.repaint();
	}

	/**
	 * Construct engine, but do not initialise it yet. Call initEngine,
	 * initEngineApplet, or initEngineComponent to initialise the engine.
	 */
	public JGEngine() {
		imageutil.setComponent(this);
	}

	@Override
	public void initEngineComponent(int width, int height) {
		i_am_applet = false;
		jre.create_frame = false;
		el.winwidth = width;
		el.winheight = height;
		init();
	}

	/**
	 * Init engine as applet; call this in your engine constructor. Applet init()
	 * will start the game.
	 */
	@Override
	public void initEngineApplet() {
		i_am_applet = true;
		// we get the width/height only after init is called
	}

	/**
	 * Init engine as application. Passing (0,0) for width, height will result in a
	 * full-screen window without decoration. Passing another value results in a
	 * regular window with decoration.
	 * 
	 * @param width  real screen width, 0 = use screen size
	 * @param height real screen height, 0 = use screen size
	 */
	@Override
	public void initEngine(int width, int height) {
		i_am_applet = false;
		if (width == 0) {
			Dimension scrsize = Toolkit.getDefaultToolkit().getScreenSize();
			el.winwidth = scrsize.width;
			el.winheight = scrsize.height;
			jre.win_decoration = false;
		} else {
			el.winwidth = width;
			el.winheight = height;
			jre.win_decoration = true;
		}
		init();
	}

	@Override
	public void setCanvasSettings(int nrtilesx, int nrtilesy, int tilex, int tiley, JGColor fgcolor, JGColor bgcolor,
			JGFont msgfont) {
		el.nrtilesx = nrtilesx;
		el.nrtilesy = nrtilesy;
		el.viewnrtilesx = nrtilesx;
		el.viewnrtilesy = nrtilesy;
		el.tilex = tilex;
		el.tiley = tiley;
		setColorsFont(fgcolor, bgcolor, msgfont);
		el.view_initialised = true;
	}

	@Override
	public void setScalingPreferences(double min_aspect_ratio, double max_aspect_ratio, int crop_top, int crop_left,
			int crop_bottom, int crop_right) {
		el.min_aspect = min_aspect_ratio;
		el.max_aspect = max_aspect_ratio;
		el.crop_top = crop_top;
		el.crop_left = crop_left;
		el.crop_bottom = crop_bottom;
		el.crop_right = crop_right;
	}

	@Override
	public void setSmoothing(boolean smooth_magnify) {
		el.smooth_magnify = smooth_magnify;
	}

	@Override
	public void requestGameFocus() {
		canvas.requestFocus();
	}

	// note: these get and set methods do not delegate calls

	@Override
	public boolean isApplet() {
		return i_am_applet;
	}

	@Override
	public boolean isMidlet() {
		return false;
	}

	@Override
	public boolean isOpenGL() {
		return false;
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public int viewWidth() {
		return el.viewnrtilesx * el.tilex;
	}

	@Override
	public int viewHeight() {
		return el.viewnrtilesy * el.tiley;
	}

	@Override
	public int viewTilesX() {
		return el.viewnrtilesx;
	}

	@Override
	public int viewTilesY() {
		return el.viewnrtilesy;
	}

	@Override
	public int viewXOfs() {
		return el.pendingxofs;
	}

	@Override
	public int viewYOfs() {
		return el.pendingyofs;
	}

	// public int viewTileXOfs() { return canvas.tilexofs; }
	// public int viewTileYOfs() { return canvas.tileyofs; }

	@Override
	public int pfWidth() {
		return el.nrtilesx * el.tilex;
	}

	@Override
	public int pfHeight() {
		return el.nrtilesy * el.tiley;
	}

	@Override
	public int pfTilesX() {
		return el.nrtilesx;
	}

	@Override
	public int pfTilesY() {
		return el.nrtilesy;
	}

	@Override
	public boolean pfWrapX() {
		return el.pf_wrapx;
	}

	@Override
	public boolean pfWrapY() {
		return el.pf_wrapy;
	}

	@Override
	public int tileWidth() {
		return el.tilex;
	}

	@Override
	public int tileHeight() {
		return el.tiley;
	}

	@Override
	public int displayWidth() {
		return el.winwidth;
	}

	@Override
	public int displayHeight() {
		return el.winheight;
	}

	@Override
	public double getFrameRate() {
		return el.fps;
	}

	@Override
	public double getGameSpeed() {
		return el.gamespeed;
	}

	@Override
	public double getFrameSkip() {
		return el.maxframeskip;
	}

	@Override
	public boolean getVideoSyncedUpdate() {
		return false;
	}

	@Override
	public int getOffscreenMarginX() {
		return el.offscreen_margin_x;
	}

	@Override
	public int getOffscreenMarginY() {
		return el.offscreen_margin_y;
	}

	@Override
	public double getXScaleFactor() {
		return el.x_scale_fac;
	}

	@Override
	public double getYScaleFactor() {
		return el.y_scale_fac;
	}

	@Override
	public double getMinScaleFactor() {
		return el.min_scale_fac;
	}

	/**
	 * Initialise engine; don't call directly. This is supposed to be called by the
	 * applet viewer or the initer.
	 */
	@Override
	public void init() {
		jre.storeInit();
		if (el.winwidth == 0) {
			// get width/height from applet dimensions
			el.winwidth = getWidth();
			el.winheight = getHeight();
		}
		initCanvas();
		if (!el.view_initialised) {
			exitEngine("Canvas settings not initialised, use setCanvasSettings().");
		}

		el.initPF();

		if (!i_am_applet && jre.create_frame) {
			jre.createWindow(this, jre.win_decoration);
		}
		// setAudioLatency(getAudioLatencyPlatformEstimate());
		// System.out.println("X"+el.canvas_xofs+" Y"+el.canvas_yofs);
		canvas = new JGCanvas(el.winwidth - (el.canvas_xofs > 0 ? el.canvas_xofs * 2 : 0),
				el.winheight - (el.canvas_yofs > 0 ? el.canvas_yofs * 2 : 0));
		jre.canvas = canvas;

		jre.clearKeymap();
		canvas.addMouseListener(jre);
		canvas.addMouseMotionListener(jre);
		canvas.addFocusListener(jre);

		// set bg colors so that the canvas's padding is in the proper color
		canvas.setBackground(getAWTColor(el.bg_color));
		setBackground(getAWTColor(el.bg_color));
		if (jre.my_win != null)
			jre.my_win.setBackground(getAWTColor(el.bg_color));
		// determine default font size (unscaled)
		el.msg_font = new JGFont("Helvetica", 0, (int) (16.0 / (640.0 / (el.tilex * el.nrtilesx))));
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		add(canvas);
		if (!JGObject.setEngine(this)) {
			/**
			 * yes, you see that right. I've used a random interface with a method that
			 * allows me to pass a Graphics. We shall move this stuff to JGCanvas later, i
			 * suppose
			 */
			canvas.setInitPainter(new ListCellRenderer<Graphics>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends Graphics> d1, Graphics g, int d2,
						boolean initialise, boolean d4) {
					// if (initialise) {
					// g.setColor(bg_color);
					// g.fillRect(0,0,width,height);
					// }
					setFont(g, el.msg_font);
					setColor(g, el.fg_color);
					drawString(g, "JGame is already running in this VM", el.viewWidth() / 2, el.viewHeight() / 3, 0,
							false);
					return null;
				}
			});
			return;
		}
		el.is_inited = true;
		canvas.setInitPainter(new ListCellRenderer<Graphics>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends Graphics> d1, Graphics g, int d2,
					boolean initialise, boolean d4) {
				// if (initialise) {
				// g.setColor(bg_color);
				// g.fillRect(0,0,width,height);
				// }
				setFont(g, el.msg_font);
				setColor(g, el.fg_color);
				JGImage splash = el.existsImage("splash_image") ? el.getImage("splash_image") : null;
				if (splash != null) {
					JGPoint splash_size = getImageSize("splash_image");
					drawImage(g, viewWidth() / 2 - splash_size.x / 2, Math.max(0, viewHeight() / 4 - splash_size.y / 2),
							"splash_image", false);
				}
				drawString(g, canvas.progress_message, viewWidth() / 2, viewHeight() / 2, 0, false);
				// if (canvas.progress_message!=null) {
				// drawString(g,canvas.progress_message,
				// viewWidth()/2,2*viewHeight()/3,0);
				// }
				// paint the right hand side black in case the bar decreases
				setColor(g, el.bg_color);
				drawRect(g, (int) (viewWidth() * (0.1 + 0.8 * canvas.progress_bar)), (int) (viewHeight() * 0.75),
						(int) (viewWidth() * 0.8 * (1.0 - canvas.progress_bar)), (int) (viewHeight() * 0.05), true,
						false, false);
				// left hand side of bar
				setColor(g, el.fg_color);
				drawRect(g, (int) (viewWidth() * 0.1), (int) (viewHeight() * 0.75),
						(int) (viewWidth() * 0.8 * canvas.progress_bar), (int) (viewHeight() * 0.05), true, false,
						false);
				// length stripes
				drawRect(g, (int) (viewWidth() * 0.1), (int) (viewHeight() * 0.75), (int) (viewWidth() * 0.8),
						(int) (viewHeight() * 0.008), true, false, false);
				drawRect(g, (int) (viewWidth() * 0.1), (int) (viewHeight() * (0.75 + 0.046)), (int) (viewWidth() * 0.8),
						(int) (viewHeight() * 0.008), true, false, false);
				drawString(g, canvas.author_message, viewWidth() - 16,
						viewHeight() - getFontHeight(g, el.msg_font) - 10, 1, false);
				return null;
			}
		});
		if (jre.my_win != null) {
			jre.my_win.setVisible(true);
			jre.my_win.validate();
			// insets are known, resize window
			jre.setWindowSize(jre.win_decoration);
		}
		// initialise keyboard handling
		canvas.addKeyListener(jre);
		canvas.requestFocus();
		thread = new Thread(new JGEngineThread());
		thread.start();
	}

	@Override
	abstract public void initCanvas();

	@Override
	abstract public void initGame();

	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public void startApp() {
		if (!el.is_inited) {
			init();
		} else {
			start();
		}
	}

	@Override
	public void pauseApp() {
		stop();
	}

	@Override
	public void destroyApp(boolean unconditional) {
		destroy();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void wakeUpOnKey(int key) {
		jre.wakeUpOnKey(key);
	}

	@Override
	public void destroy() {
		// kill game thread
		el.is_exited = true;
		// applets cannot interrupt threads; their threads will
		// be destroyed for them (not always, though ...).
		if (thread != null) {
			if (!i_am_applet)
				thread.interrupt();
			try {
				thread.join(2000); // give up after 2 sec
			} catch (InterruptedException e) {
				e.printStackTrace();
				// give up
			}
		}
		// remove frame??
		// close files?? that appears to be unnecessary
		// reset global variables
		if (el.is_inited) {
			JGObject.setEngine(null);
		}
		// stop all samples
		disableAudio();
		System.out.println("JGame engine disposed.");
	}

	@Override
	public void setViewOffset(int xofs, int yofs, boolean centered) {
		el.setViewOffset(xofs, yofs, centered);
	}

	@Override
	public void setBGImgOffset(int depth, double xofs, double yofs, boolean centered) {
	}

	@Override
	public void setViewZoomRotate(double zoom, double rotate) {
	}

	@Override
	public void setPFSize(int nrtilesx, int nrtilesy) {
		el.setPFSize(nrtilesx, nrtilesy);
	}

	@Override
	public void setPFWrap(boolean wrapx, boolean wrapy, int shiftx, int shifty) {
		el.setPFWrap(wrapx, wrapy, shiftx, shifty);
	}

	@Override
	public void setFrameRate(double fps, double maxframeskip) {
		el.setFrameRate(fps, maxframeskip);
	}

	@Override
	public void setVideoSyncedUpdate(boolean value) {
	}

	@Override
	public void setGameSpeed(double gamespeed) {
		el.setGameSpeed(gamespeed);
	}

	@Override
	public void setRenderSettings(int alpha_thresh, JGColor render_bg_col) {
		el.setRenderSettings(alpha_thresh, render_bg_col);
	}

	@Override
	public void setOffscreenMargin(int xmargin, int ymargin) {
		el.setOffscreenMargin(xmargin, ymargin);
	}

	/**
	 * Set global background colour, which is displayed in borders, and behind
	 * transparent tiles if no BGImage is defined.
	 */
	@Override
	public void setBGColor(JGColor bgcolor) {
		Color bgcol = new Color(bgcolor.r, bgcolor.g, bgcolor.b);
		if (canvas != null)
			canvas.setBackground(bgcol);
		if (jre.my_win != null)
			jre.my_win.setBackground(bgcol);
		el.bg_color = bgcolor;
	}

	/**
	 * Set global foreground colour, used for printing text and status messages. It
	 * is also the default colour for painting
	 */
	@Override
	public void setFGColor(JGColor fgcolor) {
		el.fg_color = fgcolor;
	}

	/**
	 * Set the (unscaled) message font, used for displaying status messages. It is
	 * also the default font for painting.
	 */
	@Override
	public void setMsgFont(JGFont msgfont) {
		el.msg_font = msgfont;
	}

	/**
	 * Set foreground and background colour, and message font in one go; passing a
	 * null means ignore that argument.
	 */
	@Override
	public void setColorsFont(JGColor fgcolor, JGColor bgcolor, JGFont msgfont) {
		if (msgfont != null)
			el.msg_font = msgfont;
		if (fgcolor != null)
			el.fg_color = fgcolor;
		if (bgcolor != null)
			setBGColor(bgcolor);
	}

	/**
	 * Set parameters of outline surrounding text (for example, used to increase
	 * contrast).
	 * 
	 * @param thickness 0 = turn off outline
	 */
	@Override
	public void setTextOutline(int thickness, JGColor colour) {
		// curiously, I've seen the init screen draw in-between these two
		// statements. Check of if that's what really happened
		el.outline_colour = colour;
		el.outline_thickness = thickness;
	}

	@Override
	public void setMouseCursor(int cursor) {
		if (cursor == DEFAULT_CURSOR)
			canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		else if (cursor == CROSSHAIR_CURSOR)
			canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		else if (cursor == HAND_CURSOR)
			canvas.setCursor(new Cursor(Cursor.HAND_CURSOR));
		else if (cursor == WAIT_CURSOR)
			canvas.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		else if (cursor == NO_CURSOR)
			canvas.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(null_image, new Point(0, 0), "hidden"));
	}

	/** 1x1 pixel image with transparent colour */
	private BufferedImage null_image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	/**
	 * Set mouse cursor, null means hide cursor.
	 * 
	 * @param cursor is of type java.awt.Cursor
	 */
	@Override
	public void setMouseCursor(Object cursor) {
		if (cursor == null) {
			canvas.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(null_image, new Point(0, 0), "hidden"));
		} else {
			canvas.setCursor((Cursor) cursor);
		}
	}

	/* timers */

	@Override
	public void removeAllTimers() {
		el.removeAllTimers();
	}

	@Override
	public void registerTimer(JGTimer timer) {
		el.registerTimer(timer);
	}

	/* game state */

	@Override
	public void setGameState(String state) {
		el.setGameState(state);
	}

	@Override
	public void addGameState(String state) {
		el.addGameState(state);
	}

	@Override
	public void removeGameState(String state) {
		el.removeGameState(state);
	}

	@Override
	public void clearGameState() {
		el.clearGameState();
	}

	@Override
	public boolean inGameState(String state) {
		return el.inGameState(state);
	}

	@Override
	public boolean inGameStateNextFrame(String state) {
		return el.inGameStateNextFrame(state);
	}

	/** Do some administration, call doFrame. */
	private void doFrameAll() {
		jre.audioNewFrame();
		// the first flush is needed to remove any objects that were created
		// in the main routine after the last moveObjects or checkCollision
		el.flushRemoveList();
		el.flushAddList();
		// tick timers before doing state transitions, because timers may
		// initiate new transitions.
		el.tickTimers();
		el.flushRemoveList();
		el.flushAddList();
		// the game state transition starts here
		el.gamestate = el.gamestate_nextframe;
		el.gamestate_nextframe = new Vector<String>(10, 20);
		el.gamestate_nextframe.addAll(el.gamestate);
		// we assume that state transitions will not initiate new state
		// transitions!
		invokeGameStateMethods("start", el.gamestate_new);
		el.gamestate_new.clear();
		el.flushRemoveList();
		el.flushAddList();
		try {
			doFrame();
		} catch (JGameError ex) {
			exitEngine(dbgExceptionToString(ex));
		} catch (Exception ex) {
			dbgShowException("MAIN", ex);
		}
		invokeGameStateMethods("doFrame", el.gamestate);
		el.frameFinished();
	}

	private void invokeGameStateMethods(String prefix, Vector<String> states) {
		for (Enumeration<String> e = states.elements(); e.hasMoreElements();) {
			String state = e.nextElement();
			jre.tryMethod(this, prefix + state, new Object[] {});
		}
	}

	@Override
	public void doFrame() {
	}

	void paintFrame(Graphics g) {
		buf_gfx = g;
		setColor(g, el.fg_color);
		setFont(el.msg_font);
		try {
			paintFrame();
		} catch (JGameError ex) {
			exitEngine(dbgExceptionToString(ex));
		} catch (Exception ex) {
			dbgShowException("MAIN", ex);
		}
		invokeGameStateMethods("paintFrame", el.gamestate);
		if ((debugflags & GAMESTATE_DEBUG) != 0) {
			String state = "{";
			for (Enumeration<String> e = el.gamestate.elements(); e.hasMoreElements();) {
				state += e.nextElement();
				if (e.hasMoreElements())
					state += ",";
			}
			state += "}";
			setFont(el.msg_font);
			setColor(g, el.fg_color);
			drawString(state, el.viewWidth(), el.viewHeight() - (int) getFontHeight(g, el.msg_font), 1);
		}
		if ((debugflags & MSGSINPF_DEBUG) != 0)
			paintDbgFrameLogs(buf_gfx);
		buf_gfx = null;
	}

	@Override
	public void paintFrame() {
	}

	/** get Graphics used to draw on buffer (JRE, non JOGL only). */
	public Graphics getBufferGraphics() {
		return buf_gfx;
	}

	/* some convenience functions for drawing during repaint and paintFrame() */

	@Override
	public void setColor(JGColor col) {
		if (buf_gfx != null)
			setColor(buf_gfx, col);
	}

	/** Convert JGColor to AWT color (JRE only). */
	public Color getAWTColor(JGColor col) {
		return new Color(col.r, col.g, col.b);
	}

	@Override
	public void setFont(JGFont font) {
		setFont(buf_gfx, font);
	}

	public void setFont(Graphics g, JGFont jgfont) {
		if (canvas != null && g != null) {
			Font font = new Font(jgfont.name, jgfont.style, (int) jgfont.size);
			// font = font.deriveFont((float)jgfont.size);
			font = font.deriveFont((float) (jgfont.size * el.min_scale_fac));
			g.setFont(font);
		}
	}

	@Override
	public void setStroke(double thickness) {
		Graphics2D g = (Graphics2D) buf_gfx;
		g.setStroke(new BasicStroke((float) (thickness * el.min_scale_fac)));
	}

	@Override
	public void setBlendMode(int src_func, int dst_func) {
	}

	@Override
	public double getFontHeight(JGFont jgfont) {
		if (buf_gfx != null)
			return getFontHeight(buf_gfx, jgfont);
		return 0.0;
	}

	double getFontHeight(Graphics g, JGFont jgfont) {
		Font font;
		if (jgfont == null) {
			font = g.getFont();
		} else {
			font = new Font(jgfont.name, jgfont.style, (int) jgfont.size);
		}
		FontRenderContext fontrc = ((Graphics2D) g).getFontRenderContext();
		Rectangle2D fontbounds = font.getMaxCharBounds(fontrc);
		return fontbounds.getHeight();
		// return fontbounds.getHeight() / canvas.y_scale_fac;
	}

	void drawImage(Graphics g, double x, double y, String imgname, boolean pf_relative) {
		if (imgname == null)
			return;
		x = el.scaleXPos(x, pf_relative);
		y = el.scaleYPos(y, pf_relative);
		JREImage img = (JREImage) el.getImage(imgname);
		if (img != null)
			g.drawImage(img.img, (int) x, (int) y, this);
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2, double thickness, JGColor color) {
		if (color != null)
			setColor(color);
		setStroke(thickness);
		drawLine(x1, y1, x2, y2, true);
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2) {
		drawLine(x1, y1, x2, y2, true);
	}

	@Override
	public void drawLine(double x1, double y1, double x2, double y2, boolean pf_relative) {
		if (buf_gfx == null)
			return;
		// if (pf_relative) {
		// x1 -= canvas.xofs;
		// y1 -= canvas.yofs;
		// x2 -= canvas.xofs;
		// y2 -= canvas.yofs;
		// }
		buf_gfx.drawLine(el.scaleXPos(x1, pf_relative), el.scaleYPos(y1, pf_relative), el.scaleXPos(x2, pf_relative),
				el.scaleYPos(y2, pf_relative));
	}

	@Override
	public void drawPolygon(double[] x, double[] y, JGColor[] col, int len, boolean filled, boolean pf_relative) {
		if (buf_gfx == null)
			return;
		int[] xpos = new int[3];
		int[] ypos = new int[3];
		xpos[0] = el.scaleXPos(x[0], pf_relative);
		ypos[0] = el.scaleYPos(y[0], pf_relative);
		xpos[1] = el.scaleXPos(x[1], pf_relative);
		ypos[1] = el.scaleYPos(y[1], pf_relative);
		xpos[2] = el.scaleXPos(x[len - 1], pf_relative);
		ypos[2] = el.scaleYPos(y[len - 1], pf_relative);
		if (!filled) {
			// draw first and last line segment
			if (col != null)
				setColor(buf_gfx, col[1]);
			buf_gfx.drawLine(xpos[0], ypos[0], xpos[1], ypos[1]);
			if (col != null)
				setColor(buf_gfx, col[0]);
			buf_gfx.drawLine(xpos[2], ypos[2], xpos[0], ypos[0]);
		}
		for (int i = 2; i < len; i++) {
			xpos[2] = el.scaleXPos(x[i], pf_relative);
			ypos[2] = el.scaleYPos(y[i], pf_relative);
			if (col != null)
				setColor(buf_gfx, col[i]);
			if (filled) {
				buf_gfx.fillPolygon(xpos, ypos, 3);
			} else {
				buf_gfx.drawLine(xpos[1], ypos[1], xpos[2], ypos[2]);
			}
			xpos[1] = xpos[2];
			ypos[1] = ypos[2];
		}
	}

	@Override
	public void drawRect(double x, double y, double width, double height, boolean filled, boolean centered,
			double thickness, JGColor color) {
		if (color != null)
			setColor(color);
		setStroke(thickness);
		drawRect(x, y, width, height, filled, centered, true);
	}

	@Override
	public void drawRect(double x, double y, double width, double height, boolean filled, boolean centered) {
		drawRect(x, y, width, height, filled, centered, true);
	}

	@Override
	public void drawRect(double x, double y, double width, double height, boolean filled, boolean centered,
			boolean pf_relative) {
		if (buf_gfx == null)
			return;
		drawRect(buf_gfx, x, y, width, height, filled, centered, pf_relative);
	}

	@Override
	public void drawRect(double x, double y, double width, double height, boolean filled, boolean centered,
			boolean pf_relative, JGColor[] shadecol) {
		drawRect(buf_gfx, x, y, width, height, filled, centered, pf_relative);
	}

	@Override
	public void drawRect(double x, double y, double width, double height, boolean filled, boolean centered,
			boolean pf_relative, JGColor[] shadecol, String tileimage) {
		drawRect(buf_gfx, x, y, width, height, filled, centered, pf_relative);
	}

	void drawRect(Graphics g, double x, double y, double width, double height, boolean filled, boolean centered,
			boolean pf_relative) {
		if (centered) {
			x -= (width / 2);
			y -= (height / 2);
		}
		JGRectangle r = el.scalePos(x, y, width, height, pf_relative);
		if (filled) {
			g.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
		} else {
			g.drawRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
		}
	}

	@Override
	public void drawOval(double x, double y, double width, double height, boolean filled, boolean centered,
			double thickness, JGColor color) {
		if (color != null)
			setColor(color);
		setStroke(thickness);
		drawOval(x, y, width, height, filled, centered, true);
	}

	@Override
	public void drawOval(double x, double y, double width, double height, boolean filled, boolean centered) {
		drawOval(x, y, width, height, filled, centered, true);
	}

	@Override
	public void drawOval(double x, double y, double width, double height, boolean filled, boolean centered,
			boolean pf_relative) {
		if (buf_gfx == null)
			return;
		// if (pf_relative) {
		// x -= canvas.xofs;
		// y -= canvas.yofs;
		// }
		x = el.scaleXPos(x, pf_relative);
		y = el.scaleYPos(y, pf_relative);
		width = el.scaleXPos(width, false);
		height = el.scaleYPos(height, false);
		if (centered) {
			x -= (width / 2);
			y -= (height / 2);
		}
		if (filled) {
			buf_gfx.fillOval((int) x, (int) y, (int) width, (int) height);
		} else {
			buf_gfx.drawOval((int) x, (int) y, (int) width, (int) height);
		}
	}

	@Override
	public void drawImage(double x, double y, String imgname) {
		if (buf_gfx == null)
			return;
		drawImage(buf_gfx, x, y, imgname, true);
	}

	@Override
	public void drawImage(double x, double y, String imgname, boolean pf_relative) {
		if (buf_gfx == null)
			return;
		drawImage(buf_gfx, x, y, imgname, pf_relative);
	}

	@Override
	public void drawImage(double x, double y, String imgname, JGColor blend_col, double alpha, double rot, double scale,
			boolean pf_relative) {
		if (buf_gfx == null)
			return;
		drawImage(buf_gfx, x, y, imgname, pf_relative);
	}

	/* new versions of drawImage */

	@Override
	public void drawImage(String imgname, double x, double y) {
		drawImage(x, y, imgname);
	}

	@Override
	public void drawImage(String imgname, double x, double y, boolean pf_relative) {
		drawImage(x, y, imgname, pf_relative);
	}

	@Override
	public void drawImage(String imgname, double x, double y, boolean pf_relative, JGColor blend_col, double alpha,
			double rot, double scale) {
		drawImage(x, y, imgname, blend_col, alpha, rot, scale, pf_relative);
	}

	@Override
	public void drawString(String str, double x, double y, int align, JGFont font, JGColor color) {
		if (font != null)
			setFont(font);
		if (color != null)
			setColor(color);
		drawString(buf_gfx, str, x, y, align, false);
	}

	@Override
	public void drawString(String str, double x, double y, int align) {
		drawString(buf_gfx, str, x, y, align, false);
	}

	@Override
	public void drawString(String str, double x, double y, int align, boolean pf_relative) {
		// if (pf_relative) {
		// x -= canvas.xofs;
		// y -= canvas.yofs;
		// }
		drawString(buf_gfx, str, x, y, align, pf_relative);
	}

	@Override
	public void drawString(String str, double x, double y, int align, boolean pf_relative, JGFont font, JGColor color) {
		if (font != null)
			setFont(font);
		if (color != null)
			setColor(color);
		// if (pf_relative) {
		// x -= canvas.xofs;
		// y -= canvas.yofs;
		// }
		drawString(buf_gfx, str, x, y, align, pf_relative);
	}

	/**
	 * Internal function for writing on both buffer and screen. Coordinates are
	 * always relative to view.
	 */
	void drawString(Graphics g, String str, double x, double y, int align, boolean pf_relative) {
		if (g == null)
			return;
		if (str.equals(""))
			return;
		x = el.scaleXPos(x, pf_relative);
		y = el.scaleYPos(y, pf_relative);
		Font font = g.getFont();
		FontRenderContext fontrc = ((Graphics2D) g).getFontRenderContext();
		// Rectangle2D fontbounds = font.getMaxCharBounds(fontrc);
		// Rectangle2D stringbounds = getStringBounds(str, fontrc);
		// XXX a lot of time in spent in TextLayout.<init>
		TextLayout layout = new TextLayout(str, font, fontrc);
		Rectangle2D strbounds = layout.getBounds();
		int xpos, ypos;
		if (align == -1) {
			xpos = (int) (x - strbounds.getMinX());
			ypos = (int) (y - strbounds.getMinY());
		} else if (align == 0) {
			xpos = (int) (x - strbounds.getCenterX());
			ypos = (int) (y - strbounds.getMinY());
		} else {
			xpos = (int) (x - strbounds.getMaxX());
			ypos = (int) (y - strbounds.getMinY());
		}
		if (el.outline_thickness > 0) {
			Color origcol = g.getColor();
			setColor(el.outline_colour);
			int real_thickness = Math.max(el.scaleXPos(el.outline_thickness, false), 1);
			for (int i = -real_thickness; i <= real_thickness; i++) {
				if (i == 0)
					continue;
				g.drawString(str, xpos + i, ypos);
			}
			for (int i = -real_thickness; i <= real_thickness; i++) {
				if (i == 0)
					continue;
				g.drawString(str, xpos, ypos + i);
			}
			g.setColor(origcol);
		}
		g.drawString(str, xpos, ypos);
	}

	@Override
	public void drawImageString(String string, double x, double y, int align, String imgmap, int char_offset,
			int spacing) {
		el.drawImageString(this, string, x, y, align, imgmap, char_offset, spacing, false);
	}

	@Override
	public void drawImageString(String string, double x, double y, int align, String imgmap, int char_offset,
			int spacing, boolean pf_relative) {
		el.drawImageString(this, string, x, y, align, imgmap, char_offset, spacing, pf_relative);
		// ImageMap map = (ImageMap) el.imagemaps.get(imgmap);
		// if (map==null) throw new JGameError(
		// "Font image map '"+imgmap+"' not found.",true );
		// if (align==0) {
		// x -= (map.tilex+spacing) * string.length()/2;
		// } else if (align==1) {
		// x -= (map.tilex+spacing) * string.length();
		// }
		// //Image img = map.getScaledImage();
		// for (int i=0; i<string.length(); i++) {
		// int imgnr = -char_offset+string.charAt(i);
		// //Point coord = map.getImageCoord(imgnr);
		// String lettername = imgmap+"#"+string.charAt(i);
		// //System.out.println(lettername);
		// //System.out.println("N"+(letter==null));
		// if (!el.existsImage(lettername)) {
		// el.defineImage(lettername, "FONT", 0,
		// el.getSubImage(imgmap,imgnr),
		// "-", 0,0,0,0);
		// }
		// JGImage letter = getImage(lettername);
		// drawImage(buf_gfx, x,y,lettername,pf_relative);
		// //Point scaledtl = canvas.scalePos(x, y);
		// //Point scaledbr = canvas.scalePos(x+map.tilex, y+map.tiley);
		// //buf_gfx.drawImage(map.img,
		// // scaledtl.x, scaledtl.y, scaledbr.x, scaledbr.y,
		// // coord.x, coord.y, coord.x+map.tilex, coord.y+map.tiley, null);
		// //buf_gfx.drawImage(map.img,
		// // x, y, x+map.tilex, y+map.tiley,
		// // coord.x, coord.y, coord.x+map.tilex, coord.y+map.tiley, null);
		// x += map.tilex + spacing;
		// }
	}

	/* input */

	@Override
	public JGPoint getMousePos() {
		return new JGPoint(jre.mousepos.x, jre.mousepos.y);
	}

	@Override
	public int getMouseX() {
		return jre.mousepos.x;
	}

	@Override
	public int getMouseY() {
		return jre.mousepos.y;
	}

	@Override
	public boolean getMouseButton(int nr) {
		return jre.mousebutton[nr];
	}

	@Override
	public void clearMouseButton(int nr) {
		jre.mousebutton[nr] = false;
	}

	@Override
	public void setMouseButton(int nr) {
		jre.mousebutton[nr] = true;
	}

	@Override
	public boolean getMouseInside() {
		return jre.mouseinside;
	}

	@Override
	public boolean hasMultitouch() {
		return false;
	}

	@Override
	public int[] touchGetIDs() {
		return null;
	}

	@Override
	public double touchGetX(int id) {
		return 0;
	}

	@Override
	public double touchGetY(int id) {
		return 0;
	}

	@Override
	public boolean touchIDExists(int id) {
		return false;
	}

	@Override
	public boolean getKey(int key) {
		return jre.keymap[key];
	}

	@Override
	public void clearKey(int key) {
		jre.keymap[key] = false;
	}

	@Override
	public void setKey(int key) {
		jre.keymap[key] = true;
	}

	@Override
	public int getLastKey() {
		return jre.lastkey;
	}

	@Override
	public char getLastKeyChar() {
		return jre.lastkeychar;
	}

	@Override
	public void clearLastKey() {
		jre.clearLastKey();
	}

	/** Non-static version for the sake of the interface. */
	@Override
	public String getKeyDesc(int key) {
		return JREEngine.getKeyDescStatic(key);
	}

	public static String getKeyDescStatic(int key) {
		return JREEngine.getKeyDescStatic(key);
	}

	/** Non-static version for the sake of the interface. */
	@Override
	public int getKeyCode(String keydesc) {
		return JREEngine.getKeyCodeStatic(keydesc);
	}

	public static int getKeyCodeStatic(String keydesc) {
		return JREEngine.getKeyCodeStatic(keydesc);
	}

	@Override
	public boolean hasAccelerometer() {
		switch (accelGyroMode) {
		case NativeLive:
		case UdpLive:
		case CommLive:
			return true;
		case None:
		case UdpEnabled:
		case NativeEnabled:
		case CommEnabled:
			return false;
		}
		return false;
	}

	@Override
	public boolean isSensorActive(InputType it) {
		if (cluster == null) {
			return false;
		}
		return cluster.isActive(it);
	}

	@Override
	public boolean hasGyro() {
		switch (accelGyroMode) {
		case NativeLive:
		case UdpLive:
		case CommLive:
			return true;
		case None:
		case UdpEnabled:
		case NativeEnabled:
		case CommEnabled:
			return false;
		}
		return false;
	}

	public AccelGyroMode getAccelGyroMode() {
		return accelGyroMode;
	}

	@Override
	public double getAccelX() {
		return accelGyroStatus.getData(getAccelAxisId()).x;
	}

	@Override
	public double getAccelY() {
		return accelGyroStatus.getData(getAccelAxisId()).y;
	}

	@Override
	public double getAccelZ() {
		return accelGyroStatus.getData(getAccelAxisId()).z;
	}

	@Override
	public double[] getAccelVec() {
		Point3d accel = accelGyroStatus.getData(getAccelAxisId());
		return new double[] { accel.x, accel.y, accel.z };
	}

	private int getAccelAxisId() {
		switch (DATA_CLUSTER_IN_USE) {
		case ADC:
			break;
		case ArcadeButton:
			break;
		case CsGyroRelay:
			break;
		case JavaGyro:
			return 1;
		}
		return 0;
	}

	private int getGyroAngleAxisId() {
		switch (DATA_CLUSTER_IN_USE) {
		case ADC:
			break;
		case ArcadeButton:
			break;
		case CsGyroRelay:
			break;
		case JavaGyro:
			return 2;
		}
		return 0;
	}

	private int getGyroRotationAxisId() {
		switch (DATA_CLUSTER_IN_USE) {
		case ADC:
			break;
		case ArcadeButton:
			break;
		case CsGyroRelay:
			break;
		case JavaGyro:
			return 3;
		}
		return 0;
	}

	private int getPitchRollAxisId() {
		switch (DATA_CLUSTER_IN_USE) {
		case ADC:
			break;
		case ArcadeButton:
			break;
		case CsGyroRelay:
			break;
		case JavaGyro:
			return 4;
		}
		return 0;
	}

	@Override
	public double[] getJoystickVec() {
		Integer[] joystick = joystickStatus.getData();
		double[] out = new double[joystick.length];
//	for (int x : joystick) {
		for (int i = 0; i < joystick.length; i++) {
			Integer x = joystick[i];
			if (x == null) {
				out[i] = -1;
			} else {
				out[i] = joystick[i];
			}
		}
		return out;
	}

	@Override
	public double[] getGyroAngleVec() {
		Point3d gyro = accelGyroStatus.getData(getGyroAngleAxisId());
		return new double[] { gyro.x, gyro.y, gyro.z };
	}

	@Override
	public double getGyroAngleX() {
		return accelGyroStatus.getData(getGyroAngleAxisId()).x;
	}

	@Override
	public double getGyroAngleY() {
		return accelGyroStatus.getData(getGyroRotationAxisId()).y;
	}

	@Override
	public double getGyroAngleZ() {
		return accelGyroStatus.getData(getGyroRotationAxisId()).z;
	}

	@Override
	public double[] getGyroVec() {
		Point3d gyro = accelGyroStatus.getData(getGyroRotationAxisId());
		return new double[] { gyro.x, gyro.y, gyro.z };
	}

	@Override
	public double getGyroX() {
		return accelGyroStatus.getData(getGyroRotationAxisId()).x;
	}

	@Override
	public double getGyroY() {
		return accelGyroStatus.getData(getGyroRotationAxisId()).y;
	}

	@Override
	public double getGyroZ() {
		return accelGyroStatus.getData(getGyroRotationAxisId()).z;
	}

	@Override
	public double getPitch() {
		return accelGyroStatus.getData(getPitchRollAxisId()).x;
	}

	@Override
	public double getRoll() {
		return accelGyroStatus.getData(getPitchRollAxisId()).y;
	}

	@Override
	public double getYaw() {
		return accelGyroStatus.getData(getPitchRollAxisId()).z;
	}

	@Override
	public double[] getPitchRollYawVec() {
		Point3d gyro = accelGyroStatus.getData(getPitchRollAxisId());
		return new double[] { gyro.x, gyro.y, gyro.z };
	}

	private DataReceive gyroReceiver;
	private GyroDataUpdater nativeGyro;
	private GyroStatus gyroStatus;

	public void startAccelGyroFromUdp(int port) {
		startAccelGyroFromUdp(port, (IDataReceiveListener) null);
	}

	public void startAccelGyroFromUdp(int port, IDataReceiveListener... listeners) {
		if (gyroReceiver == null || !gyroReceiver.isRunning()) {
			gyroReceiver = new UdpDataReceive(cluster, port);
			gyroReceiver.add(new IDataReceiveListener() {

				@Override
				public void serverOnline() {
					accelGyroMode = AccelGyroMode.UdpEnabled;
				}

				@Override
				public void serverOffline() {
					accelGyroMode = AccelGyroMode.None;
				}

				@Override
				public void accelGyroUpdated() {
					accelGyroMode = AccelGyroMode.UdpLive;
				}

				@Override
				public void sentenceReceived(String sentence) {
				}

				@Override
				public void serverAbort(String msg) {
					accelGyroMode = AccelGyroMode.None;
				}
			});
			GyroNetworkStatus gyroNetworkStatus = new GyroNetworkStatus();
			gyroReceiver.add(gyroNetworkStatus);
			gyroStatus = gyroNetworkStatus;

			if (listeners != null) {
				for (IDataReceiveListener l : listeners) {
					gyroReceiver.add(l);
				}
			}
			gyroReceiver.activateServer();
		}
	}

	public boolean startAccelGyroFromComm(ConnectedSerialPort serialPort, IDataReceiveListener... listeners) {
		if (gyroReceiver == null || !gyroReceiver.isRunning()) {
			gyroReceiver = new SerialDataReceive(cluster, serialPort);
			gyroReceiver.add(new IDataReceiveListener() {

				@Override
				public void serverOnline() {
					accelGyroMode = AccelGyroMode.CommEnabled;
				}

				@Override
				public void serverOffline() {
					accelGyroMode = AccelGyroMode.None;
				}

				@Override
				public void accelGyroUpdated() {
					accelGyroMode = AccelGyroMode.CommLive;
				}

				@Override
				public void sentenceReceived(String sentence) {
				}

				@Override
				public void serverAbort(String msg) {
					accelGyroMode = AccelGyroMode.None;
				}
			});
			GyroNetworkStatus gyroNetworkStatus = new GyroNetworkStatus();
			gyroReceiver.add(gyroNetworkStatus);
			gyroStatus = gyroNetworkStatus;

			if (listeners != null) {
				for (IDataReceiveListener l : listeners) {
					gyroReceiver.add(l);
				}
			}
			return gyroReceiver.activateServer();
		}
		return false;
	}

	public void startAccelGyroNative() {
		nativeGyro = new GyroDataUpdater(cluster);
		nativeGyro.start();
		accelGyroMode = AccelGyroMode.NativeLive;
	}

	public GyroStatus getGyroStatus() {
		return gyroStatus;
	}

	public void enableDisplayAngleInfo() {
		this.displayAngleInfo = true;
	}

	public void enableDisplayAngleInfo(int x, int y) {
		this.displayAngleInfo = true;
		this.displayAngleInfoX = x;
		this.displayAngleInfoY = y;
	}

	public void disableDisplayAngleInfo(boolean displayAngleInfo) {
		this.displayAngleInfo = false;
	}

	/* ====== animation ====== */

	@Override
	public void defineAnimation(String id, String[] frames, double speed) {
		el.defineAnimation(id, frames, speed);
	}

	@Override
	public void defineAnimation(String id, String[] frames, double speed, boolean pingpong) {
		el.defineAnimation(id, frames, speed, pingpong);
	}

	@Override
	public Animation getAnimation(String id) {
		return el.getAnimation(id);
	}

	@Override
	public String getConfigPath(String filename) {
		if (isApplet())
			return null;
		File jgamedir;
		try {
			jgamedir = new File(System.getProperty("user.home"), ".jgame");
		} catch (Exception e) {
			// probably AccessControlException of unsigned webstart
			return null;
		}
		if (!jgamedir.exists()) {
			// try to create ".jgame"
			if (!jgamedir.mkdir()) {
				// fail
				return null;
			}
		}
		if (!jgamedir.isDirectory())
			return null;
		File file = new File(jgamedir, filename);
		// try to create file if it didn't exist
		try {
			file.createNewFile();
		} catch (IOException e) {
			return null;
		}
		if (!file.canRead())
			return null;
		if (!file.canWrite())
			return null;
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public int invokeUrl(String url, String target) {
		if (isApplet()) {
			try {
				getAppletContext().showDocument(new URL(url), target);
			} catch (MalformedURLException e) {
				return 0;
			}
			return -1;
		} else {
			return 0;
		}
	}

	void paintExitMessage(Graphics g) {
		try {
			setFont(g, debugmessage_font);
			int height = (int) (getFontHeight(g, null) / el.y_scale_fac);
			// clear background
			setColor(g, el.bg_color);
			drawRect(g, el.viewWidth() / 2, el.viewHeight() / 2, 9 * el.viewWidth() / 10, height * 5, true, true,
					false);
			setColor(g, debug_auxcolor2);
			// draw colour bars
			drawRect(g, el.viewWidth() / 2, el.viewHeight() / 2 - 5 * height / 2, 9 * viewWidth() / 10, 5, true, true,
					false);
			drawRect(g, el.viewWidth() / 2, el.viewHeight() / 2 + 5 * height / 2, 9 * viewWidth() / 10, 5, true, true,
					false);
			setColor(g, el.fg_color);
			int ypos = el.viewHeight() / 2 - 3 * height / 2;
			StringTokenizer toker = new StringTokenizer(el.exit_message, "\n");
			while (toker.hasMoreTokens()) {
				drawString(g, toker.nextToken(), el.viewWidth() / 2, ypos, 0, false);
				ypos += height + 1;
			}
		} catch (java.lang.NullPointerException e) {
			// this sometimes happens during drawString when the applet is
			// exiting
			// but calls repaint while the graphics surface is already disposed.
			// See also bug 4791314:
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4791314
		}
	}

	/* computation */

	@Override
	public boolean and(int value, int mask) {
		return el.and(value, mask);
	}

	@Override
	public double random(double min, double max) {
		return el.random(min, max);
	}

	@Override
	public double random(double min, double max, double interval) {
		return el.random(min, max, interval);
	}

	@Override
	public int random(int min, int max, int interval) {
		return el.random(min, max, interval);
	}

	@Override
	public double atan2(double y, double x) {
		return Math.atan2(y, x);
	}

	@Override
	public JGPoint getTileIndex(double x, double y) {
		return el.getTileIndex(x, y);
	}

	@Override
	public JGPoint getTileCoord(int tilex, int tiley) {
		return el.getTileCoord(tilex, tiley);
	}

	@Override
	public JGPoint getTileCoord(JGPoint tileidx) {
		return el.getTileCoord(tileidx);
	}

	@Override
	public double snapToGridX(double x, double gridsnapx) {
		return el.snapToGridX(x, gridsnapx);
	}

	@Override
	public double snapToGridY(double y, double gridsnapy) {
		return el.snapToGridY(y, gridsnapy);
	}

	@Override
	public void snapToGrid(JGPoint p, int gridsnapx, int gridsnapy) {
		el.snapToGrid(p, gridsnapx, gridsnapy);
	}

	@Override
	public boolean isXAligned(double x, double margin) {
		return el.isXAligned(x, margin);
	}

	@Override
	public boolean isYAligned(double y, double margin) {
		return el.isYAligned(y, margin);
	}

	@Override
	public double getXAlignOfs(double x) {
		return el.getXAlignOfs(x);
	}

	@Override
	public double getYAlignOfs(double y) {
		return el.getYAlignOfs(y);
	}

	// XXX please test these two methods

	@Override
	public double getXDist(double x1, double x2) {
		return el.getXDist(x1, x2);
	}

	@Override
	public double getYDist(double y1, double y2) {
		return el.getYDist(y1, y2);
	}

	/** Engine thread, executing game action. */
	class JGEngineThread implements Runnable {
		private long target_time = 0; /*
										 * time at which next frame should start
										 */
		private int frames_skipped = 0;

		public JGEngineThread() {
		}

		@Override
		public void run() {
			try {
				try {
					initGame();
				} catch (Exception e) {
					e.printStackTrace();
					throw new JGameError("Exception during initGame(): " + e);
				}
				canvas.setInitialised();
				target_time = System.currentTimeMillis() + (long) (1000.0 / el.fps);
				while (!el.is_exited) {
					if ((debugflags & MSGSINPF_DEBUG) != 0)
						refreshDbgFrameLogs();
					long cur_time = System.currentTimeMillis();
					if (!running) {
						// wait in portions of 1/2 sec until running is set;
						// reset target time
						Thread.sleep(500);
						target_time = cur_time + (long) (1000.0 / el.fps);
					} else if (cur_time < target_time + (long) (500.0 / el.fps)) {
						// we lag behind less than 1/2 frame -> do full frame.
						// This empirically produces the smoothest animation
						synchronized (el.objects) {
							doFrameAll();
							el.updateViewOffset();
						}
						canvas.repaint();
						frames_skipped = 0;
						if (cur_time + 3 < target_time) {
							// we even have some time left -> sleep it away
							Thread.sleep(target_time - cur_time);
						} else {
							// we don't, just yield to give input handler and
							// painter some time
							Thread.yield();
						}
						target_time += (1000.0 / el.fps);
						// } else if (cur_time >
						// target_time + (long)(1000.0*el.maxframeskip/el.fps))
						// {
						// // we lag behind more than the max # frames ->
						// // draw full frame and reset target time
						// synchronized (el.objects) {
						// doFrameAll();
						// el.updateViewOffset();
						// }
						// canvas.repaint();
						// frames_skipped=0;
						// // yield to give input handler + painter some time
						// Thread.yield();
						// target_time=cur_time + (long)(1000.0/el.fps);
					} else {
						// we lag behind a little -> frame skip
						synchronized (el.objects) {
							doFrameAll();
							el.updateViewOffset();
						}
						// if we skip too many frames in succession, draw a
						// frame
						if ((++frames_skipped) > el.maxframeskip) {
							canvas.repaint();
							frames_skipped = 0;
							target_time = cur_time + (long) (1000.0 / el.fps);
						} else {
							target_time += (long) (1000.0 / el.fps);
						}
						// yield to give input handler some time
						Thread.yield();
					}
				}
			} catch (InterruptedException e) {
				/* exit thread when interrupted */
				System.out.println("JGame thread exited.");
			} catch (Exception e) {
				dbgShowException("MAIN", e);
			} catch (JGameError e) {
				exitEngine("Error in main:\n" + dbgExceptionToString(e));
			}
		}
	}

	/* ===== audio ===== */

	@Override
	public void enableAudio() {
		jre.enableAudio();
	}

	@Override
	public void disableAudio() {
		jre.disableAudio();
	}

	@Override
	public void defineAudioClip(String clipid, String filename) {
		el.defineAudioClip(this, clipid, filename);
	}

	@Override
	public String lastPlayedAudio(String channel) {
		return jre.lastPlayedAudio(channel);
	}

	@Override
	public void playAudio(String clipid) {
		jre.playAudio(this, clipid);
	}

	@Override
	public void playAudio(String channel, String clipid, boolean loop) {
		jre.playAudio(this, channel, clipid, loop);
	}

	@Override
	public void stopAudio(String channel) {
		jre.stopAudio(channel);
	}

	@Override
	public void stopAudio() {
		jre.stopAudio();
	}

	/* ===== store ===== */

	@Override
	public void storeWriteInt(String id, int value) {
		jre.storeWriteInt(id, value);
	}

	@Override
	public void storeWriteDouble(String id, double value) {
		jre.storeWriteDouble(id, value);
	}

	@Override
	public void storeWriteString(String id, String value) {
		jre.storeWriteString(id, value);
	}

	@Override
	public void storeRemove(String id) {
		jre.storeRemove(id);
	}

	@Override
	public boolean storeExists(String id) {
		return jre.storeExists(id);
	}

	@Override
	public int storeReadInt(String id, int undef) {
		return jre.storeReadInt(id, undef);
	}

	@Override
	public double storeReadDouble(String id, double undef) {
		return jre.storeReadDouble(id, undef);
	}

	@Override
	public String storeReadString(String id, String undef) {
		return jre.storeReadString(id, undef);
	}

	/* ====== options ====== */

	@Override
	public void optsAddTitle(String title) {
		jre.optsAddTitle(title);
	}

	@Override
	public void optsAddNumber(String varname, String title, String desc, int decimals, double lower, double upper,
			double step, double initial) {
		jre.optsAddNumber(varname, title, desc, decimals, lower, upper, step, initial);
	}

	@Override
	public void optsAddBoolean(String varname, String title, String desc, boolean initial) {
		jre.optsAddBoolean(varname, title, desc, initial);
	}

	@Override
	public void optsAddEnum(String varname, String title, String desc, String[] values, int initial) {
		jre.optsAddEnum(varname, title, desc, values, initial);
	}

	@Override
	public void optsAddKey(String varname, String title, String desc, int initial) {
		jre.optsAddKey(varname, title, desc, initial);
	}

	@Override
	public void optsAddString(String varname, String title, String desc, int maxlen, boolean isPassword,
			String initial) {
		jre.optsAddString(varname, title, desc, maxlen, isPassword, initial);
	}

	@Override
	public void optsClear() {
		jre.optsClear();
	}

}
