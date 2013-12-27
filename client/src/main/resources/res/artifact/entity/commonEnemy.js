function onEnter()
{
}

function getDefaultCommand()
{
	return "Attack!";
}

function getCommands()
{
	var commands = new Array();
	commands[0] = "Attack!";
	return commands;
}

function doCommand(command)
{
	if(command === "Attack!")
		game.getPlayer().attack(me);
}

function constructTasks()
{
}

function onAttacked(attackee)
{
}

function onAttack(attackee)
{
	return true;
}

function onDie()
{
}
