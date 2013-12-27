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
		game.initiateDialogue(me, "artifact/entity/skeletonWarrior/crazy.jdf", 0);
}

function dialogueEvent(event)
{
	if(event === 0)
		game.getPlayer().getInventory().addItem('item/healthpack.jitm', 1);
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
