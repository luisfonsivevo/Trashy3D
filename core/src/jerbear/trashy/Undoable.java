package jerbear.trashy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Box.BoxInstance;
import jerbear.util3d.shapes.Capsule;
import jerbear.util3d.shapes.Capsule.CapsuleInstance;
import jerbear.util3d.shapes.Cone;
import jerbear.util3d.shapes.Cone.ConeInstance;
import jerbear.util3d.shapes.Cylinder;
import jerbear.util3d.shapes.Cylinder.CylinderInstance;
import jerbear.util3d.shapes.Shape;
import jerbear.util3d.shapes.ShapeInstance;
import jerbear.util3d.shapes.Sphere;
import jerbear.util3d.shapes.Sphere.SphereInstance;
import jerbear.util3d.shapes.Triangle;
import jerbear.util3d.shapes.Triangle.TriangleInstance;

public interface Undoable
{
	public void undo();
	//public void redo();
	public byte[] serialize();
	
	public class AddShape implements Undoable
	{
		public static final byte id = (byte) 0;
		
		private static Vector3 tmp1 = new Vector3();
		
		private ShapeInstance inst;
		
		public AddShape(ShapeInstance inst)
		{
			this.inst = inst;
		}
		
		public AddShape(byte[] data, World world) throws IOException
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
				int colFlags = buf.getInt();
				float mass = buf.getFloat();
				
				boolean loadPhys = colFlags != -1;
				
				int actState = loadPhys ? buf.getInt() : 0;
				float restitution = loadPhys ? buf.getFloat() : 0;
				float friction = loadPhys ? buf.getFloat() : 0;
				float rollFriction = loadPhys ? buf.getFloat() : 0;
				
				Vector3 velLin = loadPhys ? new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat()) : null;
				Vector3 velAng = loadPhys ? new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat()) : null;
				
				byte type = buf.get();
				switch(type)
				{
					case 0:
						float widthbox = buf.getFloat();
						float heightbox = buf.getFloat();
						float depthbox = buf.getFloat();
						inst = new BoxInstance(new Box(world, widthbox, heightbox, depthbox, col), 0, 0, 0, colFlags, mass);
						break;
					case 1:
						float radiuscap = buf.getFloat();
						float heightcap = buf.getFloat();
						inst = new CapsuleInstance(new Capsule(world, radiuscap, heightcap, 10, col), 0, 0, 0, colFlags, mass);
						break;
					case 2:
						float radiuscone = buf.getFloat();
						float heightcone = buf.getFloat();
						inst = new ConeInstance(new Cone(world, radiuscone, heightcone, 10, col), 0, 0, 0, colFlags, mass);
						break;
					case 3:
						float widthcyl = buf.getFloat();
						float heightcyl = buf.getFloat();
						float depthcyl = buf.getFloat();
						inst = new CylinderInstance(new Cylinder(world, widthcyl, heightcyl, depthcyl, 10, col), 0, 0, 0, colFlags, mass);
						break;
					case 4:
						float radiussph = buf.getFloat();
						inst = new SphereInstance(new Sphere(world, radiussph, 10, 10, col), 0, 0, 0, colFlags, mass);
						break;
					case 5:
						Vector3 v1 = new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat());
						Vector3 v2 = new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat());
						Vector3 v3 = new Vector3(buf.getFloat(), buf.getFloat(), buf.getFloat());
						inst = new TriangleInstance(new Triangle(world, v1, v2, v3, col), colFlags, mass);
						break;
					default:
						throw new IllegalArgumentException("Invalid shape ID: " + type);
				}
				
				inst.setTransform(transform);
				
				if(inst.isCollision())
				{
					inst.getBody().setActivationState(actState);
					inst.getBody().setRestitution(restitution);
					inst.getBody().setFriction(friction);
					inst.getBody().setRollingFriction(rollFriction);
					
					inst.getBody().setLinearVelocity(velLin);
					inst.getBody().setAngularVelocity(velAng);
				}
			}
			catch(Exception oops)
			{
				throw new IOException("Invalid serialization of AddShape", oops);
			}
		}
		
		public void undo()
		{
			inst.setDispose();
		}
		
		public byte[] serialize()
		{
			ByteBuffer buf = ByteBuffer.allocate(154).order(ByteOrder.LITTLE_ENDIAN);
			buf.put(id);
			
			Shape shape = inst.getShape();
			
			float[] transform = inst.getTransform().val;
			for(int i = 0; i < 16; i++)
			{
				buf.putFloat(transform[i]);
			}
			
			buf.putInt(Color.rgba8888(((ColorAttribute) (shape.getModel().nodes.get(0).parts.get(0).material.get(ColorAttribute.Diffuse))).color));
			buf.putInt(inst.isCollision() ? inst.getBody().getCollisionFlags() : -1);
			buf.putFloat(inst.getMass());
			
			if(inst.isCollision())
			{
				buf.putInt(inst.getBody().getActivationState());
				buf.putFloat(inst.getBody().getRestitution());
				buf.putFloat(inst.getBody().getFriction());
				buf.putFloat(inst.getBody().getRollingFriction());
				
				Vector3 tmp2 = inst.getBody().getLinearVelocity();
				buf.putFloat(tmp2.x);
				buf.putFloat(tmp2.y);
				buf.putFloat(tmp2.z);
				
				tmp2 = inst.getBody().getAngularVelocity();
				buf.putFloat(tmp2.x);
				buf.putFloat(tmp2.y);
				buf.putFloat(tmp2.z);
			}
			
			if(shape instanceof Box)
			{
				buf.put((byte) 0);
				Box dim = (Box) shape;
				dim.getDimensions(tmp1);
				buf.putFloat(tmp1.x);
				buf.putFloat(tmp1.y);
				buf.putFloat(tmp1.z);
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
				dim.getDimensions(tmp1);
				buf.putFloat(tmp1.x);
				buf.putFloat(tmp1.y);
				buf.putFloat(tmp1.z);
			}
			else if(shape instanceof Sphere)
			{
				buf.put((byte) 4);
				Sphere dim = (Sphere) shape;
				buf.putFloat(dim.getRadius());
			}
			else if(shape instanceof Triangle)
			{
				buf.put((byte) 5);
				Triangle dim = (Triangle) shape;
				
				dim.getPoint(tmp1, 0);
				buf.putFloat(tmp1.x);
				buf.putFloat(tmp1.y);
				buf.putFloat(tmp1.z);
				
				dim.getPoint(tmp1, 1);
				buf.putFloat(tmp1.x);
				buf.putFloat(tmp1.y);
				buf.putFloat(tmp1.z);
				
				dim.getPoint(tmp1, 2);
				buf.putFloat(tmp1.x);
				buf.putFloat(tmp1.y);
				buf.putFloat(tmp1.z);
			}
			
			byte[] returnVal = new byte[buf.position()];
			buf.rewind();
			buf.get(returnVal);
			
			return returnVal;
		}
	}
}