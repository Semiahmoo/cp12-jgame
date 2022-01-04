package ca.hapke.controller.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import ca.hapke.controller.serial.ConnectedSerialPort;
import ca.hapke.controller.serial.SerialCommManager;
import ca.hapke.gyro.data.DataCluster;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

/**
 * 
 * @author Mr. Hapke
 */
public abstract class AccelGyroFrame extends JFrame {

	private class StartStopListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

//			MdsToUdpAdapter adapter = (MdsToUdpAdapter) sender;
			if (btnStartStop.getText().equalsIgnoreCase("Start")) {

				// updatingThread = new UpdatingThread();
				// updatingThread.start();
				Component selectedComponent = tabsContainer.getSelectedComponent();
				boolean success = false;
				if (selectedComponent == pnlCom) {
					success = doStartSerial();
				} else if (selectedComponent == pnlUdp) {
					success = doStartUdp();
				}
				if (success) {
					postStartMethods();
					btnStartStop.setText("Stop");
				}
			} else {
				doStop();

				btnStartStop.setText("Start");
			}
		}

	}

	private static final long serialVersionUID = 8381316893140758524L;
	protected RotatingCube pnlCube;
	protected DataCluster cluster = new DataCluster();

	// GENERAL UI
	protected JTabbedPane tabsContainer;
	protected JButton btnStartStop;

	// UDP UI ELEMENTS
	protected JPanel pnlUdp;

	// USB->COM UI ELEMENTS
	protected JPanel pnlCom;
	protected JButton btnSerialScan;
	protected JScrollPane sclSerialPorts;
	protected JList<String> lstSerialPorts;

	// USB->COM MANAGEMENT
	protected SerialCommManager serialManager = new SerialCommManager();
	protected ConnectedSerialPort serialConnection = null;

//	protected AngleFormatter af;

	protected JPanel contentPane;

	public AccelGyroFrame(int fWidth, int fHeight) {
//		af = new AngleFormatter();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, fWidth, fHeight);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		tabsContainer = new JTabbedPane(JTabbedPane.TOP);
		tabsContainer.setBounds(10, 11, 507, 170);
		contentPane.add(tabsContainer);

		pnlCom = new JPanel();
		tabsContainer.addTab("Wired-USB", null, pnlCom, null);
		/* -------------------------SERIAL VIA USB->COM WIDGETS -------------------- */

		btnSerialScan = new JButton("Scan");
		btnSerialScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serialManager.updatePorts();
			}
		});
		pnlCom.setLayout(null);
		btnSerialScan.setBounds(5, 5, 146, 20);
		pnlCom.add(btnSerialScan);
		EventList<String> portsProxied = GlazedListsSwing.swingThreadProxyList(serialManager.getPortsEvents());
		DefaultEventListModel<String> portsModel = new DefaultEventListModel<>(portsProxied);

		sclSerialPorts = new JScrollPane();
		sclSerialPorts.setBounds(5, 30, 146, 105);
		pnlCom.add(sclSerialPorts);

		lstSerialPorts = new JList<>();
		sclSerialPorts.setViewportView(lstSerialPorts);
		lstSerialPorts.setModel(portsModel);
		lstSerialPorts.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lstSerialPorts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pnlUdp = new JPanel();
		tabsContainer.addTab("Network-UDP", null, pnlUdp, null);
		pnlUdp.setLayout(null);

		/* --------------------VARIOUS ENABLE/DISABLE WIDGETS -------------------- */

		btnStartStop = new JButton("Start");
		btnStartStop.addActionListener(new StartStopListener());
		btnStartStop.setBounds(522, 10, 120, 30);
		contentPane.add(btnStartStop);
	}

	protected abstract void doStop();

	protected abstract void postStartMethods();

	protected abstract boolean doStartUdp();

	protected abstract boolean doStartSerial();

	public void addElements(int xBase, int yBase) {

		pnlCube = new RotatingCube(cluster);
		pnlCube.setBounds(xBase, yBase, 780, 360);
		contentPane.add(pnlCube);

	}

	@Override
	public void dispose() {
		super.dispose();
		doStop();
	}
}