package jerbear.util3d;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
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

import jerbear.util3d.ShapeInstance.CollisionListener;

@SuppressWarnings("deprecation") //for shadows
public class World
{
	private static Vector3 tmp = new Vector3();
	private static WorldContactListener contactListener;
	
	public final ModelBatch batch, shadowBatch;
	public final Player player;
	public final Environment env;
	public final btDynamicsWorld dynamicsWorld;
	
	private DirectionalShadowLight shadowLight;
	
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
		shadowBatch = new ModelBatch(new DepthShaderProvider());
		shadowLight = new DirectionalShadowLight(8192, 8192, 30, 30, 1, 100);
		
		env = new Environment();
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1));
		env.add(shadowLight.set(0.8f, 0.8f, 0.8f, -1.0f, -0.8f, -0.2f));
		env.shadowMap = shadowLight;
		
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
		addShape(player);
	}

	public void draw(Color bg)
	{
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 5, 1f / 60f);
		
		//add new shapes if any
		Iterator<ShapeInstance> iter = shapesAdd.iterator();
		while(iter.hasNext())
		{
			ShapeInstance shape = (ShapeInstance) iter.next();
			
			shapes.add(shape);
			if(shape.isCollision())
				dynamicsWorld.addRigidBody(shape.getBody());
			
			iter.remove();
		}
		
		//shadows
		iter = shapes.iterator();
		shadowLight.begin(Vector3.Zero, player.getCamera().direction);
		shadowBatch.begin(shadowLight.getCamera());
		while(iter.hasNext())
		{
			((ShapeInstance) iter.next()).drawShadow(shadowBatch);
		}
		shadowBatch.end();
		shadowLight.end();
		
		//rendering, events, and disposal
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
	
	public ShapeInstance addShape(ShapeInstance shape)
	{
		if(shape.world != null)
			throw new IllegalArgumentException("Shape has already been added to a world");
		
		if(shape.isDisposed())
			throw new IllegalArgumentException("Shape is already disposed");
		
		shape.world = this;
		
		try
		{
			shapes.add(shape);
			if(shape.isCollision())
				dynamicsWorld.addRigidBody(shape.getBody());
		}
		catch(ConcurrentModificationException oops)
		{
			//wait until the start of the next frame to add this shape
			//(currently we are in the draw() method)
			shapesAdd.add(shape);
		}
		
		return shape;
	}
	
	public ShapeInstance getShape(int id)
	{
		for(ShapeInstance shape : shapes)
		{
			if(shape.getID() == id && !shape.isDisposed())
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
		
		shadowLight.dispose();
		shadowBatch.dispose();
		batch.dispose();
	}
	
	public Texture getTexture(FileHandle file)
	{
		Texture tex;
		for(Disposable disp : disposables)
		{
			if(disp instanceof Texture)
			{
				tex = (Texture) disp;
				TextureData texData = tex.getTextureData();
				if(texData instanceof FileTextureData)
				{
					FileHandle texFile = ((FileTextureData) texData).getFileHandle();
					if(texFile.equals(file))
						return tex;
				}
			}
		}
		
		tex = new Texture(file);
		disposables.add(tex);
		return tex;
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
				
				if(col0 == null || col1 == null)
					return true;
				
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