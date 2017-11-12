package jerbear.trashy.loader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
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
		
		public AddShape(ShapeInstance inst)
		{
			this.inst = inst;
		}
		
		public AddShape(World world, byte[] data) throws IOException
		{
			try
			{
				ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
				if(buf.get() != id)
					throw new IllegalStateException("Serialization does not match class ID");
				
				Matrix4 transform = new Matrix4();
				for(int i = 0; i < 16; i++)
				{
					transform.val[i] = buf.getFloat();
				}
				
				Color col = new Color(buf.getInt());
				
				byte type = buf.get();
				switch(type)
				{
					case 0:
						float widthbox = buf.getFloat();
						float heightbox = buf.getFloat();
						float depthbox = buf.getFloat();
						
						inst = world.addShape(new ShapeInstance(new Box(widthbox, heightbox, depthbox, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 1:
						float radiuscap = buf.getFloat();
						float heightcap = buf.getFloat();
						
						inst = world.addShape(new ShapeInstance(new Capsule(radiuscap, heightcap, 10, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 2:
						float radiuscone = buf.getFloat();
						float heightcone = buf.getFloat();
						
						inst = world.addShape(new ShapeInstance(new Cone(radiuscone, heightcone, 10, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 3:
						float widthcyl = buf.getFloat();
						float heightcyl = buf.getFloat();
						float depthcyl = buf.getFloat();
						
						inst = world.addShape(new ShapeInstance(new Cylinder(widthcyl, heightcyl, depthcyl, 10, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 4:
						float widthrect = buf.getFloat();
						float depthrect = buf.getFloat();
						
						inst = world.addShape(new ShapeInstance(new Rectangle(widthrect, depthrect, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 5:
						float radiussph = buf.getFloat();
						
						inst = world.addShape(new ShapeInstance(new Sphere(radiussph, 10, 10, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
						break;
					case 6:
						Vector3 v1 = new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat());
						Vector3 v2 = new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat());
						Vector3 v3 = new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat());
						
						inst = world.addShape(new ShapeInstance(new Triangle(v1, v2, v3, col).disposeByWorld(world), 0, 0, 0, CollisionFlags.CF_STATIC_OBJECT, 0));
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
			inst.setDispose();
		}
		
		public Undoable redo(World world)
		{
			try
			{
				return new AddShape(world, serialize());
			}
			catch(IOException oops)
			{
				//THIS SHOULD NEVER HAPPEN (backup is always a valid byte array)
				return null;
			}
		}
		
		public byte[] serialize()
		{
			ByteBuffer buf = ByteBuffer.allocate(maxSize).order(ByteOrder.LITTLE_ENDIAN);
			buf.put(id);
			
			Shape shape = inst.getShape();
			
			float[] transform = inst.getTransform(tmpM).val;
			for(int i = 0; i < 16; i++)
			{
				buf.putFloat(transform[i]);
			}
			
			buf.putInt(Color.rgba8888(((ColorAttribute) (inst.getModelInstance().nodes.get(0).parts.get(0).material.get(ColorAttribute.Diffuse))).color));
			
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
}