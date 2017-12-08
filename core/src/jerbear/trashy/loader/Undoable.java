package jerbear.trashy.loader;

import com.badlogic.gdx.files.FileHandle;
import jerbear.util3d.World;

public interface Undoable
{
	public void undo(World world);
	public Undoable redo(World world);
	public byte[] serialize();
	
	static FileHandle findFile(FileHandle original, FileHandle alternate)
	{
		alternate = alternate.child(original.name());
		
		if(original.exists())
			return original;
		else if(alternate == null)
			return original;
		else if(alternate.exists())
			return alternate;
		else
			return original;
	}
}