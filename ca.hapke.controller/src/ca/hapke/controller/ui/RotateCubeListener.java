//package ca.hapke.controller.ui;
//package ca.hapke.net.udp.ui;
//
//import javax.vecmath.Point3d;
//
//import ca.hapke.gyro.DataType;
//import ca.hapke.net.udp.data.IDataListener;
//import ca.hapke.util.MovementStrength;
//
///**
// * @author Mr. Hapke
// *
// */
//public class RotateCubeListener implements IDataListener {
//
////	private static final double threshold = 15;
////	private static final double rotationAmount = 0.04;
//
////	private static double getSensitivity() {
////		return 0.01;
////	}
//
//	private RotatingCube cube;
//	private MovementStrength mvmt;
//
//	public RotateCubeListener(RotatingCube cube) {
//		this.cube = cube;
//		this.mvmt = new MovementStrength(0, 0, 0, 0, 5, 5, 0.01);
//	}
//
//	@Override
//	public void updateOccurred(DataType status) {
//		Point3d[] data = status.getData();
//		Point3d gyro = data[4];
////		mvmt.update(gyro.x, gyro.y);
//
////		double sensitivity = getSensitivity();
////		double angleX = getRotationAmount(gyro.x);
////		double angleY = getRotationAmount(gyro.y);
////		cube.rotateCube(angleX, angleY);
//		// sensitivity * gyro.y
////		cube.rotateCube(gyro.x, gyro.z);
//		cube.rotateCube2(gyro);
//	}
//
////	private double getRotationAmount(double val) {
////		if (val > threshold)
////			return rotationAmount;
////		else if (val < -threshold)
////			return -rotationAmount;
////		else
////			return 0;
////	}
//
//}
