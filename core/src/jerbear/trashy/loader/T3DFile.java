package jerbear.trashy.loader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
		
		if(file == null || !file.isFile() || !file.exists())
			return;
		
		DataInputStream stream = new DataInputStream(new FileInputStream(file));
		
		if(stream.readByte() != 'T')
		{
			stream.close();
			throw new IOException("Not a T3D file");
		}
		
		if(stream.readByte() != '3')
		{
			stream.close();
			throw new IOException("Not a T3D file");
		}
		
		if(stream.readByte() != 'D')
		{
			stream.close();
			throw new IOException("Not a T3D file");
		}
		
		try
		{
			while(true)
			{
				byte serialID;
				Undoable undo;
				
				try
				{
					serialID = stream.readByte();
				}
				catch(EOFException oops)
				{
					//all done
					stream.close();
					break;
				}
				
				switch(serialID)
				{
					case 0:
						undo = new Undoable.AddShape(world, stream);
						break;
					case 1:
						undo = new Undoable.PaintShape(world, stream);
						break;
					default:
						throw new IOException("Invalid serialization ID: " + serialID);
				}
				
				undos.add(undo);
			}
		}
		catch(IOException oops)
		{
			stream.close();
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