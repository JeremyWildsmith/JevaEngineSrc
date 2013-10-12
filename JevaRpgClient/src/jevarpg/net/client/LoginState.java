package jevarpg.net.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.audio.Audio;
import jeva.communication.network.RemoteSocketCommunicator;
import jeva.config.VariableStore;
import jeva.game.ControlledCamera;
import jeva.graphics.IRenderable;
import jeva.graphics.ui.Button;
import jeva.graphics.ui.Label;
import jeva.graphics.ui.TextArea;
import jeva.graphics.ui.UIStyle;
import jeva.graphics.ui.Viewport;
import jeva.graphics.ui.Window;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.World;
import jevarpg.library.RpgEntityLibrary;
import jevarpg.net.NetUser.UserCredentials;

public final class LoginState implements IGameState
{

	protected static final String HOST = "jevaengine.no-ip.biz";

	protected static final int PORT = 1554;

	private ClientGame m_context;

	private Audio m_backgroundMusic;

	private Window m_loginWindow;

	private TextArea m_connectingAbout;

	private TextArea m_nickname;

	private Window m_overlayWindow;

	private ControlledCamera m_menuCamera = new ControlledCamera();

	private World m_menuWorld;

	public LoginState()
	{
		this(getCredits());
	}

	private static String getCredits()
	{
		return Core.getService(IResourceLibrary.class).openResourceContents("credits.txt");
	}

	public LoginState(String userMessage)
	{
		m_backgroundMusic = new Audio("audio/da/da.ogg");

		final UIStyle styleLarge = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/large.juis")));
		final UIStyle styleSmall = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/small.juis")));

		m_loginWindow = new Window(styleLarge, 500, 400);
		m_loginWindow.setRenderBackground(false);
		m_loginWindow.setMovable(false);
		m_loginWindow.setLocation(new Vector2D(262, 190));

		m_connectingAbout = new TextArea(Color.white, 480, 230);

		m_connectingAbout.setLocation(new Vector2D(10, 10));
		m_connectingAbout.setStyle(styleSmall);

		m_connectingAbout.setText(userMessage);

		m_loginWindow.addControl(m_connectingAbout);

		m_nickname = new TextArea(Color.white, 300, 30);
		m_nickname.setEditable(true);
		m_nickname.setText("Guest");
		m_nickname.setRenderBackground(false);

		m_loginWindow.addControl(new Label("Nickname:", Color.red), new Vector2D(10, 270));
		m_loginWindow.addControl(m_nickname, new Vector2D(190, 270));

		m_loginWindow.addControl(new Button("Connect!")
		{

			@Override
			public void onButtonPress()
			{
				try
				{
					m_nickname.setText(m_nickname.getText().trim());

					Socket clientSocket = new Socket(HOST, PORT);
					clientSocket.setTcpNoDelay(true);

					ClientCommunicator communicator = m_context.getCommunicator();

					if (communicator.isBound())
						communicator.unbind();

					communicator.setUserCredentials(new UserCredentials(m_nickname.getText()));
					communicator.bind(new RemoteSocketCommunicator(clientSocket));

					m_context.setState(new LoadingState());

				} catch (IOException e)
				{
					m_connectingAbout.setText("The servers are currently inaccessible.\n\nTry again later");
				}
			}
		}, new Vector2D(150, 300));

		final Image overlayImage;

		try
		{
			overlayImage = ImageIO.read(Core.getService(IResourceLibrary.class).openResourceStream("ui/menu.png"));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		m_overlayWindow = new Window(styleSmall, 1024, 768);
		m_overlayWindow.setRenderBackground(false);
		m_overlayWindow.setMovable(false);
		m_overlayWindow.setFocusable(false);

		Viewport view = new Viewport(new IRenderable()
		{
			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				g.drawImage(overlayImage, x, y, null);
			}
		}, 1024, 768);

		view.setRenderBackground(false);

		m_overlayWindow.addControl(view);

		m_backgroundMusic = new Audio("audio/da/da.ogg");

		m_menuWorld = World.create(new RpgEntityLibrary(), VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("map/menu.jmp")));

		m_menuCamera.lookAt(new Vector2F(27, 33));
	}

	@Override
	public void enter(ClientGame context)
	{
		m_context = context;
		m_backgroundMusic.repeat();

		context.setWorld(m_menuWorld);

		context.getWindowManager().addWindow(m_loginWindow);
		context.getWindowManager().addWindow(m_overlayWindow);
		context.setCamera(m_menuCamera);
	}

	@Override
	public void leave()
	{
		m_backgroundMusic.stop();
		m_context.clearWorld();

		m_context.getWindowManager().removeWindow(m_loginWindow);
		m_context.getWindowManager().removeWindow(m_overlayWindow);

		m_context.clearCamera();

		m_context = null;
	}

	@Override
	public void update(int deltaTime)
	{
	}
}
