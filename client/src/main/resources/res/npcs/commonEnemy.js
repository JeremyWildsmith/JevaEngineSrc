var __commonEnemy = {
		attackTickCount: 0
};

function onAttack(attackee)
{
	if(me.distance(attackee) > 1.7)
		return false;

	if(__commonEnemy.attackTickCount <= 0)
	{
		__commonEnemy.attackTickCount = __commonEnemy_config.attackInterval;
		me.playAudio(__commonEnemy_config.attackSound);
	}else
	{
		__commonEnemy.attackTickCount--;
	}
	return true;
}

function getCommands()
{
	var commands = new Array();

	if(me.getHealth() > 0)
		commands[0] = 'Attack!';
	else if(game.getPlayer().distance(me) <= 2.0)
		commands[0] = 'Loot!';
	
	return commands;
}

function getDefaultCommand()
{
	if(me.getHealth() > 0)
		return "Attack!";
	else
		return "Loot!";
}

function doCommand(command)
{
	if(command === 'Attack!')
		game.getPlayer().attack(me);
	else if(command === 'Loot!')
		game.getPlayer().loot(me);
}

function onDie()
{
	me.playAudio(__commonEnemy_config.dieSound);
}