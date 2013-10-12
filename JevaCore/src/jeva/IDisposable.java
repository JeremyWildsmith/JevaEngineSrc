package jeva;

/**
 * Implemented by classes which must be disposed by particular means before
 * being dereferenced.
 */
public interface IDisposable
{
	/**
	 * Disposes the instances accordingly, freeing any unmanaged resources.
	 * After disposing an instance it can no longer be used and must be
	 * dereferenced. All implementing classes must have this routine invoked
	 * before being dereferenced.
	 */
	void dispose();
}
