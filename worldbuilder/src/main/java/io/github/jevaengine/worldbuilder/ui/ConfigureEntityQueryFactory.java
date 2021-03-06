package io.github.jevaengine.worldbuilder.ui;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.config.json.JsonVariable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQuery;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQueryMode;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.IFileInputQueryObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.IMessageBoxObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.MessageBox;
import io.github.jevaengine.worldbuilder.world.EditorEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigureEntityQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/configureEntity.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	private final String m_base;
	
	public ConfigureEntityQueryFactory(WindowManager windowManager, IWindowFactory windowFactory, String base)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
		m_base = base;
	}
	
	public ConfigureEntityQuery create(EditorEntity subject) throws WindowConstructionException
	{
		Observers observers = new Observers();
			
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new ConfigureEntityQueryBehaviourInjector(observers, subject));
		m_windowManager.addWindow(window);
			
		window.center();
		return new ConfigureEntityQuery(observers, window);
	}
	
	public static class ConfigureEntityQuery implements IDisposable
	{
		private final Observers m_observers;
		
		private final Window m_window;
		
		private ConfigureEntityQuery(Observers observers, Window window)
		{
			m_observers = observers;
			m_window = window;
		}
		
		@Override
		public void dispose()
		{
			m_window.dispose();
		}
		
		public void setVisible(boolean isVisible)
		{
			m_window.setVisible(isVisible);
		}
		
		public void setLocation(Vector2D location)
		{
			m_window.setLocation(location);
		}
		
		public void center()
		{
			m_window.center();
		}
		
		public void addObserver(IConfigureEntityQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeOberver(IConfigureEntityQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	private class ConfigureEntityQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Logger m_logger = LoggerFactory.getLogger(ConfigureEntityQueryFactory.class);
		
		private final Observers m_observers;

		private final EditorEntity m_subject;
		
		public ConfigureEntityQueryBehaviourInjector(Observers observers, EditorEntity subject)
		{
			m_subject = subject;
			m_observers = observers;
		}
		
		@Nullable
		Vector2D parseVector2D(String vector)
		{
			String[] components = vector.split(",[ ]*");
			
			if(components.length != 2)
				return null;
			
			try
			{
				Vector2D buffer = new Vector2D();
				
				buffer.x = Integer.parseInt(components[0]);
				buffer.y = Integer.parseInt(components[1]);
				
				return buffer;
			}catch(NumberFormatException e)
			{
				return null;
			}
			
		}
		
		@Nullable
		private Vector3F parseVector3F(String vector)
		{
			String[] components = vector.split(",[ ]*");
			
			if(components.length != 3)
				return null;
			
			try
			{
				Vector3F buffer = new Vector3F();
				
				buffer.x = Float.parseFloat(components[0]);
				buffer.y = Float.parseFloat(components[1]);
				buffer.z = Float.parseFloat(components[2]);
				
				return buffer;
			}catch(NumberFormatException e)
			{
				return null;
			}
		}
		
		private void validationFailed(String cause)
		{
			try
			{
				final MessageBox msgBox = new MessageBoxFactory(m_windowManager, m_windowFactory).create("Validation failed: " + cause);
				
				msgBox.addObserver(new IMessageBoxObserver() {
					@Override
					public void okay() {
						msgBox.dispose();
					}
				});
			} catch(AssetConstructionException e)
			{
				m_logger.error("Unable to construct messagebox notifying uses of validation failures.", e);
			}
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final TextArea txtName = getControl(TextArea.class, "txtName");
			final TextArea txtTypeName = getControl(TextArea.class, "txtTypeName");
			final TextArea txtConfiguration = getControl(TextArea.class, "txtConfiguration");
			final TextArea txtDirection = getControl(TextArea.class, "txtDirection");
			final TextArea txtLocation = getControl(TextArea.class, "txtLocation");
			final TextArea txtAuxConfig = getControl(TextArea.class, "txtAuxConfig");
			
			txtName.setText(m_subject.getName());
			txtTypeName.setText(m_subject.getClassName());
			txtConfiguration.setText(m_subject.getConfig());
			txtDirection.setText(String.format("%d, %d", m_subject.getDirection().getDirectionVector().x, m_subject.getDirection().getDirectionVector().y));
			txtLocation.setText(String.format("%f, %f, %f", m_subject.getLocation().x, m_subject.getLocation().y, m_subject.getLocation().z));
			
			try(ByteArrayOutputStream bos = new ByteArrayOutputStream())
			{
				m_subject.getAuxiliaryConfig().serialize(bos, true);
				txtAuxConfig.setText(bos.toString("UTF8"));
			} catch (IOException | ValueSerializationException e)
			{
				m_logger.error("Unable to deserialize subject auxiliary configuration into JSON format. Assuming empty auxiliary configuration.", e);
				txtAuxConfig.setText("{ }" );
			}
			
			getControl(Button.class, "btnApply").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					try
					{
						String name = txtName.getText();
						String typeName = txtTypeName.getText();
						String configuration = txtConfiguration.getText();
						String direction = txtDirection.getText();
						String location = txtLocation.getText();
						
						JsonVariable auxConfig = JsonVariable.create(new ByteArrayInputStream(txtAuxConfig.getText().getBytes("UTF8")));
						
						Vector3F locVector = parseVector3F(location);
						Vector2D dirVector = parseVector2D(direction);
						
						if(name.length() <= 0)
						{
							validationFailed("You must specify a name ofr the entity.");
						} else if(typeName.length() <= 0)
						{
							validationFailed("You must specify a type name for the entity.");
						}else if(dirVector == null)
						{
							validationFailed("Unable to parse direction vector. Assure it is properly formatted.");
						}else if(locVector == null)
						{
							validationFailed("Unable to parse location vector. Assure it is properly formatted.");
						}else
						{
							m_subject.setName(name);
							m_subject.setClassName(typeName);
							m_subject.setConfig(configuration);
							m_subject.setDirection(Direction.fromVector(new Vector2F(dirVector)));
							m_subject.setLocation(locVector);
							m_subject.setAuxiliaryConfig(auxConfig);
							m_observers.apply();			
						}
					}catch(IOException | ValueSerializationException e)
					{
						validationFailed("Error occured parsing auxiliary configuration. Assure it is a properly formatted JSON document.");
					}
				}
			});
			
			getControl(Button.class, "btnCancel").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.cancel();
				}
			});
			
			getControl(Button.class, "btnBrowse").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					
					try
					{
						final FileInputQuery query = new FileInputQueryFactory(m_windowManager, m_windowFactory, m_base).create(FileInputQueryMode.OpenFile, "Entity Configuration", m_base);
					
						query.addObserver(new IFileInputQueryObserver() {
							
							@Override
							public void okay(String input) {
								txtConfiguration.setText(input);
								query.dispose();
							}
							
							@Override
							public void cancel() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e)
					{
						m_logger.error("Error constructing browse dialogue for configuration source selection.", e);
					}
				}
			});
			
			getControl(Button.class, "btnDelete").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.delete();
				}
			});
		}
	}
	
	public interface IConfigureEntityQueryObserver
	{
		void apply();
		void delete();
		void cancel();
	}
	
	private static class Observers extends StaticSet<IConfigureEntityQueryObserver>
	{
		public void apply()
		{
			for(IConfigureEntityQueryObserver o : this)
				o.apply();
		}
		
		public void delete()
		{
			for(IConfigureEntityQueryObserver o : this)
				o.delete();
		}
		
		public void cancel()
		{
			for(IConfigureEntityQueryObserver o : this)
				o.cancel();
		}
	}
}
