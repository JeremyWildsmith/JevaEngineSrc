package io.github.jevaengine.math;

import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;

public class Rect3F implements ISerializable
{
	public static final float TOLERANCE = 0.0000001F;
	
	public float x;
	public float y;
	public float z;
	
	public float width;
	public float height;
	public float depth;
	
	public Rect3F(Vector3F location, float _width, float _height, float _depth)
	{
		x = location.x;
		y = location.y;
		z = location .z;
		
		width = _width;
		height = _height;
		depth = _depth;
	}
	
	public Rect3F(float _x, float _y, float _z, float _width, float _height, float _depth)
	{
		x = _x;
		y = _y;
		z = _z;
		
		width = _width;
		height = _height;
		depth = _depth;
	}
	
	public Rect3F(float _width, float _height)
	{
		this(_width, _height, 0);
	}
	
	public Rect3F(float _width, float _height, float _depth)
	{
		width = _width;
		height = _height;
		depth = _depth;
	}
	
	public Rect3F(Rect3F src)
	{
		x = src.x;
		y = src.y;
		z = src.z;
		
		width = src.width;
		height = src.height;
		depth = src.depth;
	}
	
	public Rect3F()
	{
		x = 0;
		y = 0;
		z = 0;
		
		width = 0;
		height = 0;
		depth = 0;
	}
	
	public boolean hasVolume()
	{
		return width > TOLERANCE && height > TOLERANCE && depth > TOLERANCE;
	}
	
	public Vector3F min()
	{
		return getPoint(0, 0, 0);
	}
	
	public Vector3F max()
	{
		return getPoint(1.0F, 1.0F, 1.0F);
	}
	
	public Vector3F getPoint(float widthRatio, float heightRatio, float depthRatio)
	{
		return new Vector3F(x + width * widthRatio, y + height * heightRatio, z + depth * depthRatio);
	}
	
	public Rect2F getXy()
	{
		return new Rect2F(x, y, width, height);
	}
	
	public Rect3F add(Vector3F v)
	{
		return new Rect3F(x + v.x, y + v.y, z + v.z, width, height, depth);
	}
	
	@Override
	public void serialize(IVariable target) throws ValueSerializationException
	{
		target.addChild("x").setValue(x);
		target.addChild("y").setValue(y);
		target.addChild("z").setValue(z);
		
		target.addChild("width").setValue(width);
		target.addChild("height").setValue(height);
		target.addChild("depth").setValue(depth);
	}

	@Override
	public void deserialize(IImmutableVariable source) throws ValueSerializationException
	{
		try
		{
			if(source.childExists("x"))
				this.x = source.getChild("x").getValue(Double.class).floatValue();
			
			if(source.childExists("y"))
				this.y = source.getChild("y").getValue(Double.class).floatValue();
			
			if(source.childExists("z"))
				this.z = source.getChild("z").getValue(Double.class).floatValue();
			
			this.width = source.getChild("width").getValue(Double.class).floatValue();
			this.height = source.getChild("height").getValue(Double.class).floatValue();
			this.depth = source.getChild("depth").getValue(Double.class).floatValue();
		} catch(NoSuchChildVariableException e)
		{
			throw new ValueSerializationException(e);
		}
	}
}
