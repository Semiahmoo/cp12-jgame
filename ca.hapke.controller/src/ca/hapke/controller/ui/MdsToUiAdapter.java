//package ca.hapke.controller.ui;
//
//import javax.vecmath.Point3d;
//
//import ca.hapke.gyro.DataType;
//import ca.hapke.gyro.IDataListener;
//
///**
// * 
// * @author Mr. Hapke
// *
// */
//public class MdsToUiAdapter implements IDataListener {
//
//	protected ColouredTextField[][] txtFields;
//
//	public MdsToUiAdapter(ColouredTextField[][] txtFields) {
//		this.txtFields = txtFields;
//	}
//
//	@Override
//	public void updateOccurred(DataType status) {
//		if (status == null || txtFields == null)
//			return;
//
//		for (int set = 0; set < status.getData().length; set++) {
//			Point3d val = status.getAxis(set);
//			txtFields[0][set].setValue(val.x);
//			txtFields[1][set].setValue(val.y);
//			txtFields[2][set].setValue(val.z);
//		}
//
//		if (pnlGyroManual != null)
//			pnlGyroManual.repaint();
//	}
//
//}
