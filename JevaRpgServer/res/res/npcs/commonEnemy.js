var __commonEnemy = {
		attackTickCount: 0
};

function onEnter()
{
	if(Math.random() < 0.3)
	{
		me.addItem('item/healthpack.jitm', 1);
	}
}

function getCommands()
{
	var commands = new Array();

	if(me.getHealth() > 0)
		commands[0] = 'Kill!';
	
	return commands;
}

function doCommand(command)
{
	if(command == 'Kill!')
		me.setHealth(0);
}

function taskBusyState(isIdle)
{
	if(isIdle && me.getHealth() > 0)
	{
		me.idle(500);
		me.wonder(4);
		me.look();
	}
}

function onAttacked(attackee)
{
	if(me.getHealth() > 0)
	{
		me.moveTo(attackee.getLocation().x, attackee.getLocation().y, 1);
		me.attack(attackee);
	}
}

function onAttack(attackee)
{
	if(me.distance(attackee) > 1.7)
		return false;

	if(__commonEnemy.attackTickCount <= 0)
	{
		__commonEnemy.attackTickCount = __commonEnemy_config.attackInterval;
		attackee.setHealth(attackee.getHealth() - 5);
	}else
		__commonEnemy.attackTickCount--;

	return true;
}

function lookFound(target)
{
	if(me.isConflictingAllegiance(target.target))
	{
		me.cancelTasks();
		me.moveTo(target.location.x, target.location.y, 1);
		me.attack(target.target);
		return true;
	}

	return false;
}

function onDie()
{
}