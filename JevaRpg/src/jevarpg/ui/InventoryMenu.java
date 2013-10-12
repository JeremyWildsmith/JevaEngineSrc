package jevarpg.ui;

import java.awt.Graphics2D;
import java.lang.ref.WeakReference;

import jeva.graphics.ui.Button;
import jeva.graphics.ui.Window;
import jeva.graphics.ui.MenuStrip;
import jeva.graphics.ui.MenuStrip.IMenuStripListener;
import jeva.graphics.ui.Panel;
import jeva.graphics.ui.UIStyle;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.joystick.InputManager.InputMouseEvent.EventType;
import jeva.joystick.InputManager.InputMouseEvent.MouseButton;
import jeva.math.Vector2D;
import jevarpg.IItemStore;
import jevarpg.ItemSlot;
import jevarpg.RpgCharacter;

public class InventoryMenu extends Window
{
    private IItemStore m_inventoryHost;
    private WeakReference<RpgCharacter> m_accessor;
    private MenuStrip m_menuStrip;

    public InventoryMenu(UIStyle style)
    {
        super(style, 240, 100);
        this.setVisible(false);

        m_accessor = new WeakReference<RpgCharacter>(null);

        m_menuStrip = new MenuStrip();
    }

    public void accessInventory(IItemStore host, RpgCharacter accessor)
    {
        if (!host.allowStoreAccess(accessor))
            return;

        this.clearControls();

        this.setVisible(true);

        m_inventoryHost = host;
        m_accessor = new WeakReference<RpgCharacter>(accessor);

        int x = 15;
        int y = 15;

        ItemSlot[] slots = m_inventoryHost.getSlots();

        for (int i = 0; i < slots.length; i++)
        {
            InventorySlotContainer slot = new InventorySlotContainer(i);
            slot.setRenderBackground(false);
            slot.setStyle(getStyle());

            if (x + slot.getBounds().width > this.getBounds().width)
            {
                y += 15 + slot.getBounds().height;
                x = 15;
            }

            this.addControl(slot, new Vector2D(x, y));

            x += 15 + (slot.getBounds().width);
        }

        this.setHeight(y + 70);

        this.addControl(new Button("Exit")
        {

            @Override
            public void onButtonPress()
            {
                InventoryMenu.this.setVisible(false);

            }
        }, new Vector2D(70, y + 35));
    }

    @Override
    public void update(int delta)
    {
        super.update(delta);

        // If the accessor is disposed off, the inventory host can be released.
        if (m_accessor.get() == null)
            m_inventoryHost = null;

        if (this.isVisible() && (m_accessor.get() == null || !m_inventoryHost.allowStoreAccess(m_accessor.get())))
            this.setVisible(false);
    }

    private class InventorySlotContainer extends Panel
    {
        private int m_slotIndex;

        public InventorySlotContainer(int slotIndex)
        {
            super(30, 30);
            m_slotIndex = slotIndex;
        }

        @Override
        public void onMouseEvent(InputMouseEvent mouseEvent)
        {
            if (m_accessor.get() == null || !m_inventoryHost.allowStoreAccess(m_accessor.get()))
                return;

            if (mouseEvent.type == EventType.MouseClicked && mouseEvent.mouseButton == MouseButton.Right)
            {
                InventoryMenu.this.addControl(m_menuStrip, mouseEvent.location.difference(InventoryMenu.this.getAbsoluteLocation()));

                String options[] = m_inventoryHost.getSlotActions(m_accessor.get(), m_slotIndex);

                if (options.length > 0)
                {
                    m_menuStrip.setContext(options, new IMenuStripListener()
                    {
                        @Override
                        public void onCommand(String bommand)
                        {
                            if (m_accessor.get() != null && m_inventoryHost.allowStoreAccess(m_accessor.get()))
                                m_inventoryHost.doSlotAction(m_accessor.get(), bommand, m_slotIndex);
                        }
                    });
                }
            }
        }

        @Override
        public void onKeyEvent(InputKeyEvent keyEvent)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void update(int deltaTime)
        {
        }

        @Override
        public void render(Graphics2D g, int x, int y, float fScale)
        {
            super.render(g, x, y, fScale);

            ItemSlot slot = m_inventoryHost.getSlots()[m_slotIndex];

            if (!slot.isEmpty())
                slot.getItem().getGraphic().render(g, x + this.getBounds().width / 3, y + this.getBounds().height / 3, fScale);
        }
    }
}
