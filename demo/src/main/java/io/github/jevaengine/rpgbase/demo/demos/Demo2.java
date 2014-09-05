/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo.demos;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.NullIInitializationProgressMonitor;
import io.github.jevaengine.audio.IAudioClip;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.audio.NullAudioClip;
import io.github.jevaengine.game.FollowCamera;
import io.github.jevaengine.game.ICamera;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.joystick.InputMouseEvent.MouseButton;
import io.github.jevaengine.joystick.InputMouseEvent.MouseEventType;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.character.RpgCharacter.IActionObserver;
import io.github.jevaengine.rpgbase.demo.DemoMenu;
import io.github.jevaengine.rpgbase.demo.IState;
import io.github.jevaengine.rpgbase.demo.IStateContext;
import io.github.jevaengine.rpgbase.dialogue.IDialogueListenerSession;
import io.github.jevaengine.rpgbase.dialogue.IDialogueSpeakerSession;
import io.github.jevaengine.rpgbase.ui.CharacterDialogueQueryFactory;
import io.github.jevaengine.rpgbase.ui.CharacterDialogueQueryFactory.CharacterDialogueQuery;
import io.github.jevaengine.rpgbase.ui.CharacterDialogueQueryFactory.ICharacterDialogueQueryObserver;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.MenuStrip;
import io.github.jevaengine.ui.MenuStrip.IMenuStripListener;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.Timer;
import io.github.jevaengine.ui.Timer.ITimerObserver;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.Window.IWindowObserver;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewObserver;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.IWorldFactory;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.entity.Actor;
import io.github.jevaengine.world.scene.isometric.PaintersIsometricSceneBufferFactory;
import io.github.jevaengine.world.steering.AvoidanceBehavior;
import io.github.jevaengine.world.steering.ISteeringDriver;
import io.github.jevaengine.world.steering.PointSubject;
import io.github.jevaengine.world.steering.SeekBehavior;
import io.github.jevaengine.world.steering.VelocityLimitSteeringDriver;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeremy
 */
public class Demo2 implements IState
{
	private static final String DEMO_MAP = "world/demo2.jmp";
	private static final String DEMO_VIEW_WINDOW = "ui/windows/demoview.jwl";
	
	private static final String BACKGROUND_MUSIC = "audio/themeMusic/windAndTree.ogg";
	
	private static final int TILE_WIDTH = 64;
	private static final int TILE_HEIGHT = 32;
	
	private IStateContext m_context;
	private World m_world;
	private Window m_window;
	
	private final IWindowFactory m_windowFactory;
	private final IWorldFactory m_worldFactory;
	private final IAudioClipFactory m_audioClipFactory;
	
	private final Logger m_logger = LoggerFactory.getLogger(Demo2.class);

	private final PointSubject m_steerSubject = new PointSubject(new Vector2F());
	private final ISteeringDriver m_steerDriver = new VelocityLimitSteeringDriver(2.0F);

	private DialogueController m_dialogueController;
	
	private Vector2F m_movementDelta = new Vector2F();
	private RpgCharacter m_player;
	
	private IAudioClip m_backgroundMusic = new NullAudioClip();
	
	public Demo2(IWindowFactory windowFactory, IWorldFactory worldFactory, IAudioClipFactory audioClipFactory)
	{
		m_windowFactory = windowFactory;
		m_worldFactory = worldFactory;
		m_audioClipFactory = audioClipFactory;
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		try
		{
			FollowCamera camera = new FollowCamera(new PaintersIsometricSceneBufferFactory(TILE_WIDTH, TILE_HEIGHT));
			
			m_world = m_worldFactory.create(DEMO_MAP, 1.0F, 1.0F, new NullIInitializationProgressMonitor());
			m_window = m_windowFactory.create(DEMO_VIEW_WINDOW, new DemoWindowBehaviourInjector(camera));
			context.getWindowManager().addWindow(m_window);	
			m_window.center();
			
			m_player = (RpgCharacter)m_world.getEntities().getByName("player");
			
			camera.attach(m_world);
			camera.setTarget(m_player);
			m_steerDriver.attach(m_player.getBody());
			m_steerDriver.add(new SeekBehavior(1.0F, m_steerSubject));
			m_steerDriver.add(new AvoidanceBehavior(0.4F));
			
			m_dialogueController = new DialogueController(context.getWindowManager(), m_windowFactory, m_player);
			
			m_backgroundMusic = m_audioClipFactory.create(BACKGROUND_MUSIC);
			m_backgroundMusic.repeat();
		
		} catch (AssetConstructionException e)
		{
			m_logger.error("Error occured constructing demo world or world view. Reverting to MainMenu.", e);
			m_context.setState(new DemoMenu(m_windowFactory, m_worldFactory, m_audioClipFactory));
		}
	}	

	public void leave()
	{
		if(m_window != null)
		{
			m_context.getWindowManager().removeWindow(m_window);
			m_window.dispose();
		}
		
		m_backgroundMusic.dispose();
		
		if(m_dialogueController != null)
			m_dialogueController.dispose();
	}

	public void update(int deltaTime)
	{
		Vector2F delta = m_movementDelta.isZero() ? new Vector2F() : m_movementDelta.multiply(10);
		
		if(!delta.isZero())
		{
			Vector2F moveTarget = m_player.getBody().getLocation().getXy().add(delta);
			m_steerSubject.setLocation(moveTarget);
			m_steerDriver.update(deltaTime);
		}
		
		m_world.update(deltaTime);
	}
	
	private class DemoWindowBehaviourInjector extends WindowBehaviourInjector
	{
		private final ICamera m_camera;
		
		private Vector2D m_lastCursorLocation = new Vector2D();
		private Actor m_lastPickedEntity = null;
		
		public DemoWindowBehaviourInjector(ICamera camera)
		{
			m_camera = camera;
		}
		
		@Override
		public void doInject() throws NoSuchControlException
		{
			final WorldView demoWorldView = getControl(WorldView.class, "demoWorldView");
			final MenuStrip menuStrip = new MenuStrip();
			final Label lblDefaultCommand = new Label();
			final Timer defaultCommandTimer = new Timer();
			
			addControl(menuStrip);
			addControl(lblDefaultCommand);
			
			lblDefaultCommand.setVisible(false);
			
			addControl(defaultCommandTimer);
			
			defaultCommandTimer.addObserver(new ITimerObserver() {
				@Override
				public void update(int deltaTime) {
					Actor pickedEntity = demoWorldView.pick(Actor.class, m_lastCursorLocation);
					String defaultCommand = pickedEntity == null ? null : pickedEntity.getDefaultCommand(m_player);
					
					if(pickedEntity != null && defaultCommand != null)
					{
						m_lastPickedEntity = pickedEntity;
						lblDefaultCommand.setText(defaultCommand);
						lblDefaultCommand.setVisible(true);
					}else
					{
						m_lastPickedEntity = null;
						lblDefaultCommand.setVisible(false);
					}
				}
			});
			
			getControl(Button.class, "btnBack").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_context.setState(new DemoMenu(m_windowFactory, m_worldFactory, m_audioClipFactory));
				}
			});
			
			demoWorldView.setCamera(m_camera);
			
			demoWorldView.addObserver(new IWorldViewObserver() {
				@Override
				public void mouseEvent(InputMouseEvent event)
				{
					m_lastCursorLocation = event.location;
					
					if(event.mouseButton == MouseButton.Right)
					{
						if(event.type == MouseEventType.MouseClicked)
						{
							final Actor pickedActor = demoWorldView.pick(Actor.class, event.location);
							
							String[] commands = pickedActor == null ? new String[0] : pickedActor.getCommands(m_player);
							
							if(commands.length > 0)
							{
								menuStrip.setContext(commands, new IMenuStripListener() {
								
									@Override
									public void onCommand(String command) {
										pickedActor.doCommand(m_player, command);
									}
								});
								
								menuStrip.setLocation(event.location);
							}
						}
					}else if(event.mouseButton == MouseButton.Left)
					{
						menuStrip.setVisible(false);
	
						if(event.type == MouseEventType.MousePressed)
						{
							if(m_lastPickedEntity != null)
								m_lastPickedEntity.doCommand(m_player, lblDefaultCommand.getText());
							else
							{
								Vector2F delta = demoWorldView.translateScreenToWorld(new Vector2F(event.location)).difference(m_player.getBody().getLocation().getXy());
								m_movementDelta = delta.isZero() ? new Vector2F(0, -1) : new Vector2F(Direction.fromVector(delta).getDirectionVector()).normalize();
							}
						}else if(event.type == MouseEventType.MouseReleased) {
							m_movementDelta = new Vector2F(0, 0);
						}
					} else if(event.type == MouseEventType.MouseMoved)
					{
						if(!m_movementDelta.isZero())
						{
							Vector2F delta = demoWorldView.translateScreenToWorld(new Vector2F(event.location)).difference(m_player.getBody().getLocation().getXy());
							m_movementDelta = delta.isZero() ? new Vector2F(0, -1) : new Vector2F(Direction.fromVector(delta).getDirectionVector()).normalize();
						}
						
						lblDefaultCommand.setLocation(event.location);
					}
				}
			});
			
			addObserver(new IWindowObserver() {
				
				@Override
				public void onMouseEvent(InputMouseEvent event) { }
				
				@Override
				public void onKeyEvent(InputKeyEvent e) { }
				
				@Override
				public void onFocusChanged(boolean hasFocus)
				{
					if(!hasFocus)
						m_movementDelta = new Vector2F();
				}
			});
		}
	}
	
	private static class DialogueController implements IDisposable
	{
		private final Logger m_logger = LoggerFactory.getLogger(DialogueController.class);
		
		private final ArrayList<CharacterDialogueQuery> m_queries = new ArrayList<>();
		
		private final WindowManager m_windowManager;
		private final IWindowFactory m_windowFactory;
		private final RpgCharacter m_host;
		
		private final DialogueObserver m_observer = new DialogueObserver();
		
		public DialogueController(WindowManager windowManager, IWindowFactory windowFactory, RpgCharacter host)
		{
			m_windowManager = windowManager;
			m_windowFactory = windowFactory;
			m_host = host;
			m_host.addActionObserver(m_observer);
		}
		
		@Override
		public void dispose()
		{
			for(CharacterDialogueQuery q : m_queries)
				q.dispose();
			
			m_queries.clear();
			m_host.removeActionObserver(m_observer);
		}
		
		private class DialogueObserver implements IActionObserver
		{
			@Override
			public void listening(IDialogueListenerSession session)
			{
				/*
				try
				{
					final CharacterDialogueQuery query = new CharacterDialogueQueryFactory(m_windowManager, m_windowFactory, "ui/windows/characterDialogue.jwl").create(session);
				
					query.addObserver(new ICharacterDialogueQueryObserver() {
						@Override
						public void sessionEnded() {
							query.dispose();
						}
					});
					
				} catch (WindowConstructionException e) {
					m_logger.error("Unable to construct character dialoge query", e);
				}*/
				assert false;
			}

			@Override
			public void speaking(IDialogueSpeakerSession session) { }	

			@Override
			public void attackedTarget(RpgCharacter attackee) { }

		}
	}
}
