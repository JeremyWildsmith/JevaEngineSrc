var isDead = false;
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
}

function onAttack(attackee)
{
	if(attackTickCount <= 0)
	{
		attackTickCount = 25;
		me.playAudio('audio/spider/sight.wav');
	}else
		attackTickCount--;

	return true;
}

function onDie()
{
	isDead = true;
	me.cancelTasks();
	me.playAudio('audio/spider/die.wav');
}