/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package preloader;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import javax.swing.JProgressBar;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JScrollPane;
import javax.swing.JEditorPane;

public class Updater extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private JProgressBar m_updateProgressBar;
	private JButton m_btnUpdate;

	private GitUpdater m_gitUpdater = new GitUpdater();
	private JScrollPane scrollPane;
	private JEditorPane dtrpnPreparingNews;

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
					Updater frame = new Updater();
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
	public Updater()
	{

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
		}

		setResizable(false);
		setTitle("JevaEngine Updater");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 449, 301);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[425px,grow]", "[][][14px][141px,grow][][][][][][][][]"));

		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 3 1 5,grow");

		dtrpnPreparingNews = new JEditorPane();
		dtrpnPreparingNews.setEditable(false);
		dtrpnPreparingNews.setText("Preparing News...");
		try
		{
			dtrpnPreparingNews.setPage("http://jeremywildsmith.github.io/jevaengineupdate.html");
		} catch (IOException e1)
		{
			dtrpnPreparingNews.setContentType("text/html");
			dtrpnPreparingNews.setText("<center><b>Error In Query</b></center>");
		}
		scrollPane.setViewportView(dtrpnPreparingNews);

		m_btnUpdate = new JButton("Start");
		m_btnUpdate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				m_btnUpdate.setEnabled(false);
				m_gitUpdater.begin();
			}
		});

		JButton btnNewButton_1 = new JButton("Cancel");
		btnNewButton_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Updater.this.dispatchEvent(new WindowEvent(Updater.this, WindowEvent.WINDOW_CLOSING));
			}
		});

		m_updateProgressBar = new JProgressBar();
		contentPane.add(m_updateProgressBar, "cell 0 9,growx");
		contentPane.add(btnNewButton_1, "flowx,cell 0 10,alignx right");
		contentPane.add(m_btnUpdate, "cell 0 10,alignx right");
	}

	private class GitUpdater
	{
		private Thread m_updateThread;;
		private volatile boolean m_isRunning = false;

		public void begin()
		{
			if (m_isRunning)
				return;

			m_updateThread = new UpdateThread();
			m_isRunning = true;
			m_updateThread.start();
		}

		private class UpdateThread extends Thread implements ProgressMonitor
		{
			@Override
			public void run()
			{
				try
				{
					File releaseFolder = new File("data");

					if (releaseFolder.exists())
					{
						PullCommand clone = Git.open(releaseFolder).pull();
						clone.setProgressMonitor(this);
						clone.call();
					} else
					{
						CloneCommand clone = Git.cloneRepository();
						clone.setDirectory(new File("data"));
						clone.setProgressMonitor(this);
						clone.setURI("https://github.com/JeremyWildsmith/JevaEngine.git");
						clone.call();

						JOptionPane.showMessageDialog(Updater.this, "The update has been completed!");
					}

					m_isRunning = false;
					Updater.this.m_btnUpdate.setEnabled(true);
					Updater.this.setVisible(false);
					new Configuration().setVisible(true);

				} catch (GitAPIException | JGitInternalException | IOException e)
				{
					m_isRunning = false;
					Updater.this.m_updateProgressBar.setValue(0);
					Updater.this.m_btnUpdate.setEnabled(true);
					JOptionPane.showMessageDialog(Updater.this, "An error occured during update: " + e.toString());
				}
			}

			@Override
			public void beginTask(String title, final int totalWork)
			{
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							m_updateProgressBar.setValue(0);
							m_updateProgressBar.setMaximum(totalWork);
						}
					});
				} catch (InvocationTargetException | InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}

			@Override
			public void endTask()
			{
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							m_updateProgressBar.setValue(0);
						}
					});
				} catch (InvocationTargetException | InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean isCancelled()
			{
				return false;
			}

			@Override
			public void start(final int totalTasks)
			{
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							m_updateProgressBar.setValue(0);
							m_updateProgressBar.setMaximum(0);
						}
					});
				} catch (InvocationTargetException | InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}

			@Override
			public void update(final int work)
			{
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							m_updateProgressBar.setValue(m_updateProgressBar.getValue() + work);
						}
					});
				} catch (InvocationTargetException | InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}
}
