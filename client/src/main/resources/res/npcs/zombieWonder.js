function onEnter()
{
    constructTasks();
}

function onInteract()
{
}

function onDialogEvent(eventId)
{
	return -1;
}

function constructTasks()
{
	if(me.getHealth() > 0)
	{
		me.idle(500);
		me.wonder(8);
                me.invoke(constructTasks);
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