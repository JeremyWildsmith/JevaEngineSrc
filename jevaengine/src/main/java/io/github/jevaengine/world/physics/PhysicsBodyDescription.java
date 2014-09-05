package io.github.jevaengine.world.physics;

import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.math.Rect3F;

public final class PhysicsBodyDescription implements ISerializable
{
	public PhysicsBodyType type = PhysicsBodyType.Static;
	public PhysicsBodyShape shape = PhysicsBodyShape.Box;
	public Rect3F aabb = new Rect3F();
	public float density;
	public boolean isFixedRotation;
	public boolean isSensor;
	public float friction;
	
	public PhysicsBodyDescription() { }
	
	public PhysicsBodyDescription(PhysicsBodyType _type, PhysicsBodyShape _shape, Rect3F _aabb, float _density, boolean _isFixedRotation, boolean _isSensor, float _friction)
	{
		type = _type;
		shape = _shape;
		aabb = _aabb;
		density = _density;
		isFixedRotation = _isFixedRotation;
		isSensor = _isSensor;
		friction = _friction;
	}
	
	public enum PhysicsBodyShape
	{
		Circle,
		Box,
	}
	
	public enum PhysicsBodyType
	{
		Kinematic,
		Static,
		Dynamic,
	}

	@Override
	public void serialize(IVariable target) throws ValueSerializationException
	{
		target.addChild("type").setValue(type.ordinal());
		target.addChild("shape").setValue(shape.ordinal());
		target.addChild("aabb").setValue(aabb);
		target.addChild("density").setValue(density);
		target.addChild("isFixedRotation").setValue(isFixedRotation);
		target.addChild("isSensor").setValue(isSensor);
		target.addChild("friction").setValue("friction");
	}

	@Override
	public void deserialize(IImmutableVariable source) throws ValueSerializationException
	{
		try
		{
			Integer typeIndex = source.getChild("type").getValue(Integer.class);
			
			if(typeIndex < 0 || typeIndex > PhysicsBodyType.values().length)
				throw new ValueSerializationException(new IndexOutOfBoundsException("type index is outside of bounds."));
			
			type = PhysicsBodyType.values()[typeIndex];
			
			Integer shapeIndex = source.getChild("shape").getValue(Integer.class);
			
			if(shapeIndex < 0 || shapeIndex > PhysicsBodyShape.values().length)
				throw new ValueSerializationException(new IndexOutOfBoundsException("type index is outside of bounds."));
			
			shape = PhysicsBodyShape.values()[shapeIndex];
			
			aabb = source.getChild("aabb").getValue(Rect3F.class);
			density = source.getChild("density").getValue(Double.class).floatValue();
			
			if(source.childExists("isFixedRotation"))
				isFixedRotation = source.getChild("isFixedRotation").getValue(Boolean.class);
			
			if(source.childExists("isSensor"))
				isSensor = source.getChild("isSensor").getValue(Boolean.class);
			
			friction = source.getChild("friction").getValue(Double.class).floatValue();
		} catch (NoSuchChildVariableException e) {
			throw new ValueSerializationException(e);
		}
	}
}
