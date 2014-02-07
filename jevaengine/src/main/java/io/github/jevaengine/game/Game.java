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

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.pipeline.ColorHelper;
import io.github.jevaengine.graphics.pipeline.DrawBatcher;
import io.github.jevaengine.graphics.pipeline.GraphicDrawer;
import io.github.jevaengine.graphics.pipeline.PrimitiveShader;
import io.github.jevaengine.graphics.pipeline.ShapeDrawer;
import io.github.jevaengine.graphics.pipeline.TransformHelper;
import io.github.jevaengine.joystick.IInputDeviceListener;
import io.github.jevaengine.joystick.InputManager;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.Nullable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

import org.jogamp.glg2d.GLG2DCanvas;
import org.jogamp.glg2d.GLG2DColorHelper;
import org.jogamp.glg2d.GLG2DImageHelper;
import org.jogamp.glg2d.GLG2DShapeHelper;
import org.jogamp.glg2d.GLG2DSimpleEventListener;
import org.jogamp.glg2d.GLG2DTransformHelper;
import org.jogamp.glg2d.GLGraphics2D;

import com.jogamp.opengl.util.FPSAnimator;

public abstract class Game implements IDisposable
{
	private @Nullable InputManager m_inputMan;

	private Vector2D m_cursorLocation = new Vector2D();

	private int m_renderWidth;

	private int m_renderHeight;

	private InputHandler m_inputHandler = new InputHandler();
	
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

		getWindowManager().render(g, 0, 0, 1.0F);

		getCursor().render(g, m_cursorLocation.x, m_cursorLocation.y, 1.0F);

		g.dispose();
	}

	protected void update(int deltaTime)
	{
		if(m_inputMan != null)
			m_inputMan.process(m_inputHandler);
	}
	
	private class InputHandler implements IInputDeviceListener
	{
		@Override
		public void mouseMoved(InputManager.InputMouseEvent e)
		{
			m_cursorLocation = new Vector2D(e.location);

			getWindowManager().onMouseEvent(e);
		}

		@Override
		public void mouseClicked(InputManager.InputMouseEvent e)
		{
			getWindowManager().onMouseEvent(e);
		}


		@Override
		public void keyUp(InputManager.InputKeyEvent e)
		{
			getWindowManager().onKeyEvent(e);
		}

		@Override
		public void keyDown(InputManager.InputKeyEvent e)
		{
			getWindowManager().onKeyEvent(e);
		}

		@Override
		public void keyTyped(InputManager.InputKeyEvent e)
		{
			getWindowManager().onKeyEvent(e);
		}

		@Override
		public void mouseWheelMoved(InputManager.InputMouseEvent e)
		{
			getWindowManager().onMouseEvent(e);
		}

		@Override
		public void mouseLeft(InputManager.InputMouseEvent e) { }

		@Override
		public void mouseEntered(InputManager.InputMouseEvent e) { }

		@Override
		public void mouseButtonStateChanged(InputMouseEvent e) { }
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
			super(getCapabilities(), surface);
		}

		private static GLCapabilities getCapabilities()
		{
		    GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));
		    caps.setRedBits(8);
		    caps.setGreenBits(8);
		    caps.setBlueBits(8);
		    caps.setAlphaBits(8);
		    caps.setDoubleBuffered(true);
		    caps.setHardwareAccelerated(true);
		    caps.setNumSamples(4);
		    caps.setBackgroundOpaque(false);
		    caps.setSampleBuffers(true);
		    
		    return caps;
		}
		
		@Override
		protected GLG2DSimpleEventListener createG2DListener(JComponent drawingComponent)
		{	
			return new GLG2DSimpleEventListener(drawingComponent)
			{
				private PrimitiveShader m_shader = new PrimitiveShader();
				private DrawBatcher m_drawBatcher = new DrawBatcher(m_shader);
				
				@Override
				protected void prePaint(GLAutoDrawable drawable)
				{
					m_drawBatcher.setG2D(drawable.getGL().getGL4());
					m_shader.load(drawable.getGL().getGL4());
					super.prePaint(drawable);
				}
				
				@Override
				protected void paintGL(GLGraphics2D g2d)
				{
					m_shader.load(g2d.getGLContext().getGL().getGL4());
					super.paintGL(g2d);
					m_drawBatcher.flush();
				}
				
				@Override
				protected GLGraphics2D createGraphics2D(GLAutoDrawable drawable)
				{
					return new GLGraphics2D()
					{
						@Override
						protected void scissor(boolean enable)
						{
							if(!enable)
							{
								clip = null;
								m_drawBatcher.setClip(null);
							}else
							{
								m_drawBatcher.setClip(new Rect2D(clip.x, getCanvasHeight() - clip.y - clip.height, Math.max(clip.width, 0), Math.max(clip.height, 0)));
							}
						}
						
						@Override
						protected GLG2DShapeHelper createShapeHelper()
						{
							return new ShapeDrawer(m_shader, m_drawBatcher);
						}
						
						@Override
						protected GLG2DColorHelper createColorHelper()
						{
							return new ColorHelper(m_shader);
						}
						
						@Override
						protected GLG2DImageHelper createImageHelper()
						{
							return new GraphicDrawer(m_shader, m_drawBatcher);
						}
						
						@Override
						protected GLG2DTransformHelper createTransformHelper()
						{
							return new TransformHelper(m_shader, m_drawBatcher);
						}
					};
				}
			};
		}
	}

	public abstract IGameScriptProvider getScriptBridge();

	public abstract WindowManager getWindowManager();
	protected abstract void startup();

	protected abstract Sprite getCursor();
}
