var tickCount = 0;

function use(attacker, attackee)
{
	if(tickCount <= 0)
	{
		tickCount = 38;
		me.playAudio('audio/player/attack.wav');
	}else
		tickCount--;	

	return true;
}