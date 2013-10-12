var zombies = new Array();
var characters = new Array();
var spawnZones = new Array();

spawnZones[0] = {x: 16, y: 24, xmax: 29, ymax: 40};

characters[0] = 'npcs/woman.jnpc';
characters[1] = 'npcs/soldier.jnpc';
characters[2] = 'npcs/malefarmer.jnpc';
characters[3] = 'npcs/longcoatmale.jnpc';
characters[4] = 'npcs/longcoatfemale.jnpc';
characters[5] = 'npcs/maletraveler.jnpc';

function spawnZombie()
{
	var character = characters[Math.round(Math.random() * (characters.length-1))];
	var z = me.createEntity('jevarpg.RpgCharacter', character);
	
	var zone = spawnZones[Math.round(Math.random() * (spawnZones.length-1))];
	
	z.setLocation(zone.x + Math.round(Math.random() * 100) % (zone.xmax + 1 - zone.x),
					zone.y + Math.round(Math.random() * 100) % (zone.ymax + 1 - zone.y));

	return z;
}

function onEnter()
{
	for(var i = 0; i < 7; i++)
	{
		zombies[i] = spawnZombie();
	}
}

function onLeave()
{
}

function onTick()
{
}