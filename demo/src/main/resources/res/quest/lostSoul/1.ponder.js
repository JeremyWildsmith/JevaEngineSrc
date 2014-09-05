me.onDialogueEvent.add(function(event) {
	switch(event)
	{
	case 0:
		me.getListener().invokeInterface("queryControl", function(controller) {
			controller.setFlag("quest/lostSoul", 2);
			controller.moveTo(14, 24);
		});
		break;
	default:
		me.log("Unrecognized dialogue event code.");
	}
});