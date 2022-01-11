package ca.hapke.controller.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.hapke.controller.data.DataTransmit;
import ca.hapke.controller.serial.ConnectedSerialPort;
import ca.hapke.controller.serial.SerialDataTransmit;
import ca.hapke.controller.udp.UdpDataTransmit;
import ca.hapke.gyro.ArcadeButtonUpdater;
import ca.hapke.gyro.GyroDataUpdater;
import ca.hapke.gyro.JoystickDataUpdater;
import jssc.SerialPortException;

/**
 * 
 * @author Mr. Hapke
 */
public class FrmControllerSender extends AccelGyroFrame {

	private static final long serialVersionUID = 1432629860045856616L;

	// DATA TRANSMISSION STUFF
	private DataTransmit transmitter = new UdpDataTransmit(cluster);
	private GyroDataUpdater gdu;
	private JoystickDataUpdater jdu;
	protected ArcadeButtonUpdater abu;

	// UDP UI ELEMENTS
//	private JPanel pnlUdp;
	private static final String DEFAULT_IP = "192.168.137.2";
	private JTextField txtPort;
	private JTextField txtIp;
	private JComboBox<String> cmbHost;
	private JRadioButton radIp;
	private JRadioButton radHostname;
	private int destPort = 8002;

	// USB->COM UI ELEMENTS
//	private JPanel pnlCom;
//	private SerialCommManager serialManager = new SerialCommManager();
//	private JButton btnSerialScan;
//	private JScrollPane sclSerialPorts;
//
//	private JList<SerialPort> lstSerialPorts;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					FrmControllerSender frame = new FrmControllerSender();
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
	public FrmControllerSender() {
		super(815, 600);
		setTitle("Pii-Mote Controller");
		addElements(10, 190);

		/* ------------------- NETWORK UDP WIDGETS ------------------------------- */

		radHostname = new JRadioButton("Hostname");
		radHostname.setHorizontalAlignment(SwingConstants.LEFT);
//		radHostname.setSelected(true);
		radHostname.setBounds(6, 7, 109, 23);
		pnlUdp.add(radHostname);

		radIp = new JRadioButton("IP");
//		radIp.setSelected(false);
		radIp.setSelected(true);
		radIp.setBounds(6, 33, 109, 23);
		pnlUdp.add(radIp);

		txtIp = new JTextField();
		txtIp.setText(DEFAULT_IP);
		txtIp.setBounds(121, 34, 225, 20);
		txtIp.setColumns(10);
		pnlUdp.add(txtIp);

		cmbHost = new JComboBox<String>();
		cmbHost.setEditable(true);
		List<String> hosts = new ArrayList<>();
		for (int i = 1; i <= 31; i++) {
			hosts.add("SEMI-R123-W0" + (i < 10 ? "0" : "") + i + ".sd36.bc.ca");
		}
//		hosts.add("SEMI-SURF-W006");
		cmbHost.setModel(new DefaultComboBoxModel(hosts.toArray()));
		cmbHost.setBounds(121, 8, 225, 20);
		pnlUdp.add(cmbHost);

		ButtonGroup group = new ButtonGroup();
		group.add(radIp);
		group.add(radHostname);

		ChangeListener ipHostListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateIpHostUi();
			}

		};
		radIp.addChangeListener(ipHostListener);
		radHostname.addChangeListener(ipHostListener);

		updateIpHostUi();

		txtPort = new JTextField();
		txtPort.setText("" + destPort);
		txtPort.setBounds(366, 8, 86, 48);
		pnlUdp.add(txtPort);
		txtPort.setColumns(10);

		JLabel label = new JLabel(":");
		label.setBounds(351, 8, 10, 48);
		pnlUdp.add(label);

		/* --------------------VARIOUS ENABLE/DISABLE WIDGETS -------------------- */

		JButton btnStartButtons = new JButton("Start Buttons");
		btnStartButtons.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				abu = new ArcadeButtonUpdater(cluster);
			}
		});
		btnStartButtons.setBounds(527, 60, 120, 31);
		contentPane.add(btnStartButtons);

		JButton btnStartGyro = new JButton("Start Gyro");
		btnStartGyro.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gdu = new GyroDataUpdater(cluster);
			}
		});
		btnStartGyro.setBounds(657, 10, 120, 31);
		contentPane.add(btnStartGyro);

		JButton btnStartJoystick = new JButton("Start Joystick");
		btnStartJoystick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jdu = new JoystickDataUpdater(cluster);
				jdu.start();
			}
		});
		btnStartJoystick.setBounds(657, 60, 120, 31);
		contentPane.add(btnStartJoystick);

	}

	private void updateIpHostUi() {
		boolean ip = radIp.isSelected();
		cmbHost.setEnabled(!ip);
		txtIp.setEnabled(ip);
	}

	public String getIp() {
		if (radIp.isSelected())
			return txtIp.getText();
		else
			return cmbHost.getSelectedItem().toString();
	}

	protected boolean doStartSerial() {
		System.out.println("SERIAL!");
		String port = lstSerialPorts.getSelectedValue();
		if (port != null) {
			try {
				ConnectedSerialPort connection = serialManager.connect(port);
				SerialDataTransmit serialTransmitter = new SerialDataTransmit(cluster, connection);
				transmitter = serialTransmitter;
				return true;
			} catch (SerialPortException e) {
				JOptionPane.showMessageDialog(this, "Serial Port Failed to open:\n" + e.getMessage(), "Connect Failed", JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}

	protected boolean doStartUdp() {
		System.out.println("UDP!");
		String target = getIp();
		int dots = 0;
		for(int i = 0; i < target.length(); i++) {
			char c = target.charAt(i);
			if (c == '.')
				dots++;
		}
		if (dots != 3) {
			JOptionPane.showMessageDialog(rootPane, "Make sure you select a target first");
			return false;
		} else {
			UdpDataTransmit udpTrans = new UdpDataTransmit(cluster);
			udpTrans.setTarget(getIp(), Integer.parseInt(txtPort.getText()));
			transmitter = udpTrans;
			return true;
		}
	}

	protected void postStartMethods() {
		if (transmitter != null) {
			transmitter.start();
		}
	}

	protected void doStop() {
		// updatingThread.kill();
//				adapter.disableUdp();
		transmitter.stop();
		transmitter = null;
		if (gdu != null)
			gdu.stop();
		if (jdu != null)
			jdu.kill();
	}

}
