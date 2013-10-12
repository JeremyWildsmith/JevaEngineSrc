package UnitTest;

import java.util.Iterator;

/**
 * 
 * @author Jeremy Wildsmith
 */
public class Main
{
	public static void main(String[] args)
	{

		System.out.println("Welcome To UnderGround Online\n\n" + "For Latest News, Support & Feedback, Visit: reddit.com/r/jevaengine\n\n" + "Game Masters or Staff will never ask for your password. " + "Do not disclose any personal data to anyone including staff members. " + "JevaEngine is not directly affiliated with GameJolt. " + "\n\n\n\n\n" + "\n(Scroll Down For Credits)\n" + "\n" + "Credits:\n" + "JevaEngine Team - Lead Programmer - Jeremy. A. W.\n" + "    - JevaEngine Programmer\n" + "\n" + "OpenGameArt.org - cemkalyoncu\n" + "    - BoxyBold UI Font\n" + "\n" + "OpenGameArt.org - NenadSimic\n" + "    - UI select sound\n" + "\n" + "OpenGameArt.org - Sudocolon\n" + "    - UI hover over sound\n" + "\n" + "OpenGameArt.org - Michel Baradari\n" + "    - AutoRifle\\Rifle Sound Effects\n" + "\n" + "OpenGameArt.org - AllergicRacoon\n" + "    - UI select sound\n" + "\n" + "DeviantArt.com  - SteelRaven7\n" + "    - Soldier Character Graphics\n" + "\n" + "OpenGameArt.org - leeor_net\n" + "    - Character Menu Particle Effects\n" + "\n" + "OpenGameArt.org - johndh\n" + "    - Blood Particle Effect\n" + "    - Spider Character Graphics (Wciow for Model and Texture)\n" + "\n" + "OpenGameArt.org - AngryMeteor\n" + "    - Item icons\n" + "\n" + "OpenGameArt.org - Clint Bellanger\n" + "    - Dungeon Tile Graphics\n" + "    - Zombie Character Graphics\n" + "\n" + "OpenGameArt.org - Clint Bellanger,Lamoot,Blender Foundation lattice, p0ss, OpenGameArt.org Bart K. Sindwiller\n" + "    - Outside Tile Graphics\n" + "\n" + "PARPG Assets:" + " - http://wiki.parpg.net/Gallery#Renders" + "" + "Thanks To: \n" + "    - Java-Gaming.org\n" + "    - OpenGameArt.org\n" + "    - GameJolt.org\n" + "    - The Flare Project\n" + "    - PARPG");
		/*
		 * 
		 * FOR FLARE int[] frames = new int[] {4, 8, 8, 8}; String[] animations
		 * = new String[] {"idle", "walking", "attack", "die"}; String[]
		 * directions = new String[] {"sw", "w", "nw", "n", "ne", "e", "se",
		 * "s"};
		 * 
		 * for(int y = 0; y < directions.length; y++) { String dir =
		 * directions[y];
		 * 
		 * int x = 0; for(int i = 0; i < animations.length; i++) { for(int frame
		 * = 0; frame < frames[i]; frame++) { System.out.println("delay/" + dir
		 * + animations[i] + "/" + frame + ":" + 100 +";");
		 * System.out.println("anchor/" + dir + animations[i] + "/" + frame +
		 * ":64,86;"); System.out.println("animation/" + dir + animations[i] +
		 * "/" + frame + ":" + x * 128 + "," + y * 128 + ",128,128;" ); x++; } }
		 * }
		 * 
		 * return;
		 */

		/*
		 * int frameWidth = 200; int frameHeight = 200;
		 * 
		 * int[] frames = new int[] { 12, 6 }; int[] intervals = new int[] { 90,
		 * 120 }; String[] animations = new String[] { "walking", "idle" };
		 * String[] directions = new String[] { "ne", "n", "nw", "w", "sw", "s",
		 * "se", "e" };
		 * 
		 * for (int y = 0; y < directions.length; y++) { String dir =
		 * directions[y];
		 * 
		 * int x = 0; for (int i = 0; i < animations.length; i++) { for (int
		 * frame = 0; frame < frames[i]; frame++) { System.out.println("delay/"
		 * + dir + animations[i] + "/" + frame + ":" + intervals[i] + ";");
		 * System.out.println("anchor/" + dir + animations[i] + "/" + frame +
		 * ":106,119;"); System.out.println("animation/" + dir + animations[i] +
		 * "/" + frame + ":" + x * frameWidth + "," + y frameHeight + "," +
		 * frameWidth + "," + frameHeight + ";"); x++; } } }
		 */

		Month m_currentMonth = Month.getCurrent();

		Iterator<Day> it = m_currentMonth.getIterator(2013);

		Day currentDay = it.hasNext() ? it.next() : null;

		for (int i = 0; it.hasNext(); currentDay = it.next(), i++)
		{
			System.out.println("FUCK");
		}

		return;
	}
}
