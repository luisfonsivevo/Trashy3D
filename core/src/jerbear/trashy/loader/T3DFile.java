package jerbear.trashy.loader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

import com.badlogic.gdx.utils.Disposable;

import jerbear.util3d.World;

public class T3DFile implements Disposable
{
	public File file;
	
	public final World world;
	public final LinkedList<Undoable> undos = new LinkedList<Undoable>();
	
	public T3DFile(World world)
	{
		this.world = world;
		this.file = null;
	}
	
	public T3DFile(World world, File file) throws IOException
	{
		this.world = world;
		this.file = file;
		
		if(file == null)
			return;
		
		byte[] data = Files.readAllBytes(file.toPath());
		byte[] newdata;
		int remaining = data.length;
		
		try
		{
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
				
				undos.add(undo);
				byte[] serialization = undo.serialize();
				remaining -= serialization.length;
				
				newdata = new byte[remaining];
				System.arraycopy(data, serialization.length, newdata, 0, remaining);
				data = newdata;
			}
		}
		catch(IOException oops)
		{
			for(int i = undos.size() - 1; i >= 0; i--)
				undos.get(i).undo(world);
			
			throw oops;
		}
	}
	
	public void saveFile() throws IOException
	{
		DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
		stream.write((byte) 'T');
		stream.write((byte) '3');
		stream.write((byte) 'D');
		
		for(Undoable undo : undos)
		{
			stream.write(undo.serialize());
		}
		
		stream.close();
	}
	
	public String getFileName()
	{
		if(file == null)
			return "(Untitled)";
		else
			return file.getName();
	}
	
	@Override
	public void dispose()
	{
		world.dispose();
	}
}