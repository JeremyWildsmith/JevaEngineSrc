var attackTickCount = 0;

function use(attacker, attackee)
{
	if(attacker.distance(attackee) > 5.0)
		return false;
	
	if(attackTickCount <= 0)
	{
		attackTickCount = 2;
		attackee.setHealth(attackee.getHealth() - 5);
	}else
		attackTickCount--;

	return true;
}