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
public class InvalidValueFormatException extends RuntimeException
{
	public InvalidValueFormatException()
	{
	}
	
	public InvalidValueFormatException(String reason)
	{
		super(reason);
	}
}
