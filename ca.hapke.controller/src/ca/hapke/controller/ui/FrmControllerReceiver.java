package ca.hapke.controller.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ca.hapke.controller.data.DataReceive;
import ca.hapke.controller.data.IDataReceiveListener;
import ca.hapke.controller.data.ReceiveMode;
import ca.hapke.controller.serial.SerialDataReceive;
import ca.hapke.controller.udp.UdpDataReceive;
import jssc.SerialPortException;

/**
 * @author Mr. Hapke
 */
public class FrmControllerReceiver extends AccelGyroFrame {

	private static final long serialVersionUID = -3471270600310979442L;
	private JTextField txtPort;
	private ReceiveMode mode = ReceiveMode.Stopped;
	protected DataReceive receiverServer;
	protected Queue<String> messages = new ConcurrentLinkedQueue<>();

	private JButton btnSerialListen;
	private static final String LISTEN = "Listen...";
	private static final String DISCONNECT = "Disconnect";

	private IDataReceiveListener statusListener = new IDataReceiveListener() {
		@Override
		public void serverOnline() {
			mode = ReceiveMode.Running;
		}

		@Override
		public void serverOffline() {
			mode = ReceiveMode.Stopped;
		}

		@Override
		public void accelGyroUpdated() {
		}

		@Override
		public void sentenceReceived(String sentence) {
			messages.add(sentence);
		}

		@Override
		public void serverAbort(String msg) {
//			messages.add("ABORT:" + msg);
			System.err.println("ABORT:" + msg);
		}
	};
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					AccelGyroFrame frame = new FrmControllerReceiver();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrmControllerReceiver() {
		super(850, 820);
		addElements(10, 190);
		setTitle("UDP Receiver");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* -------------------------SERIAL VIA USB->COM WIDGETS -------------------- */
		// Scan and ListBox are added by super class, because that's common on both
		// sides
//		btnSerialListen = new JButton(LISTEN);
//		btnSerialListen.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				if (btnSerialListen.getText().equalsIgnoreCase(LISTEN)) {
//					if (serialConnection == null) {
//						String port = lstSerialPorts.getSelectedValue();
//
//						serialConnection = serialManager.connect(port);
//						if (serialConnection != null) {
//
//							btnSerialListen.setText(DISCONNECT);
//						}
//					}
//				} else {
//					serialConnection.close();
//					serialConnection = null;
//					btnSerialListen.setText(LISTEN);
//				}
//			}
//		});
//		btnSerialListen.setBounds(11, 235, 145, 23);
//		contentPane.add(btnSerialListen);

		/* -------------------------UDP VIA NETWORK WIDGETS -------------------- */
		JLabel lblInputPort = new JLabel("Port:");
		lblInputPort.setHorizontalAlignment(SwingConstants.TRAILING);
		lblInputPort.setBounds(10, 20, 70, 35);
		pnlUdp.add(lblInputPort);

		txtPort = new JTextField();
		txtPort.setText("8002");
		txtPort.setBounds(115, 20, 80, 35);
		pnlUdp.add(txtPort);
		txtPort.setColumns(10);

		JButton btnBaseline = new JButton("Baseline");
		btnBaseline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pnlCube.rebaseline();
			}
		});
		btnBaseline.setBounds(522, 45, 120, 30);
		contentPane.add(btnBaseline);
	}


	@Override
	protected void doStop() {
		if (receiverServer != null)
			receiverServer.shutdownServer();
		if (serialConnection != null) {
			serialConnection.close();
			serialConnection = null;
		}
	}

	@Override
	protected void postStartMethods() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean doStartUdp() {
		int port = Integer.parseInt(txtPort.getText());
		receiverServer = new UdpDataReceive(cluster, port);
		receiverServer.add(statusListener);
		boolean val = receiverServer.activateServer();

		return val;
	}

	@Override
	protected boolean doStartSerial() {
		String port = lstSerialPorts.getSelectedValue();

		try {
			serialConnection = serialManager.connect(port);
			if (serialConnection != null) {
				receiverServer = new SerialDataReceive(cluster, serialConnection);
				receiverServer.add(statusListener);
				boolean val = receiverServer.activateServer();
				return val;
			}
		} catch (SerialPortException e) {
			JOptionPane.showMessageDialog(this, "Serial Port Failed to open:\n" + e.getMessage(), "Connect Failed", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

}
