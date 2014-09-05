me.onDialogueEvent.add(function(event) {
	var listener = me.getListener();
	switch(event)
	{
	case 0:
		
		me.getListener().invokeInterface("queryControl", function(controller) {
			controller.setFlag("quest/lostSoul", 3);
		});
		
		var spider = listener.getWorld().getEntity("lostSoul_spider0");
		spider.invokeInterface("target", me.getListener());
		
		/*spider.moveTo(14, 25);
		
		spider.invoke(function() {
			spider.getScript().beginBehaviour();
			spider.getScript().target(listener);		
		});
		
		onSpiderDieHandler = function(health) {
			if(health != 0)
				return;
			
			spider.onHealthChanged.remove(onSpiderDieHandler);
			listener.speakTo(listener, "quest/lostSoul/3.gotoCave.jdf");
		};
		
		spider.onHealthChanged.add(onSpiderDieHandler);
		*/
		break;
	default:
		me.log("Unrecognized dialgoue event.");
	}
});