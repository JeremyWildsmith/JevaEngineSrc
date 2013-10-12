var attackTickCount = 0;

function onInteract()
{
}

function onDialogEvent(eventId)
{
	return -1;
}

function taskBusyState(isIdle)
{
}

function onAttacked(entityInfo)
{
	return 1.0;
}

function onAttack(attackee)
{

	if(attackTickCount <= 0)
	{
		attackTickCount = 25;
		me.playAudio('audio/zombie/attack.wav');
	}else
	{
		attackTickCount--;
	}
	return true;
}

function onDie()
{
	me.playAudio('audio/zombie/die.wav');
}