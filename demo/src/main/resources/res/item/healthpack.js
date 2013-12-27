function doCommand(user, slot, command)
{
	if(command === "Eat")
	{
		user.setHealth(user.getHealth() - 20);
		slot.clear();
	}
}

function getCommands()
{
	var commands = new Array();
	commands[0] = "Eat";
	return commands;
}