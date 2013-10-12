package jevarpg;

import java.util.HashMap;

import proguard.annotation.KeepClassMemberNames;
import jeva.config.VariableStore;
import jeva.game.Game;
import jeva.game.IGameScriptProvider;
import jeva.graphics.AnimationState;
import jeva.graphics.Sprite;
import jeva.graphics.ui.DialogMenu;
import jeva.graphics.ui.Window;
import jeva.graphics.ui.MenuStrip;
import jeva.graphics.ui.MenuStrip.IMenuStripListener;
import jeva.graphics.ui.UIStyle;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.joystick.InputManager.InputMouseEvent.MouseButton;
import jeva.math.Vector2D;
import jeva.world.IInteractable;
import jeva.world.World.WorldScriptContext;
import jeva.Core;
import jeva.IResourceLibrary;
import jevarpg.library.RpgEntityLibrary;
import jevarpg.quest.QuestState;
import jevarpg.ui.CharacterMenu;
import jevarpg.ui.InventoryMenu;
import jevarpg.ui.ItemInfoMenu;
import jevarpg.ui.QuestsMenu;

public abstract class RpgGame extends Game
{
    private Window m_contextStripContainer;
    private MenuStrip m_contextStrip;

    private DialogMenu m_dialog;
    private QuestsMenu m_questsMenu;
    private InventoryMenu m_inventoryMenu;
    private CharacterMenu m_characterMenu;
    private ItemInfoMenu m_itemInfoMenu;

    private Sprite m_cursor;

    @Override
    protected void startup()
    {
        IResourceLibrary fileSystem = Core.getService(IResourceLibrary.class);

        UIStyle styleLarge = UIStyle.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/large.juis")));
        UIStyle styleSmall = UIStyle.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/small.juis")));

        m_cursor = Sprite.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/cursor.jsf")));
        m_cursor.setAnimation("idle", AnimationState.Play);

        m_contextStrip = new MenuStrip();
        m_contextStripContainer = new Window(styleLarge, 100, 220);
        m_contextStripContainer.addControl(m_contextStrip, new Vector2D());
        m_contextStripContainer.setRenderBackground(false);
        m_contextStripContainer.setVisible(false);

        m_itemInfoMenu = new ItemInfoMenu(styleSmall);

        m_questsMenu = new QuestsMenu(styleLarge);
        m_characterMenu = new CharacterMenu(styleLarge);

        m_dialog = new DialogMenu(styleSmall, 500, 90);
        m_dialog.setRenderBackground(true);

        m_inventoryMenu = new InventoryMenu(styleLarge);

        m_dialog.setLocation(new Vector2D(10, 550));
        m_questsMenu.setLocation(new Vector2D(50, 120));
        m_characterMenu.setLocation(new Vector2D(50, 120));
        m_inventoryMenu.setLocation(new Vector2D(230, 300));

        m_dialog.setVisible(false);

        m_dialog.setMovable(false);

        getWindowManager().addWindow(m_dialog);
        getWindowManager().addWindow(m_questsMenu);
        getWindowManager().addWindow(m_characterMenu);
        getWindowManager().addWindow(m_inventoryMenu);
        getWindowManager().addWindow(m_contextStripContainer);
        getWindowManager().addWindow(m_itemInfoMenu);
    }

    public RpgEntityLibrary getEntityLibrary()
    {
        return new RpgEntityLibrary();
    }

    @Override
    public void update(int deltaTime)
    {
        m_cursor.update(deltaTime);

        super.update(deltaTime);
    }

    @Override
    protected void worldSelection(InputMouseEvent e, Vector2D location)
    {
        final IInteractable[] interactables = getWorld().getTileEffects(location).interactables.toArray(new IInteractable[0]);

        if (e.mouseButton == MouseButton.Left)
        {
            if (getPlayer() != null)
                getPlayer().moveTo(location);

            m_contextStrip.setVisible(false);
        } else if (e.mouseButton == MouseButton.Right)
        {
            if (interactables.length > 0 && interactables[0].getCommands().length > 0)
            {
                m_contextStrip.setContext(interactables[0].getCommands(), new IMenuStripListener()
                {
                    @Override
                    public void onCommand(String command)
                    {
                        interactables[0].doCommand(command);
                    }
                });
                m_contextStripContainer.setVisible(true);
                m_contextStripContainer.setLocation(e.location);
                m_contextStripContainer.setWidth(m_contextStrip.getBounds().width);
                m_contextStripContainer.setHeight(m_contextStrip.getBounds().height);
            }
        }
    }

    public DialogMenu getDialog()
    {
        return m_dialog;
    }

    public CharacterMenu getCharacterMenu()
    {
        return m_characterMenu;
    }

    public QuestsMenu getQuestsWindow()
    {
        return m_questsMenu;
    }

    public InventoryMenu getInventoryMenu()
    {
        return m_inventoryMenu;
    }

    public ItemInfoMenu getItemInfoMenu()
    {
        return m_itemInfoMenu;
    }

    @Override
    public UIStyle getGameStyle()
    {
        return m_dialog.getStyle();
    }

    @Override
    protected void onLoadedWorld()
    {
        m_characterMenu.setVisible(false);
        m_contextStrip.setVisible(false);
        m_inventoryMenu.setVisible(false);
        m_questsMenu.setVisible(false);
        m_itemInfoMenu.setVisible(false);
    }

    @Override
    protected Sprite getCursor()
    {
        return m_cursor;
    }

    public abstract RpgCharacter getPlayer();

    @Override
    public IGameScriptProvider getScriptBridge()
    {
        return new RpgGameScriptProvider();
    }

    public class RpgGameScriptProvider implements IGameScriptProvider
    {
        @Override
        public Object getGameBridge()
        {
            return new GameBridge();
        }

        @Override
        public HashMap<String, Object> getGlobals()
        {
            HashMap<String, Object> vars = new HashMap<String, Object>();

            vars.put("quest_notStarted", QuestState.NotStarted);
            vars.put("quest_failed", QuestState.Failed);
            vars.put("quest_completed", QuestState.Completed);
            vars.put("quest_inProgress", QuestState.InProgress);

            return vars;
        }

        @KeepClassMemberNames
        public class GameBridge
        {
            public WorldScriptContext getWorld()
            {
                return RpgGame.this.getWorld().getScriptBridge();
            }

            public RpgCharacter.EntityBridge<?> getPlayer()
            {
                if (RpgGame.this.getPlayer() == null)
                    return null;

                return RpgGame.this.getPlayer().getScriptBridge();
            }
        }
    }
}
