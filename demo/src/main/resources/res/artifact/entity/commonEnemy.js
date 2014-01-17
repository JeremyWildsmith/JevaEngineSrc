__commonEnemy = {
	attackTarget: null,
	attackRange: 2.5
};

function onEnter()
{
	me.beginLook();
	constructTasks();
}

function getDefaultCommand()
{
	return me.getHealth() > 0 ? "Attack!" : null;
}

function getCommands()
{
	var commands = new Array();

	if (me.getHealth() > 0)
		commands[0] = 'Attack!';

	return commands;
}

function doCommand(command)
{
	if(command === "Attack!")
		game.getPlayer().attack(me);
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
		}else
		{
			if(__commonEnemy.attackTarget.distance(me) <= __commonEnemy.attackRange)
			{
				me.attack(__commonEnemy.attackTarget);
				me.idle(1200);
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
	if(me.isConflictingAllegiance(target.target) && __commonEnemy.attackTarget == null)
	{
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
