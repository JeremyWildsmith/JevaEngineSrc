function use(consumer)
{
	consumer.setHealth(consumer.getHealth() + 100);
	return true;
}