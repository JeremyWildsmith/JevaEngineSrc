var zombies = new Array();

var spawnZones = new Array();

spawnZones[0] = {x: 10, y: 23, xmax: 19, ymax: 30};
spawnZones[1] = {x: 24, y: 20, xmax: 30, ymax: 25};
spawnZones[2] = {x: 30, y: 6, xmax: 44, ymax: 16};

function spawnZombie()
{
	var character = 'npcs/zombie.jnpc';
	var z = me.createEntity('jevarpg.RpgCharacter', character);
	
	var zone = spawnZones[Math.round(Math.random() * (spawnZones.length-1))];
	
	z.setLocation(zone.x + Math.round(Math.random() * 100) % (zone.xmax + 1 - zone.x),
					zone.y + Math.round(Math.random() * 100) % (zone.ymax + 1 - zone.y));

	return z;
}

function onEnter()
{
	for(var i = 0; i < 100; i++)
	{
		zombies[i] = spawnZombie();
	}
}

function onLeave()
{
}

function onTick()
{
	var isAlive = false;
	
	for(var i = 0; i < zombies.length; i++)
	{
		if(zombies[i].getHealth() > 0)
			isAlive = true;
	}
	
	if(!isAlive)
	{
		for(var i = 0; i < zombies.length; i++)
		{
			zombies[i].leave();
			zombies[i] = spawnZombie();
		}
	}
}