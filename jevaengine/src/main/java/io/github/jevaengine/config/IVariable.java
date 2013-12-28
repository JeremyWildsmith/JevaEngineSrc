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

public interface IVariable extends ISerializable
{
	void setValue(ISerializable value);
	void setValue(ISerializable[] value);
	
	void setValue(String value);
	void setValue(String[] value);
	
	void setValue(int value);
	void setValue(int[] value);
	
	void setValue(double value);
	void setValue(double[] value);
	
	void setValue(boolean value);
	void setValue(boolean[] value);
	
	void setValue(Object o);
	
	<T> T getValue(Class<T> cls);
	<T> T[] getValues(Class<T[]> cls);
	
	boolean childExists(String name);
	IVariable getChild(String name);
	IVariable addChild(String name);
	
	void removeChild(String name);
}
