package jerbear.util3d;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Disposable;

import jerbear.util3d.shapes.Shape;

public class ShapeInstance implements Disposable
{
	public static final int defaultCollisionFlags = CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK;
	public static int curID = 0; //used for collision detection (userval), but can be used as general purpose ID. CHANGE WITH CAUTION
	
	private static Vector3 tmp = new Vector3();
	
	private boolean disposed = false;
	private int id;
	
	private Shape shape;
	private btCollisionShape colShape;
	
	private ModelInstance modelInst;
	private btRigidBody body;
	
	private MotionState motionState;
	private CollisionListener collisionListener;
	
	private boolean isModel;
	private boolean isCollision;
	
	World world;
	
	public ShapeInstance(Shape shape, Vector3 pos)
	{
		this(shape, 0, 0, 0);
	}
	
	public ShapeInstance(Shape shape, float x, float y, float z)
	{
		this(shape, 0, 0, 0, -1, 0);
	}
	
	public ShapeInstance(Shape shape, Vector3 pos, int collisionFlags, float mass)
	{
		this(shape, pos.x, pos.y, pos.z, collisionFlags, mass);
	}

	public ShapeInstance(Shape shape, float x, float y, float z, int collisionFlags, float mass)
	{
		this.id = curID++;
		this.shape = shape;
		
		if(shape.getModel() != null)
			createModelInstance();
		
		if(collisionFlags != -1)
			createRigidBody(collisionFlags, mass);
		
		setPosition(x, y, z);
	}
	
	public void createModelInstance()
	{
		isModel = true;
		modelInst = new ModelInstance(shape.getModel());
		
		if(isCollision)
		{
			motionState = new MotionState(modelInst.transform);
			body.setMotionState(motionState);
		}
	}
	
	public void removeModelInstance()
	{
		if(isCollision)
		{
			body.setMotionState(null);
			motionState.dispose();
		}
		
		modelInst = null;
		isModel = false;
	}
	
	public void createRigidBody(int collisionFlags, float mass)
	{
		if(collisionFlags == -1 || isCollision)
			return;
		
		colShape = shape.getCollisionShape();
		
		if (mass > 0f)
			colShape.calculateLocalInertia(mass, tmp);
		else
			tmp.set(0, 0, 0);
		
		btRigidBodyConstructionInfo constructionInfo = new btRigidBodyConstructionInfo(mass, null, colShape, tmp);
		body = new btRigidBody(constructionInfo);
		body.setUserValue(id);
		body.setCollisionFlags(collisionFlags | defaultCollisionFlags);
		constructionInfo.dispose();
		
		if(isModel)
		{
			motionState = new MotionState(modelInst.transform);
			body.setMotionState(motionState);
		}
		
		isCollision = true;
	}
	
	public void removeRigidBody()
	{
		body.dispose();
		body = null;
		
		if(isModel)
		{
			motionState.dispose();
			motionState = null;
		}
		
		isCollision = false;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public Matrix4 getTransform(Matrix4 out)
	{
		return out.set(getTransform());
	}
	
	private Matrix4 getTransform()
	{
		if(isModel)
			return modelInst.transform;
		else if(isCollision)
			return body.getWorldTransform();
		else
			return null;
	}
	
	//TODO scaling
	public ShapeInstance setTransform(Matrix4 transform)
	{
		if(isModel)
			modelInst.transform.set(transform);
		
		if(isCollision)
		{
			body.setActivationState(Collision.ACTIVE_TAG);
			body.proceedToTransform(transform);
		}
		
		return this;
	}
	
	public Vector3 getPosition(Vector3 out)
	{
		return getTransform().getTranslation(out);
	}
	
	public ShapeInstance setPosition(Vector3 pos)
	{
		return setPosition(pos.x, pos.y, pos.z);
	}
	
	public ShapeInstance setPosition(float x, float y, float z)
	{
		setTransform(getTransform(new Matrix4()).setTranslation(x, y, z));
		return this;
	}
	
	public float getMass()
	{
		float mass = body.getInvMass();
		if(mass == 0)
			return 0;
		else
			return 1f / mass;
	}
	
	//TODO may cause problems if used in a CollisionListener
	public ShapeInstance setMass(float mass)
	{
		if(world != null)
			world.dynamicsWorld.removeRigidBody(body);
		
		body.setMassProps(mass, body.getLinearFactor());
		body.setActivationState(Collision.ACTIVE_TAG);
		
		if(world != null)
			world.dynamicsWorld.addRigidBody(body);
		
		return this;
	}
	
	public final int getID()
	{
		return id;
	}
	
	public boolean isModel()
	{
		return isModel;
	}
	
	public boolean isCollision()
	{
		return isCollision;
	}
	
	public Shape getShape()
	{
		return shape;
	}
	
	public ModelInstance getModelInstance()
	{
		return modelInst;
	}
	
	public Material getMaterial(int part)
	{
		if(isModel)
			return modelInst.nodes.get(0).parts.get(part).material;
		else
			return null;
	}
	
	public Material setMaterial(int part, Material mat)
	{
		if(isModel)
			modelInst.nodes.get(0).parts.get(part).material = mat;
		
		return mat;
	}
	
	public btRigidBody getBody()
	{
		return body;
	}
	
	public CollisionListener getCollisionListener()
	{
		return collisionListener;
	}
	
	public ShapeInstance setCollisionListener(CollisionListener collisionListener)
	{
		this.collisionListener = collisionListener;
		return this;
	}
	
	public void drawShadow(ModelBatch shadowBatch)
	{
		if(isModel)
			shadowBatch.render(modelInst);
	}
	
	public void draw(ModelBatch batch, Environment env)
	{
		if(isModel)
			batch.render(modelInst, env);
	}
	
	public final boolean isDisposed()
	{
		return disposed;
	}
	
	public final void setDispose()
	{
		disposed = true;
	}
	
	@Override
	public void dispose()
	{
		if(isCollision)
		{
			body.dispose();
			
			if(isModel)
				motionState.dispose();
		}
		
		modelInst = null;
		body = null;
		
		motionState = null;
		collisionListener = null;
	}
	
	public static interface CollisionListener
	{
		public void onCollision(ShapeInstance parent, ShapeInstance other);
	}
	
	private static class MotionState extends btMotionState
	{
		public Matrix4 transform;
		
		public MotionState(Matrix4 modelTransform)
		{
			transform = modelTransform;
		}

		@Override
		public void getWorldTransform (Matrix4 worldTrans)
		{
			worldTrans.set(transform);
		}

		@Override
		public void setWorldTransform (Matrix4 worldTrans)
		{
			transform.set(worldTrans);
		}
	}
}