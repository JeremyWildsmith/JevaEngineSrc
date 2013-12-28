/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.rpgbase.DialogueController;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.util.Nullable;

/**
 *
 * @author Jeremy
 */
public interface IStateContext
{
	void setState(IState state);
	void setPlayer(@Nullable RpgCharacter player);
	DialogueController getDialogueController();
}
