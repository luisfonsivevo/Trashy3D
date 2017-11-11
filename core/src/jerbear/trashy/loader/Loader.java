package jerbear.trashy.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

public class Loader
{
	public static LinkedList<Undoable> loadFile(File file) throws IOException
	{
		LinkedList<Undoable> returnVal = new LinkedList<Undoable>();
		byte[] data = Files.readAllBytes(file.toPath());
		int remaining = data.length;
		
		if(remaining >= 3)
		{
			if(data[0] == 'T' && data[1] == '3' && data[2] == 'D')
			{
				remaining -= 3;
				System.arraycopy(data, 3, data, 0, remaining);
			}
			else
			{
				throw new IOException("Not a T3D file");
			}
		}
		else
		{
			throw new IOException("Not a T3D file");
		}
		
		while(remaining > 0)
		{
			Undoable undo;
			switch(data[0])
			{
				case 0:
					undo = new Undoable.AddShape(data);
					break;
				default:
					throw new IOException("Invalid serialization ID: " + data[0]);
			}

			returnVal.add(undo);
			byte[] serialization = undo.serialize();
			remaining -= serialization.length;
			System.arraycopy(data, serialization.length, data, 0, remaining);
		}
		
		return returnVal;
	}
}