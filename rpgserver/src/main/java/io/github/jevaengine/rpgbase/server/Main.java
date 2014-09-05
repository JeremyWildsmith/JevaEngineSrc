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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.CoreMode;
import io.github.jevaengine.game.DefaultGame;
import io.github.jevaengine.rpgbase.server.library.RpgServerLibrary;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.rhino.RhinoScript;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class Main
{
	private static final int WINX = 800;
	private static final int WINY = 600;

	/*
	
	Sorry this code is a mess and kind of hacky... I'm tired and this code is really not
	important at all...
	
	*/
	
	public static void main(String[] args) throws IOException
	{	
		boolean guiMode = Arrays.binarySearch(args,"gui") >= 0;
		
		DefaultGame game = new RpgServerGame();
		Core.initialize(game, new RpgServerLibrary(), guiMode ? CoreMode.Normal : CoreMode.LogicOnly, Executors.newCachedThreadPool());
		
		if(guiMode)
			game.init(createGui());
		else
			game.init();

		try(Scanner scanner = new Scanner(System.in))
		{
			String scriptLog = "";
			int emptyCount = 0;
			
			RhinoScript m_script = new RhinoScript();
			

			long lastTime;
			
			do
			{
				lastTime = System.currentTimeMillis();
				if(System.in.available() > 0 && scanner.hasNextLine())
				{
					String line = scanner.nextLine();
					
					if(line.isEmpty())
						emptyCount++;
					else
					{
						emptyCount = 0;
						scriptLog += " " + line;
					}
					
					if(emptyCount >= 2)
					{
						try
						{
							m_script.evaluate(scriptLog);
						}catch(ScriptExecuteException e)
						{
							System.out.println("Error: " + e.toString());
						}
						emptyCount = 0;
						System.out.println("Executed script.");
						scriptLog = "";
					}
				}
				try
				{
					Thread.sleep(20);
				} catch (InterruptedException ex)
				{
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				}
			} while(game.update((int)(System.currentTimeMillis() - lastTime)));

		}
	}
	
	public static JFrame createGui()
	{
		final JFrame frame = new JFrame();
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					frame.setVisible(true);
					frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""), new Point(), "trans"));
					frame.setVisible(true);

					frame.setIgnoreRepaint(true);
					frame.setBackground(Color.black);
					frame.setResizable(false);
					frame.setTitle("Server");
					frame.setSize(WINX, WINY);
				}
			});
		} catch (InterruptedException | InvocationTargetException ex)
		{
			throw new RuntimeException(ex);
		}
		
		return frame;
	}
}
