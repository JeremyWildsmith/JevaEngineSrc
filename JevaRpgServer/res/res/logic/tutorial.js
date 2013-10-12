var hasBeenOver = false;

function onTrigger(character, isOver)
{
	if(!hasBeenOver && isOver)
	{
		hasBeenOver = true;
	}
	
	if(hasBeenOver && isOver)
	{
		character.setWorld('map/cave.jmp');
		character.setLocation(11, 5);
	}
}

function onDialogEvent(subject, event)
{
	if(event == 1)
	{
		subject.setHealth(100);
	}
	
	return -1;
}