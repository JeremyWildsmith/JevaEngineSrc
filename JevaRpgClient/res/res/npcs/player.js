var dialog = false;
var attackTickCount = 0;

function onEnter()
{
	me.loadState('states/player.state');
}

function onLeave()
{
	me.storeState('states/player.state');
}

function onDialogEvent(eventId)
{
	return -1;
}

function taskBusyState(isIdle)
{
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