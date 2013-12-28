/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo.demos;

import io.github.jevaengine.rpgbase.demo.MainMenu;
import io.github.jevaengine.rpgbase.demo.IStateContext;
import io.github.jevaengine.rpgbase.demo.IState;
import io.github.jevaengine.Core;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.game.FollowCamera;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.DialogueController;
import io.github.jevaengine.rpgbase.DialogueController.IDialogueControlObserver;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.ui.CharacterMenu;
import io.github.jevaengine.rpgbase.ui.InventoryMenu;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.MenuStrip;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewListener;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.World;

import java.awt.Color;
import java.util.ArrayList;

import javax.script.ScriptException;

/**
 *
 * @author Jeremy
 */
public class Demo2 implements IState
{
	private static final String DEMO_MAP = "world/demo2.jmp";
	private static final String PLAYER = "artifact/entity/skeletonWarrior/player.jec";
	
	private IStateContext m_context;
	
	private DialogueController m_dialogueController;
	private DialogueObserver m_dialogueObserver = new DialogueObserver();
	
	private World m_world;
	private Window m_window;
	private TextArea m_dialogue;
	private final MenuStrip m_contextStrip = new MenuStrip();
	private final Label m_cursorActionLabel = new Label("None", Color.yellow);
	private final InventoryMenu m_inventoryMenu = new InventoryMenu();
	private final CharacterMenu m_characterMenu = new CharacterMenu();
	
	private RpgCharacter m_player;
	
	public Demo2()
	{
		IResourceLibrary resourceLibrary = Core.getService(IResourceLibrary.class);
		
		m_world = World.create(resourceLibrary.openConfiguration(DEMO_MAP));

		m_player = new RpgCharacter(resourceLibrary.openConfiguration(PLAYER));
		m_player.setLocation(new Vector2F(2,2));
		m_world.addEntity(m_player);
		
		m_inventoryMenu.accessInventory(m_player.getInventory(), m_player);
		m_characterMenu.showCharacter(m_player);
		
		m_window = new Window(Core.getService(Game.class).getGameStyle(), 710, 700);
		m_window.setLocation(new Vector2D(157, 60));
		
		/*
		* Create the Camera we will be using to look at our world. The follow
		* camera allows you to provide a specific entity for the camera to focus on.
		*
		* After the camera is created, we attach it to the world we want it
		* to look at.
		*/
		FollowCamera camera = new FollowCamera();
		camera.setTarget(m_player.getInstanceName());
		camera.attach(m_world);
		
		/*
		* Create the world view port. The world view port is a control that can
		* be added to a Panel\Window that renders the world through a provided
		* camera.
		*/
		WorldView worldViewport = new WorldView(680, 400);
		worldViewport.setRenderBackground(false);
		worldViewport.setCamera(camera);
		worldViewport.addListener(new WorldViewListener());
		m_window.addControl(worldViewport, new Vector2D(10,70));
		
		/*
		* Now we need to add our label and our context menu to our worldViewport,
		* so we can display them over the world viewport. These controls are used by the
		* world view listener below to display HUD menus to the user.
		*/
		worldViewport.addControl(m_contextStrip);
		worldViewport.addControl(m_cursorActionLabel);
		m_contextStrip.setVisible(false);
		m_cursorActionLabel.setVisible(false);
		
		/*
		* Now we're going to add the dialogue text area. In this text area we'll
		* place all the game dialogue that occurs. 
		*/
		m_dialogue = new TextArea(Color.yellow, 680, 200);
		m_window.addControl(m_dialogue, new Vector2D(10, 400));
		
		/*
		 * Finally, a button to return to the main menu of the game.
		*/
		
		m_window.addControl(new Button("Go Back")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new MainMenu());
			}
		}, new Vector2D(10,10));
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		m_dialogueController = context.getDialogueController();
		m_dialogueController.clear();
		
		context.setPlayer(m_player);
		context.getDialogueController().addObserver(m_dialogueObserver);
		Core.getService(IWindowManager.class).addWindow(m_window);
		Core.getService(IWindowManager.class).addWindow(m_characterMenu);
		Core.getService(IWindowManager.class).addWindow(m_inventoryMenu);
	}

	public void leave()
	{
		Core.getService(IWindowManager.class).removeWindow(m_window);
		Core.getService(IWindowManager.class).removeWindow(m_characterMenu);
		Core.getService(IWindowManager.class).removeWindow(m_inventoryMenu);
		
		m_dialogueController.removeObserver(m_dialogueObserver);
		m_context.setPlayer(null);
	}

	public void update(int iDelta)
	{
		m_world.update(iDelta);
	}
	
	private class WorldViewListener implements IWorldViewListener
	{
		private IInteractable m_lastTarget = null;
		
		@Override
		public void worldSelection(Vector2D screenLocation, Vector2D worldLocation, MouseButton button)
		{
			//If we're in the middle of dialogue, block world interaction...
			if(m_dialogueController.isBusy())
				return;
			
			final IInteractable[] interactables = m_world.getTileEffects(worldLocation).interactables.toArray(new IInteractable[0]);

			if (button == MouseButton.Right)
			{
				if (interactables.length > 0 && interactables[0].getCommands().length > 0)
				{
					m_contextStrip.setContext(interactables[0].getCommands(), new MenuStrip.IMenuStripListener()
					{
						@Override
						public void onCommand(String command)
						{
							interactables[0].doCommand(command);
						}
					});

					m_contextStrip.setLocation(screenLocation);
				}
			}else if(button == MouseButton.Left)
			{
				if(m_lastTarget != null)
				{
					m_lastTarget.doCommand(m_lastTarget.getDefaultCommand());
					m_lastTarget = null;
					m_cursorActionLabel.setVisible(false);
				}else
					m_player.moveTo(worldLocation);
			}
		}

		@Override
		public void worldMove(Vector2D screenLocation, Vector2D worldLocation)
		{
			final IInteractable[] interactables = m_world.getTileEffects(worldLocation).interactables.toArray(new IInteractable[0]);
			
			IInteractable defaultable = null;
			
			for(int i = 0; i < interactables.length && defaultable == null; i++)
			{
				if(interactables[i].getDefaultCommand() != null)
					defaultable = interactables[i];
			}
			
			if(defaultable != null)
			{
				m_cursorActionLabel.setText(defaultable.getDefaultCommand());
				m_cursorActionLabel.setVisible(true);
				
				Vector2D offset = new Vector2D(10, 15);
				
				m_cursorActionLabel.setLocation(screenLocation.add(offset));
			
				m_lastTarget = defaultable;
			}else
			{
				m_lastTarget = null;
				m_cursorActionLabel.setVisible(false);
			}
		}
	}
	
	private class DialogueObserver implements IDialogueControlObserver
	{
		private ArrayList<Button> m_answers = new ArrayList<Button>();
		
		private void removeAnswers()
		{
			for(Button b : m_answers)
				m_window.removeControl(b);
			
			m_answers.clear();
		}
		
		private void presentAnswers(String[] answers)
		{
			removeAnswers();
			
			int offset = 0;
			
			for (final String answer : answers)
			{
				Button b = new Button(answer)
				{
					@Override
					public void onButtonPress()
					{
						m_dialogueController.say(answer);
					}
				};
				
				m_window.addControl(b, new Vector2D(offset + 10, 600));
				m_answers.add(b);
				
				offset += b.getBounds().width + 10;
			}
		}
		
		public void beginDialogue()
		{
			m_world.pause();
		}

		public void endDialogue()
		{
			m_world.resume();
			removeAnswers();
		}

		public void dialogueEvent(int event)
		{
			//We could route this event back to the speakers script
			//or do some other task with it by looking it up in a table etc...
			//it all depends on how your game dialogue works.
			
			//In this example (and most implementations will be to do this) we'll
			//route it back to the speakers script to notify the speaker the dialogue it has
			//invoked has raised an event.
			
			Entity speaker = m_dialogueController.getSpeaker();
			
			if(speaker != null)
			{
				try
				{
					speaker.getScript().invokeScriptFunction("dialogueEvent", event);
				} catch (NoSuchMethodException ex) { }
				catch (ScriptException ex)
				{
					throw new CoreScriptException(ex);
				}
			}
		}

		public void speakerSaid(String message)
		{
			Entity speaker = m_dialogueController.getSpeaker();
			String name = speaker instanceof RpgCharacter ? ((RpgCharacter)speaker).getCharacterName() : "World";
			
			m_dialogue.appendText(String.format("%s: %s\n", name, message));
			m_dialogue.scrollToEnd();
			presentAnswers(m_dialogueController.getAnswers());
		}

		public void listenerSaid(String message)
		{
			m_dialogue.appendText("Hero: " + message + "\n");
			m_dialogue.scrollToEnd();
		}
		
	}
}
