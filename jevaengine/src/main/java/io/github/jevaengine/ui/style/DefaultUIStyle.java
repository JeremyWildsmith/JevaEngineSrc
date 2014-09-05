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
package io.github.jevaengine.ui.style;

import java.util.Map;
import java.util.NoSuchElementException;

public final class DefaultUIStyle implements IUIStyle
{
	private String m_defaultStyle;
	private Map<String, ComponentStyle> m_componentStyles;

	public DefaultUIStyle(String defaultStyle, Map<String, ComponentStyle> componentStyles)
	{
		m_defaultStyle = defaultStyle;
		m_componentStyles = componentStyles;
	}
	
	@Override
	public void dispose()
	{
		for(ComponentStyle style : m_componentStyles.values())
			style.dispose();
	}

	public ComponentStyle getComponentStyle(String componentName)
	{
		ComponentStyle style = m_componentStyles.get(componentName);
		
		if(style == null)
			style = m_componentStyles.get(m_defaultStyle);
		
		if(style == null)
			throw new NoSuchElementException();
		
		return style;
	}
}
