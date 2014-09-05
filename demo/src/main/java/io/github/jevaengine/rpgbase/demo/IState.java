/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;


/**
 *
 * @author Jeremy
 */
public interface IState
{
	void enter(IStateContext context);
	void leave();
	
	void update(int deltaTime);
}
