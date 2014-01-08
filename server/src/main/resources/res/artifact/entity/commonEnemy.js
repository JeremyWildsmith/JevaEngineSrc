__commonEnemy = {
	attackTarget: null,
	attackRange: 1.7
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
	me.cancelTasks();
	
	if (me.getHealth() > 0)
	{
		if(__commonEnemy.attackTarget === null ||
			__commonEnemy.attackTarget.getHealth() <= 0 ||
			__commonEnemy.attackTarget.distance(me) > 4)
		{
			__commonEnemy.attackTarget = null;
			me.beginLook();
			me.idle(500);
			me.wonder(3);
			core.log("Looking...");
		}else
		{

			core.log("Found!");
			if(__commonEnemy.attackTarget.distance(me) <= __commonEnemy.attackRange)
			{
				me.idle(1200);
				me.attack(__commonEnemy.attackTarget);
			}else
			{
				var targetLocation = __commonEnemy.attackTarget.getLocation();
				me.invokeTimeout(2000, constructTasks);
				me.moveTo(targetLocation.x, targetLocation.y, 1);
			}
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
	if (me.distance(attackee) >= __commonEnemy.attackRange)
	{
		return false;
	}
	attackee.setHealth(attackee.getHealth() - 5);
	return true;
}

function lookFound(target)
{
	core.log("found a guy...")
	if (me.isConflictingAllegiance(target.target) && __commonEnemy.attackTarget == null)
	{
		core.log("Try Attack!");
		__commonEnemy.attackTarget = target.target;
		me.cancelTasks();
		constructTasks();
		return true;
	}

	return false;
}

function onDie()
{
	me.endLook();
}
