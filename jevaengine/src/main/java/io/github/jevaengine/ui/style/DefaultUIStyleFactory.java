package io.github.jevaengine.ui.style;

import io.github.jevaengine.audio.IAudioClip;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.audio.IAudioClipFactory.AudioClipConstructionException;
import io.github.jevaengine.audio.NullAudioClip;
import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.graphics.IFont;
import io.github.jevaengine.graphics.IFontFactory;
import io.github.jevaengine.graphics.IFontFactory.FontConstructionException;
import io.github.jevaengine.graphics.IGraphicFactory;
import io.github.jevaengine.graphics.ISpriteFactory;
import io.github.jevaengine.graphics.ISpriteFactory.SpriteConstructionException;
import io.github.jevaengine.math.Vector3D;
import io.github.jevaengine.ui.style.DefaultUIStyleFactory.UIStyleDeclaration.UIComponentStateStyleDeclaration;
import io.github.jevaengine.ui.style.DefaultUIStyleFactory.UIStyleDeclaration.UIComponentStyleDeclaration;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public final class DefaultUIStyleFactory implements IUIStyleFactory
{
	private final ISpriteFactory m_spriteFactory;
	private final IAudioClipFactory m_audioClipFactory;
	private final IConfigurationFactory m_configurationFactory;
	private final IFontFactory m_fontFactory;
	private final IGraphicFactory m_graphicFactory;
	
	@Inject
	public DefaultUIStyleFactory(ISpriteFactory spriteFactory, IAudioClipFactory audioClipFactory, IConfigurationFactory configurationFactory, IFontFactory fontFactory, IGraphicFactory graphicFactory)
	{
		m_spriteFactory = spriteFactory;
		m_audioClipFactory = audioClipFactory;
		m_configurationFactory = configurationFactory;
		m_fontFactory = fontFactory;
		m_graphicFactory = graphicFactory;
	}
	
	private ComponentStateStyle constructComponentStateStyle(UIComponentStateStyleDeclaration decl) throws SpriteConstructionException, FontConstructionException, AudioClipConstructionException
	{		
		IAudioClip enterAudio = new NullAudioClip();
		
		try
		{
			if(decl.audioEnter != null)
				enterAudio = m_audioClipFactory.create(decl.audioEnter);
			
			IFont font = m_fontFactory.create(decl.font.font, decl.font.color);
			IFrameFactory frameFactory = null;
			
			if(decl.frame == null)
				frameFactory = new NullFrameFactory();
			else if(decl.frame.background != null)
				frameFactory = new BackgroundFrameFactory(m_graphicFactory, m_spriteFactory.create(decl.frame.background));
			else
			{
				frameFactory = new TiledFrameFactory(m_graphicFactory,
						m_spriteFactory.create(decl.frame.topLeft), 
						m_spriteFactory.create(decl.frame.top), 
						m_spriteFactory.create(decl.frame.topRight),
						m_spriteFactory.create(decl.frame.left),
						m_spriteFactory.create(decl.frame.fill),
						m_spriteFactory.create(decl.frame.right),
						m_spriteFactory.create(decl.frame.bottomLeft),
						m_spriteFactory.create(decl.frame.bottom),
						m_spriteFactory.create(decl.frame.bottomRight));
			}

			return new ComponentStateStyle(font, frameFactory, enterAudio);
		} catch(SpriteConstructionException | FontConstructionException | AudioClipConstructionException e)
		{
			enterAudio.dispose();
			throw e;
		}
	}
	
	private ComponentStyle constructComponentState(UIComponentStyleDeclaration decl) throws SpriteConstructionException, FontConstructionException, AudioClipConstructionException
	{
		ComponentStateStyle defaultStateStyle = null;
		ComponentStateStyle enterStateStyle = null;
		ComponentStateStyle activatedStateStyle = null;
	
		try
		{
			defaultStateStyle = constructComponentStateStyle(decl.defaultStateStyle);
			enterStateStyle = decl.enterStateStyle == null ? defaultStateStyle : constructComponentStateStyle(decl.enterStateStyle);
			activatedStateStyle = decl.activatedStateStyle == null ? defaultStateStyle : constructComponentStateStyle(decl.activatedStateStyle);
			return new ComponentStyle(defaultStateStyle, enterStateStyle, activatedStateStyle);
			
		} catch(SpriteConstructionException | FontConstructionException | AudioClipConstructionException e)
		{
			if(defaultStateStyle != null)
				defaultStateStyle.dispose();
			
			if(enterStateStyle != null)
				enterStateStyle.dispose();
			
			if(enterStateStyle != null)
				enterStateStyle.dispose();
			
			throw e;
		}
	}
	
	public DefaultUIStyle create(String name) throws UIStyleConstructionException
	{
		try
		{
			UIStyleDeclaration styleDecl = m_configurationFactory.create(name).getValue(UIStyleDeclaration.class);
			
			Map<String, ComponentStyle> componentStyle = new HashMap<>();
			
			for(UIComponentStyleDeclaration compStyleDecl : styleDecl.componentStyles)
			{
				ComponentStyle style = constructComponentState(compStyleDecl);
				
				for(String s : compStyleDecl.components)
					componentStyle.put(s, style);
			}
			
			return new DefaultUIStyle(styleDecl.defaultStyle, componentStyle);
		} catch (ValueSerializationException | ConfigurationConstructionException | SpriteConstructionException | FontConstructionException | AudioClipConstructionException  e)
		{
			throw new UIStyleConstructionException(name, e);
		}
	}
	
	public static class UIStyleDeclaration implements ISerializable
	{
		public String defaultStyle;
		public UIComponentStyleDeclaration componentStyles[];
		
		public UIStyleDeclaration() { }

		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("componentStyles").setValue(componentStyles);
			target.addChild("defaultStyle").setValue(defaultStyle);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				componentStyles = source.getChild("componentStyles").getValues(UIComponentStyleDeclaration[].class);
				defaultStyle = source.getChild("defaultStyle").getValue(String.class);
			} catch(NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}

		public static class UIFontStyle implements ISerializable
		{
			public String font;
			public Color color;

			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				target.addChild("font").setValue(font);
				target.addChild("color").setValue(new Vector3D(color.getRed(), color.getGreen(), color.getBlue()));
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					font = source.getChild("font").getValue(String.class);
					
					Vector3D colourVec = source.getChild("color").getValue(Vector3D.class);
					color = new Color(colourVec.x, colourVec.y, colourVec.z);
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
		
		public static class UIFrameStyleDeclaration implements ISerializable
		{
			public String background;
			
			public String fill;
			public String left;
			public String right;
			public String top;
			public String bottom;
			public String topLeft;
			public String topRight;
			public String bottomLeft;
			public String bottomRight;
			
			public UIFrameStyleDeclaration() { }

			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				if(background != null)
					target.addChild("background").setValue(background);
				else
				{
					target.addChild("fill").setValue(fill);
					target.addChild("left").setValue(left);
					target.addChild("light").setValue(right);
					target.addChild("top").setValue(top);
					target.addChild("bottom").setValue(bottom);
					target.addChild("topLeft").setValue(topLeft);
					target.addChild("topRight").setValue(topRight);
					target.addChild("bottomLeft").setValue(bottomLeft);
					target.addChild("bottomRight").setValue(bottomRight);
				}
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					if(source.childExists("background"))
						background = source.getChild("background").getValue(String.class);
					else
					{
						fill = source.getChild("fill").getValue(String.class);
						left = source.getChild("left").getValue(String.class);
						right = source.getChild("right").getValue(String.class);
						top = source.getChild("top").getValue(String.class);
						bottom = source.getChild("bottom").getValue(String.class);
						topLeft = source.getChild("topLeft").getValue(String.class);
						bottomLeft = source.getChild("bottomLeft").getValue(String.class);
						topRight = source.getChild("topRight").getValue(String.class);
						bottomRight = source.getChild("bottomRight").getValue(String.class);
					}
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
		
		public static class UIComponentStateStyleDeclaration implements ISerializable
		{
			private UIFontStyle font;
			private String audioEnter;
			private UIFrameStyleDeclaration frame;
			
			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				if(audioEnter != null)
					target.addChild("audioEnter").setValue(audioEnter);
				
				if(frame != null)
					target.addChild("frame").setValue(frame);

				target.addChild("font").setValue(font);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					if(source.childExists("audioEnter"))
						audioEnter = source.getChild("audioEnter").getValue(String.class);
					
					if(source.childExists("frame"))
						frame = source.getChild("frame").getValue(UIFrameStyleDeclaration.class);
					
					font = source.getChild("font").getValue(UIFontStyle.class);
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
		
		public static class UIComponentStyleDeclaration implements ISerializable
		{
			public String[] components;
			public UIComponentStateStyleDeclaration defaultStateStyle;
			public UIComponentStateStyleDeclaration enterStateStyle;
			public UIComponentStateStyleDeclaration activatedStateStyle;
			
			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				target.addChild("components").setValue(components);
				target.addChild("defaultStateStyle").setValue(defaultStateStyle);
				
				if(enterStateStyle != null && enterStateStyle != defaultStateStyle)
					target.addChild("enterStateStyle").setValue(enterStateStyle);
				
				if(activatedStateStyle != null && activatedStateStyle != defaultStateStyle)
					target.addChild("activatedStateStyle").setValue(activatedStateStyle);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					components = source.getChild("components").getValues(String[].class);
					defaultStateStyle = source.getChild("defaultStateStyle").getValue(UIComponentStateStyleDeclaration.class);
					
					if(source.childExists("enterStateStyle"))
						enterStateStyle = source.getChild("enterStateStyle").getValue(UIComponentStateStyleDeclaration.class);
					else
						enterStateStyle = defaultStateStyle;
					
					if(source.childExists("activatedStateStyle"))
						activatedStateStyle = source.getChild("activatedStateStyle").getValue(UIComponentStateStyleDeclaration.class);
					else
						activatedStateStyle = defaultStateStyle;
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
	}
}
