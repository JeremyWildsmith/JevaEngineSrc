attackTarget = null;

function onEnter()
{
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
		if(attackTarget === null || attackTarget.getHealth() <= 0)
		{
			attackTarget = null;
		}else
		{
			me.idle(1200);
			me.attack(attackTarget);
		}
		
		me.invoke(constructTasks);
	}
}

function onAttacked(attackee)
{
	if(me.getHealth() > 0 && attackTarget === null)
	{
		attackTarget = attackee;
		me.cancelTasks();
		constructTasks();
	}
}

function onAttack(attackee)
{
	if (me.distance(attackee) > 1.7)
		return false;
	
	attackee.setHealth(attackee.getHealth() - 5);
	return true;
}

function onDie()
{
}
