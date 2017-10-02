package jerbear.util3d;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.utils.Disposable;

import jerbear.util3d.shapes.ShapeInstance;
import jerbear.util3d.shapes.ShapeInstance.CollisionListener;

public class World
{
	private static Vector3 tmp = new Vector3();
	private static WorldContactListener contactListener;
	
	public final ModelBatch batch;
	public final Player player;
	public final Environment env;
	public final btDynamicsWorld dynamicsWorld;
	
	public final ArrayList<Disposable> disposables;
	private ArrayList<ShapeInstance> shapes;
	private ArrayList<ShapeInstance> shapesAdd;
	
	private btCollisionConfiguration collisionConfig;
	private btDispatcher dispatcher;
	private btBroadphaseInterface broadphase;
	private btConstraintSolver constraintSolver;
	
	public World(Player player, float gravity)
	{
		batch = new ModelBatch();
		
		env = new Environment();
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.0f));
		env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1.0f, -0.8f, -0.2f));
		
		shapes = new ArrayList<ShapeInstance>();
		shapesAdd = new ArrayList<ShapeInstance>();
		disposables = new ArrayList<Disposable>();
		
		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		broadphase = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
		dynamicsWorld.setGravity(new Vector3(0, -gravity, 0));
		
		if(contactListener == null)
			contactListener = new WorldContactListener();
		
		contactListener.worlds.add(this);
		
		this.player = player;
		player.setWorld(this);
		addShape(player);
	}

	public void draw()
	{
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 5, 1f / 60f);
		
		Iterator<ShapeInstance> iter = shapesAdd.iterator();
		while(iter.hasNext())
		{
			ShapeInstance shape = (ShapeInstance) iter.next();
			
			shapes.add(shape);
			if(shape.getBody() != null)
				dynamicsWorld.addRigidBody(shape.getBody());
			
			iter.remove();
		}
		
		iter = shapes.iterator();
		batch.begin(player.getCamera());
		while(iter.hasNext())
		{
			ShapeInstance shape = (ShapeInstance) iter.next();
			
			shape.draw(batch, env);
			if(shape.isDisposed())
			{
				if(shape.isCollision())
					dynamicsWorld.removeRigidBody(shape.getBody());
				
				shape.dispose();
				iter.remove();
			}
		}
		batch.end();
	}
	
	public void addShape(ShapeInstance shape)
	{
		shapesAdd.add(shape);
	}
	
	public ShapeInstance getShape(int id)
	{
		for(ShapeInstance shape : shapes)
		{
			if(shape.getID() == id)
				return shape;
		}
		
		return null;
	}
	
	public ShapeInstance getShape(btRigidBody body)
	{
		for(ShapeInstance shape : shapes)
		{
			if(shape.getBody() == body)
				return shape;
		}
		
		return null;
	}
	
	public ShapeInstance rayCastClosest(float rayLength, Vector3 out)
	{
		return rayCastClosest(rayLength, player.getCamera().position, player.getCamera().direction, out);
	}
	
	public ShapeInstance rayCastClosest(float rayLength, Vector3 rayOrigin, Vector3 rayDir, Vector3 out)
	{
		tmp.set(rayDir).nor().scl(rayLength).add(rayOrigin);
		ClosestRayResultCallback callback = new ClosestRayResultCallback(rayOrigin, tmp);
		
		dynamicsWorld.rayTest(rayOrigin, tmp, callback);
		
		ShapeInstance returnVal = null;
		if(callback.hasHit())
			returnVal = getShape((btRigidBody) callback.getCollisionObject());
		
		if(out != null)
			callback.getHitPointWorld(out);
		
		callback.dispose();
		return returnVal;
	}
	
	public ShapeInstance[] rayCastAll(float rayLength)
	{
		return rayCastAll(rayLength, player.getCamera().position, player.getCamera().direction);
	}
	
	public ShapeInstance[] rayCastAll(float rayLength, Vector3 rayOrigin, Vector3 rayDir)
	{
		tmp.set(rayDir).nor().scl(rayLength).add(rayOrigin);
		AllHitsRayResultCallback callback = new AllHitsRayResultCallback(rayOrigin, tmp);
		
		dynamicsWorld.rayTest(rayOrigin, tmp, callback);
		
		ShapeInstance[] returnVal = new ShapeInstance[0];
		if(callback.hasHit())
		{
			btCollisionObjectConstArray objects = callback.getCollisionObjects();
			returnVal = new ShapeInstance[objects.size()];
			
			for(int i = 0; i < objects.size(); i++)
			{
				returnVal[i] = getShape((btRigidBody) objects.at(i));
			}
		}
		
		callback.dispose();
		return returnVal;
	}
	
	public void dispose()
	{
		contactListener.worlds.remove(this);
		if(contactListener.worlds.isEmpty())
			contactListener.dispose();
		
		dynamicsWorld.dispose();
		constraintSolver.dispose();
		broadphase.dispose();
		dispatcher.dispose();
		collisionConfig.dispose();
		
		for(Disposable dis : disposables)
			dis.dispose();
		
		for(ShapeInstance shape : shapesAdd)
			shape.dispose();
		
		for(ShapeInstance shape : shapes)
			shape.dispose();
		
		batch.dispose();
	}
	
	private static class WorldContactListener extends ContactListener
	{
		ArrayList<World> worlds = new ArrayList<World>();
		
		@Override
		public boolean onContactAdded(int userValue0, int partId0, int index0, int userValue1, int partId1, int index1)
		{
			for(World world : worlds)
			{
				ShapeInstance col0 = world.getShape(userValue0);
				ShapeInstance col1 = world.getShape(userValue1);
				CollisionListener listen0 = col0.getCollisionListener();
				CollisionListener listen1 = col1.getCollisionListener();
				
				if(listen0 != null)
					listen0.onCollision(col0, col1);
				
				if(listen1 != null)
					listen1.onCollision(col1, col0);
			}
			
			return true;
		}
	}
}