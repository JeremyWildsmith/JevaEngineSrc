me.onEnter.add(function() {
	constructTasks();
});

me.getDefaultCommand.assign(function() {
	return "Talk";
});

me.getCommands.assign(function() {
	return ["Talk"];
});

me.onCommand.add(function(command) {
});

me.onLocationSet.add(function() {
	me.cancelTasks();
	me.invoke(constructTasks);
});

function constructTasks()
{
	if (me.getHealth() > 0)
	{
		me.idle(1000 * ((Math.round(Math.random() * 10) + 1) % 3));
		me.wonder(3);
		me.invoke(constructTasks);
	}
}