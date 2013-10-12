package UnitTest;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.awt.image.VolatileImage;

import javax.swing.JFrame;

/**
 * 
 * @author Jeremy. A. W
 */
public class Game
{
	public static final int WINX = 680;
	public static final int WINY = 950;

	private Canvas m_drawCanvas;

	private VolatileImage m_backBuffer;

	public Game()
	{
		m_drawCanvas = new Canvas();
	}

	public final void init(Container target, Component inputSource)
	{
		Container container = new Container();
		target.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""), new Point(), "trans"));
		target.setVisible(true);
		target.add(container);

		target.setIgnoreRepaint(true);
		target.setSize(WINX, WINY);

		if (target instanceof JFrame)
		{
			((JFrame) target).setResizable(false);
		}

		container.setSize(WINX, WINY);
		container.setIgnoreRepaint(true);
		container.setBackground(Color.black);
		container.setLayout(null);
		container.setVisible(true);

		m_drawCanvas.setVisible(true);
		m_drawCanvas.setSize(WINX, WINY);
		container.add(m_drawCanvas);

		m_backBuffer = m_drawCanvas.getGraphicsConfiguration().createCompatibleVolatileImage(WINX, WINY);

	}

	public final void render()
	{
		GraphicsConfiguration gc = m_drawCanvas.getGraphicsConfiguration();

		do
		{
			if (m_backBuffer.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE)
				m_backBuffer = gc.createCompatibleVolatileImage(WINX, WINY);

			Graphics2D gBackBuffer = (Graphics2D) m_backBuffer.getGraphics();
			gBackBuffer.setBackground(new Color(100, 100, 100, 255));
			gBackBuffer.clearRect(0, 0, WINX, WINY);
			gBackBuffer.dispose();

			Graphics gCanvas = m_drawCanvas.getGraphics();

			gCanvas.drawImage(m_backBuffer, 0, 0, m_drawCanvas);
			gCanvas.dispose();

		} while (m_backBuffer.contentsLost());
	}

	public void update(int deltaTime)
	{
	}

}
