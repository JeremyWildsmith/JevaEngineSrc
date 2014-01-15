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
package io.github.jevaengine.mapeditor;

import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.mapeditor.MapEditor.LayerMetaData;
import io.github.jevaengine.math.Vector2F;

import java.io.FileOutputStream;
import java.io.IOException;

public interface IEditorPaneListener
{
	void setEntityLayer(int layer);
	void setTile(EditorTile tile, boolean isTraversable, boolean isStatic, String sprite, String animation, float fVisibility, boolean enableSplitting);
	void setScript(String script);
	String getScript();
	int getEntityLayer();

	void setSelectedLayerBackground(String background) throws IOException;
	void setSelectedLayerBackgroundLocation(Vector2F offset) throws IOException;
	LayerMetaData getSelectedLayerBackground();
	
	void refreshEntity(EditorEntity entity);
	void removeEntity(EditorEntity entity);

	void initializeWorld(int worldWidth, int worldHeight, int tileWidth, int tileHeight);

	void openWorld(IImmutableVariable world);
	void saveWorld(FileOutputStream fileOutputStream, EditorEntity[] entities);
	
	void selectLayer(int i);
	void deleteSelectedLayer();
	void createNewLayer();
	int getLayers();
}
