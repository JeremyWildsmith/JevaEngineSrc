package io.github.jevaengine.ui;

import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector3D;
import io.github.jevaengine.util.Nullable;

import java.awt.Color;

public class DefaultControlFactory implements IControlFactory
{

	@Override
	@Nullable
	public Class<? extends Control> lookup(String className)
	{
		if(className.equals(Button.COMPONENT_NAME))
			return Button.class;
		else if(className.equals(Label.COMPONENT_NAME))
			return Label.class;
		else if(className.equals(TextArea.COMPONENT_NAME))
			return TextArea.class;
		else if(className.equals(Viewport.COMPONENT_NAME))
			return Viewport.class;
		else if(className.equals(Panel.COMPONENT_NAME))
			return Panel.class;
		else if(className.equals(WorldView.COMPONENT_NAME))
			return WorldView.class;
		else if(className.equals(ValueGuage.COMPONENT_NAME))
			return ValueGuage.class;
		else if(className.equals(Checkbox.COMPONENT_NAME))
			return Checkbox.class;
		
		return null;
	}

	@Override
	public <T extends Control> String lookup(Class<T> controlClass)
	{
		if(controlClass.equals(Button.class))
			return Button.COMPONENT_NAME;
		else if(controlClass.equals(Label.class))
			return Label.COMPONENT_NAME;
		else if(controlClass.equals(TextArea.class))
			return TextArea.COMPONENT_NAME;
		else if(controlClass.equals(Viewport.class))
			return Viewport.COMPONENT_NAME;
		else if(controlClass.equals(Panel.class))
			return Panel.COMPONENT_NAME;
		else if(controlClass.equals(WorldView.class))
			return WorldView.COMPONENT_NAME;
		else if(controlClass.equals(ValueGuage.class))
			return ValueGuage.COMPONENT_NAME;
		else if(controlClass.equals(Checkbox.class))
			return Checkbox.COMPONENT_NAME;
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Control> T create(Class<T> controlClass, String instanceName, IImmutableVariable config) throws ControlConstructionException
	{
		try
		{
			if(controlClass.equals(Button.class))
			{
				String text = config.getChild("text").getValue(String.class);
				return (T)new Button(instanceName, text);
			}else if(controlClass.equals(Label.class))
			{
				String text = config.childExists("text") ? config.getChild("text").getValue(String.class) : "";
				return (T)new Label(instanceName, text);
			}else if(controlClass.equals(TextArea.class))
			{
				String defaultText = config.childExists("text") ? config.getChild("text").getValue(String.class) : "";
				boolean editable = config.childExists("allowEdit") ? config.getChild("allowEdit").getValue(Boolean.class) : true;
				boolean wordWrap = config.childExists("wordWrap") ? config.getChild("wordWrap").getValue(Boolean.class) : true;
				Rect2D bounds = config.getChild("bounds").getValue(Rect2D.class);
				
				TextArea textArea = new TextArea(instanceName, defaultText, bounds.width, bounds.height);
				textArea.setEditable(editable);
				textArea.setWordWrapped(wordWrap);
				
				return (T)textArea;
			}else if(controlClass.equals(Viewport.class))
			{
				Rect2D bounds = config.getChild("bounds").getValue(Rect2D.class);
				Viewport viewport = new Viewport(instanceName, bounds.width, bounds.height);
				
				return (T)viewport;
			}else if(controlClass.equals(Panel.class))
			{
				Rect2D bounds = config.getChild("bounds").getValue(Rect2D.class);
				Panel panel = new Panel(instanceName, bounds.width, bounds.height);
				
				return (T)panel;
			}else if(controlClass.equals(WorldView.class))
			{
				Rect2D bounds = config.getChild("bounds").getValue(Rect2D.class);
				WorldView worldView = new WorldView(instanceName, bounds.width, bounds.height);
				
				return (T)worldView;
			}else if(controlClass.equals(ValueGuage.class))
			{
				Rect2D bounds = config.getChild("bounds").getValue(Rect2D.class);
				Vector3D color = config.getChild("color").getValue(Vector3D.class);
				
				ValueGuage valueGuage = new ValueGuage(instanceName, new Color(color.x % 256, color.y % 256, color.z % 256), bounds.width, bounds.height);
				
				return (T)valueGuage;
			}else if(controlClass.equals(Checkbox.class))
			{
				boolean value = config.childExists("value") ? config.getChild("value").getValue(Boolean.class) : false;
				
				return (T)new Checkbox(instanceName, value);
			}
		} catch(ValueSerializationException | NoSuchChildVariableException e)
		{
			throw new ControlConstructionException(controlClass.getName(), e);
		}
		
		throw new ControlConstructionException(controlClass.getName(), new UnsupportedControlException());
	}

	@Override
	public Control create(String controlName, String instanceName, IImmutableVariable config) throws ControlConstructionException
	{
		Class<? extends Control> ctrlClass = lookup(controlName);
		
		if(ctrlClass == null)
			throw new ControlConstructionException(controlName, new UnsupportedControlException());
		
		return create(ctrlClass, instanceName, config);
	}
}
