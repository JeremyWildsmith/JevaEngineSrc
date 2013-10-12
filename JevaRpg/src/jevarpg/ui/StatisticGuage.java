package jevarpg.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import jeva.graphics.IRenderable;
import jeva.graphics.ui.Control;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.math.Vector2D;

public class StatisticGuage extends Control implements IRenderable
{

    private Vector2D m_anchor;
    private Color m_color;
    private int m_width;
    private int m_height;
    private float m_fValue;
    private boolean m_isVisible;

    public StatisticGuage(Vector2D anchor, Color color, int width, int height, float fValue)
    {
        m_anchor = anchor;
        m_color = color;
        m_width = width;
        m_height = height;
        m_fValue = fValue;
        m_isVisible = true;
    }

    public void setValue(float fValue)
    {
        m_fValue = fValue;
    }

    public float getValue()
    {
        return m_fValue;
    }

    @Override
    public void render(Graphics2D g, int x, int y, float fScale)
    {
        if (m_isVisible)
        {
            g.setColor(m_color);
            g.fillRect(x - m_anchor.x, y - m_anchor.y, (int) (m_width * m_fValue), m_height);

            g.setColor(Color.black);
            g.drawRect(x - m_anchor.x, y - m_anchor.y, m_width, m_height);
        }
    }

    @Override
    public Rectangle getBounds()
    {
        return new Rectangle(0, 0, m_width, m_height);
    }

    @Override
    public void onMouseEvent(InputMouseEvent mouseEvent)
    {
    }

    @Override
    public void onKeyEvent(InputKeyEvent keyEvent)
    {

    }

    @Override
    public void update(int deltaTime)
    {

    }

}
