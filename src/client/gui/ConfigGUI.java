package client.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.awt.FlowLayout;
import javax.swing.JLayeredPane;
import javax.swing.JTextPane;
import java.awt.CardLayout;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.event.ListSelectionListener;

import client.Client;

import javax.swing.event.ListSelectionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.DropMode;
import javax.swing.JSpinner;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ConfigGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtPath;
	private JTextArea txtLog;
	/**
	 * Launch the application.
	 */
	public static void setLookAndFeel() {
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {
			Client.log("[WARN] Error when setting look and feel.");
		}
	}

	/**
	 * Create the frame.
	 */
	public ConfigGUI() {
		setTitle("OpenDrive client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 543, 219);
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
		gbl_panelConfig.rowHeights = new int[]{28, 0, 0, 0};
		gbl_panelConfig.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_panelConfig.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		panelConfig.setLayout(gbl_panelConfig);
		
		JLabel lblNewLabel = new JLabel("Ruta de carpeta compartida");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panelConfig.add(lblNewLabel, gbc_lblNewLabel);
		
		txtPath = new JTextField();
		GridBagConstraints gbc_txtPath = new GridBagConstraints();
		gbc_txtPath.insets = new Insets(0, 0, 5, 5);
		gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPath.gridx = 1;
		gbc_txtPath.gridy = 0;
		panelConfig.add(txtPath, gbc_txtPath);
		txtPath.setColumns(10);
		
		JButton btnNewButton = new JButton("Buscar");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(panel);
				if (returnVal==JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					txtPath.setText(f.getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		panelConfig.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Intervalo de actualización");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panelConfig.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JSpinner spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 1;
		panelConfig.add(spinner, gbc_spinner);
		
		JButton btnAplicar = new JButton("Aplicar");
		btnAplicar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Client.dirRoute=txtPath.getText();
				Client.refreshRate = (int) spinner.getModel().getValue()* 1000;
			}
		});
		GridBagConstraints gbc_btnAplicar = new GridBagConstraints();
		gbc_btnAplicar.anchor = GridBagConstraints.SOUTH;
		gbc_btnAplicar.gridx = 2;
		gbc_btnAplicar.gridy = 2;
		panelConfig.add(btnAplicar, gbc_btnAplicar);
		JPanel panelLog = new JPanel();
		panel.add(panelLog, "LOG");
		panelLog.setLayout(new GridLayout(0, 1, 0, 0));
		
		txtLog = new JTextArea();
		txtLog.setEditable(false);
		txtLog.setFont(new Font("Monospaced", Font.PLAIN, 10));
		txtLog.setText("1s: Obtenido registro del servidor\r\n5s: Obteniendo archivo de cliente 1.1.1.1\r\n10s: Finalizada actualización\r\n");
		panelLog.add(txtLog);
		
		//this.pack();
	}
	
	public void addLog(String s) {
		txtLog.setText(txtLog.getText()+s+"\n");
	}
}
