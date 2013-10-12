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
package jevarpg;

import com.sun.istack.internal.Nullable;

import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepName;
import jeva.Core;
import jeva.IResourceLibrary;
import jeva.Script;
import jeva.audio.Audio;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.graphics.AnimationState;
import jeva.graphics.IRenderable;
import jeva.graphics.Sprite;

public class Item
{
    private ItemDescriptor m_descriptor;

    private String m_name;
    private String m_description;

    private Sprite m_graphic;

    private ItemType m_type;

    private Script m_script;

    @KeepClassMemberNames
    @KeepName
    public enum ItemType
    {
        Consumable(new ConsumeItemBehaviour()), General(new NoItemBehaviour()), Weapon(new AttackItemBehaviour(), "item/weaponGear.jsf"), HeadArmor(new NoItemBehaviour(), "item/headGear.jsf"), BodyArmor(new NoItemBehaviour(), "item/bodyGear.jsf"), Accessory(new NoItemBehaviour(), "item/accessoryGear.jsf");

        private Sprite m_icon;
        private IItemBehaviour m_behaviour;

        ItemType(IItemBehaviour behaviour)
        {
            m_behaviour = behaviour;
            m_icon = null;
        }

        ItemType(IItemBehaviour behaviour, String backgroundSpritePath)
        {
            m_behaviour = behaviour;
            m_icon = Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(backgroundSpritePath)));
            m_icon.setAnimation("idle", AnimationState.Play);
        }

        public IItemBehaviour getBehaviour()
        {
            return m_behaviour;
        }

        public boolean hasIcon()
        {
            return m_icon != null;
        }

        @Nullable
        public Sprite getIcon()
        {
            return m_icon;
        }
    }

    public Item(ItemDescriptor descriptor, String name, Sprite graphic, ItemType itemType, Script script, String description)
    {
        m_descriptor = descriptor;
        m_name = name;
        m_graphic = graphic;
        m_type = itemType;
        m_script = script;
        m_description = description;
    }

    public static Item create(ItemDescriptor descriptor)
    {
        Variable root = VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(descriptor.m_descriptor));

        String name = root.getVariable("name").getValue().getString();

        ItemType type = ItemType.values()[root.getVariable("itemType").getValue().getInt()];

        Sprite graphic = Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(root.getVariable("sprite").getValue().getString())));

        String description = "";

        if (root.variableExists("description"))
            description = root.getVariable("description").getValue().getString();

        Script script = new Script();

        if (root.variableExists("script"))
        {
            script.setScript(Core.getService(IResourceLibrary.class).openResourceContents(root.getVariable("script").getValue().getString()), new ItemBridge());
        }

        graphic.setAnimation("idle", AnimationState.Play);

        return new Item(descriptor, name, graphic, type, script, description);

    }

    public static Item create(String descriptor)
    {
        return create(new ItemDescriptor(descriptor));
    }

    public ItemType getType()
    {
        return m_type;
    }

    public ItemDescriptor getDescriptor()
    {
        return m_descriptor;
    }

    public String getName()
    {
        return m_name;
    }

    public Script getScript()
    {
        return m_script;
    }

    public IRenderable getGraphic()
    {
        return m_graphic;
    }

    public String getDescription()
    {
        return m_description;
    }

    public boolean use(RpgCharacter user, RpgCharacter target)
    {
        return m_type.getBehaviour().use(this, user, target);
    }

    public static class ItemDescriptor implements Comparable<ItemDescriptor>
    {
        private String m_descriptor;

        public ItemDescriptor(String descriptor)
        {
            m_descriptor = descriptor.trim().toLowerCase().replace('\\', '/');
            m_descriptor = (m_descriptor.startsWith("/") ? m_descriptor.substring(1) : m_descriptor);
        }

        @Override
        public int compareTo(ItemDescriptor item)
        {
            return m_descriptor.compareTo(item.m_descriptor);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof ItemDescriptor)
                return ((ItemDescriptor) o).m_descriptor.compareTo(m_descriptor) == 0;
            else
                return new ItemDescriptor(o.toString()).m_descriptor.compareTo(m_descriptor) == 0;
        }

        @Override
        public String toString()
        {
            return m_descriptor;
        }
    }

    @KeepClassMemberNames
    public static class ItemBridge
    {
        public void playAudio(String audioName)
        {
            new Audio(audioName).play();
        }
    }
}
