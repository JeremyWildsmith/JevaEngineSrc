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
package io.github.jevaengine.config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public abstract class Variable implements Iterable<Variable>
{

	public static final String NAME_SPLIT = "/";

	private String m_name;

	private Variable m_parent;

	public Variable()
	{
		m_parent = null;
		m_name = "";
	}

	public Variable(String name)
	{
		m_parent = null;
		m_name = name;
	}

	public Variable(Variable parent, String name)
	{
		m_parent = parent;
		m_name = name;
	}

	public final String getName()
	{
		return m_name;
	}

	public final String getFullName()
	{
		return (m_parent == null ? "" : m_parent.getFullName() + NAME_SPLIT) + m_name;
	}

	protected final Variable getChild(String name)
	{
		for (Variable v : getChildren())
		{
			if (v.getName().compareTo(name) == 0)
			{
				return v;
			}
		}

		throw new UnknownVariableException(name);
	}

	protected abstract Variable[] getChildren();

	protected abstract Variable setChild(String name, VariableValue value);

	public VariableValue getValue()
	{
		throw new UnsupportedOperationException();
	}

	public void setValue(VariableValue value)
	{
		if (m_parent == null)
		{
			throw new UnsupportedOperationException();
		}

		m_parent.setChild(m_name, value);
	}

	protected Variable createChild(String name, VariableValue value)
	{
		throw new UnsupportedOperationException();
	}

	public final Variable[] getVariableArray()
	{
		ArrayList<Variable> buffer = new ArrayList<Variable>();

		try
		{
			for (int i = 0; variableExists(String.valueOf(i)); i++)
			{
				buffer.add(getChild(String.valueOf(i)));
			}

		} catch (UnknownVariableException e)
		{
			throw new RuntimeException(e);
		}

		return buffer.toArray(new Variable[buffer.size()]);
	}

	public final Variable getVariable(String[] name)
	{
		Variable child = getChild(name[0]);

		return (name.length == 1 ? child : child.getVariable(Arrays.copyOfRange(name, 1, name.length)));
	}

	public final Variable getVariable(String name)
	{
		String[] names = name.split(Pattern.quote(NAME_SPLIT));

		return getVariable((names[0].isEmpty() ? Arrays.copyOfRange(names, 1, names.length) : names));
	}

	public final boolean variableExists(String name)
	{
		for (Variable v : getChildren())
		{
			if (v.getName().compareTo(name) == 0)
			{
				return true;
			}
		}

		return false;
	}

	public final Variable setVariable(String[] name, VariableValue value)
	{
		Variable var = (variableExists(name[0]) ? getChild(name[0]) : createChild(name[0], new VariableValue()));

		if (name.length == 1)
		{
			var.setValue(value);
		} else
		{
			return var.setVariable(Arrays.copyOfRange(name, 1, name.length), value);
		}

		return var;
	}

	public final Variable setVariable(String name, VariableValue value)
	{
		String[] names = name.split(Pattern.quote(NAME_SPLIT));
		return setVariable((names[0].length() == 0 ? Arrays.copyOfRange(names, 1, names.length) : names), value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public final Iterator<Variable> iterator()
	{
		return new VariableIterator();
	}

	private static void serialize(StringBuilder builder, String parentName, Variable parent)
	{
		String fullName = (parentName.length() > 0 ? parentName + NAME_SPLIT : "") + parent.getName();

		try
		{
			String value = parent.getValue().getString();

			if (!value.isEmpty())
			{
				builder.append(String.format("%s:%s;%n", fullName, encodeRaw(value)));
			}
		} catch (UnsupportedOperationException e)
		{
			// If variables don't have a value, or if they are null (or if such
			// value is not serializable) ignore it.
		}

		for (Variable v : parent)
		{
			serialize(builder, fullName, v);
		}
	}

	public final void serialize(OutputStream out)
	{
		StringBuilder sb = new StringBuilder();

		Variable[] children = this.getChildren();

		for (Variable v : children)
		{
			serialize(sb, "", v);
		}

		try
		{
			out.write(sb.toString().getBytes("UTF-8"));
			out.flush();
		} catch (IOException e)
		{
			throw new VariableStoreSerializationException("IO Exception occured " + e.getMessage());
		}
	}

	private static String encodeRaw(String raw)
	{
		try
		{
			return URLEncoder.encode(raw, "ISO-8859-1");
		} catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	private class VariableIterator implements Iterator<Variable>
	{

		private int m_index = -1;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return m_index < getChildren().length - 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Variable next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}

			return getChildren()[++m_index];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
