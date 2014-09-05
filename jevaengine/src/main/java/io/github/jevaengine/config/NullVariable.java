package io.github.jevaengine.config;


public final class NullVariable implements IImmutableVariable
{
	@Override
	public void serialize(IVariable target) { }

	@Override
	public void deserialize(IImmutableVariable source) { }

	@Override
	public <T> T getValue(Class<T> cls) throws ValueSerializationException
	{
		throw new ValueSerializationException(new UnsupportedValueTypeException());
	}

	@Override
	public <T> T[] getValues(Class<T[]> cls) throws ValueSerializationException
	{
		throw new ValueSerializationException(new UnsupportedValueTypeException());
	}

	@Override
	public boolean childExists(String name)
	{
		return false;
	}

	@Override
	public IImmutableVariable getChild(String name) throws NoSuchChildVariableException
	{
		throw new NoSuchChildVariableException(name);
	}

	@Override
	public String[] getChildren()
	{
		return new String[0];
	}

}
