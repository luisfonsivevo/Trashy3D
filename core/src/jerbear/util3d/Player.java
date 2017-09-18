package jerbear.util3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.Collision;

import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Box.BoxInstance;

public class Player extends BoxInstance
{
	private World world;
	private PerspectiveCamera camera;
	
	public Player(float width, float height, float depth, int collisionFlags, float mass)
	{
		this(width, height, depth, 0, 0, 0, collisionFlags, mass);
	}
	
	public Player(float width, float height, float depth, float x, float y, float z, int collisionFlags, float mass)
	{
		super(new Box(null, width, height, depth), x, y, z, collisionFlags, mass);
		
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.1f;
		camera.far = 300;
		camera.update();
		
		setTransform(getTransform().setToTranslation(x, y, z));
		getBody().setActivationState(Collision.DISABLE_DEACTIVATION);
		getBody().setFriction(0);
	}
	
	public World setWorld(World world)
	{
		this.world = world;
		return world;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public Camera getCamera()
	{
		return camera;
	}
	
	@Override
	public void draw(ModelBatch batch, Environment env)
	{
		camera.update();
		setTransform(getTransform().setToTranslation(camera.position)); //set bullet pos to cam pos
	}
	
	@Override
	public Matrix4 setTransform(Matrix4 transform)
	{
		super.setTransform(transform);
		
		if(world != null)
			transform.getTranslation(camera.position);
		
		return transform;
	}
}