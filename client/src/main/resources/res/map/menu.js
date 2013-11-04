var zombies = new Array();

var spawnZones = new Array();

spawnZones[0] = {x: 20, y: 34, xmax: 22, ymax: 37};
spawnZones[1] = {x: 32, y: 34, xmax: 42, ymax: 42};
spawnZones[2] = {x: 28, y: 39, xmax: 30, ymax: 48};

function spawnZombie()
{
	var character = 'npcs/zombieWonder.jnpc';
	var z = me.createEntity('rpgCharacter', character);
	
	var zone = spawnZones[Math.round(Math.random() * (spawnZones.length-1))];
	
	z.setLocation(zone.x + Math.round(Math.random() * 100) % (zone.xmax + 1 - zone.x),
					zone.y + Math.round(Math.random() * 100) % (zone.ymax + 1 - zone.y));

	return z;
}

function onEnter()
{
	me.setAmbientLight(1,2,11,160);
	for(var i = 0; i < 40; i++)
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