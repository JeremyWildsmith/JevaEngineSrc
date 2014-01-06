function onTrigger(intruder, isOver)
{
	if(isOver && intruder.equals(game.getPlayer()))
	{
		if(!intruder.getInventory().hasItem("item/coin.jitm", 1) &&
			intruder.isFlagSet("FOUND_COIN") &&
			intruder.getFlag("FOUND_COIN") == 1)
		{
			game.initiateDialogue(me, "artifact/entity/tutorial/coinPickup.jdf");
			intruder.getInventory().addItem("item/coin.jitm", 1);
		}
	}
}