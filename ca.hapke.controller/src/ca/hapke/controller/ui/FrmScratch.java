//package ca.hapke.controller.ui;
//
//import java.awt.Component;
//import java.awt.EventQueue;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.ButtonGroup;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JList;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JScrollPane;
//import javax.swing.JTabbedPane;
//import javax.swing.JTextField;
//import javax.swing.ListSelectionModel;
//import javax.swing.SwingConstants;
//import javax.swing.border.EmptyBorder;
//import javax.swing.border.EtchedBorder;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//
//import ca.hapke.controller.serial.SerialCommManager;
//import ca.odell.glazedlists.EventList;
//import ca.odell.glazedlists.swing.DefaultEventListModel;
//import ca.odell.glazedlists.swing.GlazedListsSwing;
//import jssc.SerialPort;
//
///**
// * @author Mr. Hapke
// */
//public class FrmScratch extends JFrame {
//
//	private static final long serialVersionUID = -5622834879900655050L;
//
//	private JPanel mainPane;
//
//	private JTextField txtPort;
//	private JTextField txtIp;
//	private JComboBox<String> cmbHost;
//	private JRadioButton radIp;
//	private JRadioButton radHostname;
//	private int destPort = 8002;
//
//
//	private static final String _10_50 = "10.50.";
//	private JPanel pnlUdp;
//	private JTabbedPane tabsContainer;
//	
//	
//
//	private JPanel pnlCom;
//	private SerialCommManager comManager = new SerialCommManager();
//	private JButton btnScan;
//	private JScrollPane sclPorts;
//	
//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					FrmScratch frame = new FrmScratch();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}
//
//	/**
//	 * Create the frame.
//	 */
//	public FrmScratch() {
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setBounds(100, 100, 706, 300);
//				
//		
//		mainPane = new JPanel();
//		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
//		setContentPane(mainPane);
//		mainPane.setLayout(null);
//		
//		tabsContainer = new JTabbedPane(JTabbedPane.TOP);
//		tabsContainer.setBounds(10, 11, 507, 170);
//		mainPane.add(tabsContainer);
//
//		pnlCom = new JPanel();
//		tabsContainer.addTab("Wired-USB", null, pnlCom, null);
//		
//		pnlUdp = new JPanel();
//		tabsContainer.addTab("Network-UDP", null, pnlUdp, null);
//		pnlUdp.setLayout(null);
//		
//		/* ------------------- NETWORK UDP WIDGETS ------------------------------- */
//		
//		radHostname = new JRadioButton("Hostname");
//		radHostname.setHorizontalAlignment(SwingConstants.LEFT);
//		radHostname.setSelected(true);
//		radHostname.setBounds(6, 7, 109, 23);
//		pnlUdp.add(radHostname);
//
//		radIp = new JRadioButton("IP");
//		radIp.setSelected(false);
////		radIp.setSelected(true);
//		radIp.setBounds(6, 33, 109, 23);
//		pnlUdp.add(radIp);
//
//		txtIp = new JTextField();
//		txtIp.setText(_10_50);
//		txtIp.setBounds(121, 34, 225, 20);
//		txtIp.setColumns(10);
//		pnlUdp.add(txtIp);
//
//		cmbHost = new JComboBox<String>();
//		cmbHost.setEditable(true);
//		List<String> hosts = new ArrayList<>();
//		for (int i = 1; i <= 31; i++) {
//			hosts.add("SEMI-R123-W0" + (i < 10 ? "0" : "") + i + ".sd36.bc.ca");
//		}
////		hosts.add("SEMI-SURF-W006");
//		cmbHost.setModel(new DefaultComboBoxModel(hosts.toArray()));
//		cmbHost.setBounds(121, 8, 225, 20);
//		pnlUdp.add(cmbHost);
//
//		ButtonGroup group = new ButtonGroup();
//		group.add(radIp);
//		group.add(radHostname);
//
//		ChangeListener ipHostListener = new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				updateIpHostUi();
//			}
//
//		};
//		radIp.addChangeListener(ipHostListener);
//		radHostname.addChangeListener(ipHostListener);
//
//		updateIpHostUi();
//
//		txtPort = new JTextField();
//		txtPort.setText("" + destPort);
//		txtPort.setBounds(366, 8, 86, 48);
//		pnlUdp.add(txtPort);
//		txtPort.setColumns(10);
//		
//		JLabel label = new JLabel(":");
//		label.setBounds(351, 8, 10, 48);
//		pnlUdp.add(label);
//		
//		/* -------------------------SERIAL VIA USB->COM WIDGETS -------------------- */
//		
//		btnScan = new JButton("Scan");
//		btnScan.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				comManager.updatePorts();
//			}
//		});
//		pnlCom.setLayout(null);
//		btnScan.setBounds(5, 5, 146, 20);
//		pnlCom.add(btnScan);
//		EventList<SerialPort> portsProxied = GlazedListsSwing.swingThreadProxyList(comManager.getPortsEvents());
//		DefaultEventListModel<SerialPort> portsModel = new DefaultEventListModel<>(portsProxied);
//		
//		sclPorts = new JScrollPane();
//		sclPorts.setBounds(5, 30, 146, 105);
//		pnlCom.add(sclPorts);
//		
//		JList<SerialPort> lstPorts = new JList<>();
//		sclPorts.setViewportView(lstPorts);
//		lstPorts.setModel(portsModel);
//		lstPorts.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
//		lstPorts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		
//		JButton btnNewButton = new JButton("Go");
//		btnNewButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				Component selectedComponent = tabsContainer.getSelectedComponent();
//				if (selectedComponent == pnlCom) {
//					System.out.println("COM!");
//				} else if (selectedComponent == pnlUdp) {
//					System.out.println("UDP!");
//				}
//			}
//		});
//		btnNewButton.setBounds(158, 227, 89, 23);
//		mainPane.add(btnNewButton);
//
//
//	}
//
//	protected void updateIpHostUi() {
//		// TODO Auto-generated method stub
//		
//	}
//}
