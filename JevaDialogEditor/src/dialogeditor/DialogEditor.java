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
package dialogeditor;

import java.awt.EventQueue;

import jeva.config.VariableStore;
import jeva.game.DialogPath;
import jeva.game.DialogPath.Answer;
import jeva.game.DialogPath.Query;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;

/*
 * 
 * This code is a _complete_ mess, it was quickly hacked up, and the GUI Code Generation
 * finished it. It is not worth maintaining this code, if you must, you are _way_ better of
 * just rewriting the entire thing.
 * 
 */
public class DialogEditor
{

	private JFrame frame;
	private JTextField txtEvent;
	private JTextArea txtText;
	private JComboBox<String> lstId;
	final JTree dialogTree = new JTree();

	private ArrayList<QueryNode> m_nodes = new ArrayList<QueryNode>();

	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					DialogEditor window = new DialogEditor();
					window.frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	
	public DialogEditor()
	{
		initialize();
	}

	protected int getQueryCount(QueryNode query)
	{
		for (AnswerNode answer : query.getAnswers())
		{
			if (answer.getDialog() != null)
				return 1 + getQueryCount(answer.getDialog());
		}

		return 0;
	}

	protected int getQueryCount()
	{
		int bount = 0;

		for (QueryNode query : m_nodes)
		{
			bount += 1 + getQueryCount(query);
		}
		return bount;
	}

	public void setQueryMax(QueryNode query, int length)
	{
		if (query.getId() >= length)
			query.setId(-1);

		for (AnswerNode answer : query.getAnswers())
		{
			if (answer.getDialog() != null)
				setQueryMax(answer.getDialog(), length);
		}
	}

	public void setQueryMax(int length)
	{
		for (QueryNode query : m_nodes)
			setQueryMax(query, length);
	}

	public void reserveIndex(QueryNode query, int index)
	{
		if (query.getId() == index)
			query.setId(-1);

		for (AnswerNode answer : query.getAnswers())
		{
			if (answer.getDialog() != null)
				reserveIndex(answer.getDialog(), index);
		}
	}

	public DefaultMutableTreeNode[] addNode(Node parent, Node child)
	{
		if (parent == null)
		{
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(child);
			m_nodes.add((QueryNode) child);
			((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(node, (DefaultMutableTreeNode) (((DefaultTreeModel) dialogTree.getModel()).getRoot()), 0);

			return new DefaultMutableTreeNode[]
			{ node };
		} else
		{
			ArrayList<DefaultMutableTreeNode> createdNodes = new ArrayList<DefaultMutableTreeNode>();

			for (DefaultMutableTreeNode node : getQuery(parent))
			{
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(child);
				if (parent instanceof QueryNode)
				{
					((QueryNode) node.getUserObject()).addAnswer((AnswerNode) child);
					((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(newNode, node, 0);

				} else if (parent instanceof AnswerNode)
				{
					AnswerNode answer = (AnswerNode) node.getUserObject();

					if (answer.getDialog() != null)
						throw new RuntimeException("Answer cannot have multiple dialogs.");

					answer.setDialog((QueryNode) child);
					((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(newNode, node, 0);
				} else
					throw new RuntimeException("Parent/Child is neither and instance of QueryNode or AnswerNode.");

				createdNodes.add(newNode);
			}

			return createdNodes.toArray(new DefaultMutableTreeNode[createdNodes.size()]);
		}
	}

	public void removeNode(Node parent, Node child)
	{
		DefaultMutableTreeNode[] nodes = getQuery(child);
		if (parent == null && m_nodes.indexOf(child) >= 0)
		{
			m_nodes.remove(m_nodes.indexOf(child));
		} else if (parent instanceof QueryNode)
		{
			((QueryNode) parent).removeAnswer((AnswerNode) child);
		} else if (parent instanceof AnswerNode)
		{
			((AnswerNode) parent).setDialog(null);
		}

		for (DefaultMutableTreeNode node : getQuery(child))
			((DefaultTreeModel) dialogTree.getModel()).removeNodeFromParent(node);
	}

	public DefaultMutableTreeNode[] getQuery(Node node, DefaultMutableTreeNode root)
	{
		ArrayList<DefaultMutableTreeNode> queries = new ArrayList<DefaultMutableTreeNode>();

		for (int i = 0; i < root.getChildCount(); i++)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);

			if (child.getUserObject() == node)
				queries.add(child);

			queries.addAll(Arrays.asList(getQuery(node, child)));
		}

		return queries.toArray(new DefaultMutableTreeNode[queries.size()]);
	}

	public DefaultMutableTreeNode[] getQuery(Node node)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) dialogTree.getModel().getRoot();

		if (root != null)
			return getQuery(node, root);

		return null;
	}

	public DefaultMutableTreeNode getQuery(int id, DefaultMutableTreeNode root)
	{
		for (int i = 0; i < root.getChildCount(); i++)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);

			if (child.getUserObject() instanceof QueryNode)
			{
				if (((QueryNode) child.getUserObject()).getId() == id && id >= 0)
					return new DefaultMutableTreeNode(child.getUserObject());
			}

			DefaultMutableTreeNode node = getQuery(id, child);
			if (node != null)
				return node;
		}

		return null;
	}

	public DefaultMutableTreeNode getQuery(int id)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) dialogTree.getModel().getRoot();

		if (root != null)
			return getQuery(id, root);

		return null;
	}

	public void fillNode(DefaultMutableTreeNode dest)
	{
		QueryNode q = (QueryNode) dest.getUserObject();

		for (AnswerNode a : q.getAnswers())
		{
			DefaultMutableTreeNode answerNode = new DefaultMutableTreeNode(a);
			((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(answerNode, dest, 0);

			if (a.getDialog() != null)
			{
				DefaultMutableTreeNode dialogNode = new DefaultMutableTreeNode(a.getDialog());
				((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(dialogNode, answerNode, 0);
				fillNode(dialogNode);
			}
		}
	}

	public QueryNode openQuery(DefaultMutableTreeNode parent, Query query)
	{
		AnswerNode internalParentAnswer = null;

		if (parent != null && parent.getUserObject() instanceof AnswerNode)
			internalParentAnswer = ((AnswerNode) parent.getUserObject());

		QueryNode internalQuery = new QueryNode();
		internalQuery.setText(query.getQuery());
		internalQuery.setEvent(query.getEventCode());
		internalQuery.setId(query.getId());

		DefaultMutableTreeNode queryNode = new DefaultMutableTreeNode(internalQuery);

		((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(queryNode, parent, 0);

		for (Answer a : query.getAnswers())
		{
			AnswerNode internalAnswer = new AnswerNode();
			DefaultMutableTreeNode answerNode = new DefaultMutableTreeNode(internalAnswer);

			internalAnswer.setText(a.getAnswer());
			internalAnswer.setEvent(a.getEventCode());

			internalQuery.addAnswer(internalAnswer);
			((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(answerNode, queryNode, 0);

			if (a.getNextQuery() != null)
			{
				DefaultMutableTreeNode next = getQuery(a.getNextQuery().getId());

				if (next == null)
					openQuery(answerNode, a.getNextQuery());
				else
				{
					internalAnswer.setDialog(((QueryNode) next.getUserObject()));
					((DefaultTreeModel) dialogTree.getModel()).insertNodeInto(next, answerNode, 0);
					fillNode(next);
				}
			}
		}

		if (internalParentAnswer != null)
			internalParentAnswer.setDialog(internalQuery);

		return internalQuery;
	}

	public void openQuery(Query[] queries)
	{
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode("Dialog");
		dialogTree.setModel(new DefaultTreeModel(parent));
		m_nodes.clear();

		for (Query q : queries)
		{
			m_nodes.add(openQuery(parent, q));
		}
		// ((DefaultTreeModel)dialogTree.getModel())
		dialogTree.updateUI();

		lstId.removeAllItems();
		lstId.addItem("X");
		lstId.setSelectedIndex(0);

		int queryCount = getQueryCount();

		for (int i = 0; i < queryCount; i++)
			lstId.addItem(String.valueOf(i));
	}

	
	private void initialize()
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 761, 308);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[827px]", "[153px][][][][]"));

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, "cell 0 0,grow");

		dialogTree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (dialogTree.getSelectionPath() != null)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) dialogTree.getSelectionPath().getLastPathComponent();
					Node dialogNode = (node.getUserObject() == null || !(node.getUserObject() instanceof Node) ? null : (Node) node.getUserObject());

					if (dialogNode != null)
					{
						txtText.setText(dialogNode.getText());
						txtEvent.setText(String.valueOf(dialogNode.getEventCode()));

						lstId.setEnabled(dialogNode instanceof QueryNode);

						if (dialogNode instanceof QueryNode)
						{
							lstId.setSelectedIndex(((QueryNode) dialogNode).getId() + 1);
						}

					}
				}
			}
		});
		dialogTree.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent event)
			{
				if (dialogTree.getSelectionPath() != null)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) dialogTree.getSelectionPath().getLastPathComponent();

					Node child = ((DefaultMutableTreeNode) node).getUserObject() instanceof Node ? (Node) ((DefaultMutableTreeNode) node).getUserObject() : null;
					Node parent = node.getParent() != null && ((DefaultMutableTreeNode) node.getParent()).getUserObject() instanceof Node ? ((Node) ((DefaultMutableTreeNode) node.getParent()).getUserObject()) : null;

					if (event.getKeyCode() == KeyEvent.VK_DELETE)
					{
						removeNode(parent, child);
					} else if (event.getKeyCode() == KeyEvent.VK_INSERT && event.isShiftDown() && child instanceof AnswerNode)
					{
						String selectedLink = JOptionPane.showInputDialog("Enter the ID of the node you would like to link to:");

						if (selectedLink != null)
						{
							int selectedLinkId = 0;
							try
							{
								selectedLinkId = Integer.parseInt(selectedLink);

								DefaultMutableTreeNode q = getQuery(selectedLinkId);

								if (q != null)
								{
									DefaultMutableTreeNode[] createdNodes = addNode(child, (QueryNode) q.getUserObject());

									for (DefaultMutableTreeNode createdNode : createdNodes)
										fillNode(createdNode);

								} else
									JOptionPane.showMessageDialog(DialogEditor.this.frame, "No query with that index exists.");

							} catch (NumberFormatException e)
							{
								JOptionPane.showMessageDialog(DialogEditor.this.frame, "Input is invalid - unable to parse as integer.");
							}
						}
					} else if (event.getKeyCode() == KeyEvent.VK_INSERT)
					{
						if (child instanceof AnswerNode)
						{
							if (((AnswerNode) child).getDialog() == null)
								addNode(child, new QueryNode());

						} else if (child instanceof QueryNode)
							addNode(child, new AnswerNode());
						else if (child == null)
							addNode(child, new QueryNode());

						dialogTree.updateUI();
					}

					lstId.removeAllItems();
					lstId.addItem("X");
					lstId.setSelectedIndex(0);

					int queryCount = getQueryCount();

					for (int i = 0; i < queryCount; i++)
						lstId.addItem(String.valueOf(i));

					setQueryMax(queryCount);
				}
			}
		});

		dialogTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Dialog")
		{
			{

			}
		}));
		scrollPane.setViewportView(dialogTree);

		JButton btnOpenJdf = new JButton("Open JDF");
		btnOpenJdf.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				m_nodes.clear();

				JFileChooser fileChooser = new JFileChooser();

				try
				{
					if (fileChooser.showOpenDialog(DialogEditor.this.frame) == JFileChooser.APPROVE_OPTION)
					{
						FileInputStream fis = new FileInputStream(fileChooser.getSelectedFile());

						DialogPath path = DialogPath.create(VariableStore.create(fis));

						openQuery(path.getQueries());
					}
				} catch (FileNotFoundException e)
				{
					JOptionPane.showMessageDialog(frame, "Error occured attempting to open Dialgue: " + e.toString());
				}
			}
		});
		frame.getContentPane().add(btnOpenJdf, "flowx,cell 0 1");

		JButton btnSaveJdf = new JButton("Save JDF");
		btnSaveJdf.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ArrayList<DialogPath.Query> queries = new ArrayList<DialogPath.Query>();

				for (QueryNode query : m_nodes)
				{
					queries.add(query.toJevaQuery());
				}

				VariableStore store = new VariableStore();
				new DialogPath(queries.toArray(new DialogPath.Query[queries.size()])).serialize(store);

				StringBuilder sb = new StringBuilder();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				store.serialize(baos);

				try
				{
					JFileChooser fileChooser = new JFileChooser();
					if (fileChooser.showSaveDialog(DialogEditor.this.frame) == JFileChooser.APPROVE_OPTION)
					{
						File f = fileChooser.getSelectedFile();
						FileOutputStream fos;
						fos = new FileOutputStream(f);

						fos.write(baos.toByteArray());

						fos.flush();
						fos.close();
					}
				} catch (IOException e1)
				{
					JOptionPane.showMessageDialog(frame, "Error occured attempting to store Dialgue: " + e1.toString());
				}
			}
		});
		frame.getContentPane().add(btnSaveJdf, "cell 0 1");

		JLabel lblText = new JLabel("Text:");
		frame.getContentPane().add(lblText, "flowx,cell 0 2");

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, "cell 0 2");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel.add(scrollPane_1);

		txtText = new JTextArea();
		txtText.setRows(3);
		txtText.setLineWrap(true);
		scrollPane_1.setViewportView(txtText);
		txtText.setColumns(60);

		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (dialogTree.getSelectionPath() != null)
				{
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) dialogTree.getSelectionPath().getLastPathComponent();
					Node dialogNode = (node.getUserObject() == null || !(node.getUserObject() instanceof Node) ? null : (Node) node.getUserObject());

					if (dialogNode != null)
					{
						Integer eventCode = -1;
						try
						{
							eventCode = Integer.valueOf(txtEvent.getText());
						} catch (NumberFormatException evt)
						{

						}

						dialogNode.setText(txtText.getText());
						dialogNode.setEvent(eventCode.intValue());

						if (dialogNode instanceof QueryNode)
						{
							for (QueryNode queryNode : m_nodes)
								reserveIndex(queryNode, lstId.getSelectedIndex() - 1);

							((QueryNode) dialogNode).setId(lstId.getSelectedIndex() - 1);

						}

						dialogTree.updateUI();
					}
				}
			}
		});

		JLabel lblEvent = new JLabel("Event ID:");
		frame.getContentPane().add(lblEvent, "flowx,cell 0 3");
		frame.getContentPane().add(btnApply, "cell 0 4");

		txtEvent = new JTextField();
		frame.getContentPane().add(txtEvent, "cell 0 3");
		txtEvent.setColumns(10);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		frame.getContentPane().add(horizontalStrut, "cell 0 2");

		JLabel lblId = new JLabel("ID: ");
		frame.getContentPane().add(lblId, "cell 0 2");

		lstId = new JComboBox();
		lstId.setEnabled(false);
		lstId.setModel(new DefaultComboBoxModel(new String[]
		{ "Don't Care", "0", "1", "2", "3", "4", "5" }));
		frame.getContentPane().add(lstId, "cell 0 2");
	}
}
