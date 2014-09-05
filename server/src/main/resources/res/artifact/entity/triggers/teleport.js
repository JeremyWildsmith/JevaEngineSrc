/**
 * Arguments (Script):
 * location - Vector3F representing where in world to place the intruding entity.
 * world (optional) - The world to place the entity into.
 * flags (optional) - Prequested flags for enitities entering this trigger to be teleported
 */

var config = me.getConfiguration();
var destLocation = config.getChild("location").getValueVec3F();
var destWorldName = config.childExists("world") ? config.getChild("world").getValueString() : null;
var flags = config.childExists("flags") ? config.getChild("flags").getValueStringArray() : new Array();

function testFlags(intruder)
{
	for(f in flags)
	{
		if(!intruder.isFlagSet(f))
			return false;
	}
	
	return true;
}

me.onAreaEnter.add(function(intruder) {
	
	if(!testFlags(intruder))
		return;
	
	intruder.cancelTasks();
	
	if(destWorldName != null)
	{
		game.getWorld(destWorldName, function(nextWorld) {
				var currentWorld = intruder.getWorld();
				
				currentWorld.removeEntity(intruder);
				nextWorld.addEntity(intruder);
				
				intruder.setLocation(destLocation);
			});
	}
	else
		intruder.setLocation(destLocation);
});