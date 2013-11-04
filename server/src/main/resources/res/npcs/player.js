var attackTickCount = 0;

function onEnter()
{
	me.addItem('item/rifle.jitm', 1);
	me.addItem('item/autorifle.jitm', 1);
}

function onLeave()
{
}

function getCommands()
{
	var commands = new Array();
	commands[0] = "Kill";
	commands[1] = "Send to Hell!";
	
	return commands;
}

function doCommand(command)
{
	if(command == "Kill")
		me.setHealth(0);
	else if(command == "Send to Hell!")
		me.setWorld('map/cave.jmp');
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