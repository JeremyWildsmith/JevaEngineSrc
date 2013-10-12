package jevamap;

import java.io.FileOutputStream;
import java.util.ArrayList;

import jeva.config.VariableStore;
import jeva.world.WorldDirection;

public interface IEditorPaneListener
{
	void setEntityLayer(int layer);

	void setNullSprite(String name);

	void setTile(EditorTile tile, boolean isTraversable, boolean isStatic, String sprite, String animation, WorldDirection direction, float fVisibility, boolean enableSplitting);

	void refreshEntity(EditorEntity entity);

	void removeEntity(EditorEntity entity);

	void initializeWorld(int worldWidth, int worldHeight, int tileWidth, int tileHeight);

	void openMap(VariableStore map);

	void saveMap(FileOutputStream fileOutputStream, ArrayList<EditorEntity> entities);

	void selectLayer(int i);

	void deleteSelectedLayer();

	void createNewLayer();

	void applyScript(String script);
}
