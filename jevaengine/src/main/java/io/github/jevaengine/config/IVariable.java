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

public interface IVariable extends IImmutableVariable
{
	void setValue(ISerializable value) throws ValueSerializationException;
	void setValue(ISerializable[] value) throws ValueSerializationException;
	
	void setValue(String value) throws ValueSerializationException;
	void setValue(String[] value) throws ValueSerializationException;
	
	void setValue(int value) throws ValueSerializationException;
	void setValue(int[] value) throws ValueSerializationException;
	
	void setValue(double value) throws ValueSerializationException;
	void setValue(double[] value) throws ValueSerializationException;
	
	void setValue(boolean value) throws ValueSerializationException;
	void setValue(boolean[] value) throws ValueSerializationException;
	
	void setValue(Object o) throws ValueSerializationException;
	
	boolean childExists(String name);
	IVariable getChild(String name) throws NoSuchChildVariableException;
	IVariable addChild(String name);
	void removeChild(String name) throws NoSuchChildVariableException;
}
