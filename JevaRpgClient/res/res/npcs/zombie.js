var attackTickCount = 0;

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

function getCommands()
{
	var commands = new Array();

	commands[0] = 'Attack!';
	commands[1] = 'Loot!';
	
	return commands;
}

function doCommand(command)
{
	if(command == 'Attack!')
		game.getPlayer().attack(me);
}

function onDie()
{
	me.playAudio('audio/zombie/die.wav');
}