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
package io.github.jevaengine.game;

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.pipeline.ColorDrawer;
import io.github.jevaengine.graphics.pipeline.GraphicDrawer;
import io.github.jevaengine.graphics.pipeline.PrimitiveShader;
import io.github.jevaengine.graphics.pipeline.ShapeDrawer;
import io.github.jevaengine.joystick.IInputDeviceListener;
import io.github.jevaengine.joystick.InputManager;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.util.Nullable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

import org.jogamp.glg2d.GLG2DCanvas;
import org.jogamp.glg2d.GLG2DColorHelper;
import org.jogamp.glg2d.GLG2DImageHelper;
import org.jogamp.glg2d.GLG2DShapeHelper;
import org.jogamp.glg2d.GLG2DSimpleEventListener;
import org.jogamp.glg2d.GLGraphics2D;

import com.jogamp.opengl.util.FPSAnimator;

public abstract class Game implements IInputDeviceListener, IDisposable
{
	private @Nullable InputManager m_inputMan;

	private Vector2D m_cursorLocation = new Vector2D();

	private int m_renderWidth;

	private int m_renderHeight;
	
	private IDisposable m_gameLoopDisposer;
	
	public Game()
	{
	}

	public final void init(final JFrame target)
	{
		m_renderWidth = target.getWidth();
		m_renderHeight = target.getHeight();

		startup();
		
		RenderSurface renderSurface = new RenderSurface();
		GLG2DCanvas canvas = new CustomGLG2DCanvas(renderSurface);
		canvas.setSize(target.getWidth(), target.getHeight());
		
		target.setContentPane(canvas);
		
		final FPSAnimator anim = new FPSAnimator(canvas.getGLDrawable(), 60);
		
		m_gameLoopDisposer = new IDisposable() {
			@Override
			public void dispose() {
				anim.stop();
			}
		};
		
		m_inputMan = InputManager.create(target);
		
		anim.start();
	}
	
	public final void init()
	{
		m_renderWidth = 0;
		m_renderHeight = 0;

		startup();
		
		final Timer timer = new Timer(18, new ActionListener() {
			private long m_lastTick = System.currentTimeMillis();

			@Override
			public void actionPerformed(ActionEvent e) {
				//Do not call super - very large performance overhead.
				long currentTick = System.currentTimeMillis();
				Game.this.update((int)(currentTick - m_lastTick));
				m_lastTick = currentTick;
			}
		});
		timer.setRepeats(true);
		timer.start();
		
		m_gameLoopDisposer = new IDisposable() {
			@Override
			public void dispose() {
				timer.stop();
			}
		};
	}

	@Override
	public void dispose()
	{
		m_gameLoopDisposer.dispose();
	}
	
	public final Vector2D getResolution()
	{
		return new Vector2D(m_renderWidth, m_renderHeight);
	}

	protected final void render(Graphics2D g)
	{
		g.setColor(Color.black);
		g.fillRect(0, 0, m_renderWidth, m_renderHeight);

		Core.getService(IWindowManager.class).render(g, 0, 0, 1.0F);

		getCursor().render(g, m_cursorLocation.x, m_cursorLocation.y, 1.0F);

		g.dispose();
	}

	protected void update(int deltaTime)
	{
		if(m_inputMan != null)
			m_inputMan.process(this);

		Core.getService(IWindowManager.class).update(deltaTime);
	}
	@Override
	public void mouseMoved(InputManager.InputMouseEvent e)
	{
		m_cursorLocation = new Vector2D(e.location);

		Core.getService(IWindowManager.class).onMouseEvent(e);
	}

	@Override
	public void mouseClicked(InputManager.InputMouseEvent e)
	{
		Core.getService(IWindowManager.class).onMouseEvent(e);
	}


	@Override
	public void keyUp(InputManager.InputKeyEvent e)
	{
		Core.getService(IWindowManager.class).onKeyEvent(e);
	}

	@Override
	public void keyDown(InputManager.InputKeyEvent e)
	{
		Core.getService(IWindowManager.class).onKeyEvent(e);
	}

	@Override
	public void keyTyped(InputManager.InputKeyEvent e)
	{
		Core.getService(IWindowManager.class).onKeyEvent(e);
	}

	@Override
	public void mouseWheelMoved(InputManager.InputMouseEvent e)
	{
		Core.getService(IWindowManager.class).onMouseEvent(e);
	}

	@Override
	public void mouseLeft(InputManager.InputMouseEvent e)
	{
	}

	@Override
	public void mouseEntered(InputManager.InputMouseEvent e)
	{
	}
	
	private class RenderSurface extends JComponent
	{
		private static final long serialVersionUID = 1L;
		private long m_lastTick = System.currentTimeMillis();

		@Override
		public void paintComponent(Graphics g)
		{
			//Do not call super - very large performance overhead.
		}
		
		@Override
		public void paint(Graphics g)
		{
			//Do not call super - very large performance overhead.
			long currentTick = System.currentTimeMillis();
			Game.this.update((int)(currentTick - m_lastTick));
			m_lastTick = currentTick;
			Game.this.render((Graphics2D)g);
		}
	}
	
	private static class CustomGLG2DCanvas extends GLG2DCanvas
	{
		private static final long serialVersionUID = 1L;

		public CustomGLG2DCanvas(JComponent surface)
		{
			super(surface);
		}

		@Override
		protected GLG2DSimpleEventListener createG2DListener(JComponent drawingComponent)
		{	
			return new GLG2DSimpleEventListener(drawingComponent)
			{
				private PrimitiveShader m_shader = new PrimitiveShader();
				
				@Override
				protected void paintGL(GLGraphics2D g2d)
				{
					m_shader.begin((GL2)g2d.getGLContext().getGL());
					super.paintGL(g2d);
				}
				
				@Override
				protected GLGraphics2D createGraphics2D(GLAutoDrawable drawable)
				{
					return new GLGraphics2D()
					{
						@Override
						protected GLG2DShapeHelper createShapeHelper()
						{
							return new ShapeDrawer(m_shader);
						}
						
						@Override
						protected GLG2DColorHelper createColorHelper()
						{
							return new ColorDrawer(m_shader);
						}
						
						@Override
						protected GLG2DImageHelper createImageHelper()
						{
							return new GraphicDrawer(m_shader);
						}
					};
				}
			};
		}
	}

	public abstract UIStyle getGameStyle();

	public abstract IGameScriptProvider getScriptBridge();

	protected abstract void startup();

	protected abstract Sprite getCursor();
}
