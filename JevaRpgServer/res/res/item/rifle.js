var tickCount = 0;

function use(attacker, attackee)
{
	if(attacker.distance(attackee) > 5.0)
		return false;

	if(tickCount <= 0)
	{
		tickCount = 38;
		attackee.setHealth(attackee.getHealth() - 15);
	}else
		tickCount--;	

	return true;
}