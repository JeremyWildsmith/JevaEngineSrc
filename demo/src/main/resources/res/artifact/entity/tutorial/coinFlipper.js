function onEnter()
{
	constructTasks();
}

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
	if(command === "Talk")
		game.initiateDialogue(me, "artifact/entity/tutorial/findCoin.jdf");
}

function dialogueEvent(listener, event)
{
	if(event === 1)
		listener.setFlag("FOUND_COIN", 1);
	else if(event == 2)
	{
		if(listener.getInventory().removeItem("item/coin.jitm", 1) == 1)
			listener.setFlag("FOUND_COIN", 2);
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
}

function onAttack(attackee)
{
	return false;
}

function onDie()
{
}
