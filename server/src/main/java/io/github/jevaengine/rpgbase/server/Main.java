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
import io.github.jevaengine.Core.CoreMode;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.Script;
import io.github.jevaengine.game.Game;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		
		Game game = new ServerGame();
		Core.initialize(game, new ServerLibrary(), guiMode ? CoreMode.Normal : CoreMode.LogicOnly);
		
		if(guiMode)
			game.init(createGui(), WINX, WINY);
		else
			game.init();

		final int targetTime = 1000 / 60;

		long lastTime = System.nanoTime() / 1000000;
		long curTime = lastTime;

		try(Scanner scanner = new Scanner(System.in))
		{
		
			String scriptLog = "";
			int emptyCount = 0;
			
			Script m_script = new Script();
			
			while (true)
			{
				curTime = System.nanoTime() / 1000000;
	
				game.update((int) ((curTime - lastTime)));
				game.render();
	
				int cycleLength = (int) (curTime - lastTime);
	
				lastTime = curTime;
				
				if(System.in.available() > 0 && scanner.hasNextLine())
				{
	
					String line = scanner.nextLine();
					
					if(line.isEmpty())
						emptyCount++;
					else
						scriptLog += " " + line;
					
					if(emptyCount >= 2)
					{
						try
						{
							m_script.evaluate(scriptLog);
						}catch(CoreScriptException e)
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
					Thread.sleep(Math.max(targetTime - cycleLength, 20));
				} catch (InterruptedException ex)
				{
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
	
	public static Frame createGui()
	{
		final Frame frame = new Frame();
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
