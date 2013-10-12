var attackTickCount = 0;

function onEnter()
{
	me.addItem('item/rifle.jitm', 1);
	me.addItem('item/autorifle.jitm', 1);
}

function onLeave()
{
}

function onDialogEvent(eventId)
{
	return -1;
}

function taskBusyState(isIdle)
{
	if(isIdle)
	{
		me.idle(10);
	}
}


function onAttack(attackee)
{
	return false;
}

function onAttacked(attacker)
{
}

function onDie()
{
}