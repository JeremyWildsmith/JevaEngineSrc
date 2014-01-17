attackTarget = null;

function onEnter()
{
	constructTasks();
}

function getDefaultCommand()
{
	return null;
}

function getCommands()
{
	var commands = new Array();

	return commands;
}

function doCommand(command)
{
}

function constructTasks()
{
	core.log("Construct Tasks.");
	me.cancelTasks();
	
	if (me.getHealth() > 0)
	{
		if(attackTarget != null)
		{
			if(attackTarget.getHealth() <= 0)
			{
				attackTarget = null;
			}else
			{
				me.attack(attackTarget);
				me.idle(1200);
				me.invoke(constructTasks);
			}
		}
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
	core.log("on attack!")
	
	if (me.distance(attackee) > 2.5)
	{
		core.log("Player too far to attack!")
		attackTarget = null;
		return false;
	}
	
	if(attackTarget != attackee)
	{
		attackTarget = attackee;
		constructTasks();
	}
	
	attackee.setHealth(attackee.getHealth() - 5);
	return true;
}

function onDie()
{
}
