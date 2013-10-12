var attackTickCount = 0;

function onEnter()
{
	if(Math.random() < 0.3)
	{
		me.addItem('item/healthpack.jitm', 1);
	}
}

function onInteract()
{
}

function onDialogEvent(eventId)
{
	return -1;
}

function taskBusyState(isIdle)
{
	if(isIdle && me.getHealth() > 0)
	{
		me.idle(500);
		me.wonder(4);
		me.look();
	}
}

function onAttacked(attackee)
{
	if(me.getHealth() > 0)
	{
		me.moveTo(attackee.getLocation().x, attackee.getLocation().y, 1);
		me.attack(attackee);
	}
}

function onAttack(attackee)
{
	if(me.distance(attackee) > 1.7)
		return false;

	if(attackTickCount <= 0)
	{
		attackTickCount = 25;
		attackee.setHealth(attackee.getHealth() - 5);
	}else
		attackTickCount--;

	return true;
}

function lookFound(target)
{
	if(me.isConflictingAllegiance(target.target))
	{
		me.cancelTasks();
		me.moveTo(target.location.x, target.location.y, 1);
		me.attack(target.target);
		return true;
	}

	return false;
}

function onDie()
{
}