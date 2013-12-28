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
class UnsupportedValueTypeException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnsupportedValueTypeException(String reason)
	{
		super(reason);
	}
	
	public UnsupportedValueTypeException()
	{
	}
	
}
