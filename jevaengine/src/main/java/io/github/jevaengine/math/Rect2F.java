/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.math;

import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;

/**
 *
 * @author Jeremy
 */
public class Rect2F implements ISerializable
{
	public float x;
	public float y;
	public float width;
	public float height;

	public Rect2F() { }
	
	public Rect2F(Rect2D rect)
	{
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}
	
	public Rect2F(float x, float y, float width, float height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Rect2F difference(Vector2F src)
	{
		return new Rect2F(x - src.x, y - src.y, width, height);
	}

	public Rect2F add(Vector2F v)
	{
		return new Rect2F(x + v.x, y + v.y, width, height);
	}
	

	public Rect2F add(Vector2D v)
	{
		return new Rect2F(x + v.x, y + v.y, width, height);
	}
	
	public Rect2D round()
	{
		return new Rect2D(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
	}
	
	public boolean intersects(Rect2F rect)
	{
		float xMax0 = x + width;
		float yMax0 = y + height;
		float xMax1 = rect.x + rect.width;
		float yMax1 = rect.y + rect.height;
		
		return (xMax0 > rect.x &&
				yMax0 > rect.y &&
				x < xMax1 &&
				y < yMax1);
	}
	
	public boolean contains(Vector2F location)
	{
		return (location.x >= x &&
				location.x - x <= width &&
				location.y >= y &&
				location.y - y <= height);
	}
	
	@Override
	public void serialize(IVariable target)
	{
		target.addChild("x").setValue(x);
		target.addChild("y").setValue(y);
		target.addChild("width").setValue(width);
		target.addChild("height").setValue(height);
	}

	@Override
	public void deserialize(IVariable source)
	{
		this.x = source.getChild("x").getValue(Double.class).floatValue();
		this.y = source.getChild("y").getValue(Double.class).floatValue();
		this.width = source.getChild("width").getValue(Double.class).floatValue();
		this.height = source.getChild("height").getValue(Double.class).floatValue();
	}
}
