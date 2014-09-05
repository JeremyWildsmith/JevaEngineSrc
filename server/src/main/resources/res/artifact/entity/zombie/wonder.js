me.onEnter.add(function() {
	constructTasks();
});

function getDefaultCommand()
{
	return "Talk";
}

function getCommands()
{
	return ["Talk"];
}

function doCommand(command)
{
}

function dialogueEvent(event)
{
}

function constructTasks()
{
	if (me.getHealth() > 0)
	{
		me.idle(500);
		me.wonder(4);
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

function onDie()
{
}
