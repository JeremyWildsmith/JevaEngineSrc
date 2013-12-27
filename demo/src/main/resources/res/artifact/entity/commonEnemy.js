var __commonEnemy = {
	attackTickCount: 0
};

function onEnter()
{
	me.beginLook();
	constructTasks();
}

function getDefaultCommand()
{
	return me.getHealth() == 0 ? "Revive" : "Kill!";
}

function getCommands()
{
	var commands = new Array();

	if (me.getHealth() > 0)
		commands[0] = 'Kill!';
	else
		commands[1] = "Revive";

	return commands;
}

function doCommand(command)
{
	if (command === 'Kill!')
		me.setHealth(0);
	else if (command === "Revive")
	{
		me.setHealth(100);
	constructTasks();
	}
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
	if (me.getHealth() > 0)
	{
		me.moveTo(attackee.getLocation().x, attackee.getLocation().y, 1);
		me.attack(attackee);
		me.invoke(constructTasks);
	}
}

function onAttack(attackee)
{
	if (me.distance(attackee) > 1.7)
		return false;

	if (__commonEnemy.attackTickCount <= 0)
	{
		__commonEnemy.attackTickCount = __commonEnemy_config.attackInterval;
		attackee.setHealth(attackee.getHealth() - 5);
	} else
		__commonEnemy.attackTickCount--;

	return true;
}

function lookFound(target)
{
	if (me.isConflictingAllegiance(target.target))
	{
		me.cancelTasks();
		me.moveTo(target.location.x, target.location.y, 1);
		me.attack(target.target);
		me.invoke(constructTasks);
		return true;
	}

	return false;
}

function onDie()
{
	me.endLook();
}
