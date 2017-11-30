package jerbear.trashy.loader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;

import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Capsule;
import jerbear.util3d.shapes.Cone;
import jerbear.util3d.shapes.Cylinder;
import jerbear.util3d.shapes.Rectangle;
import jerbear.util3d.shapes.Shape;
import jerbear.util3d.shapes.Sphere;
import jerbear.util3d.shapes.Triangle;

public interface Undoable
{
	public void undo(World world);
	public Undoable redo(World world);
	public byte[] serialize();
	
	public class AddShape implements Undoable
	{
		public static final byte id = (byte) 0;
		public static final int maxSize = 106;
		
		private static Vector3 tmpV = new Vector3();
		private static Matrix4 tmpM = new Matrix4();
		
		private ShapeInstance inst;
		private byte[] backup;
		
		public AddShape(ShapeInstance inst)
		{
			this.inst = inst;
		}
		
		public AddShape(World world, DataInputStream stream) throws IOException
		{
			try
			{
				ShapeInstance.curID = stream.readInt();
				
				Matrix4 transform = new Matrix4();
				for(int i = 0; i < 16; i++)
				{
					transform.val[i] = stream.readFloat();
				}
				
				byte type = stream.readByte();
				switch(type)
				{
					case 0:
						float widthbox = stream.readFloat();
						float heightbox = stream.readFloat();
						float depthbox = stream.readFloat();
						inst = world.addShape(new ShapeInstance(new Box(widthbox, heightbox, depthbox, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 1:
						float radiuscap = stream.readFloat();
						float heightcap = stream.readFloat();
						
						inst = world.addShape(new ShapeInstance(new Capsule(radiuscap, heightcap, 10, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 2:
						float radiuscone = stream.readFloat();
						float heightcone = stream.readFloat();
						
						inst = world.addShape(new ShapeInstance(new Cone(radiuscone, heightcone, 10, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 3:
						float widthcyl = stream.readFloat();
						float heightcyl = stream.readFloat();
						float depthcyl = stream.readFloat();
						
						inst = world.addShape(new ShapeInstance(new Cylinder(widthcyl, heightcyl, depthcyl, 10, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 4:
						float widthrect = stream.readFloat();
						float depthrect = stream.readFloat();
						
						inst = world.addShape(new ShapeInstance(new Rectangle(widthrect, depthrect, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 5:
						float radiussph = stream.readFloat();
						
						inst = world.addShape(new ShapeInstance(new Sphere(radiussph, 10, 10, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 6:
						Vector3 v1 = new Vector3(stream.readFloat(), stream.readFloat(), stream.readFloat());
						Vector3 v2 = new Vector3(stream.readFloat(), stream.readFloat(), stream.readFloat());
						Vector3 v3 = new Vector3(stream.readFloat(), stream.readFloat(), stream.readFloat());
						
						inst = world.addShape(new ShapeInstance(new Triangle(v1, v2, v3, Color.RED).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					default:
						throw new IllegalArgumentException("Invalid shape ID: " + type);
				}
				
				inst.setTransform(transform);
			}
			catch(Exception oops)
			{
				throw new IOException("Invalid serialization of AddShape", oops);
			}
		}
		
		public void undo(World world)
		{
			backup = serialize();
			inst.setDispose();
		}
		
		public Undoable redo(World world)
		{
			try
			{
				DataInputStream stream = new DataInputStream(new ByteArrayInputStream(backup));
				stream.readByte(); //throw out ID
				Undoable returnVal = new AddShape(world, stream);
				stream.close();
				return returnVal;
			}
			catch(IOException oops)
			{
				//THIS SHOULD NEVER HAPPEN (backup is always a valid byte array)
				return null;
			}
		}
		
		public byte[] serialize()
		{
			ByteBuffer buf = ByteBuffer.allocate(maxSize).order(ByteOrder.BIG_ENDIAN); //DataInputStream only allows big endian
			buf.put(id);
			
			buf.putInt(inst.getID());
			
			float[] transform = inst.getTransform(tmpM).val;
			for(int i = 0; i < 16; i++)
			{
				buf.putFloat(transform[i]);
			}

			Shape shape = inst.getShape();
			if(shape instanceof Box)
			{
				buf.put((byte) 0);
				Box dim = (Box) shape;
				dim.getDimensions(tmpV);
				buf.putFloat(tmpV.x);
				buf.putFloat(tmpV.y);
				buf.putFloat(tmpV.z);
			}
			else if(shape instanceof Capsule)
			{
				buf.put((byte) 1);
				Capsule dim = (Capsule) shape;
				buf.putFloat(dim.getRadius());
				buf.putFloat(dim.getHeight());
			}
			else if(shape instanceof Cone)
			{
				buf.put((byte) 2);
				Cone dim = (Cone) shape;
				buf.putFloat(dim.getRadius());
				buf.putFloat(dim.getHeight());
			}
			else if(shape instanceof Cylinder)
			{
				buf.put((byte) 3);
				Cylinder dim = (Cylinder) shape;
				dim.getDimensions(tmpV);
				buf.putFloat(tmpV.x);
				buf.putFloat(tmpV.y);
				buf.putFloat(tmpV.z);
			}
			else if(shape instanceof Rectangle)
			{
				buf.put((byte) 4);
				Rectangle dim = (Rectangle) shape;
				dim.getDimensions(tmpV);
				buf.putFloat(tmpV.x);
				buf.putFloat(tmpV.z);
			}
			else if(shape instanceof Sphere)
			{
				buf.put((byte) 5);
				Sphere dim = (Sphere) shape;
				buf.putFloat(dim.getRadius());
			}
			else if(shape instanceof Triangle)
			{
				buf.put((byte) 6);
				Triangle dim = (Triangle) shape;
				
				dim.getPoint(tmpV, 0);
				buf.putFloat(tmpV.x);
				buf.putFloat(tmpV.y);
				buf.putFloat(tmpV.z);
				
				dim.getPoint(tmpV, 1);
				buf.putFloat(tmpV.x);
				buf.putFloat(tmpV.y);
				buf.putFloat(tmpV.z);
				
				dim.getPoint(tmpV, 2);
				buf.putFloat(tmpV.x);
				buf.putFloat(tmpV.y);
				buf.putFloat(tmpV.z);
			}
			else
			{
				throw new IllegalArgumentException("Can't serialize shape: " + shape.getClass().getName());
			}
			
			byte[] returnVal = new byte[buf.position()];
			buf.rewind();
			buf.get(returnVal);
			
			return returnVal;
		}
	}
	
	//TODO can only save/load colored materials
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
		
		public PaintShape(World world, DataInputStream data) throws IOException
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
					
					Texture tex = world.getTexture(Gdx.files.absolute(new String(ca)));
					mat = new Material(TextureAttribute.createDiffuse(tex));
				}
				else
				{
					mat = new Material(ColorAttribute.createDiffuse(new Color(data.readInt())));
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
				Undoable returnVal = new PaintShape(world, stream);
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
}