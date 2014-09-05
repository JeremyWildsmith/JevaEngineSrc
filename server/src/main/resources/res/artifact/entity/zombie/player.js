var attackTarget = null;

me.onEnter.add(function() {
	constructTasks();
});

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

me.onAttacked.add(function(attackee) {
	if(me.getHealth() > 0 && attackTarget === null)
	{
		attackTarget = attackee;
		me.cancelTasks();
		constructTasks();
	}
});

me.doAttack.assign(function(attackee) {
	core.log("on attack!")
	
	if (me.distance(attackee) > 2.5)
	{
		core.log("Player too far to attack!")
		attackTarget = null;
		return false;
	}
	
	if(!attackee.equals(attackTarget))
	{
		attackTarget = attackee;
		constructTasks();
	}
	
	attackee.setHealth(attackee.getHealth() - 5);
	return true;
});
