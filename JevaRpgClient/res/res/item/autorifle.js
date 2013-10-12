var attackTickCount = 0;

function use(attacker, attackee)
{
	if(attackTickCount <= 0)
	{
		attackTickCount = 2;
		me.playAudio('audio/player/assault.wav');
	}else
		attackTickCount--;

	return true;
}