package jevarpg;

public class NoItemBehaviour implements IItemBehaviour
{
    @Override
    public boolean use(Item item, RpgCharacter user, RpgCharacter target)
    {
        return false;
    }

}
