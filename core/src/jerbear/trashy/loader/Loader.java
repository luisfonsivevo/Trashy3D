package jerbear.trashy.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

import jerbear.util3d.World;

public class Loader
{
	public static LinkedList<Undoable> loadFile(File file, World world) throws IOException
	{
		LinkedList<Undoable> returnVal = new LinkedList<Undoable>();
		byte[] data = Files.readAllBytes(file.toPath());
		byte[] newdata;
		int remaining = data.length;
		
		if(remaining >= 3)
		{
			if(data[0] == 'T' && data[1] == '3' && data[2] == 'D')
			{
				remaining -= 3;
				
				newdata = new byte[remaining];
				System.arraycopy(data, 3, newdata, 0, remaining);
				data = newdata;
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
			System.out.println(remaining);
			Undoable undo;
			switch(data[0])
			{
				case 0:
					undo = new Undoable.AddShape(world, data);
					break;
				case 1:
					undo = new Undoable.PaintShape(world, data);
					break;
				default:
					throw new IOException("Invalid serialization ID: " + data[0]);
			}
			
			returnVal.add(undo);
			byte[] serialization = undo.serialize();
			remaining -= serialization.length;
			
			newdata = new byte[remaining];
			System.arraycopy(data, serialization.length, newdata, 0, remaining);
			data = newdata;
		}
		
		return returnVal;
	}
}