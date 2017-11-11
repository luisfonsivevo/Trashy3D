package jerbear.trashy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;

import jerbear.util3d.Player;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.ShapeInstance.CollisionListener;

public class FPSPlayer extends Player implements CollisionListener
{
	private static Vector3 tmp1 = new Vector3();
	private static Vector3 tmp2 = new Vector3();
	private static Matrix4 tmpM = new Matrix4();
	
	private static int jumpTimerMax = 30;
	
	public boolean pause;
	
	private boolean firstFrame = true;
	
	private float speed;
	private boolean canJump = false;
	private boolean fly = false;
	private int jumpTimer = 0;
	
	private float pitch = 0;
	private float yaw = 0;
	
	public FPSPlayer(float width, float height, float depth, float speed, int collisionFlags, float mass)
	{
		this(width, height, depth, 0, 0, 0, speed, collisionFlags, mass);
	}
	
	public FPSPlayer(float width, float height, float depth, float x, float y, float z, float speed, int collisionFlags, float mass)
	{
		super(width, height, depth, x, y, z, collisionFlags, mass);
		this.speed = speed;
		setCollisionListener(this);
	}
	
	@Override
	public void draw(ModelBatch batch, Environment env)
	{
		Vector3 dir = getCamera().direction;
		tmp2.set(0, getBody().getLinearVelocity().y, 0);
		
		boolean ctrlKey = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
		
		if(!pause)
		{
			if(!firstFrame)
			{
				float xd = Gdx.input.getDeltaX() / 4f;
				float yd = Gdx.input.getDeltaY() / 4f;
				
				yaw -= xd;
				while(yaw >= 360) yaw -= 360;
				while(yaw <  0)   yaw += 360;
				
				pitch = MathUtils.clamp(pitch - yd, -89.9f, 89.9f);
				
				dir.set(0, 0, -1);
				dir.rotate(Vector3.Y, yaw);
				dir.rotate(tmp1.set(dir).crs(Vector3.Y), pitch);
			}
			else
			{
				firstFrame = false;
				Gdx.input.setCursorCatched(true);
				return;
			}
			
			if(!ctrlKey)
			{
				if(Gdx.input.isKeyPressed(Keys.W))
				{
					tmp1.set(dir.x, 0, dir.z).nor().scl(speed);
					tmp2.add(tmp1);
				}
				
				if(Gdx.input.isKeyPressed(Keys.S))
				{
					tmp1.set(dir.x, 0, dir.z).nor().scl(speed);
					tmp2.sub(tmp1);
				}
				
				if(Gdx.input.isKeyPressed(Keys.A))
				{
					tmp1.set(0, 1, 0).crs(dir).nor().scl(speed);
					tmp2.add(tmp1);
				}
				
				if(Gdx.input.isKeyPressed(Keys.D))
				{
					tmp1.set(dir).crs(0, 1, 0).nor().scl(speed);;
					tmp2.add(tmp1);
				}
				
				boolean space = Gdx.input.isKeyJustPressed(Keys.SPACE);
				if(space && jumpTimer <= 0 && !fly)
				{
					if(canJump)
						getBody().applyCentralForce(tmp1.set(0, 220 * speed, 0));
					
					jumpTimer = jumpTimerMax;
				}
				else if(space && !canJump && jumpTimer > 0 && !fly)
				{
					fly = true;
					getBody().setCollisionFlags(ShapeInstance.defaultCollisionFlags | CollisionFlags.CF_KINEMATIC_OBJECT);
				}
				else if(space && fly && jumpTimer <= 0)
				{
					jumpTimer = jumpTimerMax;
				}
				else if(space && fly && jumpTimer > 0)
				{
					fly = false;
					jumpTimer = jumpTimerMax;
					getBody().setCollisionFlags(ShapeInstance.defaultCollisionFlags);
				}
			}
		}
		
		if(fly)
		{
			if(!pause && !ctrlKey)
			{
				if(Gdx.input.isKeyPressed(Keys.SPACE))
					tmp2.y += speed;
				
				if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
					tmp2.y -= speed;
			}
			
			getPosition(tmp1);
			tmp2.scl(Gdx.graphics.getDeltaTime() * 2).add(tmp1);
		}
		else
		{
			getBody().setLinearVelocity(tmp2);
			getPosition(tmp2);
		}
		
		setTransform(tmpM.setToTranslation(tmp2)); //delete any rotation
		getBody().setAngularVelocity(Vector3.Zero);
		
		canJump = false;
		if(--jumpTimer < 0)
			jumpTimer = 0;
		
		super.draw(batch, env);
	}

	@Override
	public void onCollision(ShapeInstance parent, ShapeInstance other)
	{
		if(Math.abs(getBody().getLinearVelocity().y) < 1)
			canJump = true;
	}
}