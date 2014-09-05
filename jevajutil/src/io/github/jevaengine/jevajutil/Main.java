package io.github.jevaengine.jevajutil;

import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.config.json.JsonVariable;
import io.github.jevaengine.graphics.DefaultSpriteFactory.SpriteDeclaration;
import io.github.jevaengine.graphics.DefaultSpriteFactory.SpriteDeclaration.AnimationDeclaration;
import io.github.jevaengine.graphics.DefaultSpriteFactory.SpriteDeclaration.FrameDeclaration;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Main
{
	public static void main(String[] args)
	{
		//Configuration...
		final String dest = "C:/Users/Jeremy/jeclipse-workspace/jevaengine2/stove.jsf";
		final Rect2D frameBounds = new Rect2D(180, 210);
		final String texture = "walk.png";
		final String directions[] = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
		final int animationFrames = 11;
		final int animationFrameDelay = 110;
		final Vector2D anchor = new Vector2D(90, 135);
		
		ArrayList<SpriteDeclaration.AnimationDeclaration> animations = new ArrayList<>();
		
		for(int y = 0; y < directions.length; y++)
		{
			AnimationDeclaration animation = new AnimationDeclaration();
			animation.frames = new FrameDeclaration[animationFrames];
			animation.name = directions[y];

			for(int x = 0; x < animationFrames; x++)
			{
				FrameDeclaration frame = new FrameDeclaration();
				frame.anchor = new Vector2D(anchor);
				frame.delay = animationFrameDelay;
				frame.region = new Rect2D(x * frameBounds.width, y * frameBounds.height, frameBounds.width, frameBounds.height);
				animation.frames[x] = frame;
			}
			
			animations.add(animation);
		}
		
		SpriteDeclaration sd = new SpriteDeclaration();
		sd.animations = animations.toArray(new AnimationDeclaration[animations.size()]);
		sd.scale = 1.0F;
		sd.texture = texture;
		sd.defaultAnimation = directions[0];
		
		JsonVariable rootVar = new JsonVariable();

		try(FileOutputStream fos = new FileOutputStream(dest))
		{
			sd.serialize(rootVar);
			rootVar.serialize(fos, true);
		} catch (ValueSerializationException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
