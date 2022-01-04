package ca.hapke.controller.ui;

import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

import ca.hapke.gyro.data.AbstractGyroDataType;
import ca.hapke.gyro.data.DataCluster;
import ca.hapke.gyro.data.DataType;
import ca.hapke.gyro.data.DataType.InputType;
import ca.hapke.util.AngleFormatter;
import ca.hapke.util.RunKillThread;

/**
 * https://rosettacode.org/wiki/Draw_a_rotating_cube#Java
 * 
 *
 * @author Mr. Hapke
 */
public class RotatingCube extends JPanel {

	private static final int DIAL_MARGIN = 70;
	private static final int CUBE_BASE_X = 150;
	private static final int CUBE_BASE_Y = 150;
	private static final double CUBE_SCALE = 50;
	private static final double SQRT2 = sqrt(2);
	private static final double FINISH = 0.5;
	private static final double START = -0.2;

	private static final long serialVersionUID = -4593639743621966914L;

	final double[][] nodesBase = { { -1, -1, -1 }, { -1, -1, 1 }, { -1, 1, -1 }, { -1, 1, 1 }, { 1, -1, -1 },
			{ 1, -1, 1 }, { 1, 1, -1 }, { 1, 1, 1 } };
	final double[][] axesNodesBase = { { START, 0, 0 }, { FINISH, 0, 0 }, { 0, START, 0 }, { 0, FINISH, 0 },
			{ 0, 0, START }, { 0, 0, FINISH } };

//	double[][] axesNodesScaled = new double[axesNodesBase.length][axesNodesBase[0].length];
	double[][] axesNodes = new double[axesNodesBase.length][axesNodesBase[0].length];

	final int[][] axesEdges = { { 0, 1 }, { 2, 3 }, { 4, 5 } };
	final int[][] edges = { { 0, 1 }, { 1, 3 }, { 3, 2 }, { 2, 0 }, { 4, 5 }, { 5, 7 }, { 7, 6 }, { 6, 4 }, { 0, 4 },
			{ 1, 5 }, { 2, 6 }, { 3, 7 } };

	final Color[] nodeColours = { Color.red, Color.blue, Color.orange, Color.CYAN, Color.getHSBColor(268, 86, 82),
			Color.green, Color.yellow, Color.BLACK };
//	final Color[] edgeColours = {Color.green, Color.green, Color.green, Color.green, 
//			Color.magenta, Color.magenta, Color.magenta, Color.magenta, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN};
	final Color[] edgeColours = { Color.black, Color.black, Color.black, Color.black, Color.black, Color.black,
			Color.black, Color.black, Color.black, Color.black, Color.black, Color.black };
	final Color[] axesEdgeColours = { Color.magenta, Color.cyan, Color.ORANGE };

	double[][] nodes = new double[nodesBase.length][nodesBase[0].length];

	private AngleFormatter af;
	private boolean baseline = true;
	private RollPitchYaw angles = new RollPitchYaw();
	private GMatrix proj;
	private List<Dial> dials = new ArrayList<>();

	private static final int FONT_SIZE = 12;
	private Font monospace;
	private Font regularFont;

	private DataCluster cluster;
	private RotatingCubeRepainter rcr;

	private class RotatingCubeRepainter extends RunKillThread {

		@Override
		public void doWork() {
			while (!kill) {
				AbstractGyroDataType gdt = (AbstractGyroDataType) cluster.getData(InputType.JavaGyro);
//				AbstractGyroDataType gdt = (AbstractGyroDataType) cluster.getData(InputType.CsGyroRelay);
				Point3d data = gdt.getData(1);
				if (data != null)
					rotateCube2(data);
				repaint();
				try {
					Thread.sleep(17);
				} catch (InterruptedException e) {
					kill = true;
				}
			}
		}

	}

	public RotatingCube(DataCluster cluster) {
		this.cluster = cluster;
		this.af = new AngleFormatter(3, 3, 1, 1, 0.1);
		setPreferredSize(new Dimension(200, 200));
		setBackground(Color.white);

		createProjectionMatrix();

		monospace = new Font("Monospaced", Font.PLAIN, FONT_SIZE);
		regularFont = new Font("Arial", Font.PLAIN, FONT_SIZE);

		dials.add(new Dial("x-axis [roll]", angles.roll, regularFont));
		dials.add(new Dial("y-axis [pitch]", angles.pitch, regularFont));
		dials.add(new Dial("z-axis [yaw]", angles.yaw, regularFont));

		rcr = new RotatingCubeRepainter();
		rcr.start();

	}

	public void rebaseline() {
		baseline = true;
	}

	public void rotateCube2(Point3d gyro) {
		if (baseline && gyro.x != 0 && gyro.y != 0 && gyro.z != 0) {
			angles.roll.setBaseline(gyro.x);
			angles.pitch.setBaseline(gyro.y);
			angles.yaw.setBaseline(gyro.z);
//			bx = gyro.x;
//			by = gyro.y;
//			bz = gyro.z;
			baseline = false;
		}

		angles.roll.setValue(gyro.x);
		angles.pitch.setValue(gyro.y);
		angles.yaw.setValue(gyro.z);

		// cube
		GMatrix rot = createRotationMatrix();

		for (int i = 0; i < nodesBase.length && i < nodes.length; i++) {
			double[] base = nodesBase[i];
			GMatrix node = new GMatrix(1, 3, base);
			node.mul(rot);

			double[] output = nodes[i];
			output[0] = node.getElement(0, 0) * CUBE_SCALE;
			output[1] = node.getElement(0, 1) * CUBE_SCALE;
			output[2] = node.getElement(0, 2) * CUBE_SCALE;
		}

		for (int i = 0; i < axesNodesBase.length && i < axesNodes.length; i++) {
			double[] base = axesNodesBase[i];
			GMatrix node = new GMatrix(1, 3, base);
			node.mul(rot);

			double[] output = axesNodes[i];
			output[0] = node.getElement(0, 0) * CUBE_SCALE;
			output[1] = node.getElement(0, 1) * CUBE_SCALE;
			output[2] = node.getElement(0, 2) * CUBE_SCALE;
		}

	}

	/***
	 * 2 rows, 3 columns
	 */
	private void createProjectionMatrix() {
		proj = new GMatrix(2, 3);
		proj.setColumn(0, new double[] { 1 / SQRT2, -1 / SQRT2 });
		proj.setColumn(1, new double[] { -1, 0 });
		proj.setColumn(2, new double[] { 0, -1 });
	}

	/*-
	 * [1]  Roll CCW about x-axis by gamma (g)
	 * [2] Pitch CCW about y-axis by beta  (b)
	 * [3]   Yaw CCW about z-axis by alpha (a)
	 */
	private GMatrix createRotationMatrix() {
		double a;
		double b;
		double g;
		g = Math.toRadians(angles.roll.getTheta());
		b = Math.toRadians(angles.pitch.getTheta());
		a = Math.toRadians(angles.yaw.getTheta());

		Matrix3d rotation = new Matrix3d();
		// rotation.m[y][x] format... library in row-dominant format
		double ca = cos(a);
		double cb = cos(b);
		double cg = cos(g);

		double sa = sin(a);
		double sb = sin(b);
		double sg = sin(g);

		rotation.m00 = ca * cb;
		rotation.m01 = ca * sb * sg - sa * cg;
		rotation.m02 = ca * sb * cg + sa * sg;

		rotation.m10 = sa * cb;
		rotation.m11 = sa * sb * sg + ca * cg;
		rotation.m12 = sa * sb * cg - ca * sg;

		rotation.m20 = -sb;
		rotation.m21 = cb * sg;
		rotation.m22 = cb * cg;

		GMatrix rot = new GMatrix(3, 3);
		rot.set(rotation);
		return rot;
	}

	void drawCube(Graphics2D g) {
		g.setColor(Color.blue);
		g.setFont(monospace);
		g.drawString(" Roll: " + af.format(this.angles.roll.getTheta()), 10, 10);
		g.drawString("Pitch: " + af.format(this.angles.pitch.getTheta()), 10, 24);
		g.drawString("  Yaw: " + af.format(this.angles.yaw.getTheta()), 10, 38);
		g.translate(50, 80);
		// axes
		drawEdges(g, axesEdges, axesEdgeColours, axesNodes, true, false, 2);

		g.translate(CUBE_BASE_X, CUBE_BASE_Y);

		// cube
		drawEdges(g, edges, edgeColours, nodes, false, false, 4);

		g.translate(CUBE_SCALE * 2.5, 0);

		g.setFont(regularFont);
		g.setColor(Color.black);

		for (Dial d : dials) {
			d.draw(g, regularFont);
			g.translate(2 * Dial.DIAL_RADIUS + DIAL_MARGIN, 0);
		}
	}

	private void drawEdges(Graphics2D g, int[][] edges, Color[] colours, double[][] edgeNodeLookup, boolean labels,
			boolean coordinate, int nodeRadius) {
		String axis = "";
		for (int i = 0; i < edges.length && i < colours.length; i++) {
			int[] edge = edges[i];
			g.setColor(colours[i]);

			try {
				int x1, y1, x2, y2;

				// 3 rows, 2 columns... 3 dimensions, 2 vectors (0=start, 1=end)
				GMatrix in = new GMatrix(3, 2);

				in.setColumn(0, edgeNodeLookup[edge[0]]);
				in.setColumn(1, edgeNodeLookup[edge[1]]);
				GMatrix out = new GMatrix(2, 2);
				out.mul(proj, in);
				x1 = (int) round(out.getElement(0, 0));
				y1 = (int) round(out.getElement(1, 0));
				x2 = (int) round(out.getElement(0, 1));
				y2 = (int) round(out.getElement(1, 1));
				g.drawLine(x1, y1, x2, y2);

				g.fillOval(x1 - nodeRadius, y1 - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
				g.fillOval(x2 - nodeRadius, y2 - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);

				if (labels) {
					if (i == 0)
						axis = "x";
					else if (i == 1)
						axis = "y";
					else if (i == 2)
						axis = "z";
				}
				if (coordinate) {
					axis = axis + "[" + af.format(in.getElement(0, 1)) + "," + af.format(in.getElement(1, 1)) + "]";
				}
				if (axis.length() > 0)
					g.drawString(axis, x2 + 5, y2 + 3);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);
		Graphics2D g = (Graphics2D) gg;
		g.setFont(monospace);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int x = 450;
		int y = 18;
		int yIncrement = 18;
		for (DataType dt : cluster.getValues()) {
//			g.drawString("FPS: " + af.format(dt.getFps()), x, 10);
			for (int i = 0; i < dt.getDimensions(); i++) {
				String s = dt.getValueString(i);
				g.setColor(Color.black);
				y += yIncrement;
				g.drawString(s, x, y);

				String n = dt.getName(i);
				FontMetrics fontMetrics = g.getFontMetrics();
				g.setColor(Color.green);
				g.drawString(n, (x - 10) - fontMetrics.stringWidth(n), y);
			}
		}

		drawCube(g);
	}

}