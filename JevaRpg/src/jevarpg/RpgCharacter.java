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

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.script.ScriptException;

import com.sun.istack.internal.Nullable;

import proguard.annotation.KeepClassMemberNames;
import jeva.Core;
import jeva.CoreScriptException;
import jeva.IResourceLibrary;
import jeva.config.ShallowVariable;
import jeva.config.UnknownVariableException;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.config.VariableValue;
import jeva.game.Character;
import jeva.graphics.IRenderable;
import jeva.graphics.ParticleEmitter;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.util.StaticSet;
import jeva.world.SynchronousOneShotTask;
import jeva.world.EffectMap;
import jeva.world.Entity;
import jeva.world.EntityInstantiationException;
import jeva.world.TraverseRouteTask;
import jeva.world.WorldDirection;
import jeva.world.EffectMap.TileEffects;
import jevarpg.AttackTask.IAttacker;
import jevarpg.Item.ItemDescriptor;
import jevarpg.Item.ItemType;
import jevarpg.quest.Quest;
import jevarpg.quest.QuestState;
import jevarpg.quest.QuestTask;
import jevarpg.ui.StatisticGuage;

public class RpgCharacter extends Character
{
    private CharacterAllegiance m_allegiance;

    private int m_showHealthTimeout;

    private int m_iMaxHealth;
    private int m_health;

    private Inventory m_inventory;

    private HashMap<String, Quest> m_quests = new HashMap<String, Quest>();

    private ParticleEmitter m_bloodEmitter;

    private int m_bleedTimeout = 0;

    private Observers m_observers = new Observers();

    private RpgCharacterAnimator m_animator;

    private RpgCharacterScript m_script = new RpgCharacterScript();

    public <Y extends RpgCharacter, T extends RpgCharacterBridge<Y>> RpgCharacter(@Nullable String name, Variable root, T entityContext)
    {
        super(name, root, entityContext);
        init();
    }

    public <Y extends RpgCharacter, T extends RpgCharacterBridge<Y>> RpgCharacter(@Nullable String name, Variable root)
    {
        super(name, root, new RpgCharacterBridge<>());
        init();
    }

    public RpgCharacter(@Nullable String name, List<VariableValue> arguments)
    {
        super(name, initVariable(arguments), new RpgCharacterBridge<>());

        init();
    }

    private void init()
    {
        setDirection(WorldDirection.values()[(int) (Math.random() * 10) % WorldDirection.Zero.ordinal()]);

        m_animator = new RpgCharacterAnimator(super.getAnimator());

        addObserver(m_animator);
        addObserver(m_script);

        m_showHealthTimeout = 0;

        m_bloodEmitter = ParticleEmitter.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("particle/blood.jpar")), new Vector2D(0, 5));

        if (getEntityVariables().variableExists("allegiance"))
        {
            int allegianceIndex = getEntityVariables().getVariable("allegiance").getValue().getInt();

            if (allegianceIndex < 0 || allegianceIndex >= CharacterAllegiance.values().length)
                throw new CoreScriptException("Character claims allegiance via unassigned index.");

            m_allegiance = CharacterAllegiance.values()[allegianceIndex];
        } else
            m_allegiance = CharacterAllegiance.Nuetral;

        if (getEntityVariables().variableExists("health"))
            m_iMaxHealth = getEntityVariables().getVariable("health").getValue().getInt();
        else
            m_iMaxHealth = 100;

        m_health = m_iMaxHealth;

        if (getEntityVariables().variableExists("inventorySize"))
            m_inventory = new Inventory(getEntityVariables().getVariable("inventorySize").getValue().getInt());
        else
            m_inventory = new Inventory();
    }

    private static Variable initVariable(List<VariableValue> arguments)
    {
        if (arguments.size() < 1)
            throw new EntityInstantiationException("Illegal number of arguments");

        return VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(arguments.get(0).getString()));
    }

    public void addObserver(IRpgCharacterObserver observer)
    {
        m_observers.add(observer);
        super.addObserver(observer);
    }

    public void removeObserver(IRpgCharacterObserver observer)
    {
        m_observers.remove(observer);
        super.removeObserver(observer);
    }

    @Override
    protected RpgCharacterAnimator getAnimator()
    {
        return m_animator;
    }

    @Override
    public WorldDirection[] getAllowedMovements()
    {
        return WorldDirection.ALL_MOVEMENT;
    }

    @Override
    public int getTileWidth()
    {
        return 1;
    }

    @Override
    public int getTileHeight()
    {
        return 1;
    }

    public CharacterAllegiance getAllegiance()
    {
        return m_allegiance;
    }

    public int getHealth()
    {
        return m_health;
    }

    public void setHealth(int health)
    {
        if (health != m_health)
        {
            if (health < m_health)
                m_bleedTimeout = 200;

            if (health == 0)
            {
                cancelTasks();
                RpgCharacter.this.addTask(new SynchronousOneShotTask()
                {

                    @Override
                    public void run(Entity world)
                    {
                        RpgCharacter.this.m_observers.die();
                    }
                });
            } else if (m_health == 0 && health > 0)
            {
                cancelTasks();
                m_animator.idle();
            }
            m_health = Math.min(m_iMaxHealth, Math.max(0, health));

            m_showHealthTimeout = 5000;
            m_observers.healthChanged(health);
        }
    }

    public int getMaxHealth()
    {
        return m_iMaxHealth;
    }

    public boolean isDead()
    {
        return m_health == 0;
    }

    public void moveTo(Vector2D destination)
    {
        cancelTasks();
        addTask(new TraverseRouteTask(new CharacterRouteTraveler(), destination, 0.0F));
    }

    public void attack(@Nullable RpgCharacter target)
    {
        addTask(createAttackTask(target));
    }

    protected AttackTask createAttackTask(@Nullable final RpgCharacter attackee)
    {
        return new AttackTask(new IAttacker()
        {

            @Override
            public boolean attack(RpgCharacter target)
            {
                ItemSlot weapon = m_inventory.getGearSlot(ItemType.Weapon);

                boolean usedWeapon = !weapon.isEmpty() && weapon.getItem().use(RpgCharacter.this, target);

                boolean usedNatural = !usedWeapon && m_script.onAttack(target);

                if (usedWeapon || usedNatural)
                {
                    m_observers.attack(target);
                    target.m_observers.attacked(RpgCharacter.this);
                    return true;
                } else
                    return false;
            }

            @Override
            public RpgCharacter getCharacter()
            {
                return RpgCharacter.this;
            }

        }, attackee);
    }

    protected final AttackTask createAttackTask()
    {
        return createAttackTask(null);
    }

    public Inventory getInventory()
    {
        return m_inventory;
    }

    public Quest getQuest(String quest)
    {
        String formal = quest.trim().replace('\\', '/').toLowerCase();

        if (!m_quests.containsKey(formal))
        {
            m_quests.put(formal, Quest.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(formal))));
        }

        return m_quests.get(formal);
    }

    @Override
    public void blendEffectMap(EffectMap globalEffectMap)
    {
        globalEffectMap.applyOverlayEffects(getLocation().round(), new TileEffects(this.isDead()));
        globalEffectMap.applyOverlayEffects(getLocation().round(), new TileEffects(this));
    }

    @Override
    public void doLogic(int deltaTime)
    {
        if (m_showHealthTimeout > 0)
            m_showHealthTimeout -= deltaTime;

        if (m_bleedTimeout > 0)
        {
            m_bloodEmitter.setEmit(true);
            m_bleedTimeout -= deltaTime;
        } else
            m_bloodEmitter.setEmit(false);

        m_bloodEmitter.update(deltaTime);
        super.doLogic(deltaTime);
    }

    @Override
    public IRenderable[] getGraphics()
    {
        ArrayList<IRenderable> renders = new ArrayList<IRenderable>();
        renders.addAll(Arrays.asList(super.getGraphics()));

        renders.add(m_bloodEmitter);

        if (m_showHealthTimeout > 0)
            renders.add(new StatisticGuage(new Vector2D(25, -10), new Color(255, 0, 0, 122), 50, 3, (float) m_health / (float) m_iMaxHealth));

        return renders.toArray(new IRenderable[renders.size()]);
    }

    @Override
    public Variable[] getChildren()
    {
        ArrayList<Variable> variables = new ArrayList<Variable>();

        variables.addAll(Arrays.asList(super.getChildren()));

        variables.add(new InventoryVariable());
        variables.add(new QuestSetVariable());

        return variables.toArray(new Variable[variables.size()]);
    }

    public class Inventory implements IItemStore
    {
        private ItemSlot m_weaponGear = new ItemSlot();
        private ItemSlot m_headGear = new ItemSlot();
        private ItemSlot m_bodyGear = new ItemSlot();
        private ItemSlot m_accessoryGear = new ItemSlot();

        private ArrayList<ItemSlot> m_inventory;

        public Inventory(int slotCount)
        {
            m_inventory = new ArrayList<ItemSlot>(slotCount);

            for (int i = 0; i < slotCount; i++)
                m_inventory.add(new ItemSlot());
        }

        public Inventory()
        {
            this(0);
        }

        public ItemSlot getGearSlot(ItemType gearType)
        {
            switch (gearType)
            {
            case Accessory:
                return m_accessoryGear;
            case BodyArmor:
                return m_bodyGear;
            case HeadArmor:
                return m_headGear;
            case Weapon:
                return m_weaponGear;
            default:
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean allowStoreAccess(RpgCharacter accessor)
        {
            return accessor == RpgCharacter.this || (RpgCharacter.this.isDead() && accessor.getLocation().difference(RpgCharacter.this.getLocation()).getLength() < 2.5F);
        }

        @Override
        public ItemSlot[] getSlots()
        {
            return m_inventory.toArray(new ItemSlot[m_inventory.size()]);
        }

        @Override
        public boolean hasItem(ItemDescriptor item)
        {
            for (ItemSlot slot : m_inventory)
            {
                if (!slot.isEmpty() && slot.getItem().getDescription().equals(item))
                    return true;
            }

            return false;
        }

        @Override
        public boolean addItem(ItemDescriptor item)
        {
            for (int i = 0; i < m_inventory.size(); i++)
            {
                if (m_inventory.get(i).isEmpty())
                {
                    m_inventory.get(i).setItem(Item.create(item));
                    m_observers.addItem(item);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean removeItem(ItemDescriptor item)
        {
            for (ItemSlot slot : m_inventory)
            {
                if (!slot.isEmpty() && slot.getItem().getDescription().equals(item))
                {
                    m_observers.removeItem(item);
                    slot.clear();
                    return true;
                }
            }

            return false;
        }

        public void equip(Item item)
        {
            switch (item.getType())
            {
            case Accessory:
                m_accessoryGear.setItem(item);
                break;
            case BodyArmor:
                m_bodyGear.setItem(item);
                break;
            case HeadArmor:
                m_headGear.setItem(item);
                break;
            case Weapon:
                m_weaponGear.setItem(item);
                break;
            default:
                throw new NoSuchElementException();
            }

            m_observers.equip(item);
        }

        public void equip(ItemSlot slot)
        {
            ItemSlot destination = null;

            if (slot.isEmpty())
                return;

            switch (slot.getItem().getType())
            {
            case Accessory:
                destination = m_accessoryGear;
                break;
            case BodyArmor:
                destination = m_bodyGear;
                break;
            case HeadArmor:
                destination = m_headGear;
                break;
            case Weapon:
                destination = m_weaponGear;
                break;
            default:
                throw new NoSuchElementException();
            }

            m_observers.equip(m_inventory.indexOf(slot));

            Item destItem = destination.isEmpty() ? null : destination.getItem();

            if (slot.isEmpty())
                destination.clear();
            else
                destination.setItem(slot.getItem());

            if (destItem != null)
                slot.setItem(destItem);
            else
                slot.clear();
        }

        public boolean unequip(ItemType gearType)
        {
            ItemSlot gearSlot = getGearSlot(gearType);

            if (!gearSlot.isEmpty() && addItem(gearSlot.getItem().getDescriptor()))
            {
                gearSlot.clear();
                m_observers.unequip(gearType);
                return true;
            }

            return false;
        }

        public void consume(ItemSlot slot)
        {
            if (!slot.isEmpty())
            {
                m_observers.consume(m_inventory.indexOf(slot));

                if (slot.getItem().use(RpgCharacter.this, RpgCharacter.this))
                    slot.clear();
            }
        }

        public boolean loot(RpgCharacter accessor, ItemSlot slot)
        {
            if (slot.isEmpty())
                return false;

            for (ItemSlot accessorSlot : accessor.getInventory().getSlots())
            {
                if (accessorSlot.isEmpty())
                {
                    m_observers.loot(accessor, m_inventory.indexOf(slot));
                    accessorSlot.setItem(slot.getItem());
                    slot.clear();
                    return true;
                }
            }

            return false;
        }

        public void drop(ItemSlot slot)
        {
            m_observers.drop(m_inventory.indexOf(slot));
            slot.clear();
        }

        public boolean isFull()
        {
            for (ItemSlot slot : m_inventory)
            {
                if (slot.isEmpty())
                    return false;
            }

            return true;
        }

        @Override
        public int getSlotIndex(ItemSlot slot)
        {
            int index = m_inventory.indexOf(slot);

            if (index < 0)
                throw new NoSuchElementException();

            return index;
        }

        @Override
        public String[] getSlotActions(RpgCharacter accessor, int slotIndex)
        {
            ItemSlot slot = getSlots()[slotIndex];

            if (slot.isEmpty())
                return new String[]
                {};

            if (accessor == RpgCharacter.this)
            {
                switch (slot.getItem().getType())
                {
                case Accessory:
                case BodyArmor:
                case HeadArmor:
                case Weapon:
                    return new String[]
                    { "Equip", "Drop" };
                case Consumable:
                    return new String[]
                    { "Consume", "Drop" };
                case General:
                    return new String[]
                    { "About", "Drop" };
                default:
                    throw new RuntimeException("Unknown Item Type");
                }
            } else if (RpgCharacter.this.isDead())
            {
                return new String[] {"Loot"};
            }

            return new String[]
            {};
        }

        @Override
        public void doSlotAction(RpgCharacter accessor, String action, int slotIndex)
        {
            ItemSlot slot = getSlots()[slotIndex];

            if (action.compareTo("Drop") == 0)
                drop(slot);
            else if (action.compareTo("Loot") == 0)
            {
                loot(accessor, slot);
            } else if (action.compareTo("Consume") == 0)
            {
                consume(slot);
            } else if (action.compareTo("Equip") == 0)
            {
                equip(slot);
            }
        }

        public ItemSlot[] getGearSlots()
        {
            return new ItemSlot[]
            { m_accessoryGear, m_headGear, m_bodyGear, m_weaponGear };
        }
    }

    public class QuestSetVariable extends Variable
    {
        public QuestSetVariable()
        {
            super("quests");
        }

        @Override
        protected Variable[] getChildren()
        {
            ArrayList<Variable> variables = new ArrayList<Variable>();

            for (Map.Entry<String, Quest> q : m_quests.entrySet())
            {
                variables.add(new QuestVariable(encodeQuestName(q.getKey()), q.getValue()));
            }

            return variables.toArray(new Variable[variables.size()]);
        }

        @Override
        protected Variable setChild(String name, VariableValue value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Variable createChild(String name, VariableValue value)
        {
            Quest q = getQuest(decodeQuestName(name));

            return new QuestVariable(name, q);
        }

        private String encodeQuestName(String raw)
        {
            try
            {
                return URLEncoder.encode(raw, "ISO-8859-1");
            } catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }

        private String decodeQuestName(String raw)
        {
            try
            {
                return URLDecoder.decode(raw, "ISO-8859-1");
            } catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }

        public class QuestVariable extends Variable
        {
            private Quest m_quest;

            public QuestVariable(String questName, Quest quest)
            {
                super(questName);

                m_quest = quest;
            }

            @Override
            protected Variable[] getChildren()
            {
                ArrayList<Variable> variables = new ArrayList<Variable>();

                for (QuestTask t : m_quest.getTasks())
                {
                    variables.add(new ShallowVariable(this, t.getId(), new VariableValue(t.getState().ordinal())));
                }

                return variables.toArray(new Variable[variables.size()]);
            }

            @Override
            protected Variable setChild(String name, VariableValue value)
            {
                m_quest.getTask(name).setState(QuestState.values()[value.getInt()]);

                return new ShallowVariable(this, name, value);
            }
        }
    }

    public class InventoryVariable extends Variable
    {
        InventoryVariable()
        {
            super("inventory");
        }

        @Override
        protected Variable[] getChildren()
        {
            ArrayList<ShallowVariable> children = new ArrayList<ShallowVariable>();

            ItemSlot[] slots = getInventory().getSlots();

            for (int i = 0; i < slots.length; i++)
            {
                ItemSlot slot = slots[i];

                children.add(new ShallowVariable(this, String.valueOf(i), (slot.isEmpty() ? new VariableValue() : new VariableValue(slot.getItem().getDescriptor().toString()))));
            }

            return children.toArray(new ShallowVariable[children.size()]);
        }

        @Override
        protected Variable setChild(String name, VariableValue value)
        {
            int slotIndex = Integer.parseInt(name);

            if (slotIndex >= getInventory().getSlots().length && slotIndex < 0)
                throw new UnknownVariableException(String.format("Inventory slot %d does not exist", slotIndex));

            ItemSlot slot = getInventory().getSlots()[slotIndex];

            if (value.getString().length() <= 0)
                slot.clear();
            else
                slot.setItem(Item.create(value.getString()));

            return new ShallowVariable(this, name, value);
        }
    }

    private class RpgCharacterScript implements IRpgCharacterObserver
    {
        @Override
        public void die()
        {
            if (getScript().isReady())
            {
                try
                {
                    getScript().invokeScriptFunction("onDie");
                } catch (NoSuchMethodException e)
                {
                } catch (ScriptException e)
                {
                    throw new CoreScriptException("Error invoking RpgCharacter onDie: " + e.toString());
                }
            }
        }

        @Override
        public void onAttacked(RpgCharacter attacker)
        {
            if (getScript().isReady())
            {
                try
                {
                    getScript().invokeScriptFunction("onAttacked", attacker.getScriptBridge());
                } catch (NoSuchMethodException e)
                {
                } catch (ScriptException e)
                {
                    throw new CoreScriptException("Error invoking RpgCharacter onAttacked: " + e.toString());
                }
            }
        }

        public boolean onAttack(RpgCharacter attackee)
        {
            if (!getScript().isReady())
                return false;

            try
            {
                Object o = getScript().invokeScriptFunction("onAttack", attackee.getScriptBridge());

                if (!(o instanceof Boolean))
                    throw new CoreScriptException("onAttack returned unexpected value, expected a boolean");

                return ((Boolean) o).booleanValue();

            } catch (NoSuchMethodException e)
            {
                return false;
            } catch (ScriptException e)
            {
                throw new CoreScriptException("Error occured executing onAttack: " + e.toString());
            }
        }

        @Override
        public void movingTowards(Vector2F target) { }

        @Override
        public void directionChanged(WorldDirection direction) { }

        @Override
        public void placement(Vector2F location) { }

        @Override
        public void moved(Vector2F delta) { }

        @Override
        public void enterWorld() { }

        @Override
        public void leaveWorld() { }

        @Override
        public void taskBusyState(boolean isBusy) { }

        @Override
        public void attack(RpgCharacter attackee) { }

        @Override
        public void healthChanged(int health) { }

        @Override
        public void addItem(ItemDescriptor item) { }

        @Override
        public void removeItem(ItemDescriptor item) { }

        @Override
        public void equip(int slot) { }

        @Override
        public void unequip(ItemType gearType) { }

        @Override
        public void drop(int slot) { }

        @Override
        public void loot(RpgCharacter accessor, int slot) { }

        @Override
        public void consume(int slot) { }

        @Override
        public void equip(Item item) { }

        @Override
        public void onDialogEvent(Entity subject, int event) { }
    }

    protected class RpgCharacterAnimator extends CharacterAnimator implements IRpgCharacterObserver
    {
        private String m_lastDirectionalAnimation = "idle";
        private boolean m_lastDirectionalPlayOnce = false;

        private WorldDirection m_lastDirection;

        public RpgCharacterAnimator(CharacterAnimator animator)
        {
            super(animator);
            m_lastDirection = getDirection() == WorldDirection.Zero ? WorldDirection.XMinus : RpgCharacter.this.getDirection();
            idle();
        }

        private void setDirectionalAnimation(String animationName, boolean playOnce)
        {
            m_lastDirectionalAnimation = animationName;
            m_lastDirectionalPlayOnce = playOnce;

            m_lastDirection = getDirection() == WorldDirection.Zero ? m_lastDirection : getDirection();

            setAnimation(m_lastDirection.toString() + animationName, playOnce);
        }

        public void updateDirectional()
        {
            setDirectionalAnimation(m_lastDirectionalAnimation, m_lastDirectionalPlayOnce);
        }

        private void idle()
        {
            setDirectionalAnimation("idle", false);
        }

        public void attack(@Nullable RpgCharacter attackee)
        {
            setDirectionalAnimation(attackee != null ? "attack" : "idle", false);
        }

        @Override
        public void die()
        {
            setDirectionalAnimation("die", true);
        }

        @Override
        public void movingTowards(@Nullable Vector2F target)
        {
            if (isDead() && target == null)
                die();
            else if (target == null)
                setDirectionalAnimation("idle", false);
            else
                setDirectionalAnimation("walking", false);
        }

        @Override
        public void directionChanged(WorldDirection direction)
        {
            updateDirectional();
        }

        @Override
        public void enterWorld()
        {
            idle();
        }

        @Override
        public void leaveWorld() { }

        @Override
        public void taskBusyState(boolean isBusy) { }

        @Override
        public void healthChanged(int health) { }

        @Override
        public void placement(Vector2F location) { }

        @Override
        public void moved(Vector2F delta) { }

        @Override
        public void onAttacked(RpgCharacter attacker) { }

        @Override
        public void addItem(ItemDescriptor item) { }

        @Override
        public void removeItem(ItemDescriptor item) { }

        @Override
        public void equip(int slot) { }

        @Override
        public void unequip(ItemType gearType) { }

        @Override
        public void drop(int slot) { }

        @Override
        public void loot(RpgCharacter accessor, int slot) { }

        @Override
        public void consume(int slot) { }

        @Override
        public void equip(Item item) { }

        @Override
        public void onDialogEvent(Entity subject, int event) { }
    }

    public interface IRpgCharacterObserver extends ICharacterObserver
    {
        void die();

        void healthChanged(int health);

        void attack(@Nullable RpgCharacter attackee);

        void onAttacked(RpgCharacter attacker);

        void addItem(ItemDescriptor item);

        void removeItem(ItemDescriptor item);

        void equip(int slotIndex);

        void equip(Item item);

        void unequip(ItemType gearType);

        void drop(int slotIndex);

        void loot(RpgCharacter accessor, int slotIndex);

        void consume(int slotIndex);
    }

    private class Observers extends StaticSet<IRpgCharacterObserver>
    {
        public void die()
        {
            for (IRpgCharacterObserver observer : this)
                observer.die();
        }

        public void equip(Item item)
        {
            for (IRpgCharacterObserver observer : this)
                observer.equip(item);
        }

        public void attack(@Nullable RpgCharacter attackee)
        {
            for (IRpgCharacterObserver observer : this)
                observer.attack(attackee);
        }

        public void attacked(RpgCharacter attacker)
        {
            for (IRpgCharacterObserver observer : this)
                observer.onAttacked(attacker);
        }

        public void healthChanged(int health)
        {
            for (IRpgCharacterObserver observer : this)
                observer.healthChanged(health);
        }

        public void addItem(ItemDescriptor item)
        {
            for (IRpgCharacterObserver observer : this)
                observer.addItem(item);
        }

        public void removeItem(ItemDescriptor item)
        {
            for (IRpgCharacterObserver observer : this)
                observer.removeItem(item);
        }

        public void equip(int slot)
        {
            for (IRpgCharacterObserver observer : this)
                observer.equip(slot);
        }

        public void unequip(ItemType gearType)
        {
            for (IRpgCharacterObserver observer : this)
                observer.unequip(gearType);
        }

        public void drop(int slot)
        {
            for (IRpgCharacterObserver observer : this)
                observer.drop(slot);
        }

        public void loot(RpgCharacter accessor, int slot)
        {
            for (IRpgCharacterObserver observer : this)
                observer.loot(accessor, slot);
        }

        public void consume(int slot)
        {
            for (IRpgCharacterObserver observer : this)
                observer.consume(slot);
        }
    }

    @KeepClassMemberNames
    public static class RpgCharacterBridge<Y extends RpgCharacter> extends CharacterBridge<Y>
    {
        public void attack(EntityBridge<Entity> entity)
        {
            if (!(entity instanceof RpgCharacter.RpgCharacterBridge<?>))
                return;
            
            RpgCharacter character = ((RpgCharacter.RpgCharacterBridge<?>) entity).getMe();


            getMe().attack(character);
        }

        public boolean isConflictingAllegiance(EntityBridge<Entity> entity)
        {
            if (!(entity instanceof RpgCharacter.RpgCharacterBridge<?>))
                return false;

            RpgCharacter character = ((RpgCharacter.RpgCharacterBridge<?>) entity).getMe();

            return character.m_allegiance.conflictsWith(((RpgCharacter) getMe()).m_allegiance);
        }

        public int addItem(String descriptor, int quantity)
        {
            int added;

            for (added = 0; 
            		added < quantity && getMe().getInventory().addItem(new ItemDescriptor(descriptor));
            		added++);

            return added;
        }

        public boolean hasItem(String descriptor, int quantity)
        {
            Item.ItemDescriptor searchingFor = new ItemDescriptor(descriptor);

            int count = 0;

            for (ItemSlot slot : getMe().getInventory().getSlots())
            {
                if (!slot.isEmpty() && slot.getItem().getDescriptor().equals(searchingFor))
                    count++;
            }

            return (count >= quantity);
        }

        public int removeItem(String descriptor, int quantity)
        {
            int taken = 0;

            for (taken = 0; taken < quantity && getMe().getInventory().removeItem(new ItemDescriptor(descriptor)); taken++)
                ;

            return taken;
        }

        public int getHealth()
        {
            return ((RpgCharacter) getMe()).getHealth();
        }

        public void setHealth(int health)
        {
            ((RpgCharacter) getMe()).setHealth(Math.max(Math.min(getMe().getMaxHealth(), health), 0));
        }

        public Quest getQuest(String quest)
        {
            return getMe().getQuest(quest);
        }
        
        public void loot(RpgCharacterBridge<RpgCharacter> target)
        {
        	getMe().addTask(new LootTask(getMe(), target.getMe().getInventory()));
        }
    }
}
