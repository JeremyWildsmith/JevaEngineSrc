function onEnter()
{
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
		me.idle(5000 * Math.random() + 1000);
		me.wonder(8);
	}
}

function onAttacked(attackee)
{

}

function onAttack(attackee)
{
	return false;
}

function lookFound(target)
{
	return true;
}

function onDie()
{
}