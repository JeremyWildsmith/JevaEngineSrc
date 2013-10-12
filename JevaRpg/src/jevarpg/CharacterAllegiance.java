package jevarpg;

public enum CharacterAllegiance
{
    Nuetral, Enemy, Ally;

    public boolean conflictsWith(CharacterAllegiance allegience)
    {
        return (this != allegience) || (allegience == Nuetral);
    }
}
