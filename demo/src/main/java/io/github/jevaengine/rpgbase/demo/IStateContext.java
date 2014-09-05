/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.ui.WindowManager;

/**
 *
 * @author Jeremy
 */
public interface IStateContext
{
	void setState(IState state);
	WindowManager getWindowManager();
}
