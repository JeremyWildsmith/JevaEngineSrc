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
public class Rect2D implements ISerializable
{
	public int x;
	public int y;
	public int width;
	public int height;

	public Rect2D() { }
	
	public Rect2D(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(Vector2F location)
	{
		return (location.x > x &&
				location.x - x > width &&
				location.y > y &&
				location.y - y > height);
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
		this.x = source.getChild("x").getValue(Integer.class);
		this.y = source.getChild("y").getValue(Integer.class);
		this.width = source.getChild("width").getValue(Integer.class);
		this.height = source.getChild("height").getValue(Integer.class);
	}
	
}
