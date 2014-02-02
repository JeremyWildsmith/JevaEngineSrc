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

package io.github.jevaengine.mapeditor;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceFormatException;
import io.github.jevaengine.ResourceIOException;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.config.JsonVariable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration;
import io.github.jevaengine.mapeditor.MapEditor.LayerMetaData;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.WorldDirection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 *
 * @author Jeremy
 */
public class EditorPane extends javax.swing.JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IEditorPaneListener m_listener;
	private String m_baseDirectory;
	
	/**
	 * Creates new form NewEditorPane
	 */
	public EditorPane(IEditorPaneListener listener, String baseDirectory)
	{
		m_baseDirectory = baseDirectory;
		m_listener = listener;
		initComponents();
		
		DefaultComboBoxModel<WorldDirection> modelDir = (DefaultComboBoxModel<WorldDirection>)lstEntityDirection.getModel();
		
		for(WorldDirection d : WorldDirection.ALL_MOVEMENT)
			modelDir.addElement(d);
	}
	
	public void refresh()
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				refreshLayers();
				txtWorldScript.setText(m_listener.getScript());
				txtEntityLayer.setValue(m_listener.getEntityLayer());
				
				LayerMetaData selectedLayerMeta = m_listener.getSelectedLayerBackground();
				
				String background = selectedLayerMeta.getBackground();
				txtLayerBackground.setText(background == null ? "" : background);

				txtLayerBackgroundOffsetX.setText(String.valueOf(selectedLayerMeta.getBackgroundLocation().x));
				txtLayerBackgroundOffsetY.setText(String.valueOf(selectedLayerMeta.getBackgroundLocation().y));
			}
		});
	}
	
	public void addEntity(final EditorEntity entity)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				DefaultListModel model = (DefaultListModel)lstEntities.getModel();
				model.addElement(entity);
			}
		});
	}
	
	public void clearEntities()
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				DefaultListModel model = (DefaultListModel)lstEntities.getModel();
				model.clear();
			}
		});
	}
	
	public void selectedTile(final String spriteName, final String animationName, final int x, final int y, 
								final boolean isStatic, final boolean isTraversable, final boolean allowsSplitting,
								final float fVisibility)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				txtBrushSprite.setText(spriteName);
				
				lblSelectionX.setText(String.valueOf(x));
				lblSelectionY.setText(String.valueOf(y));
				
				chkTraversable.setSelected(isTraversable);
				chkStatic.setSelected(isStatic);
				sldVisibility.setValue((int)(fVisibility * 100.0F));
				refreshTileSpriteAnimations();
				
				DefaultComboBoxModel model = (DefaultComboBoxModel)lstBrushAnimation.getModel();
				
				for(int i = 0; i < model.getSize(); i++)
				{
					if(model.getElementAt(i).equals(animationName))
					{
						model.setSelectedItem(model.getElementAt(i));
						break;
					}
				}
			}
		});
	}
	
	private void refreshTileSpriteAnimations()
	{
		try
		{
			Sprite sprite = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(txtBrushSprite.getText()).getValue(SpriteDeclaration.class));

			String[] animations = sprite.getAnimations();

			if (animations.length > 0)
			{
				DefaultComboBoxModel model = (DefaultComboBoxModel) lstBrushAnimation.getModel();
				model.removeAllElements();

				for (String animationName : animations)
					model.addElement(animationName);

				if (model.getSize() != 0)
					model.setSelectedItem(model.getElementAt(0));
				
			} else
			{
				txtBrushSprite.setText("");
				JOptionPane.showMessageDialog(this, "This sprite cannot be used as it has no animations.");
			}
		}catch(ResourceIOException e)
		{
			txtBrushSprite.setText("");
			JOptionPane.showMessageDialog(this, "Could not open sprite resource.");
		}
	}
	
	private void refreshLayers()
	{
		DefaultComboBoxModel model = (DefaultComboBoxModel)lstSelectedLayer.getModel();
		
		int layers = m_listener.getLayers();
		int previousLayer = model.getSelectedItem() == null ? 0 : model.getIndexOf(model.getSelectedItem());
		
		while(model.getSize() > layers)
			model.removeElementAt(model.getSize() - 1);
		
		while(model.getSize() < layers)
			model.addElement(String.valueOf(model.getSize()));
		
		if(previousLayer >= model.getSize())
			previousLayer = model.getSize() - 1;
		
		if(previousLayer > 0)
			model.setSelectedItem(model.getElementAt(previousLayer));
		
		m_listener.selectLayer(previousLayer);
	}
	
	private int getBrushSize()
	{
		int value = 1;
		
		try
		{
			value = Integer.parseInt(txtFillDimensions.getText());
		} catch (NumberFormatException e)
		{
		}

		value = Math.max(1, Math.min(10, value));
		txtFillDimensions.setText(String.valueOf(value));
		
		return value;
	}
	
	public Brush getBrush()
	{
		String brushSprite = txtBrushSprite.getText();
		
		Object selected = lstBrushAnimation.getModel().getSelectedItem();
		String brushAnimation = selected == null ? "" : selected.toString();
		
		if(brushSprite.length() == 0 || brushAnimation.length() == 0)
			radSelection.getModel().setSelected(true);
		
		if(radSelection.getModel().isSelected())
			return new Brush(getBrushSize());
		else
			return new Brush(brushSprite,
								brushAnimation, 
								(float)sldVisibility.getValue() / (float)sldVisibility.getMaximum(),  
								getBrushSize(),
								chkStatic.isSelected(),
								chkTraversable.isSelected());
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        burshGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtWorldScript = new javax.swing.JTextField();
        btnBrowseScript = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        lstSelectedLayer = new javax.swing.JComboBox();
        btnNewLayer = new javax.swing.JButton();
        btnDeleteLayer = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtEntityLayer = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        txtBrushSprite = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnSpriteBrowse = new javax.swing.JButton();
        lstBrushAnimation = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        chkTraversable = new javax.swing.JCheckBox();
        sldVisibility = new javax.swing.JSlider();
        lblVisibility = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        radSelection = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        txtFillDimensions = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        lblSelectionX = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lblSelectionY = new javax.swing.JLabel();
        chkStatic = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstEntities = new javax.swing.JList();
        jLabel8 = new javax.swing.JLabel();
        txtEntityName = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtEntityClass = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtEntityConfig = new javax.swing.JTextField();
        btnBrowseEntityConfig = new javax.swing.JButton();
        btnEntityCreate = new javax.swing.JButton();
        btnDeleteEntity = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        lstEntityDirection = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        txtEntityX = new javax.swing.JTextField();
        txtEntityY = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuNewMap = new javax.swing.JMenuItem();
        mnuExport = new javax.swing.JMenuItem();
        mnuImport = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        jMenu3.setText("jMenu3");

        jMenuItem3.setText("jMenuItem3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Editor");
        setAlwaysOnTop(true);
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        jLabel5.setText("World Script:");

        txtWorldScript.setEditable(false);

        btnBrowseScript.setText("...");
        btnBrowseScript.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnBrowseScriptActionPerformed(evt);
            }
        });

        jLabel6.setText("Selected Layer:");
        jLabel6.setToolTipText("");

        lstSelectedLayer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                lstSelectedLayerActionPerformed(evt);
            }
        });

        btnNewLayer.setText("New");
        btnNewLayer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnNewLayerActionPerformed(evt);
            }
        });

        btnDeleteLayer.setText("Delete");
        btnDeleteLayer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnDeleteLayerActionPerformed(evt);
            }
        });

        jLabel7.setText("Entity Layer");

        txtEntityLayer.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                txtEntityLayerStateChanged(evt);
            }
        });
        
        txtLayerBackground = new JTextField();
        txtLayerBackground.setEditable(false);
        
        lblBackground = new JLabel();
        lblBackground.setText("Layer Background:");
        
        btnBrowseBackground = new JButton();
        btnBrowseBackground.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		btnBrowseBackgroundActionPerformed(e);
        	}
        });
        btnBrowseBackground.setText("...");
        
        txtLayerBackgroundOffsetX = new JTextField();
        txtLayerBackgroundOffsetX.setText("0");
        
        txtLayerBackgroundOffsetY = new JTextField();
        txtLayerBackgroundOffsetY.setText("0");
        
        label = new JLabel();
        label.setText("Location:");
        
        btnApplyBackgroundOffset = new JButton("Apply");
        btnApplyBackgroundOffset.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		btnApplyBackgroundOffsetActionPerformed(e);
        	}
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(jLabel6)
        						.addComponent(jLabel7))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING, false)
        						.addComponent(txtEntityLayer, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        						.addComponent(lstSelectedLayer, Alignment.LEADING, 0, 113, Short.MAX_VALUE))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnNewLayer)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnDeleteLayer))
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(lblBackground)
        						.addComponent(jLabel5))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(jPanel1Layout.createSequentialGroup()
        							.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
        								.addComponent(txtLayerBackground, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        								.addComponent(txtWorldScript))
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        								.addComponent(btnBrowseBackground, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
        								.addComponent(btnBrowseScript)))
        						.addGroup(jPanel1Layout.createSequentialGroup()
        							.addComponent(label, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(txtLayerBackgroundOffsetX, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(txtLayerBackgroundOffsetY, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
        							.addGap(18)
        							.addComponent(btnApplyBackgroundOffset)))))
        			.addGap(20))
        );
        jPanel1Layout.setVerticalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel6)
        				.addComponent(lstSelectedLayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(btnNewLayer)
        				.addComponent(btnDeleteLayer))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel7)
        				.addComponent(txtEntityLayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(42)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(txtLayerBackground, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblBackground)
        				.addComponent(btnBrowseBackground))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        					.addComponent(txtLayerBackgroundOffsetY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        					.addComponent(btnApplyBackgroundOffset)
        					.addComponent(txtLayerBackgroundOffsetX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addGap(3)
        					.addComponent(label)))
        			.addGap(35)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(txtWorldScript, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(jLabel5)
        				.addComponent(btnBrowseScript))
        			.addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel1.setLayout(jPanel1Layout);

        jTabbedPane1.addTab("World", jPanel1);

        txtBrushSprite.setEditable(false);

        jLabel1.setText("Sprite: ");

        btnSpriteBrowse.setText("...");
        btnSpriteBrowse.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnSpriteBrowseActionPerformed(evt);
            }
        });

        jLabel2.setText("Animation:");

        chkTraversable.setSelected(true);
        chkTraversable.setText("Traversable");

        sldVisibility.setValue(0);
        sldVisibility.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                sldVisibilityStateChanged(evt);
            }
        });

        lblVisibility.setText("0.0");

        jLabel12.setText("Visibility:");

        jPanel4.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        burshGroup.add(radSelection);
        radSelection.setSelected(true);
        radSelection.setText("Selection");

        burshGroup.add(jRadioButton2);
        jRadioButton2.setText("Fill Square");

        txtFillDimensions.setText("1");

        jLabel14.setText("Selection X:");

        lblSelectionX.setText("0");

        jLabel16.setText("Selection Y:");

        lblSelectionY.setText("0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectionX, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSelectionY, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(radSelection)
                        .addGap(53, 53, 53)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFillDimensions, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(54, 54, 54))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radSelection)
                    .addComponent(jRadioButton2)
                    .addComponent(txtFillDimensions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(lblSelectionX))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(lblSelectionY))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chkStatic.setSelected(true);
        chkStatic.setText("Static");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(jLabel1)
        						.addComponent(jLabel2)
        						.addComponent(jLabel12))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(jPanel2Layout.createSequentialGroup()
        							.addGroup(jPanel2Layout.createParallelGroup(Alignment.TRAILING, false)
        								.addComponent(txtBrushSprite, Alignment.LEADING)
        								.addComponent(lstBrushAnimation, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE))
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnSpriteBrowse))
        						.addGroup(jPanel2Layout.createSequentialGroup()
        							.addComponent(sldVisibility, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(lblVisibility))))
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addGap(10)
        					.addComponent(chkTraversable)
        					.addGap(26)
        					.addComponent(chkStatic)))
        			.addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(txtBrushSprite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(jLabel1)
        				.addComponent(btnSpriteBrowse))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(lstBrushAnimation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(jLabel2))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING, false)
        				.addComponent(sldVisibility, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        				.addComponent(lblVisibility, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        				.addComponent(jLabel12, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(chkTraversable)
        				.addComponent(chkStatic))
        			.addGap(25)
        			.addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel2.setLayout(jPanel2Layout);

        jLabel1.getAccessibleContext().setAccessibleName("Sprite:");

        jTabbedPane1.addTab("Brush", jPanel2);

        lstEntities.setModel(new DefaultListModel()
        );
        lstEntities.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                lstEntitiesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstEntities);

        jLabel8.setText("Name:");

        jLabel9.setText("Class:");

        jLabel10.setText("Configuration:");

        btnBrowseEntityConfig.setText("...");
        btnBrowseEntityConfig.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnBrowseEntityConfigActionPerformed(evt);
            }
        });

        btnEntityCreate.setText("Create\\Apply");
        btnEntityCreate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnEntityCreateActionPerformed(evt);
            }
        });

        btnDeleteEntity.setText("Delete");
        btnDeleteEntity.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnDeleteEntityActionPerformed(evt);
            }
        });

        jLabel3.setText("Direction:");

        lstEntityDirection.setModel(new DefaultComboBoxModel<WorldDirection>()
        );

        jLabel4.setText("Location:");

        txtEntityX.setText("0");

        txtEntityY.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel8)
                                .addComponent(jLabel9))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtEntityClass, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtEntityName, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel10)
                                .addComponent(jLabel3))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(lstEntityDirection, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtEntityX, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtEntityY, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(btnDeleteEntity)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btnEntityCreate))))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(txtEntityConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnBrowseEntityConfig))))))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtEntityName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtEntityClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtEntityConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseEntityConfig))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lstEntityDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtEntityX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEntityY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeleteEntity)
                    .addComponent(btnEntityCreate))
                .addGap(22, 22, 22))
        );

        jTabbedPane1.addTab("Entities", jPanel3);

        jMenu1.setText("File");

        mnuNewMap.setText("New Map");
        mnuNewMap.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuNewMapActionPerformed(evt);
            }
        });
        jMenu1.add(mnuNewMap);

        mnuExport.setText("Export");
        mnuExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuExportActionPerformed(evt);
            }
        });
        jMenu1.add(mnuExport);

        mnuImport.setText("Import");
        mnuImport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuImportActionPerformed(evt);
            }
        });
        jMenu1.add(mnuImport);

        jMenuBar1.add(jMenu1);

        jMenu4.setText("About");
        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSpriteBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnSpriteBrowseActionPerformed
    {//GEN-HEADEREND:event_btnSpriteBrowseActionPerformed
		JFileChooser chooser = new JFileChooser(new File(m_baseDirectory));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileFilter()
		{

			@Override
			public boolean accept(File f)
			{
				return (f.getName().endsWith(".jsf") && f.getAbsolutePath().startsWith(m_baseDirectory)) || f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return "JevaSpriteFile";
			}
		});

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				URI base = new File(m_baseDirectory).toURI();
				String relative = base.relativize(chooser.getSelectedFile().toURI()).toString();

				txtBrushSprite.setText(relative);

				refreshTileSpriteAnimations();
			} catch (ResourceIOException e)
			{
				JOptionPane.showMessageDialog(this, "Error opening sprite resource: " + e.toString());
			}
		}
    }//GEN-LAST:event_btnSpriteBrowseActionPerformed

    private void sldVisibilityStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_sldVisibilityStateChanged
    {//GEN-HEADEREND:event_sldVisibilityStateChanged
        lblVisibility.setText(String.valueOf((float)sldVisibility.getValue() / (float)sldVisibility.getMaximum()));
    }//GEN-LAST:event_sldVisibilityStateChanged

    private void btnNewLayerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnNewLayerActionPerformed
    {//GEN-HEADEREND:event_btnNewLayerActionPerformed
       m_listener.createNewLayer();
	   refreshLayers();
    }//GEN-LAST:event_btnNewLayerActionPerformed

    private void mnuNewMapActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuNewMapActionPerformed
    {//GEN-HEADEREND:event_mnuNewMapActionPerformed
        NewMap mapDialog = new NewMap(this, true);
        mapDialog.setVisible(true);

        if(mapDialog.isLastQueryValid())
        {
            m_listener.initializeWorld(mapDialog.getWorldWidth(), mapDialog.getWorldHeight(),
										mapDialog.getTileWidth(), mapDialog.getTileHeight());
			refresh();
        }
		
		mapDialog.dispose();
    }//GEN-LAST:event_mnuNewMapActionPerformed

    private void lstSelectedLayerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_lstSelectedLayerActionPerformed
    {//GEN-HEADEREND:event_lstSelectedLayerActionPerformed
        DefaultComboBoxModel model = (DefaultComboBoxModel)lstSelectedLayer.getModel();
		
		int layer = model.getIndexOf(model.getSelectedItem());
		
		if(layer < 0)
			JOptionPane.showMessageDialog(this, "Invalid layer selected");
		else
			m_listener.selectLayer(model.getIndexOf(model.getSelectedItem()));
		
		refresh();
    }//GEN-LAST:event_lstSelectedLayerActionPerformed

    private void btnDeleteLayerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnDeleteLayerActionPerformed
    {//GEN-HEADEREND:event_btnDeleteLayerActionPerformed
        DefaultComboBoxModel model = (DefaultComboBoxModel)lstSelectedLayer.getModel();
		
		int layer = model.getIndexOf(model.getSelectedItem());
		
		if(layer == 0)
			JOptionPane.showMessageDialog(this, "Map must retain its base layer");
		else if(layer < 0)
			JOptionPane.showMessageDialog(this, "No valid layer has been selected");
		else
		{
			m_listener.deleteSelectedLayer();
			refreshLayers();
		}
    }//GEN-LAST:event_btnDeleteLayerActionPerformed

    private void btnBrowseScriptActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnBrowseScriptActionPerformed
    {//GEN-HEADEREND:event_btnBrowseScriptActionPerformed
		JFileChooser chooser = new JFileChooser(new File(m_baseDirectory));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileFilter()
		{

			@Override
			public boolean accept(File f)
			{
				return (f.getName().endsWith(".js") && f.getAbsolutePath().startsWith(m_baseDirectory)) || f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return "JavaScript";
			}
		});

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			URI base = new File(m_baseDirectory).toURI();
			String relative = base.relativize(chooser.getSelectedFile().toURI()).toString();

			m_listener.setScript(relative);

			refresh();
		}
    }//GEN-LAST:event_btnBrowseScriptActionPerformed
    
    private void btnBrowseBackgroundActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnBrowseScriptActionPerformed
    {//GEN-HEADEREND:event_btnBrowseScriptActionPerformed
		JFileChooser chooser = new JFileChooser(new File(m_baseDirectory));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			URI base = new File(m_baseDirectory).toURI();
			String relative = base.relativize(chooser.getSelectedFile().toURI()).toString();
			try {
				m_listener.setSelectedLayerBackground(relative);
			} catch (ResourceIOException | ResourceFormatException e) {
				JOptionPane.showMessageDialog(this, "IOException accessing background resource file: " + e.toString());
			}

			refresh();
		}
    }//GEN-LAST:event_btnBrowseScriptActionPerformed

    private void txtEntityLayerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_txtEntityLayerStateChanged
    {//GEN-HEADEREND:event_txtEntityLayerStateChanged
		m_listener.setEntityLayer((Integer)txtEntityLayer.getValue());
    }//GEN-LAST:event_txtEntityLayerStateChanged

    private void mnuExportActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuExportActionPerformed
    {//GEN-HEADEREND:event_mnuExportActionPerformed
        JFileChooser chooser = new JFileChooser();
		
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				try(FileOutputStream fis = new FileOutputStream(chooser.getSelectedFile()))
				{
					EditorEntity elements[] = new EditorEntity[lstEntities.getModel().getSize()];

					for(int i = 0; i < lstEntities.getModel().getSize(); i++)
						elements[i] = (EditorEntity)lstEntities.getModel().getElementAt(i);

					m_listener.saveWorld(fis, elements);
				}
				
			} catch (FileNotFoundException ex)
			{
				JOptionPane.showMessageDialog(this, "Could not open the file for writing: " + ex.toString());
			} catch (IOException ex)
			{
				JOptionPane.showMessageDialog(this, "IO Error occured while saving file: " + ex.toString());
			}
		}
    }//GEN-LAST:event_mnuExportActionPerformed

    private void mnuImportActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuImportActionPerformed
    {//GEN-HEADEREND:event_mnuImportActionPerformed
		JFileChooser chooser = new JFileChooser();
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				try(FileInputStream fis = new FileInputStream(chooser.getSelectedFile()))
				{
					EditorEntity elements[] = new EditorEntity[lstEntities.getModel().getSize()];

					for(int i = 0; i < lstEntities.getModel().getSize(); i++)
						elements[i] = (EditorEntity)lstEntities.getModel().getElementAt(i);
					
					m_listener.openWorld(JsonVariable.create(fis));
					refresh();
				}
				
			} catch (FileNotFoundException ex)
			{
				JOptionPane.showMessageDialog(this, "Could not open the file for reading: " + ex.toString());
			} catch (IOException ex)
			{
				JOptionPane.showMessageDialog(this, "IO Error occured while reading file: " + ex.toString());
			}
		}
    }//GEN-LAST:event_mnuImportActionPerformed

    private void btnBrowseEntityConfigActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnBrowseEntityConfigActionPerformed
    {//GEN-HEADEREND:event_btnBrowseEntityConfigActionPerformed
        JFileChooser chooser = new JFileChooser(new File(m_baseDirectory));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileFilter()
		{

			@Override
			public boolean accept(File f)
			{
				return (f.getName().endsWith(".jec") && f.getAbsolutePath().startsWith(m_baseDirectory)) || f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return "JevaEntityConfiguration";
			}
		});

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			URI base = new File(m_baseDirectory).toURI();
			String relative = base.relativize(chooser.getSelectedFile().toURI()).toString();

			txtEntityConfig.setText(relative);
		}
    }//GEN-LAST:event_btnBrowseEntityConfigActionPerformed

    private void btnDeleteEntityActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnDeleteEntityActionPerformed
    {//GEN-HEADEREND:event_btnDeleteEntityActionPerformed
        DefaultListModel model = (DefaultListModel)lstEntities.getModel();
		
		for(int i = 0; i < lstEntities.getModel().getSize(); i++)
		{
			EditorEntity e = (EditorEntity)model.getElementAt(i);
			
			if(e.getName().equals(txtEntityName.getText()))
			{
				m_listener.removeEntity(e);
				model.removeElement(e);
				return;
			}
		}
		
		JOptionPane.showMessageDialog(this, "Invalid entity name provided for deletion.");
    }//GEN-LAST:event_btnDeleteEntityActionPerformed

    private void btnApplyBackgroundOffsetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnEntityCreateActionPerformed
    {
    	try
    	{
    		float x = Float.parseFloat(txtLayerBackgroundOffsetX.getText());
    		float y = Float.parseFloat(txtLayerBackgroundOffsetY.getText());
    		
    		m_listener.setSelectedLayerBackgroundLocation(new Vector2F(x, y));
    	}catch(NumberFormatException e)
    	{
    		JOptionPane.showMessageDialog(this, "Invalid X and Y floating points.");
    	}catch(ResourceIOException e)
    	{
    		JOptionPane.showMessageDialog(this, "IO Error occured attempting to access background resource file: " + e.toString());
        }
    }
    
    private void btnEntityCreateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnEntityCreateActionPerformed
    {//GEN-HEADEREND:event_btnEntityCreateActionPerformed

		DefaultComboBoxModel<WorldDirection> modelDir = (DefaultComboBoxModel<WorldDirection>)lstEntityDirection.getModel();
		
		float x = -1;
		float y = -1;
		try
		{
			x = Float.parseFloat(txtEntityX.getText());
			y = Float.parseFloat(txtEntityY.getText());
		}catch(NumberFormatException e) { }
		
		if(txtEntityName.getText().length() > 0 && 
			txtEntityClass.getText().length() > 0 &&
			x >= 0 && y >= 0 &&
			modelDir.getSelectedItem() != null)
		{
			DefaultListModel modelEntity = (DefaultListModel)lstEntities.getModel();

			EditorEntity target = null;

			for(int i = 0; i < lstEntities.getModel().getSize(); i++)
			{
				EditorEntity e = (EditorEntity)modelEntity.getElementAt(i);

				if(e.getName().equals(txtEntityName.getText()))
				{
					target = e;
					break;
				}
			}

			if(target == null)
			{
				target = new EditorEntity(txtEntityName.getText(), 
											txtEntityClass.getText(),
											txtEntityConfig.getText());
				modelEntity.addElement(target);
			}else
			{
				target.setName(txtEntityName.getText());
				target.setClassName(txtEntityClass.getText());
				target.setConfig(txtEntityConfig.getText());
				lstEntities.repaint();
			}
			
			target.setLocation(new Vector2F(x, y));
			target.setDirection((WorldDirection)modelDir.getSelectedItem());
			
			m_listener.refreshEntity(target);
		}
		else
			JOptionPane.showMessageDialog(this, "Please fill out all fields correctly before creating an entity.");
    }//GEN-LAST:event_btnEntityCreateActionPerformed

    private void lstEntitiesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_lstEntitiesValueChanged
    {//GEN-HEADEREND:event_lstEntitiesValueChanged
        EditorEntity e = (EditorEntity)lstEntities.getSelectedValue();
		DefaultComboBoxModel<WorldDirection> modelDir = (DefaultComboBoxModel<WorldDirection>)lstEntityDirection.getModel();
		
		if(e == null)
			return;
		
		txtEntityName.setText(e.getName());
		txtEntityClass.setText(e.getClassName());
		txtEntityConfig.setText(e.getConfig());
		modelDir.setSelectedItem(e.getDirection());
		txtEntityX.setText(String.valueOf(e.getLocation().x));
		txtEntityY.setText(String.valueOf(e.getLocation().y));
    }//GEN-LAST:event_lstEntitiesValueChanged

	public class Brush
	{
		public String sprite;
		public String animation;
		public float visibility;
		public int size;
		public boolean isStatic;
		public boolean isTraversable;
		public boolean isSelection;
		
		public Brush(int _size)
		{
			size = _size;
			isSelection = true;
		}
		
		public Brush(String _sprite, String _animation, float _visibility, int _size,
						boolean _isStatic, boolean _isTraversable)
		{
			sprite = _sprite;
			animation = _animation;
			visibility = _visibility;
			size = _size;
			isStatic = _isStatic;
			isTraversable = _isTraversable;
			isSelection = false;
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowseEntityConfig;
    private javax.swing.JButton btnBrowseScript;
    private javax.swing.JButton btnDeleteEntity;
    private javax.swing.JButton btnDeleteLayer;
    private javax.swing.JButton btnEntityCreate;
    private javax.swing.JButton btnNewLayer;
    private javax.swing.JButton btnSpriteBrowse;
    private javax.swing.ButtonGroup burshGroup;
    private javax.swing.JCheckBox chkStatic;
    private javax.swing.JCheckBox chkTraversable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblSelectionX;
    private javax.swing.JLabel lblSelectionY;
    private javax.swing.JLabel lblVisibility;
    private javax.swing.JComboBox lstBrushAnimation;
    private javax.swing.JList lstEntities;
    private javax.swing.JComboBox lstEntityDirection;
    private javax.swing.JComboBox lstSelectedLayer;
    private javax.swing.JMenuItem mnuExport;
    private javax.swing.JMenuItem mnuImport;
    private javax.swing.JMenuItem mnuNewMap;
    private javax.swing.JRadioButton radSelection;
    private javax.swing.JSlider sldVisibility;
    private javax.swing.JTextField txtBrushSprite;
    private javax.swing.JTextField txtEntityClass;
    private javax.swing.JTextField txtEntityConfig;
    private javax.swing.JSpinner txtEntityLayer;
    private javax.swing.JTextField txtEntityName;
    private javax.swing.JTextField txtEntityX;
    private javax.swing.JTextField txtEntityY;
    private javax.swing.JTextField txtFillDimensions;
    private javax.swing.JTextField txtWorldScript;
    private JTextField txtLayerBackground;
    private JLabel lblBackground;
    private JButton btnBrowseBackground;
    private JTextField txtLayerBackgroundOffsetX;
    private JTextField txtLayerBackgroundOffsetY;
    private JLabel label;
    private JButton btnApplyBackgroundOffset;
}
