var zombies = new Array();

var spawnZones = new Array();

spawnZones[0] = {x: 0, y: 0, xmax: 11, ymax: 5};

function spawnZombie()
{
	var z = me.createEntity('rpgCharacter', 'npcs/innocentspider.jnpc');
	
	var zone = spawnZones[Math.round(Math.random() * (spawnZones.length-1))];
	
	z.setLocation(zone.x + Math.round(Math.random() * 100) % (zone.xmax + 1 - zone.x),
					zone.y + Math.round(Math.random() * 100) % (zone.ymax + 1 - zone.y));

	return z;
}

function onEnter()
{
	me.setAmbientLight(0,0,0,150);
	
	for(var i = 0; i < 5; i++)
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