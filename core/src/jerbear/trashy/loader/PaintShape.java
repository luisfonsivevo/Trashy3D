package jerbear.trashy.loader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.FileTextureData;

import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;

//TODO detect if using materials with alpha, then disable bfc
public class PaintShape implements Undoable
{
	public static final byte id = (byte) 1;
	public static final int minSize = 10;
	
	private ShapeInstance inst;
	private int side;
	private Material mat, matPrv;
	
	private byte[] backup;
	
	public PaintShape(ShapeInstance inst, int side, Material mat)
	{
		this.inst = inst;
		this.side = side;
		this.mat = mat;
		this.matPrv = inst.getMaterial(side);
	}
	
	public PaintShape(World world, DataInputStream data, FileHandle alternate) throws IOException
	{
		try
		{
			inst = world.getShape(data.readInt());
			side = data.readInt();
			
			if(data.readBoolean())
			{
				char[] ca = new char[data.readInt()];
				for(int i = 0; i < ca.length; i++)
				{
					ca[i] = data.readChar();
				}
				
				FileHandle texFile = Gdx.files.absolute(new String(ca));
				Texture tex = world.getTexture(Undoable.findFile(texFile, alternate));
				mat = new Material(TextureAttribute.createDiffuse(tex), new BlendingAttribute());
			}
			else
			{
				mat = new Material(ColorAttribute.createDiffuse(new Color(data.readInt())), new BlendingAttribute());
			}
			
			matPrv = inst.getMaterial(side);
			inst.setMaterial(side, mat);
		}
		catch(Exception oops)
		{
			throw new IOException("Invalid serialization of PaintShape", oops);
		}
	}
	
	@Override
	public void undo(World world)
	{
		backup = serialize();
		inst.setMaterial(side, matPrv);
	}
	
	@Override
	public Undoable redo(World world)
	{
		try
		{
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(backup));
			stream.readByte();
			Undoable returnVal = new PaintShape(world, stream, null);
			stream.close();
			return returnVal;
		}
		catch(IOException oops)
		{
			return null;
		}
	}
	
	@Override
	public byte[] serialize()
	{
		int bufSize = minSize;
		boolean hasTex = mat.has(TextureAttribute.Diffuse);
		
		String tex = null;
		int col = 0;
		
		if(hasTex) //add length of file string
		{
			tex = ((FileTextureData) ((TextureAttribute) mat.get(TextureAttribute.Diffuse)).textureDescription.texture.getTextureData()).getFileHandle().path();
			bufSize += 4 + tex.length() * 2;
		}
		else
		{
			col = Color.rgba8888(((ColorAttribute) mat.get(ColorAttribute.Diffuse)).color);
			bufSize += 4;
		}
		
		ByteBuffer buf = ByteBuffer.allocate(bufSize).order(ByteOrder.BIG_ENDIAN);
		buf.put(id);
		
		buf.putInt(inst.getID());
		buf.putInt(side);
		buf.put((byte) (hasTex ? 1 : 0));
		
		if(hasTex)
		{
			buf.putInt(tex.length());
			for(char c : tex.toCharArray())
				buf.putChar(c);
		}
		else
		{
			buf.putInt(col);
		}
		
		return buf.array();
	}
}