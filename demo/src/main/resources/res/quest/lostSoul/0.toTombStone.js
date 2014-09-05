me.onDialogueEvent.add(function(event) {

	if(event === 0)
	{
		me.getListener().invokeInterface("queryControl", function(controller) {
			controller.setFlag("quest/lostSoul", 1);
			controller.moveTo(19, 37);
			controller.idle(1200);
			controller.moveTo(17, 30);
			controller.idle(500);
			controller.moveTo(17, 30);
		});
	}
});