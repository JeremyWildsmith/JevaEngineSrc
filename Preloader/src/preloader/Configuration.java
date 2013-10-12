package preloader;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.AbstractListModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Font;

public class Configuration extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private JList<String> m_lstResolution;
	private JCheckBox m_chkFullscreen;
	private JCheckBox m_chkNoD3D;
	private JCheckBox m_chkTranslucentOptimization;
	private JCheckBox m_chkEnableOpengl;
	private JSpinner m_initialHeap;
	private JSpinner m_maxHeap;
	private JLabel lblDoNotChange;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Configuration frame = new Configuration();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Configuration()
	{

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 533, 280);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][][grow][][][][]", "[][][grow][][][][][][][][][]"));

		JLabel lblNewLabel = new JLabel("Graphics Configuration");
		contentPane.add(lblNewLabel, "cell 0 0 7 1,alignx center");

		lblDoNotChange = new JLabel("Do not change unless you experience performance issues");
		lblDoNotChange.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblDoNotChange.setForeground(Color.RED);
		contentPane.add(lblDoNotChange, "cell 1 1 6 1");

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 1 2 6 7,grow");

		m_lstResolution = new JList<String>();
		m_lstResolution.setModel(new AbstractListModel<String>()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			String[] values = new String[]
			{ "640x480", "800x600", "1024x768", "1280x800", "1280x1024" };

			public int getSize()
			{
				return values.length;
			}

			public String getElementAt(int index)
			{
				return values[index];
			}
		});
		m_lstResolution.setSelectedIndex(2);

		scrollPane.setViewportView(m_lstResolution);

		m_chkFullscreen = new JCheckBox("Full Screen");
		m_chkFullscreen.setSelected(true);
		contentPane.add(m_chkFullscreen, "flowx,cell 1 9,alignx center");

		m_chkNoD3D = new JCheckBox("Force No D3D");
		contentPane.add(m_chkNoD3D, "cell 2 9");

		m_chkEnableOpengl = new JCheckBox("Enable OpenGl");
		contentPane.add(m_chkEnableOpengl, "cell 3 9");

		m_chkTranslucentOptimization = new JCheckBox("Translucent Acceleration");
		m_chkTranslucentOptimization.setSelected(true);
		contentPane.add(m_chkTranslucentOptimization, "cell 6 9");

		JLabel lblInitialHeap = new JLabel("Initial Heap (MB)");
		contentPane.add(lblInitialHeap, "cell 1 10,alignx right");

		m_initialHeap = new JSpinner();
		m_initialHeap.setModel(new SpinnerNumberModel(512, 256, 1024, 128));
		contentPane.add(m_initialHeap, "cell 2 10,growx");

		JLabel lblMaxHeapmb = new JLabel("Max Heap (MB)");
		contentPane.add(lblMaxHeapmb, "cell 1 11");

		m_maxHeap = new JSpinner();
		m_maxHeap.setEnabled(false);
		m_maxHeap.setModel(new SpinnerNumberModel(new Integer(1280), null, null, new Integer(1)));
		contentPane.add(m_maxHeap, "cell 2 11,growx");

		JButton btnNewButton = new JButton("Start Game");

		btnNewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if (m_lstResolution.getSelectedValue() == null)
					JOptionPane.showMessageDialog(Configuration.this, "You must select a resolution before continuing.");
				else
				{
					try
					{
						String command = String.format("%s -Xms%dM -Xmx%dM", "\"" + System.getProperty("java.home") + "/bin/java\"", m_initialHeap.getValue(), m_maxHeap.getValue());
						command += " -Dsun.java2d.d3d=" + (m_chkNoD3D.isSelected() ? "false" : "true");
						command += " -Dsun.java2d.translaccel=" + (m_chkTranslucentOptimization.isSelected() ? "true" : "false");
						command += " -Dsun.java2d.opengl=" + (m_chkEnableOpengl.isSelected() ? "true" : "false");
						command += " -cp data jevarpg.net.client.Main";

						if (m_chkFullscreen.isSelected())
							command += " " + m_lstResolution.getSelectedValue();

						Runtime.getRuntime().exec(command);

						Configuration.this.dispatchEvent(new WindowEvent(Configuration.this, WindowEvent.WINDOW_CLOSING));

					} catch (Exception e)
					{
						JOptionPane.showMessageDialog(Configuration.this, "An error occured: " + e.toString());
					}
				}
			}
		});

		contentPane.add(btnNewButton, "cell 6 11,alignx center");
	}
}
