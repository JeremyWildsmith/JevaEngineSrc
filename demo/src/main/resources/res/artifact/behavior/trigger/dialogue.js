/**
 * Arguments (Script):
 * dialogue - Path to the dialogue path to enqueue.
 * speaker (optional) - Name of speaking entity, if not provided than the speaker will be the intruding entity.
 * 
 * If the dialogue path does not present and enterable query (i.e. all entry conditions fail) then this trigger effectively does nothing.
 */

var config = me.getArguments();
var dialogue = config.getChild("dialogue").getValueString();
var speakerName = config.childExists("speaker") ? config.getChild("speaker").getValueString() : null;

me.onAreaEnter.add(function(intruder) {
	if(!intruder.getName().equals("player"))
		return;
	
	var speaker = speakerName == null ? intruder : intruder.getWorld().getEntity(speakerName);
	
	intruder.invokeInterface("queryControl", function(controller) {
		controller.speakTo(intruder, dialogue);		
	});
});