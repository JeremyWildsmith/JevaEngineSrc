var zombies = new Array();

var spawnZones = new Array();

spawnZones[0] = {x: 10, y: 22, xmax: 15, ymax: 27};
spawnZones[1] = {x: 29, y: 27, xmax: 39, ymax: 39};
spawnZones[2] = {x: 4, y: 11, xmax: 5, ymax: 12};
spawnZones[3] = {x: 11, y: 1, xmax: 12, ymax: 6};
spawnZones[4] = {x: 16, y: 15, xmax: 39, ymax: 24};
spawnZones[5] = {x: 24, y: 1, xmax: 32, ymax: 14};
spawnZones[6] = {x: 34, y: 1, xmax: 39, ymax: 14};

function spawnZombie()
{
	var character = Math.random() > 0.3 ? 'npcs/zombie.jnpc' : 'npcs/innocentspider.jnpc';
	var z = me.createEntity('jevarpg.RpgCharacter', character);
	
	var zone = spawnZones[Math.round(Math.random() * (spawnZones.length-1))];
	
	z.setLocation(zone.x + Math.round(Math.random() * 100) % (zone.xmax + 1 - zone.x),
					zone.y + Math.round(Math.random() * 100) % (zone.ymax + 1 - zone.y));

	return z;
}

function onEnter()
{
	me.setAmbientLight(0,0,0,150);
	
	for(var i = 0; i < 0; i++)
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