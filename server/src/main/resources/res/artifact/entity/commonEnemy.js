__commonEnemy = {
	attackTarget: null
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
		if(__commonEnemy.attackTarget === null || __commonEnemy.attackTarget.getHealth() <= 0)
		{
			__commonEnemy.attackTarget = null;
			me.idle(500);
			me.wonder(3);
		}else
		{
			me.idle(1200);
			me.attack(__commonEnemy.attackTarget);
		}
		
		me.invoke(constructTasks);
	}
}

function onAttacked(attackee)
{
	if(me.getHealth() > 0 && __commonEnemy.attackTarget === null)
	{
		__commonEnemy.attackTarget = attackee;
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
