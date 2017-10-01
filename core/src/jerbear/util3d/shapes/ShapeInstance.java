package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Disposable;

import jerbear.util3d.World;

public class ShapeInstance implements Disposable
{
	public static final int defaultCollisionFlags = 8;
	
	private static Vector3 tmp = new Vector3();
	private static int curID = 0;
	
	private boolean disposed = false;
	private float mass;
	private int id;
	
	private boolean isModel;
	private boolean isCollision;
	
	private Shape shape;
	private ModelInstance modelInst;
	private btCollisionShape colShape;
	private btRigidBodyConstructionInfo constructionInfo;
	private btRigidBody body;
	private MotionState motionState;
	
	private CollisionListener collisionListener;
	
	protected final void construct(World world, Shape shape, ModelInstance modelInst, btCollisionShape colShape, int collisionFlags, float mass)
	{
		this.shape = shape;
		this.modelInst = modelInst;
		this.mass = mass;
		this.id = curID++;
		
		isModel = modelInst != null;
		isCollision = collisionFlags != -1;
		
		if(isCollision)
		{
			this.colShape = colShape;
			
			if (mass > 0f)
				colShape.calculateLocalInertia(mass, tmp);
			else
				tmp.set(0, 0, 0);
			
			constructionInfo = new btRigidBodyConstructionInfo(mass, null, colShape, tmp);
			
			body = new btRigidBody(constructionInfo);
			body.setUserValue(id);
			body.setCollisionFlags(collisionFlags | defaultCollisionFlags);
			
			if(isModel)
			{
				motionState = new MotionState(modelInst.transform);
				body.setMotionState(motionState);
			}
		}
		
		if(world != null)
			world.addShape(this);
	}
	
	public final boolean isDisposed()
	{
		return disposed;
	}
	
	public final void setDispose()
	{
		disposed = true;
	}
	
	public Matrix4 getTransform()
	{
		if(modelInst != null)
			return new Matrix4(modelInst.transform);
		else
			return new Matrix4(body.getWorldTransform());
	}
	
	public Matrix4 setTransform(Matrix4 transform)
	{
		if(isModel)
			modelInst.transform.set(transform);
		
		if(isCollision)
		{
			body.setActivationState(Collision.ACTIVE_TAG);
			body.proceedToTransform(transform);
		}
		
		return transform;
	}
	
	public Vector3 getPosition(Vector3 out)
	{
		return getTransform().getTranslation(out);
	}
	
	public Vector3 setPosition(Vector3 pos)
	{
		setTransform(getTransform().setToTranslation(pos));
		return pos;
	}
	
	public void setPosition(float x, float y, float z)
	{
		setTransform(getTransform().setToTranslation(x, y, z));
	}
	
	public final float getMass()
	{
		return mass;
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
	
	public final btRigidBody getBody()
	{
		return body;
	}
	
	public Shape getShape()
	{
		return shape;
	}
	
	public final ModelInstance getInstance()
	{
		return modelInst;
	}
	
	public final CollisionListener getCollisionListener()
	{
		return collisionListener;
	}
	
	public final void setCollisionListener(CollisionListener collisionListener)
	{
		this.collisionListener = collisionListener;
	}
	
	public void draw(ModelBatch batch, Environment env)
	{
		if(modelInst != null)
			batch.render(modelInst, env);
	}
	
	@Override
	public void dispose()
	{
		if(!isCollision)
			return;
		
		body.dispose();
		
		if(modelInst != null)
			motionState.dispose();
		
		constructionInfo.dispose();
		colShape.dispose();
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