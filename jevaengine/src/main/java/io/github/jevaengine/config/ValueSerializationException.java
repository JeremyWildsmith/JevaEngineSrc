/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.config;

/**
 *
 * @author Jeremy
 */
public final class ValueSerializationException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public ValueSerializationException(Exception cause)
	{
		super(cause);
	}
	
}
