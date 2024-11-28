package client.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.FlowLayout;
import javax.swing.JLayeredPane;
import javax.swing.JTextPane;
import java.awt.CardLayout;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.DropMode;

public class ConfigGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigGUI frame = new ConfigGUI();
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
	public ConfigGUI() {
		setTitle("OpenDrive client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 527, 346);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setResizeWeight(0.5);
		contentPane.add(splitPane);
		
		JList<String> list_1 = new JList<String>();
		
		list_1.setModel(new AbstractListModel<String>() {
			String[] values = new String[] {"Configuración", "Registro"};
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		list_1.setSelectedIndex(0);
		list_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		splitPane.setLeftComponent(list_1);
		
		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new CardLayout(0, 0));
		list_1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				CardLayout cl = (CardLayout) (panel.getLayout());
				if (list_1.getSelectedIndex()==1) {
					cl.show(panel, "LOG");
				} else {
					cl.show(panel, "CONFIG");
				}
				
			}
		});
//		this.addWindowListener(new WindowAdapter() {
//			public void windowClosed(WindowEvent e) {
//				
//			}
//		});
		JPanel panelConfig = new JPanel();
		panel.add(panelConfig, "CONFIG");
		GridBagLayout gbl_panelConfig = new GridBagLayout();
		gbl_panelConfig.columnWidths = new int[] {142, 0, 0};
		gbl_panelConfig.rowHeights = new int[]{28, 0};
		gbl_panelConfig.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panelConfig.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelConfig.setLayout(gbl_panelConfig);
		
		JLabel lblNewLabel = new JLabel("Ruta de carpeta compartida");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panelConfig.add(lblNewLabel, gbc_lblNewLabel);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panelConfig.add(textField, gbc_textField);
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("Buscar");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		panelConfig.add(btnNewButton, gbc_btnNewButton);
		JPanel panelLog = new JPanel();
		panel.add(panelLog, "LOG");
		panelLog.setLayout(new GridLayout(0, 1, 0, 0));
		
		JTextArea txtrsObtenidoRegistro = new JTextArea();
		txtrsObtenidoRegistro.setEditable(false);
		txtrsObtenidoRegistro.setFont(new Font("Monospaced", Font.PLAIN, 10));
		txtrsObtenidoRegistro.setText("1s: Obtenido registro del servidor\r\n5s: Obteniendo archivo de cliente 1.1.1.1\r\n10s: Finalizada actualización\r\n");
		panelLog.add(txtrsObtenidoRegistro);
		
		this.pack();
	}
}
